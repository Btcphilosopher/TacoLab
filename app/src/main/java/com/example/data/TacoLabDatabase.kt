package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MenuItemEntity::class,
        OrderEntity::class,
        LoyaltyEntity::class,
        FavoriteMealEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TacoLabConverters::class)
abstract class TacoLabDatabase : RoomDatabase() {
    abstract fun tacoLabDao(): TacoLabDao

    companion object {
        @Volatile
        private var INSTANCE: TacoLabDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TacoLabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TacoLabDatabase::class.java,
                    "tacolab_database_v2"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // This callback runs only once on creation
                        INSTANCE?.let { database ->
                            scope.launch(Dispatchers.IO) {
                                populateInitialData(database.tacoLabDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(dao: TacoLabDao) {
            val menuItems = listOf(
                MenuItemEntity(
                    "taco_crunch",
                    "Double Stack Crunch Taco",
                    "Tacos",
                    3.49,
                    320,
                    true,
                    "Double-layered taco with crispy corn shell, warm flour tortilla, seasoned beef, cheddar, shredded lettuce, and taco sauce."
                ),
                MenuItemEntity(
                    "taco_volcano",
                    "Volcano Lava Taco",
                    "Tacos",
                    2.89,
                    290,
                    true,
                    "Red-lava corn shell stuffed with spicy flame-grilled chicken, Habanero cream salsa, lettuce, and Cotija cheese."
                ),
                MenuItemEntity(
                    "burrito_cantina",
                    "Cantina Epic Burrito",
                    "Burritos",
                    8.49,
                    780,
                    true,
                    "Slow-braised carnitas (pork), lime-cilantro rice, black beans, real hand-smashed guacamole, and mild Pico de Gallo."
                ),
                MenuItemEntity(
                    "burrito_supreme",
                    "AI Lab Purist Burrito",
                    "Burritos",
                    7.99,
                    650,
                    true,
                    "Lab special. Seasoned lean beef, warm black beans, shredded cheese, spicy fire salsa, and sour cream, toasted on the flat-top."
                ),
                MenuItemEntity(
                    "quesadilla_mega",
                    "Mega-Melt Cheese Quesadilla",
                    "Quesadillas",
                    5.99,
                    610,
                    true,
                    "Large grilled flour tortilla loaded with flame-grilled chicken, melted cheddar-jack cheese blend, and creamy jalapeño sauce."
                ),
                MenuItemEntity(
                    "nachos_bell_lab",
                    "Bell Lab Super Nachos",
                    "Nachos",
                    7.29,
                    1100,
                    true,
                    "Piles of crispy tortilla chips drenched in warm high-temp cheddar queso, seasoned beef, pinto beans, fresh guacamole, and jalapeños."
                ),
                MenuItemEntity(
                    "bowl_keto_tex",
                    "Keto Tex Protein Bowl",
                    "Bowls",
                    8.99,
                    430,
                    true,
                    "Romaine lettuce and cabbage mix, double flame-grilled chicken, Cotija cheese, hand-mashed guac, and low-carb fire salsa."
                ),
                MenuItemEntity(
                    "fries_mexi_lab",
                    "Mexi-Lab Seasoned Fries",
                    "Fries",
                    3.49,
                    450,
                    true,
                    "Crispy crinkle-cut fries with smokey chili-lime seasoning, served side-by-side with warm cheese queso sauce."
                ),
                MenuItemEntity(
                    "drink_freeze_baja",
                    "Baja Frost Freeze",
                    "Drinks",
                    2.79,
                    190,
                    true,
                    "Sub-zero slush blend of sparkling tropical lime and sweet blue desert agave extracts."
                ),
                MenuItemEntity(
                    "dessert_churros",
                    "Cinnamon Sweet Churros",
                    "Desserts",
                    3.19,
                    360,
                    true,
                    "Three sweet golden-brown pastry churros rolled in cinnamon-sugar dust with a shot of warm cajeta caramel."
                )
            )
            dao.insertMenuItems(menuItems)

            // Inject initial loyal profile
            dao.insertLoyaltyProfile(
                LoyaltyEntity(
                    id = 1,
                    pointsBalance = 380,
                    tier = "Taco Regular",
                    pointsToNextTier = 120,
                    orderStreakCount = 5,
                    referralsCount = 3
                )
            )
        }
    }
}
