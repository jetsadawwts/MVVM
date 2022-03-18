package com.program.mvvm.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DogBreed::class], version = 1)
abstract class DogDatabase: RoomDatabase() {
    abstract fun dogDao(): DogDao

        companion object {
            @Volatile private  var instance: DogDatabase? = null
            private val LOOK = Any()

            operator fun invoke(context: Context) = instance ?: synchronized(LOOK) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }

            private fun buildDatabase(context: Context) = Room.databaseBuilder(
                context.applicationContext,
                DogDatabase::class.java,
                 "dogdatabase"
            ).build()
        }
}