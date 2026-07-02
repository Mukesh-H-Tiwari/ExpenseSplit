package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val appDao: AppDao) {
    val allGroups: Flow<List<Group>> = appDao.getAllGroups()

    fun getGroupById(groupId: Int): Flow<Group?> = appDao.getGroupById(groupId)

    fun getMembersByGroupId(groupId: Int): Flow<List<Member>> = appDao.getMembersByGroupId(groupId)

    fun getExpensesByGroupId(groupId: Int): Flow<List<Expense>> = appDao.getExpensesByGroupId(groupId)

    fun getExpenseSplitsByGroupId(groupId: Int): Flow<List<ExpenseSplit>> = appDao.getExpenseSplitsByGroupId(groupId)

    fun getSettleUpsByGroupId(groupId: Int): Flow<List<SettleUp>> = appDao.getSettleUpsByGroupId(groupId)

    suspend fun createGroupWithMembers(name: String, description: String, memberNames: List<String>): Int {
        val group = Group(name = name, description = description)
        val groupId = appDao.insertGroup(group).toInt()
        val members = memberNames.filter { it.isNotBlank() }.map {
            Member(groupId = groupId, name = it.trim())
        }
        if (members.isNotEmpty()) {
            appDao.insertMembers(members)
        }
        return groupId
    }

    suspend fun deleteGroup(group: Group) {
        appDao.deleteGroup(group)
    }

    suspend fun insertMember(member: Member) {
        appDao.insertMember(member)
    }

    suspend fun deleteMember(member: Member) {
        appDao.deleteMember(member)
    }

    suspend fun addExpenseWithSplits(expense: Expense, splits: List<ExpenseSplit>) {
        appDao.insertExpenseWithSplits(expense, splits)
    }

    suspend fun deleteExpense(expense: Expense) {
        appDao.deleteExpense(expense)
    }

    suspend fun insertSettleUp(settleUp: SettleUp) {
        appDao.insertSettleUp(settleUp)
    }

    suspend fun deleteSettleUp(settleUp: SettleUp) {
        appDao.deleteSettleUp(settleUp)
    }
}
