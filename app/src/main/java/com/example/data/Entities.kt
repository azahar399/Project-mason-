package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "floors")
data class Floor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "work_columns")
data class WorkColumn(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val displayOrder: Int,
    val isSequential: Boolean,
    val requiresColumnId: Int? // Used for 'Sink Fitting requires Sink Cutting'
)

@Entity(
    tableName = "flats",
    foreignKeys = [
        ForeignKey(entity = Floor::class, parentColumns = ["id"], childColumns = ["floorId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("floorId")]
)
data class Flat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val floorId: Int,
    val sqFt: Double = 1200.0
)

@Entity(tableName = "persons")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val isMason: Boolean,
    val dailyRate: Double = 0.0
)

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("personId")]
)
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val amount: Double,
    val date: Long,
    val type: String = "advance", // "advance", "reward", "penalty"
    val description: String? = null
)

@Entity(
    tableName = "work_entries",
    foreignKeys = [
        ForeignKey(entity = Flat::class, parentColumns = ["id"], childColumns = ["flatId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = WorkColumn::class, parentColumns = ["id"], childColumns = ["workColumnId"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["masonId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["helperId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("flatId"), Index("workColumnId"), Index("masonId"), Index("helperId")]
)
data class WorkEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val flatId: Int,
    val workColumnId: Int,
    val masonId: Int?,
    val helperId: Int?,
    val date: Long,
    val description: String? = null,
    val isProblem: Boolean = false
)

@Entity(
    tableName = "other_work_entries",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["helperId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [Index("personId"), Index("helperId")]
)
data class OtherWorkEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val helperId: Int?,
    val date: Long,
    val description: String
)

data class WorkEntryWithDetails(
    @Embedded val workEntry: WorkEntry,
    @Relation(parentColumn = "flatId", entityColumn = "id") val flat: Flat,
    @Relation(parentColumn = "workColumnId", entityColumn = "id") val workColumn: WorkColumn,
    @Relation(parentColumn = "masonId", entityColumn = "id") val mason: Person?,
    @Relation(parentColumn = "helperId", entityColumn = "id") val helper: Person?
)

data class OtherWorkEntryWithDetails(
    @Embedded val otherWorkEntry: OtherWorkEntry,
    @Relation(parentColumn = "personId", entityColumn = "id") val person: Person,
    @Relation(parentColumn = "helperId", entityColumn = "id") val helper: Person?
)

@Entity(
    tableName = "daily_wage_entries",
    foreignKeys = [
        ForeignKey(entity = Person::class, parentColumns = ["id"], childColumns = ["personId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["personId", "date"], unique = true)]
)
data class DailyWageEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val personId: Int,
    val date: Long,
    val unit: Double = 0.0,
    val advance: Double = 0.0,
    val reward: Double = 0.0,
    val penalty: Double = 0.0
)
