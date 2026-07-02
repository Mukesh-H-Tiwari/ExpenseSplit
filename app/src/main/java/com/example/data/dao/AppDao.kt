package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Groups
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupById(groupId: Int): Flow<Group?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    @Delete
    suspend fun deleteGroup(group: Group)

    // Members
    @Query("SELECT * FROM members WHERE groupId = :groupId ORDER BY name ASC")
    fun getMembersByGroupId(groupId: Int): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<Member>)

    @Delete
    suspend fun deleteMember(member: Member)

    // Expenses
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun getExpensesByGroupId(groupId: Int): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Splits
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseSplits(splits: List<ExpenseSplit>)

    @Query("SELECT es.* FROM expense_splits es INNER JOIN expenses e ON es.expenseId = e.id WHERE e.groupId = :groupId")
    fun getExpenseSplitsByGroupId(groupId: Int): Flow<List<ExpenseSplit>>

    @Query("DELETE FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun deleteExpenseSplitsByExpenseId(expenseId: Int)

    // SettleUps
    @Query("SELECT * FROM settle_ups WHERE groupId = :groupId ORDER BY date DESC")
    fun getSettleUpsByGroupId(groupId: Int): Flow<List<SettleUp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettleUp(settleUp: SettleUp): Long

    @Delete
    suspend fun deleteSettleUp(settleUp: SettleUp)

    // Transaction to insert Expense and Splits safely
    @Transaction
    suspend fun insertExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>) {
        val expenseId = insertExpense(expense).toInt()
        val updatedSplits = splits.map { it.copy(expenseId = expenseId) }
        insertExpenseSplits(updatedSplits)
    }
}
