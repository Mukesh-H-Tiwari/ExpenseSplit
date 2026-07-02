package com.example.domain

import com.example.data.model.*

data class MemberBalance(
    val member: Member,
    val amountPaid: Double = 0.0,
    val amountOwed: Double = 0.0,
    val netBalance: Double = 0.0
)

data class Debt(
    val fromMember: Member,
    val toMember: Member,
    val amount: Double
)

object DebtSimplifier {
    fun calculateBalances(
        members: List<Member>,
        expenses: List<Expense>,
        splits: List<ExpenseSplit>,
        settleUps: List<SettleUp>
    ): List<MemberBalance> {
        val paidMap = mutableMapOf<Int, Double>().withDefault { 0.0 }
        val owedMap = mutableMapOf<Int, Double>().withDefault { 0.0 }
        
        // Add expenses
        for (expense in expenses) {
            val paidBy = expense.paidById
            paidMap[paidBy] = paidMap.getValue(paidBy) + expense.amount
        }
        
        // Add splits
        for (split in splits) {
            val memberId = split.memberId
            owedMap[memberId] = owedMap.getValue(memberId) + split.amount
        }
        
        // Add settle ups
        for (settleUp in settleUps) {
            val fromId = settleUp.fromMemberId
            val toId = settleUp.toMemberId
            paidMap[fromId] = paidMap.getValue(fromId) + settleUp.amount
            owedMap[toId] = owedMap.getValue(toId) + settleUp.amount
        }
        
        return members.map { member ->
            val paid = paidMap.getValue(member.id)
            val owed = owedMap.getValue(member.id)
            MemberBalance(
                member = member,
                amountPaid = paid,
                amountOwed = owed,
                netBalance = paid - owed
            )
        }
    }

    fun calculateDebts(balances: List<MemberBalance>): List<Debt> {
        val debtors = mutableListOf<Pair<Member, Double>>()
        val creditors = mutableListOf<Pair<Member, Double>>()
        
        for (b in balances) {
            if (b.netBalance < -0.005) {
                debtors.add(b.member to -b.netBalance)
            } else if (b.netBalance > 0.005) {
                creditors.add(b.member to b.netBalance)
            }
        }
        
        val debts = mutableListOf<Debt>()
        
        var debtorIdx = 0
        var creditorIdx = 0
        
        val mutableDebtors = debtors.toMutableList()
        val mutableCreditors = creditors.toMutableList()
        
        while (debtorIdx < mutableDebtors.size && creditorIdx < mutableCreditors.size) {
            val (debtor, debtAmount) = mutableDebtors[debtorIdx]
            val (creditor, creditAmount) = mutableCreditors[creditorIdx]
            
            val settleAmount = minOf(debtAmount, creditAmount)
            
            if (settleAmount > 0.005) {
                debts.add(Debt(debtor, creditor, settleAmount))
            }
            
            mutableDebtors[debtorIdx] = debtor to (debtAmount - settleAmount)
            mutableCreditors[creditorIdx] = creditor to (creditAmount - settleAmount)
            
            if (mutableDebtors[debtorIdx].second <= 0.005) {
                debtorIdx++
            }
            if (mutableCreditors[creditorIdx].second <= 0.005) {
                creditorIdx++
            }
        }
        
        return debts
    }
}
