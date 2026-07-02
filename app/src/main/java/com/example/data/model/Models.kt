package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val name: String,
    val email: String = ""
)

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["paidById"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("paidById")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val description: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis(),
    val paidById: Int
)

@Entity(
    tableName = "expense_splits",
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("expenseId"), Index("memberId")]
)
data class ExpenseSplit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expenseId: Int,
    val memberId: Int,
    val amount: Double
)

@Entity(
    tableName = "settle_ups",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["fromMemberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["toMemberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("fromMemberId"), Index("toMemberId")]
)
data class SettleUp(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val fromMemberId: Int,
    val toMemberId: Int,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)
