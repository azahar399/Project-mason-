package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class AppViewModel(private val repository: AppRepository) : ViewModel() {
    private val chatbotEngine = com.example.utils.ChatbotEngine(repository)
    private val geminiChatbot = com.example.utils.GeminiChatbot(repository)
    val allFloors: StateFlow<List<Floor>> = repository.allFloors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkColumns: StateFlow<List<WorkColumn>> = repository.allWorkColumns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFlats: StateFlow<List<Flat>> = repository.allFlats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMasons: StateFlow<List<Person>> = repository.allMasons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHelpers: StateFlow<List<Person>> = repository.allHelpers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkEntries: StateFlow<List<WorkEntryWithDetails>> = repository.allWorkEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOtherWorkEntries: StateFlow<List<OtherWorkEntryWithDetails>> = repository.allOtherWorkEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPersons: StateFlow<List<Person>> = repository.allPersons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLedgerTransactions: StateFlow<List<LedgerTransaction>> = repository.allLedgerTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val allDailyWageEntries: StateFlow<List<DailyWageEntry>> = repository.allDailyWageEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            try {
                repository.allWorkColumns.take(1).collect { cols ->
                    if (cols.size < 26) {
                        try {
                            cols.forEach { repository.deleteWorkColumn(it) }
                        } catch (de: Throwable) {
                            android.util.Log.e("AppViewModel", "Error deleting work columns during setup", de)
                        }
                        val newCols = ('A'..'Z').mapIndexed { index, c -> 
                            WorkColumn(name = "", displayOrder = index, isSequential = false, requiresColumnId = null)
                        }
                        try {
                            newCols.forEach { repository.insertWorkColumn(it) }
                        } catch (ie: Throwable) {
                            android.util.Log.e("AppViewModel", "Error inserting initial work columns", ie)
                        }
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("AppViewModel", "WorkColumns init failed", e)
            }
            
            try {
                repository.allFloors.take(1).collect { floors ->
                    floors.find { it.name == "Main Grid" }?.let { mainGrid ->
                        try {
                            repository.deleteFloor(mainGrid)
                        } catch (de: Throwable) {
                            android.util.Log.e("AppViewModel", "Error deleting Main Grid floor", de)
                        }
                    }
                    floors.find { it.name == "Excel Grid Floor" }?.let { excelGrid ->
                        try {
                            repository.deleteFloor(excelGrid)
                        } catch (de: Throwable) {
                            android.util.Log.e("AppViewModel", "Error deleting Excel Grid Floor", de)
                        }
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("AppViewModel", "Floors cleanup failed", e)
            }
            
            try {
                repository.allFlats.take(1).collect { flats ->
                    flats.filter { it.name == "" }.forEach {
                        try {
                            repository.deleteFlat(it)
                        } catch (de: Throwable) {
                            android.util.Log.e("AppViewModel", "Error deleting empty flat", de)
                        }
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("AppViewModel", "Flats cleanup failed", e)
            }
        }
    }

    fun insertFloor(name: String) = viewModelScope.launch { repository.insertFloor(Floor(name = name)) }
    suspend fun insertFloorAndGetId(name: String): Int {
        return repository.insertFloor(Floor(name = name)).toInt()
    }
    fun deleteFloor(floor: Floor) = viewModelScope.launch { repository.deleteFloor(floor) }

    fun insertFlat(name: String, floorId: Int, sqFt: Double = 1200.0) = viewModelScope.launch { repository.insertFlat(Flat(name = name, floorId = floorId, sqFt = sqFt)) }
    suspend fun insertFlatAndGetId(name: String, floorId: Int, sqFt: Double = 1200.0): Int {
        return repository.insertFlat(Flat(name = name, floorId = floorId, sqFt = sqFt)).toInt()
    }
    fun updateFlat(flat: Flat) = viewModelScope.launch { repository.updateFlat(flat) }
    fun deleteFlat(flat: Flat) = viewModelScope.launch { repository.deleteFlat(flat) }

    fun updateWorkColumn(workColumn: WorkColumn) = viewModelScope.launch { repository.updateWorkColumn(workColumn) }
    fun insertWorkColumn(workColumn: WorkColumn) = viewModelScope.launch { repository.insertWorkColumn(workColumn) }
    fun deleteWorkColumn(workColumn: WorkColumn) = viewModelScope.launch { repository.deleteWorkColumn(workColumn) }

    fun insertPerson(name: String, isMason: Boolean, dailyRate: Double = 0.0) = viewModelScope.launch { repository.insertPerson(Person(name = name, isMason = isMason, dailyRate = dailyRate)) }
    suspend fun insertPersonAndGetId(name: String, isMason: Boolean, dailyRate: Double = 0.0): Int {
        return repository.insertPerson(Person(name = name, isMason = isMason, dailyRate = dailyRate)).toInt()
    }
    fun updatePerson(person: Person) = viewModelScope.launch { repository.updatePerson(person) }
    fun deletePerson(person: Person) = viewModelScope.launch { repository.deletePerson(person) }

    fun insertLedgerTransaction(personId: Int, amount: Double, date: Long, type: String = "advance", description: String? = null) = viewModelScope.launch {
        repository.insertLedgerTransaction(LedgerTransaction(personId = personId, amount = amount, date = date, type = type, description = description))
    }
    
    fun saveDailyWageEntry(personId: Int, date: Long, unit: Double, advance: Double, reward: Double, penalty: Double) = viewModelScope.launch {
        repository.saveDailyWageEntry(personId, date, unit, advance, reward, penalty)
    }

    fun deleteLedgerTransaction(ledgerTransaction: LedgerTransaction) = viewModelScope.launch { repository.deleteLedgerTransaction(ledgerTransaction) }

    fun insertWorkEntry(flatId: Int, workColumnId: Int, masonId: Int?, helperId: Int?, date: Long, description: String? = null, isProblem: Boolean = false) = viewModelScope.launch {
        repository.insertWorkEntry(WorkEntry(flatId = flatId, workColumnId = workColumnId, masonId = masonId, helperId = helperId, date = date, description = description, isProblem = isProblem))
    }
    fun updateWorkEntry(workEntry: WorkEntry) = viewModelScope.launch { repository.updateWorkEntry(workEntry) }
    fun deleteWorkEntry(workEntry: WorkEntry) = viewModelScope.launch { repository.deleteWorkEntry(workEntry) }

    fun insertOtherWorkEntry(personId: Int, helperId: Int?, description: String, date: Long) = viewModelScope.launch {
        repository.insertOtherWorkEntry(OtherWorkEntry(personId = personId, helperId = helperId, description = description, date = date))
    }

    fun getPendingWorksForFlat(flatId: Int, entriesForFlat: List<WorkEntryWithDetails>, allColumns: List<WorkColumn>): List<WorkColumn> {
        val completedColIds = entriesForFlat.filter { !it.workEntry.isProblem }.map { it.workEntry.workColumnId }
        val pending = mutableListOf<WorkColumn>()

        for (col in allColumns.filter { it.isSequential }.sortedBy { it.displayOrder }) {
            if (!completedColIds.contains(col.id)) {
                pending.add(col)
                return pending 
            }
        }

        for (col in allColumns.filter { !it.isSequential }) {
            if (!completedColIds.contains(col.id)) {
                if (col.requiresColumnId == null || completedColIds.contains(col.requiresColumnId)) {
                    pending.add(col)
                }
            }
        }
        
        return pending
    }

    suspend fun processPremiumChatCommand(command: String, apiKey: String): String {
        return geminiChatbot.processPremiumMessage(command, apiKey)
    }

    suspend fun processChatCommand(command: String): String {
        return chatbotEngine.processMessage(command)
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
