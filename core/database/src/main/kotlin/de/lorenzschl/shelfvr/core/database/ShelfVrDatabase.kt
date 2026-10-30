package de.lorenzschl.shelfvr.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version 1. Jede Schemaänderung braucht eine Migration plus Test in `MigrationTest`;
 * destruktive Migrationen sind nicht erlaubt (Fortschritt im Offline-Puffer, NFR-04).
 */
@Database(
    entities = [ServerEntity::class, AccountEntity::class, ShelfConfigEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ShelfVrDatabase : RoomDatabase() {
    abstract fun serverDao(): ServerDao
    abstract fun accountDao(): AccountDao
    abstract fun shelfConfigDao(): ShelfConfigDao

    companion object {
        const val NAME = "shelfvr.db"
    }
}
