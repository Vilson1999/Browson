package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.HistoryDao
import com.example.data.local.dao.SpeedDialDao
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HistoryEntity
import com.example.data.local.entities.SpeedDialEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HistoryEntity::class, BookmarkEntity::class, SpeedDialEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun speedDialDao(): SpeedDialDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getInstance(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "operawave_browser.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default speed dials
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            database.speedDialDao().insertSpeedDials(defaultSpeedDials)
                            // Also pre-seed a couple useful default bookmarks
                            database.bookmarkDao().insertBookmark(
                                BookmarkEntity(
                                    url = "https://www.google.com",
                                    title = "Google Search",
                                    folder = "Favorites"
                                )
                            )
                            database.bookmarkDao().insertBookmark(
                                BookmarkEntity(
                                    url = "https://en.wikipedia.org",
                                    title = "Wikipedia - The Free Encyclopedia",
                                    folder = "Favorites"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        val defaultSpeedDials = listOf(
            SpeedDialEntity(
                title = "Google",
                url = "https://www.google.com",
                iconColor = 0xFF4285F4,
                sortOrder = 0
            ),
            SpeedDialEntity(
                title = "YouTube",
                url = "https://m.youtube.com",
                iconColor = 0xFFFF0000,
                sortOrder = 1
            ),
            SpeedDialEntity(
                title = "Wikipedia",
                url = "https://en.m.wikipedia.org",
                iconColor = 0xFF333333,
                sortOrder = 2
            ),
            SpeedDialEntity(
                title = "Reddit",
                url = "https://www.reddit.com",
                iconColor = 0xFFFF4500,
                sortOrder = 3
            ),
            SpeedDialEntity(
                title = "GitHub",
                url = "https://github.com",
                iconColor = 0xFF24292E,
                sortOrder = 4
            ),
            SpeedDialEntity(
                title = "Amazon",
                url = "https://www.amazon.com",
                iconColor = 0xFFFF9900,
                sortOrder = 5
            ),
            SpeedDialEntity(
                title = "X (Twitter)",
                url = "https://x.com",
                iconColor = 0xFF14171A,
                sortOrder = 6
            ),
            SpeedDialEntity(
                title = "BBC News",
                url = "https://www.bbc.com/news",
                iconColor = 0xFFBB1919,
                sortOrder = 7
            )
        )
    }
}
