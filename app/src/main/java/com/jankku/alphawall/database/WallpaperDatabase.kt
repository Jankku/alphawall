package com.jankku.alphawall.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jankku.alphawall.database.dao.CategoryDao
import com.jankku.alphawall.database.model.Category
import com.jankku.alphawall.database.model.RemoteKey
import com.jankku.alphawall.database.model.Wallpaper
import com.jankku.alphawall.util.Constants.DATABASE_NAME

@Database(
    entities = [Wallpaper::class, RemoteKey::class, Category::class],
    version = 3,
    exportSchema = false
)
abstract class WallpaperDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: WallpaperDatabase? = null

        fun getDatabase(
            context: Context
        ): WallpaperDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WallpaperDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}