package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.ExpenseRepository
import com.example.domain.Debt
import com.example.domain.DebtSimplifier
import com.example.domain.MemberBalance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GroupDetailsState(
    val group: Group? = null,
    val members: List<Member> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val splits: List<ExpenseSplit> = emptyList(),
    val settleUps: List<SettleUp> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val debts: List<Debt> = emptyList()
)

class ExpenseSplitViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // List of all groups
    val allGroups: StateFlow<List<Group>> = repository.allGroups
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current selected Group ID
    private val _currentGroupId = MutableStateFlow<Int?>(null)
    val currentGroupId: StateFlow<Int?> = _currentGroupId.asStateFlow()

    // Combined details for current group
    @OptIn(ExperimentalCoroutinesApi::class)
    val groupDetailsState: StateFlow<GroupDetailsState> = _currentGroupId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(GroupDetailsState())
            } else {
                combine(
                    repository.getGroupById(id),
                    repository.getMembersByGroupId(id),
                    repository.getExpensesByGroupId(id),
                    repository.getExpenseSplitsByGroupId(id),
                    repository.getSettleUpsByGroupId(id)
                ) { group, members, expenses, splits, settleUps ->
                    val balances = DebtSimplifier.calculateBalances(members, expenses, splits, settleUps)
                    val debts = DebtSimplifier.calculateDebts(balances)
                    GroupDetailsState(
                        group = group,
                        members = members,
                        expenses = expenses,
                        splits = splits,
                        settleUps = settleUps,
                        balances = balances,
                        debts = debts
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupDetailsState()
        )

    fun selectGroup(groupId: Int?) {
        _currentGroupId.value = groupId
    }

    fun createGroup(name: String, description: String, memberNames: List<String>) {
        viewModelScope.launch {
            val id = repository.createGroupWithMembers(name, description, memberNames)
            _currentGroupId.value = id
        }
    }

    fun deleteGroup(group: Group) {
        viewModelScope.launch {
            if (_currentGroupId.value == group.id) {
                _currentGroupId.value = null
            }
            repository.deleteGroup(group)
        }
    }

    fun addMemberToCurrentGroup(name: String) {
        val groupId = _currentGroupId.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertMember(Member(groupId = groupId, name = name.trim()))
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun addExpense(
        description: String,
        amount: Double,
        paidByMemberId: Int,
        splitWithMemberIds: List<Int>
    ) {
        val groupId = _currentGroupId.value ?: return
        if (description.isBlank() || amount <= 0.0 || splitWithMemberIds.isEmpty()) return

        viewModelScope.launch {
            val expense = Expense(
                groupId = groupId,
                description = description.trim(),
                amount = amount,
                paidById = paidByMemberId
            )
            // Divide equally among split members
            val splitAmount = amount / splitWithMemberIds.size
            val splits = splitWithMemberIds.map { memberId ->
                ExpenseSplit(
                    expenseId = 0, // Set inside transaction
                    memberId = memberId,
                    amount = splitAmount
                )
            }
            repository.addExpenseWithSplits(expense, splits)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addSettleUp(fromMemberId: Int, toMemberId: Int, amount: Double) {
        val groupId = _currentGroupId.value ?: return
        if (amount <= 0.0) return

        viewModelScope.launch {
            val settleUp = SettleUp(
                groupId = groupId,
                fromMemberId = fromMemberId,
                toMemberId = toMemberId,
                amount = amount
            )
            repository.insertSettleUp(settleUp)
        }
    }

    fun deleteSettleUp(settleUp: SettleUp) {
        viewModelScope.launch {
            repository.deleteSettleUp(settleUp)
        }
    }
}

class ExpenseSplitViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseSplitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseSplitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
