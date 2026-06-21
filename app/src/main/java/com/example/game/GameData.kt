package com.example.game

data class GameState(
    val shopName: String = "Mogul Mart",
    val coins: Long = 10000,
    val ownedShelves: Map<String, Int> = mapOf("SHELF_VEGGIE" to 1), // ID to level (1+)
    val ownedDecorations: Set<String> = emptySet(),
    val isVip: Boolean = false,
    val lastSaved: Long = System.currentTimeMillis()
)

data class ShelfInfo(
    val id: String,
    val name: String,
    val baseCost: Long,
    val baseEarnings: Long,
    val earnIntervalTicks: Int, // ticks of 100ms
    val emoji: String,
    val hexColor: String,
    val category: String,
    val description: String
)

data class DecorInfo(
    val id: String,
    val name: String,
    val cost: Long,
    val coinMultiplier: Float, // multiplier on checkout (e.g. 1.15f for +15%)
    val speedMultiplier: Float, // multiplier on spawn rate (e.g. 1.2f for +20% faster)
    val emoji: String,
    val hexColor: String,
    val description: String
)

enum class CustomerState {
    ENTERING,
    BROWSING,
    CHECKOUT,
    LEAVING
}

data class SimulatedCustomer(
    val id: String,
    val name: String,
    val emoji: String,
    val state: CustomerState,
    val progress: Float, // 0.0f to 1.0f
    val targetShelfId: String?,
    val speechBubble: String? = null,
    val speechBubbleExpiry: Int = 0 // tick countdown
)

object GameRegistry {
    // Predefined items to buy
    val SHELVES = listOf(
        ShelfInfo(
            id = "SHELF_VEGGIE",
            name = "Fresh Veggie Stand",
            baseCost = 1000L,
            baseEarnings = 30L,
            earnIntervalTicks = 40, // 4 seconds
            emoji = "🥦🌽🍎",
            hexColor = "#4CAF50",
            category = "Produce",
            description = "Start simple! Stock organic vegetables and fresh local fruits."
        ),
        ShelfInfo(
            id = "SHELF_BAKERY",
            name = "Sweet Bakery Rack",
            baseCost = 3500L,
            baseEarnings = 120L,
            earnIntervalTicks = 60, // 6 seconds
            emoji = "🥐🍩🍞",
            hexColor = "#FF9800",
            category = "Bakery",
            description = "Smells like warm sourdough, glazed donuts, and buttery croissants."
        ),
        ShelfInfo(
            id = "SHELF_DAIRY",
            name = "Dairy Chiller",
            baseCost = 8000L,
            baseEarnings = 350L,
            earnIntervalTicks = 80, // 8 seconds
            emoji = "🥛🧀🧈",
            hexColor = "#00BCD4",
            category = "Cold Bar",
            description = "Keep it chilly! Chilled milk cartons, rich cheeses, and organic butter."
        ),
        ShelfInfo(
            id = "SHELF_ELECTRONICS",
            name = "Gadget Counter",
            baseCost = 15000L,
            baseEarnings = 850L,
            earnIntervalTicks = 110, // 11 seconds
            emoji = "🎧📱🎮",
            hexColor = "#9C27B0",
            category = "Tech",
            description = "High margin items! Headsets, smartphones, and dynamic gaming consoles."
        ),
        ShelfInfo(
            id = "SHELF_BOUTIQUE",
            name = "VIP Designer Corner",
            baseCost = 30000L,
            baseEarnings = 2200L,
            earnIntervalTicks = 150, // 15 seconds
            emoji = "👜🕶️👠",
            hexColor = "#E91E63",
            category = "Fashion",
            description = "Luxury handbags and designer glasses for elite catalog shoppers."
        )
    )

    val DECORATIONS = listOf(
        DecorInfo(
            id = "DECOR_BONSAI",
            name = "Zen Bonsai Plant",
            cost = 1500L,
            coinMultiplier = 1.10f, // +10% coins
            speedMultiplier = 1.05f, // +5% speed
            emoji = "🪴",
            hexColor = "#8BC34A",
            description = "A serene bonsai that calms the shoppers, increasing tips and order speed."
        ),
        DecorInfo(
            id = "DECOR_NEON",
            name = "Bright Open Neon",
            cost = 3000L,
            coinMultiplier = 1.15f, // +15% coins
            speedMultiplier = 1.20f, // +20% faster spawn
            emoji = "🚨",
            hexColor = "#FF5722",
            description = "Flashing orange open sign that draws massive crowds into your storefront."
        ),
        DecorInfo(
            id = "DECOR_FLOOR",
            name = "Glossy Marble Prep",
            cost = 6000L,
            coinMultiplier = 1.30f, // +30% coins
            speedMultiplier = 1.0f,
            emoji = "✨",
            hexColor = "#607D8B",
            description = "Polished white marble floor that screams high class, premium checkout rates."
        ),
        DecorInfo(
            id = "DECOR_CHANDELIER",
            name = "Crystal Chandelier",
            cost = 12000L,
            coinMultiplier = 1.45f, // +45% coins
            speedMultiplier = 1.15f, // +15% client speed
            emoji = "💡",
            hexColor = "#FFEB3B",
            description = "A lavish glowing chandelier. Customers tip extra under luxury light."
        ),
        DecorInfo(
            id = "DECOR_STATUE",
            name = "Golden Supermarket Cup",
            cost = 25000L,
            coinMultiplier = 1.70f, // +70% coins
            speedMultiplier = 1.30f, // +30% speed
            emoji = "🏆",
            hexColor = "#FFD700",
            description = "The ultimate trophy. Doubles prestige attraction and shopper checkout tips."
        )
    )

    val CUSTOMER_NAMES = listOf("Rohan", "Anjali", "Vikram", "Priya", "Rahul", "Neha", "Kabir", "Meera", "Aarav", "Kiara", "Dev", "Aditi")
    val CUSTOMER_EMOJIS = listOf("👨‍🦱", "👩", "👨", "👩‍🦰", "👴", "👵", "👱‍♂️", "👱‍♀️", "👲", "👩‍⚕️", "🧒", "👨‍🎓")
    val BROWSING_LINES = listOf(
        "Mmm, smells so fresh!",
        "Where is the discount stall?",
        "Everything is stocked beautifully!",
        "This store looks amazing!",
        "Wow, the floor is literally glowing!",
        "Let's see what's on discount.",
        "I love shopping at Mogul!",
        "I am buying healthy stuff."
    )
    val CHECKOUT_LINES = listOf(
        "Hope the lines are short!",
        "Tipping extra for the zen vibe!",
        "Love the decorations!",
        "Awesome customer checkout service.",
        "My absolute favorite local store!",
        "Tapping fast speeds up delivery!"
    )
}
