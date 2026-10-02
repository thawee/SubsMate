package com.mate.subsmate.ui.add_subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.CategoryDefaults
import com.mate.subsmate.domain.model.PaymentType
import com.mate.subsmate.domain.model.ServiceTemplate
import com.mate.subsmate.domain.model.TemplateLibrary
import com.mate.subsmate.ui.theme.GlassyCard
import com.mate.subsmate.ui.utils.CurrencyUtils
import com.mate.subsmate.ui.utils.IconUtils
import com.mate.subsmate.ui.utils.VendorUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: AddSubscriptionViewModel,
    currency: String,
    onNavigateBack: () -> Unit,
    onNavigateToReceiptScan: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getSymbol(if (uiState.id != 0L && uiState.currency.isNotBlank()) uiState.currency else currency)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()

    val isNameValid = uiState.name.isNotBlank()
    val isPriceValid = uiState.isVariablePrice || uiState.price.toDoubleOrNull() != null
    val loanPrice = uiState.price.toDoubleOrNull() ?: 0.0
    val isLoanValid = !uiState.isLoan || (
        uiState.totalLoanAmount.toDoubleOrNull()?.let { it > 0.0 } == true &&
        loanPrice > 0.0
    )
    val isFormValid = isNameValid && isPriceValid && isLoanValid

    var showDatePicker by remember { mutableStateOf(false) }
    var showTrialDatePicker by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(uiState.isLoan || uiState.isCreditCard || uiState.isTrial) }

    // Reusable text field color helper
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
    )
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    // Auto-dismiss error after 3 seconds
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }

    if (uiState.isSaved) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.firstBillingDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onFirstBillingDateChange(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTrialDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.trialEndDate ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showTrialDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onTrialEndDateChange(it)
                    }
                    showTrialDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTrialDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.id == 0L) "New Subscription" else "Edit Subscription", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = {
            uiState.errorMessage?.let { error ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(error)
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ─── Quick Pick: Popular Services ───
            if (uiState.id == 0L) {
                Text("⭐ Popular Apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TemplateLibrary.templates) { template ->
                        TemplateItem(template = template) {
                            viewModel.onTemplateSelected(template)
                        }
                    }
                }
            }

            // ─── Scan Receipt ───
            if (uiState.id == 0L) {
                OutlinedButton(
                    onClick = onNavigateToReceiptScan,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📸 Scan a receipt")
                }
            }

            // ─── 📱 What are you paying for? ───
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("📱 What are you paying for?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Name") },
                        placeholder = { Text("e.g. Netflix, Spotify, YouTube...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors
                    )

                    // Category Picker — bigger chips for easy tapping
                    Text("📂 Pick a category", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CategoryDefaults.categories) { category ->
                            val isSelected = uiState.categoryId == category.id
                            val catColor = Color(android.graphics.Color.parseColor(category.colorHex))
                            val chipBg = if (isSelected) catColor.copy(alpha = 0.2f)
                                         else if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                            val chipBorder = if (isSelected) catColor
                                             else if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)
                            Surface(
                                onClick = { viewModel.onCategoryChange(category.id) },
                                shape = RoundedCornerShape(14.dp),
                                color = chipBg,
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, chipBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = IconUtils.getIconByName(category.iconName),
                                        contentDescription = category.name,
                                        tint = if (isSelected) catColor
                                               else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        category.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) catColor
                                                else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ─── 💰 How much does it cost? ───
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("💰 How much does it cost?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Price changes each time?", style = MaterialTheme.typography.bodyMedium)
                            Text("Turn on if you pay a different amount each bill", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = uiState.isVariablePrice,
                            onCheckedChange = { viewModel.onVariablePriceToggle(it) }
                        )
                    }

                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.onPriceChange(it) },
                        label = { Text(if (uiState.isVariablePrice) "About how much? ($currencySymbol)" else "Price ($currencySymbol)") },
                        placeholder = { Text(if (uiState.isVariablePrice) "Optional — enter a rough amount" else "e.g. 299") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.price.isNotEmpty() && uiState.price.toDoubleOrNull() == null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors
                    )
                }
            }

            // ─── 📅 When do you pay? ───
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("📅 When do you pay?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Text("How often?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BillingCycle.values().forEach { cycle ->
                            val label = when (cycle) {
                                BillingCycle.MONTHLY -> "Every month"
                                BillingCycle.YEARLY -> "Every year"
                                BillingCycle.CUSTOM -> "Custom"
                            }
                            FilterChip(
                                selected = uiState.billingCycle == cycle,
                                onClick = { viewModel.onBillingCycleChange(cycle) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    if (uiState.billingCycle == BillingCycle.CUSTOM) {
                        OutlinedTextField(
                            value = uiState.customCycleDays,
                            onValueChange = { viewModel.onCustomCycleDaysChange(it) },
                            label = { Text("Every how many days?") },
                            placeholder = { Text("e.g. 14 for every 2 weeks") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = uiState.customCycleDays.isNotEmpty() && (uiState.customCycleDays.toIntOrNull() == null || uiState.customCycleDays.toIntOrNull()!! <= 0),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors
                        )
                    }

                    HorizontalDivider(color = dividerColor)

                    // Date Picker Trigger
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("First payment date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(dateFormatter.format(Date(uiState.firstBillingDate)), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    HorizontalDivider(color = dividerColor)

                    Text("💳 How do you pay?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = uiState.paymentType == PaymentType.AUTO_PAY,
                            onClick = { viewModel.onPaymentTypeChange(PaymentType.AUTO_PAY) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Auto-pay")
                        }
                        SegmentedButton(
                            selected = uiState.paymentType == PaymentType.MANUAL,
                            onClick = { viewModel.onPaymentTypeChange(PaymentType.MANUAL) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("I pay myself")
                        }
                    }
                }
            }

            // ─── 🔔 Reminders ───
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("🔔 Remind me before", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(1, 2, 3, 7)) { days ->
                            val label = when (days) {
                                1 -> "1 day"
                                7 -> "1 week"
                                else -> "$days days"
                            }
                            FilterChip(
                                selected = uiState.reminderDaysBefore == days,
                                onClick = { viewModel.onReminderChange(days) },
                                label = { Text(label, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // ─── 📝 Notes ───
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("📝 Notes (optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = viewModel::onNotesChange,
                        placeholder = { Text("Add a reminder or detail about this subscription") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = textFieldColors
                    )
                }
            }

            // ─── ⚙️ Advanced Options (collapsed by default) ───
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Surface(
                        onClick = { showAdvanced = !showAdvanced },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("⚙️ More options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    "Loans, credit cards, free trials",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            Icon(
                                imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showAdvanced) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showAdvanced,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.padding(top = 16.dp)) {

                            // ── 🏦 Loan / Installment ──
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("🏦 Loan / Installment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Paying something off over time?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                    Switch(
                                        checked = uiState.isLoan,
                                        onCheckedChange = { viewModel.onLoanToggle(it) }
                                    )
                                }

                                if (uiState.isLoan) {
                                    HorizontalDivider(color = dividerColor)

                                    // How much did you borrow + Interest Rate
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.totalLoanAmount,
                                            onValueChange = { viewModel.onTotalLoanAmountChange(it) },
                                            label = { Text("Total borrowed") },
                                            placeholder = { Text("e.g. 30000") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = uiState.totalLoanAmount.isNotEmpty() && (uiState.totalLoanAmount.toDoubleOrNull() == null || uiState.totalLoanAmount.toDoubleOrNull()!! <= 0.0),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = uiState.interestRate,
                                            onValueChange = { viewModel.onInterestRateChange(it) },
                                            label = { Text("Interest (%)") },
                                            placeholder = { Text("e.g. 3.5") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = uiState.interestRate.isNotEmpty() && (uiState.interestRate.toDoubleOrNull() == null || uiState.interestRate.toDoubleOrNull()!! < 0.0),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )
                                    }

                                    // Total terms + Already paid
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.totalInstallments,
                                            onValueChange = { viewModel.onTotalInstallmentsChange(it) },
                                            label = { Text("Total months") },
                                            placeholder = { Text("e.g. 48") },
                                            supportingText = {
                                                val principal = uiState.totalLoanAmount.toDoubleOrNull()
                                                val price = uiState.price.toDoubleOrNull()
                                                val rate = uiState.interestRate.toDoubleOrNull()
                                                val estimated = if (principal != null && price != null && price > 0.0) {
                                                    com.mate.subsmate.ui.utils.LoanUtils.estimateTotalInstallments(principal, price, rate)
                                                } else null
                                                if (uiState.totalInstallments.isBlank() && estimated != null) {
                                                    Text("≈ $estimated months (auto)", style = MaterialTheme.typography.labelSmall)
                                                } else {
                                                    Text("From your contract", style = MaterialTheme.typography.labelSmall)
                                                }
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = uiState.totalInstallments.isNotEmpty() && (uiState.totalInstallments.toIntOrNull() == null || uiState.totalInstallments.toIntOrNull()!! <= 0),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = uiState.currentInstallment,
                                            onValueChange = { viewModel.onCurrentInstallmentChange(it) },
                                            label = { Text("Already paid") },
                                            placeholder = { Text("e.g. 12") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = uiState.currentInstallment.isNotEmpty() && (
                                                uiState.currentInstallment.toIntOrNull() == null ||
                                                uiState.currentInstallment.toIntOrNull()!! < 0
                                            ),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )
                                    }

                                    Text(
                                        "💡 Tip: Check your loan contract for exact numbers. The auto-estimate may be a little off.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )

                                    OutlinedTextField(
                                        value = uiState.extraPrincipalPaid,
                                        onValueChange = { viewModel.onExtraPrincipalPaidChange(it) },
                                        label = { Text("Extra already paid off") },
                                        placeholder = { Text("Optional — any lump sums you've paid") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        isError = uiState.extraPrincipalPaid.isNotEmpty() && (uiState.extraPrincipalPaid.toDoubleOrNull() == null || uiState.extraPrincipalPaid.toDoubleOrNull()!! < 0.0),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = textFieldColors
                                    )

                                    // Extra Payment Calculator
                                    HorizontalDivider(color = dividerColor)
                                    Text("🧮 What if I pay extra each month?", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

                                    OutlinedTextField(
                                        value = uiState.extraPerMonth,
                                        onValueChange = { viewModel.onExtraPerMonthChange(it) },
                                        label = { Text("Extra per month ($currencySymbol)") },
                                        placeholder = { Text("e.g. 500") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        isError = uiState.extraPerMonth.isNotEmpty() && uiState.extraPerMonth.toDoubleOrNull() == null,
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = textFieldColors
                                    )

                                    uiState.extraPerMonth.toDoubleOrNull()?.let { extra ->
                                        if (extra > 0.0) {
                                            val principal = uiState.totalLoanAmount.toDoubleOrNull() ?: return@let
                                            val price = uiState.price.toDoubleOrNull() ?: return@let
                                            val rate = uiState.interestRate.toDoubleOrNull()
                                            val total = uiState.totalInstallments.toIntOrNull()
                                                ?: com.mate.subsmate.ui.utils.LoanUtils.estimateTotalInstallments(principal, price, rate)
                                                ?: return@let
                                            val remaining = total - (uiState.currentInstallment.toIntOrNull() ?: 0)
                                            if (remaining <= 0) return@let

                                            val impact = com.mate.subsmate.ui.utils.LoanUtils.calculateExtraPaymentImpact(
                                                remainingBalance = principal,
                                                monthlyPayment = price,
                                                annualInterestRate = rate,
                                                extraPerMonth = extra,
                                                remainingMonths = remaining
                                            )
                                            Surface(
                                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text("🎉 You'd save $currencySymbol${String.format("%,.0f", impact.interestSaved)} in interest!", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                                    Text("Done ${impact.monthsSaved} months sooner", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text("Finished in ${impact.newPayoffMonths} months instead", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = dividerColor)

                            // ── 💳 Credit Card ──
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("💳 Credit Card", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("Track your card balance and payments", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                    Switch(
                                        checked = uiState.isCreditCard,
                                        onCheckedChange = { viewModel.onCreditCardToggle(it) }
                                    )
                                }

                                if (uiState.isCreditCard) {
                                    HorizontalDivider(color = dividerColor)

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.statementDayOfMonth,
                                            onValueChange = { viewModel.onStatementDayOfMonthChange(it) },
                                            label = { Text("Statement day") },
                                            placeholder = { Text("e.g. 15") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = uiState.statementDayOfMonth.isNotEmpty() && (uiState.statementDayOfMonth.toIntOrNull() == null || uiState.statementDayOfMonth.toIntOrNull() !in 1..31),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = uiState.dueDayOfMonth,
                                            onValueChange = { viewModel.onDueDayOfMonthChange(it) },
                                            label = { Text("Due day") },
                                            placeholder = { Text("e.g. 5") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = uiState.dueDayOfMonth.isNotEmpty() && (uiState.dueDayOfMonth.toIntOrNull() == null || uiState.dueDayOfMonth.toIntOrNull() !in 1..31),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.currentStatementBalance,
                                            onValueChange = { viewModel.onCurrentStatementBalanceChange(it) },
                                            label = { Text("Current balance") },
                                            placeholder = { Text("Optional") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = uiState.currentStatementBalance.isNotEmpty() && uiState.currentStatementBalance.toDoubleOrNull() == null,
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = uiState.minimumPaymentDue,
                                            onValueChange = { viewModel.onMinimumPaymentDueChange(it) },
                                            label = { Text("Min payment") },
                                            placeholder = { Text("Optional") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = uiState.minimumPaymentDue.isNotEmpty() && uiState.minimumPaymentDue.toDoubleOrNull() == null,
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.creditLimit,
                                            onValueChange = { viewModel.onCreditLimitChange(it) },
                                            label = { Text("Credit limit") },
                                            placeholder = { Text("Optional") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = uiState.creditLimit.isNotEmpty() && uiState.creditLimit.toDoubleOrNull() == null,
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )

                                        OutlinedTextField(
                                            value = uiState.cardApr,
                                            onValueChange = { viewModel.onCardAprChange(it) },
                                            label = { Text("Interest (%)") },
                                            placeholder = { Text("Optional") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            isError = uiState.cardApr.isNotEmpty() && uiState.cardApr.toDoubleOrNull() == null,
                                            modifier = Modifier.weight(1f),
                                            singleLine = true,
                                            colors = textFieldColors
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = dividerColor)

                            // ── 🎁 Free Trial ──
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("🎁 Free Trial", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text("So you don't forget when it ends!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                    Switch(
                                        checked = uiState.isTrial,
                                        onCheckedChange = { viewModel.onTrialToggle(it) }
                                    )
                                }

                                if (uiState.isTrial) {
                                    OutlinedCard(
                                        onClick = { showTrialDatePicker = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent),
                                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("Trial ends on", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                Text(
                                                    uiState.trialEndDate?.let { dateFormatter.format(Date(it)) } ?: "Pick a date",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ─── Save Button ───
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveSubscription(currency) },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = if (uiState.id == 0L) "✅ Save" else "✅ Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TemplateItem(template: ServiceTemplate, onClick: () -> Unit) {
    val logo = VendorUtils.getLogo(template.name)
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.8f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp).widthIn(min = 64.dp, max = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(android.graphics.Color.parseColor(template.colorHex)).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (logo != null) {
                    AsyncImage(
                        model = logo,
                        contentDescription = template.name,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = IconUtils.getIconByName(template.iconName),
                        contentDescription = template.name,
                        tint = Color(android.graphics.Color.parseColor(template.colorHex)),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                template.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
