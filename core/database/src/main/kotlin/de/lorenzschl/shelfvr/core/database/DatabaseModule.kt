package de.lorenzschl.shelfvr.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): ShelfVrDatabase =
        Room.databaseBuilder(context, ShelfVrDatabase::class.java, ShelfVrDatabase.NAME).build()

    @Provides
    fun serverDao(db: ShelfVrDatabase): ServerDao = db.serverDao()

    @Provides
    fun accountDao(db: ShelfVrDatabase): AccountDao = db.accountDao()

    @Provides
    fun shelfConfigDao(db: ShelfVrDatabase): ShelfConfigDao = db.shelfConfigDao()
}
