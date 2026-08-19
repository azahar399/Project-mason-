package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM floors ORDER BY name ASC")
    fun getAllFloors(): Flow<List<Floor>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFloor(floor: Floor): Long

    @Delete
    suspend fun deleteFloor(floor: Floor)

    @Query("SELECT * FROM work_columns ORDER BY displayOrder ASC")
    fun getAllWorkColumns(): Flow<List<WorkColumn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkColumn(workColumn: WorkColumn)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkColumns(workColumns: List<WorkColumn>)

    @Update
    suspend fun updateWorkColumn(workColumn: WorkColumn)

    @Delete
    suspend fun deleteWorkColumn(workColumn: WorkColumn)

    @Query("SELECT * FROM flats ORDER BY name ASC")
    fun getAllFlats(): Flow<List<Flat>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFlat(flat: Flat): Long

    @Update
    suspend fun updateFlat(flat: Flat)

    @Delete
    suspend fun deleteFlat(flat: Flat)

    @Query("SELECT * FROM persons WHERE isMason = 1 ORDER BY name ASC")
    fun getAllMasons(): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE isMason = 0 ORDER BY name ASC")
    fun getAllHelpers(): Flow<List<Person>>
    
    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersons(): Flow<List<Person>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC")
    fun getAllLedgerTransactions(): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerTransaction(ledgerTransaction: LedgerTransaction)

    @Delete
    suspend fun deleteLedgerTransaction(ledgerTransaction: LedgerTransaction)

    @Transaction
    @Query("SELECT * FROM work_entries ORDER BY date DESC")
    fun getAllWorkEntries(): Flow<List<WorkEntryWithDetails>>

    @Transaction
    @Query("SELECT * FROM work_entries WHERE flatId = :flatId ORDER BY date DESC")
    fun getWorkEntriesForFlat(flatId: Int): Flow<List<WorkEntryWithDetails>>

    @Transaction
    @Query("SELECT * FROM work_entries WHERE masonId = :personId OR helperId = :personId ORDER BY date DESC")
    fun getWorkEntriesForPerson(personId: Int): Flow<List<WorkEntryWithDetails>>
    
    @Transaction
    @Query("SELECT * FROM work_entries WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY date DESC")
    fun getWorkEntriesForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkEntryWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkEntry(workEntry: WorkEntry)

    @Update
    suspend fun updateWorkEntry(workEntry: WorkEntry)

    @Delete
    suspend fun deleteWorkEntry(workEntry: WorkEntry)

    @Transaction
    @Query("SELECT * FROM other_work_entries ORDER BY date DESC")
    fun getAllOtherWorkEntries(): Flow<List<OtherWorkEntryWithDetails>>
    
    @Transaction
    @Query("SELECT * FROM other_work_entries WHERE personId = :personId OR helperId = :personId ORDER BY date DESC")
    fun getOtherWorkEntriesForPerson(personId: Int): Flow<List<OtherWorkEntryWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtherWorkEntry(otherWorkEntry: OtherWorkEntry)

    @Delete
    suspend fun deleteOtherWorkEntry(otherWorkEntry: OtherWorkEntry)

    @Query("SELECT * FROM daily_wage_entries ORDER BY date DESC")
    fun getAllDailyWageEntries(): Flow<List<DailyWageEntry>>

    @Query("SELECT * FROM daily_wage_entries WHERE personId = :personId AND date = :date LIMIT 1")
    suspend fun getDailyWageEntryByDate(personId: Int, date: Long): DailyWageEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyWageEntry(entry: DailyWageEntry)

    @Delete
    suspend fun deleteDailyWageEntry(entry: DailyWageEntry)
}
