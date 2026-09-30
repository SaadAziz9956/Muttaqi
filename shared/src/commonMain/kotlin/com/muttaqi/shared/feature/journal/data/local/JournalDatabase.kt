package com.muttaqi.shared.feature.journal.data.local

import androidx.room.ColumnInfo
import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** The journal's own database, `journal.db`, on both platforms */
@Database(entities = [JournalEntryEntity::class], version = 1)
@ConstructedBy(JournalDatabaseConstructor::class)
internal abstract class JournalDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao

    companion object {
        const val FILE_NAME = "journal.db"
    }
}

// Room generates the actual for each platform
@Suppress("KotlinNoActualForExpect")
internal expect object JournalDatabaseConstructor : RoomDatabaseConstructor<JournalDatabase> {
    override fun initialize(): JournalDatabase
}

/** One entry as stored; times are milliseconds since 1970 (UTC) */
@Entity(tableName = "journal_entries", indices = [Index("created_at")])
internal data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Dao
internal interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY created_at DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun find(id: String): JournalEntryEntity?

    @Upsert
    suspend fun upsert(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun delete(id: String)

    /** Skips any entry whose id is already stored */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMissing(entries: List<JournalEntryEntity>)
}
