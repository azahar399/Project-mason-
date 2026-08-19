package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    val allFloors: Flow<List<Floor>> = appDao.getAllFloors()
    val allWorkColumns: Flow<List<WorkColumn>> = appDao.getAllWorkColumns()
    val allFlats: Flow<List<Flat>> = appDao.getAllFlats()
    val allMasons: Flow<List<Person>> = appDao.getAllMasons()
    val allHelpers: Flow<List<Person>> = appDao.getAllHelpers()
    val allPersons: Flow<List<Person>> = appDao.getAllPersons()
    val allWorkEntries: Flow<List<WorkEntryWithDetails>> = appDao.getAllWorkEntries()
    val allOtherWorkEntries: Flow<List<OtherWorkEntryWithDetails>> = appDao.getAllOtherWorkEntries()
    val allLedgerTransactions: Flow<List<LedgerTransaction>> = appDao.getAllLedgerTransactions()

    suspend fun insertFloor(floor: Floor): Long = appDao.insertFloor(floor)
    suspend fun deleteFloor(floor: Floor) = appDao.deleteFloor(floor)

    suspend fun insertWorkColumn(workColumn: WorkColumn) = appDao.insertWorkColumn(workColumn)
    suspend fun updateWorkColumn(workColumn: WorkColumn) = appDao.updateWorkColumn(workColumn)
    suspend fun deleteWorkColumn(workColumn: WorkColumn) = appDao.deleteWorkColumn(workColumn)
    suspend fun insertWorkColumns(workColumns: List<WorkColumn>) = appDao.insertWorkColumns(workColumns)

    suspend fun insertFlat(flat: Flat): Long = appDao.insertFlat(flat)
    suspend fun updateFlat(flat: Flat) = appDao.updateFlat(flat)
    suspend fun deleteFlat(flat: Flat) = appDao.deleteFlat(flat)

    suspend fun insertPerson(person: Person): Long = appDao.insertPerson(person)
    suspend fun updatePerson(person: Person) = appDao.updatePerson(person)
    suspend fun deletePerson(person: Person) = appDao.deletePerson(person)

    suspend fun insertLedgerTransaction(ledgerTransaction: LedgerTransaction) = appDao.insertLedgerTransaction(ledgerTransaction)
    suspend fun deleteLedgerTransaction(ledgerTransaction: LedgerTransaction) = appDao.deleteLedgerTransaction(ledgerTransaction)

    fun getWorkEntriesForFlat(flatId: Int) = appDao.getWorkEntriesForFlat(flatId)
    fun getWorkEntriesForPerson(personId: Int) = appDao.getWorkEntriesForPerson(personId)
    fun getWorkEntriesForDate(startOfDay: Long, endOfDay: Long) = appDao.getWorkEntriesForDate(startOfDay, endOfDay)
    
    suspend fun insertWorkEntry(workEntry: WorkEntry) = appDao.insertWorkEntry(workEntry)
    suspend fun updateWorkEntry(workEntry: WorkEntry) = appDao.updateWorkEntry(workEntry)
    suspend fun deleteWorkEntry(workEntry: WorkEntry) = appDao.deleteWorkEntry(workEntry)

    fun getOtherWorkEntriesForPerson(personId: Int) = appDao.getOtherWorkEntriesForPerson(personId)
    suspend fun insertOtherWorkEntry(otherWorkEntry: OtherWorkEntry) = appDao.insertOtherWorkEntry(otherWorkEntry)
    suspend fun deleteOtherWorkEntry(otherWorkEntry: OtherWorkEntry) = appDao.deleteOtherWorkEntry(otherWorkEntry)

    val allDailyWageEntries: Flow<List<DailyWageEntry>> = appDao.getAllDailyWageEntries()

    suspend fun saveDailyWageEntry(personId: Int, date: Long, unit: Double, advance: Double, reward: Double, penalty: Double) {
        val existing = appDao.getDailyWageEntryByDate(personId, date)
        if (existing != null) {
            appDao.insertDailyWageEntry(existing.copy(unit = unit, advance = advance, reward = reward, penalty = penalty))
        } else {
            appDao.insertDailyWageEntry(DailyWageEntry(personId = personId, date = date, unit = unit, advance = advance, reward = reward, penalty = penalty))
        }
    }
}
