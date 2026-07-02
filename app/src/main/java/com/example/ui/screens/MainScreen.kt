package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.ExpenseSplitViewModel
import com.example.ui.viewmodel.GroupDetailsState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ExpenseSplitViewModel,
    modifier: Modifier = Modifier
) {
    val currentGroupId by viewModel.currentGroupId.collectAsStateWithLifecycle()
    val allGroups by viewModel.allGroups.collectAsStateWithLifecycle()
    val groupDetails by viewModel.groupDetailsState.collectAsStateWithLifecycle()

    var showCreateGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (currentGroupId == null) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ExpenseSplit",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentGroupId == null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateGroupDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Create Group") },
                    text = { Text("New PG/Hostel Group") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("create_group_fab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentGroupId == null) {
                GroupsListScreen(
                    groups = allGroups,
                    onGroupClick = { viewModel.selectGroup(it.id) },
                    onDeleteGroup = { viewModel.deleteGroup(it) }
                )
            } else {
                ActiveGroupScreen(
                    groupState = groupDetails,
                    viewModel = viewModel,
                    onBackClick = { viewModel.selectGroup(null) }
                )
            }

            if (showCreateGroupDialog) {
                CreateGroupDialog(
                    onDismiss = { showCreateGroupDialog = false },
                    onConfirm = { name, desc, members ->
                        viewModel.createGroup(name, desc, members)
                        showCreateGroupDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun GroupsListScreen(
    groups: List<Group>,
    onGroupClick: (Group) -> Unit,
    onDeleteGroup: (Group) -> Unit
) {
    if (groups.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to ExpenseSplit",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track shared PG expenses, roommate splits, and easily settle up with roomies in one place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Shared PG & Room Tracker",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select a group below to view expenses, roommate balances, and settle debts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(groups, key = { it.id }) { group ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGroupClick(group) }
                        .testTag("group_card_${group.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (group.description.isNotEmpty()) {
                                Text(
                                    text = group.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeleteGroup(group) },
                            modifier = Modifier.testTag("delete_group_button_${group.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Group",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGroupScreen(
    groupState: GroupDetailsState,
    viewModel: ExpenseSplitViewModel,
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showSettleUpDialog by remember { mutableStateOf(false) }
    var prefilledSettleUpDebtorId by remember { mutableStateOf<Int?>(null) }
    var prefilledSettleUpCreditorId by remember { mutableStateOf<Int?>(null) }
    var prefilledSettleUpAmount by remember { mutableStateOf(0.0) }

    val tabs = listOf("Expenses", "Balances", "History", "Roommates")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = groupState.group?.name ?: "Group Details",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (groupState.group?.description?.isNotEmpty() == true) {
                            Text(
                                text = groupState.group.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars
            ) {
                tabs.forEachIndexed { index, label ->
                    val icon = when (index) {
                        0 -> Icons.Default.ReceiptLong
                        1 -> Icons.Default.AccountBalanceWallet
                        2 -> Icons.Default.History
                        else -> Icons.Default.People
                    }
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label) },
                        icon = { Icon(icon, contentDescription = label) },
                        modifier = Modifier.testTag("tab_$label")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 && groupState.members.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Expense") },
                    text = { Text("Add Expense") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag("add_expense_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> ExpensesTab(
                    groupState = groupState,
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                1 -> BalancesTab(
                    groupState = groupState,
                    onSettleUpClick = { debtorId, creditorId, amount ->
                        prefilledSettleUpDebtorId = debtorId
                        prefilledSettleUpCreditorId = creditorId
                        prefilledSettleUpAmount = amount
                        showSettleUpDialog = true
                    }
                )
                2 -> SettlementsTab(
                    groupState = groupState,
                    onDeleteSettleUp = { viewModel.deleteSettleUp(it) }
                )
                3 -> RoommatesTab(
                    groupState = groupState,
                    onAddRoommate = { viewModel.addMemberToCurrentGroup(it) },
                    onDeleteRoommate = { viewModel.deleteMember(it) }
                )
            }
        }
    }

    if (showAddExpenseDialog && groupState.members.isNotEmpty()) {
        AddExpenseDialog(
            members = groupState.members,
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { description, amount, paidById, splitWithIds ->
                viewModel.addExpense(description, amount, paidById, splitWithIds)
                showAddExpenseDialog = false
            }
        )
    }

    if (showSettleUpDialog && groupState.members.isNotEmpty()) {
        SettleUpDialog(
            members = groupState.members,
            initialDebtorId = prefilledSettleUpDebtorId,
            initialCreditorId = prefilledSettleUpCreditorId,
            initialAmount = prefilledSettleUpAmount,
            onDismiss = {
                showSettleUpDialog = false
                prefilledSettleUpDebtorId = null
                prefilledSettleUpCreditorId = null
                prefilledSettleUpAmount = 0.0
            },
            onConfirm = { fromId, toId, amount ->
                viewModel.addSettleUp(fromId, toId, amount)
                showSettleUpDialog = false
                prefilledSettleUpDebtorId = null
                prefilledSettleUpCreditorId = null
                prefilledSettleUpAmount = 0.0
            }
        )
    }
}

// ----------------------------------------------------
// EXPENSES TAB
// ----------------------------------------------------
@Composable
fun ExpensesTab(
    groupState: GroupDetailsState,
    onDeleteExpense: (Expense) -> Unit
) {
    if (groupState.expenses.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Shared Expenses Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Get started by adding a split expense using the '+' button below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
        val memberMap = remember(groupState.members) { groupState.members.associateBy { it.id } }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val totalGroupSpent = groupState.expenses.sumOf { it.amount }
                val totalDebtsSum = groupState.debts.sumOf { it.amount }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Group Total Spendings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(50))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE VIEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = String.format("$%.2f", totalGroupSpent),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 36.sp
                            )
                            Text(
                                text = "total group spend",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Column 1: Total Outstanding Debts
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "OUTSTANDING DEBT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("$%.2f", totalDebtsSum),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            // Column 2: Roommates Count
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.6f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "ACTIVE ROOMMATES",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${groupState.members.size} members",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF388E3C)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            items(groupState.expenses, key = { it.id }) { expense ->
                val payerName = memberMap[expense.paidById]?.name ?: "Unknown"
                val splitsForThis = groupState.splits.filter { it.expenseId == expense.id }
                val countSplits = splitsForThis.size

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_card_${expense.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = expense.description,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Paid by $payerName",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            if (countSplits > 0) {
                                val splitSampleNames = splitsForThis.mapNotNull { memberMap[it.memberId]?.name }
                                Text(
                                    text = "Split equally with: ${splitSampleNames.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(expense.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = String.format("$%.2f", expense.amount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = { onDeleteExpense(expense) },
                                modifier = Modifier.testTag("delete_expense_button_${expense.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Expense",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// BALANCES TAB
// ----------------------------------------------------
@Composable
fun BalancesTab(
    groupState: GroupDetailsState,
    onSettleUpClick: (Int, Int, Double) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balances breakdown
        Text(
            text = "Roommate Net Balances",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (groupState.balances.isEmpty()) {
                    Text(
                        text = "No roommates configured to show balances.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    groupState.balances.forEachIndexed { index, balance ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (balance.netBalance >= 0) Icons.Default.ArrowCircleUp else Icons.Default.ArrowCircleDown,
                                contentDescription = null,
                                tint = if (balance.netBalance > 0.005) Color(0xFF2E7D32) else if (balance.netBalance < -0.005) Color(0xFFC62828) else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = balance.member.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format("Paid $%.2f | Spent $%.2f", balance.amountPaid, balance.amountOwed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (balance.netBalance > 0.005) {
                                    String.format("Gets back $%.2f", balance.netBalance)
                                } else if (balance.netBalance < -0.005) {
                                    String.format("Owes $%.2f", -balance.netBalance)
                                } else {
                                    "Settled"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (balance.netBalance > 0.005) Color(0xFF2E7D32) else if (balance.netBalance < -0.005) Color(0xFFC62828) else Color.Gray
                            )
                        }
                        if (index < groupState.balances.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Debt Simplification (Settle Up Suggestions)
        Text(
            text = "Suggested Settle-Up Steps",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (groupState.debts.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Awesome! Everyone is fully settled up!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                groupState.debts.forEach { debt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_card_${debt.fromMember.id}_to_${debt.toMember.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = debt.fromMember.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC62828)
                                    )
                                    Text(
                                        text = " owes ",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = debt.toMember.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Text(
                                    text = String.format("Settle balance with $%.2f", debt.amount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onSettleUpClick(debt.fromMember.id, debt.toMember.id, debt.amount) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("settle_button_${debt.fromMember.id}_${debt.toMember.id}")
                            ) {
                                Text("Settle Up")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SETTLEMENTS TAB (HISTORY)
// ----------------------------------------------------
@Composable
fun SettlementsTab(
    groupState: GroupDetailsState,
    onDeleteSettleUp: (SettleUp) -> Unit
) {
    if (groupState.settleUps.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Settle Ups Recorded",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "When roommates pay each other back, use the 'Settle Up' action on the Balances tab.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
        val memberMap = remember(groupState.members) { groupState.members.associateBy { it.id } }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(groupState.settleUps, key = { it.id }) { settleUp ->
                val payerName = memberMap[settleUp.fromMemberId]?.name ?: "Unknown"
                val receiverName = memberMap[settleUp.toMemberId]?.name ?: "Unknown"

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_up_card_${settleUp.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFF2E7D32).copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = payerName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Text(" paid ")
                                Text(
                                    text = receiverName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateFormat.format(Date(settleUp.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = String.format("$%.2f", settleUp.amount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                            IconButton(
                                onClick = { onDeleteSettleUp(settleUp) },
                                modifier = Modifier.testTag("delete_settle_up_${settleUp.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Undo Settlement",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// ROOMMATES TAB
// ----------------------------------------------------
@Composable
fun RoommatesTab(
    groupState: GroupDetailsState,
    onAddRoommate: (String) -> Unit,
    onDeleteRoommate: (Member) -> Unit
) {
    var newRoommateName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Roommate to PG",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newRoommateName,
                            onValueChange = { newRoommateName = it },
                            label = { Text("Roommate Name") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("roommate_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newRoommateName.isNotBlank()) {
                                    onAddRoommate(newRoommateName)
                                    newRoommateName = ""
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                .size(50.dp)
                                .testTag("add_roommate_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Roommates List (${groupState.members.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(groupState.members, key = { it.id }) { member ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roommate_card_${member.id}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onDeleteRoommate(member) },
                        modifier = Modifier.testTag("delete_roommate_button_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Roommate",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// DIALOGS
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, List<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var members by remember { mutableStateOf(listOf("", "")) } // Starts with 2 slots

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create Hostel/PG Group",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name (e.g. Room 302)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_group_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (e.g. Flat bills)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_group_desc"),
                    singleLine = true
                )

                HorizontalDivider()

                Text(
                    text = "Roommates Name List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                members.forEachIndexed { index, mName ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = mName,
                            onValueChange = { value ->
                                members = members.toMutableList().apply { this[index] = value }
                            },
                            label = { Text("Roommate ${index + 1}") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_group_roommate_$index"),
                            singleLine = true
                        )
                        if (members.size > 2) {
                            IconButton(
                                onClick = {
                                    members = members.toMutableList().apply { removeAt(index) }
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { members = members + "" },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add another roommate")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    desc,
                                    members.filter { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.testTag("confirm_create_group_button")
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    members: List<Member>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Int, List<Int>) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var paidByMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0) }
    var selectedSplitMembers by remember(members) { mutableStateOf(members.map { it.id }.toSet()) }

    var expandedPayerDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Split Expense",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (e.g. Groceries)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_desc_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    singleLine = true
                )

                // Paid By Dropdown selector
                ExposedDropdownMenuBox(
                    expanded = expandedPayerDropdown,
                    onExpandedChange = { expandedPayerDropdown = !expandedPayerDropdown }
                ) {
                    val currentPayerName = members.firstOrNull { it.id == paidByMemberId }?.name ?: "Select roommate"
                    OutlinedTextField(
                        value = currentPayerName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Who Paid?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayerDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("expense_payer_selector")
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPayerDropdown,
                        onDismissRequest = { expandedPayerDropdown = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    paidByMemberId = m.id
                                    expandedPayerDropdown = false
                                },
                                modifier = Modifier.testTag("payer_item_${m.id}")
                            )
                        }
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Split with Roommates",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                members.forEach { member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSplitMembers = if (selectedSplitMembers.contains(member.id)) {
                                    selectedSplitMembers - member.id
                                } else {
                                    selectedSplitMembers + member.id
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedSplitMembers.contains(member.id),
                            onCheckedChange = { checked ->
                                selectedSplitMembers = if (checked == true) {
                                    selectedSplitMembers + member.id
                                } else {
                                    selectedSplitMembers - member.id
                                }
                            },
                            modifier = Modifier.testTag("split_checkbox_${member.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(member.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                if (selectedSplitMembers.isNotEmpty()) {
                    val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0
                    val splitAmount = parsedAmount / selectedSplitMembers.size
                    Text(
                        text = String.format("Each pays: $%.2f", splitAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0
                            if (description.isNotBlank() && parsedAmount > 0.0 && selectedSplitMembers.isNotEmpty()) {
                                onConfirm(
                                    description,
                                    parsedAmount,
                                    paidByMemberId,
                                    selectedSplitMembers.toList()
                                )
                            }
                        },
                        enabled = description.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0.0 && selectedSplitMembers.isNotEmpty(),
                        modifier = Modifier.testTag("confirm_add_expense")
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun SettleUpDialog(
    members: List<Member>,
    initialDebtorId: Int?,
    initialCreditorId: Int?,
    initialAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Double) -> Unit
) {
    var fromMemberId by remember { mutableStateOf(initialDebtorId ?: members.firstOrNull()?.id ?: 0) }
    var toMemberId by remember { mutableStateOf(initialCreditorId ?: members.getOrNull(1)?.id ?: members.firstOrNull()?.id ?: 0) }
    var amountStr by remember { mutableStateOf(if (initialAmount > 0) String.format("%.2f", initialAmount) else "") }

    var expandedDebtor by remember { mutableStateOf(false) }
    var expandedCreditor by remember { mutableStateOf(false) }

    val fromMemberName = members.firstOrNull { it.id == fromMemberId }?.name ?: "Select Payer"
    val toMemberName = members.firstOrNull { it.id == toMemberId }?.name ?: "Select Receiver"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Record Settle-Up",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Debtor (From)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { expandedDebtor = true },
                        modifier = Modifier.fillMaxWidth().testTag("settle_payer_dropdown")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Who Paid?", style = MaterialTheme.typography.labelSmall)
                                Text(fromMemberName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedDebtor,
                        onDismissRequest = { expandedDebtor = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    fromMemberId = m.id
                                    expandedDebtor = false
                                }
                            )
                        }
                    }
                }

                // Creditor (To)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { expandedCreditor = true },
                        modifier = Modifier.fillMaxWidth().testTag("settle_receiver_dropdown")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Who Received?", style = MaterialTheme.typography.labelSmall)
                                Text(toMemberName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = expandedCreditor,
                        onDismissRequest = { expandedCreditor = false }
                    ) {
                        members.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    toMemberId = m.id
                                    expandedCreditor = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_amount_input"),
                    singleLine = true
                )

                if (fromMemberId == toMemberId) {
                    Text(
                        text = "Sender and Receiver cannot be the same roommate.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val parsedAmount = amountStr.toDoubleOrNull() ?: 0.0
                            if (parsedAmount > 0.0 && fromMemberId != toMemberId) {
                                onConfirm(fromMemberId, toMemberId, parsedAmount)
                            }
                        },
                        enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0.0 && fromMemberId != toMemberId,
                        modifier = Modifier.testTag("confirm_settle_up")
                    ) {
                        Text("Record Settle Up")
                    }
                }
            }
        }
    }
}
