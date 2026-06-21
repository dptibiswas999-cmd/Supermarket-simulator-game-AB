package com.example.game

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class CoinPopup(
    val id: String,
    val text: String,
    val x: Float,
    val y: Float,
    val ageTicks: Int // Countdown or age in ticks
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("supermarket_simulator_save", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val stateAdapter = moshi.adapter(GameState::class.java)

    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    // Sub-states for simulation
    private val _customers = MutableStateFlow<List<SimulatedCustomer>>(emptyList())
    val customers: StateFlow<List<SimulatedCustomer>> = _customers.asStateFlow()

    private val _coinPopups = MutableStateFlow<List<CoinPopup>>(emptyList())
    val coinPopups: StateFlow<List<CoinPopup>> = _coinPopups.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastSpawnTick = 0

    init {
        loadGameState()
        startGameLoop()
    }

    private fun loadGameState() {
        try {
            val json = sharedPrefs.getString("game_state", null)
            if (json != null) {
                stateAdapter.fromJson(json)?.let { savedState ->
                    _uiState.value = savedState
                }
            } else {
                // First launch
                _uiState.value = GameState(
                    shopName = "Mogul Mart",
                    coins = 10000L,
                    ownedShelves = mapOf("SHELF_VEGGIE" to 1),
                    ownedDecorations = emptySet(),
                    isVip = false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback
            _uiState.value = GameState()
        }
    }

    private fun saveGameState() {
        _uiState.update { it.copy(lastSaved = System.currentTimeMillis()) }
        try {
            val json = stateAdapter.toJson(_uiState.value)
            sharedPrefs.edit().putString("game_state", json).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            while (true) {
                delay(100) // simulation update every 100ms
                tickSimulation()
            }
        }
    }

    private fun tickSimulation() {
        val state = _uiState.value
        val currentCustomers = _customers.value.toMutableList()
        val currentPopups = _coinPopups.value.toMutableList()

        // 1. Tick floating popups
        val updatedPopups = currentPopups.map { it.copy(ageTicks = it.ageTicks - 1) }.filter { it.ageTicks > 0 }
        _coinPopups.value = updatedPopups

        // 2. Spawn customer depending on speed boosts
        var speedMod = 1.0f
        state.ownedDecorations.forEach { decorId ->
            val decor = GameRegistry.DECORATIONS.find { it.id == decorId }
            if (decor != null) {
                speedMod *= decor.speedMultiplier
            }
        }
        if (state.isVip) {
            speedMod *= 1.4f // VIP spawns 40% faster
        }

        val baseSpawnIntervalTicks = 70 // default 7 seconds
        val actualSpawnInterval = (baseSpawnIntervalTicks / speedMod).coerceAtLeast(15f).toInt()

        lastSpawnTick++
        if (lastSpawnTick >= actualSpawnInterval) {
            lastSpawnTick = 0
            if (currentCustomers.size < 5) { // limit 5 customers max on screen to keep things legible
                spawnCustomer()
            }
        }

        // 3. Move every customer
        val iterator = currentCustomers.listIterator()
        while (iterator.hasNext()) {
            val c = iterator.next()
            var bubble = c.speechBubble
            var bubbleExp = c.speechBubbleExpiry

            if (bubbleExp > 0) {
                bubbleExp--
                if (bubbleExp == 0) {
                    bubble = null
                }
            }

            when (c.state) {
                CustomerState.ENTERING -> {
                    val nextProgress = c.progress + 0.05f
                    if (nextProgress >= 1.0f) {
                        // Switch to browsing
                        val randomSpeech = if (Random.nextFloat() < 0.4f) {
                            GameRegistry.BROWSING_LINES.random()
                        } else null

                        iterator.set(
                            c.copy(
                                state = CustomerState.BROWSING,
                                progress = 0.0f,
                                speechBubble = randomSpeech,
                                speechBubbleExpiry = if (randomSpeech != null) 25 else 0
                            )
                        )
                    } else {
                        iterator.set(c.copy(progress = nextProgress))
                    }
                }
                CustomerState.BROWSING -> {
                    val nextProgress = c.progress + 0.03f // slow browser
                    if (nextProgress >= 1.0f) {
                        val randomSpeech = if (Random.nextFloat() < 0.3f) {
                            GameRegistry.CHECKOUT_LINES.random()
                        } else null

                        // Move to checkout
                        iterator.set(
                            c.copy(
                                state = CustomerState.CHECKOUT,
                                progress = 0.0f,
                                speechBubble = randomSpeech,
                                speechBubbleExpiry = if (randomSpeech != null) 25 else 0
                            )
                        )
                    } else {
                        iterator.set(c.copy(progress = nextProgress))
                    }
                }
                CustomerState.CHECKOUT -> {
                    val nextProgress = c.progress + 0.06f
                    if (nextProgress >= 1.0f) {
                        // Complete checkout! Add coins!
                        val targetId = c.targetShelfId
                        if (targetId != null) {
                            triggerCheckoutPayment(targetId)
                        }
                        // Change state to leaving
                        iterator.set(c.copy(state = CustomerState.LEAVING, progress = 0.0f, targetShelfId = null))
                    } else {
                        iterator.set(c.copy(progress = nextProgress))
                    }
                }
                CustomerState.LEAVING -> {
                    val nextProgress = c.progress + 0.07f
                    if (nextProgress >= 1.0f) {
                        iterator.remove() // disappears
                    } else {
                        iterator.set(c.copy(progress = nextProgress))
                    }
                }
            }
        }
        _customers.value = currentCustomers
    }

    private fun spawnCustomer() {
        val state = _uiState.value
        // Select a shelf that the player owns
        if (state.ownedShelves.isEmpty()) return
        val shelfList = state.ownedShelves.keys.toList()
        val pickedShelf = shelfList.random()

        val id = UUID.randomUUID().toString()
        val name = GameRegistry.CUSTOMER_NAMES.random()
        val emoji = GameRegistry.CUSTOMER_EMOJIS.random()

        val newCust = SimulatedCustomer(
            id = id,
            name = name,
            emoji = emoji,
            state = CustomerState.ENTERING,
            progress = 0.0f,
            targetShelfId = pickedShelf
        )
        _customers.value = _customers.value + newCust
    }

    private fun triggerCheckoutPayment(shelfId: String) {
        val state = _uiState.value
        val shelf = GameRegistry.SHELVES.find { it.id == shelfId } ?: return
        val level = state.ownedShelves[shelfId] ?: 1

        // Base profit scale
        var profit = shelf.baseEarnings * level

        // Decoration Multiplier
        var coinMultiplier = 1.0f
        state.ownedDecorations.forEach { decorId ->
            val decor = GameRegistry.DECORATIONS.find { it.id == decorId }
            if (decor != null) {
                coinMultiplier += (decor.coinMultiplier - 1.0f)
            }
        }
        if (state.isVip) {
            coinMultiplier += 0.50f // VIP gives permanent +50% earnings addition
        }

        val totalEarnings = (profit * coinMultiplier).toLong()

        // Update coins
        _uiState.update { it.copy(coins = it.coins + totalEarnings) }

        // Register visual popup
        val text = "+🪙 $totalEarnings"
        // Cashier position in grid coordinates is (2,2)
        val jitterX = Random.nextFloat() * 0.4f - 0.2f
        val jitterY = Random.nextFloat() * 0.4f - 0.2f
        val newPopup = CoinPopup(
            id = UUID.randomUUID().toString(),
            text = text,
            x = 2f + jitterX,
            y = 2.4f + jitterY,
            ageTicks = 12 // displays for 1.2 seconds
        )
        _coinPopups.value = _coinPopups.value + newPopup
        saveGameState()
    }

    // Public Player Actions
    fun renameShop(newName: String) {
        if (newName.isBlank()) return
        _uiState.update { it.copy(shopName = newName.trim()) }
        saveGameState()
    }

    fun buyOrUpgradeShelf(shelfId: String) {
        val state = _uiState.value
        val shelf = GameRegistry.SHELVES.find { it.id == shelfId } ?: return
        val currentLevel = state.ownedShelves[shelfId]

        if (currentLevel == null) {
            // Buying for the first time
            if (state.coins >= shelf.baseCost) {
                _uiState.update {
                    it.copy(
                        coins = it.coins - shelf.baseCost,
                        ownedShelves = it.ownedShelves + (shelfId to 1)
                    )
                }
                saveGameState()
            }
        } else {
            // Upgrading existing shelf
            val upgradeCost = (shelf.baseCost * (currentLevel + 1) * 0.8).toLong()
            if (state.coins >= upgradeCost) {
                _uiState.update {
                    it.copy(
                        coins = it.coins - upgradeCost,
                        ownedShelves = it.ownedShelves + (shelfId to (currentLevel + 1))
                    )
                }
                saveGameState()
            }
        }
    }

    fun buyDecoration(decorId: String) {
        val state = _uiState.value
        val decor = GameRegistry.DECORATIONS.find { it.id == decorId } ?: return
        if (state.ownedDecorations.contains(decorId)) return

        if (state.coins >= decor.cost) {
            _uiState.update {
                it.copy(
                    coins = it.coins - decor.cost,
                    ownedDecorations = it.ownedDecorations + decorId
                )
            }
            saveGameState()
        }
    }

    fun claimPremiumVip(referenceCode: String) {
        // Unlock 50,000 coins and grant VIP status!
        _uiState.update {
            it.copy(
                coins = it.coins + 50000L,
                isVip = true
            )
        }
        saveGameState()

        // Trigger a huge floating VIP claim celebration popup on the cashier list screen
        val newPopup1 = CoinPopup(
            id = UUID.randomUUID().toString(),
            text = "👑 VIP UNLOCKED 👑",
            x = 1f,
            y = 1f,
            ageTicks = 35
        )
        val newPopup2 = CoinPopup(
            id = UUID.randomUUID().toString(),
            text = "+🪙 50,000 COINS",
            x = 1f,
            y = 1.3f,
            ageTicks = 35
        )
        _coinPopups.value = _coinPopups.value + newPopup1 + newPopup2
    }

    fun triggerTapBonus() {
        // Active tapping on cashier gives small instant cache payouts
        val state = _uiState.value
        val bonus = if (state.isVip) 100L else 30L
        _uiState.update { it.copy(coins = it.coins + bonus) }
        saveGameState()

        // Trigger visual
        val newPopup = CoinPopup(
            id = UUID.randomUUID().toString(),
            text = "+🪙 $bonus Tap!",
            x = 1.5f + Random.nextFloat() * 1.0f - 0.5f,
            y = 1.5f + Random.nextFloat() * 1.0f - 0.5f,
            ageTicks = 8
        )
        _coinPopups.value = _coinPopups.value + newPopup
    }
}
