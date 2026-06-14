package com.mate.subsmate.ui.add_subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.PaymentType
import com.mate.subsmate.domain.model.ServiceTemplate
import com.mate.subsmate.domain.model.TemplateLibrary
import com.mate.subsmate.ui.theme.GlassyCard
import com.mate.subsmate.ui.utils.VendorUtils
import com.mate.subsmate.ui.utils.IconUtils
import com.mate.subsmate.ui.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: AddSubscriptionViewModel,
    currency: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getSymbol(currency)
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isDark = isSystemInDarkTheme()

    val isNameValid = uiState.name.isNotBlank()
    val isPriceValid = uiState.isVariablePrice || uiState.price.toDoubleOrNull() != null
    val isLoanValid = !uiState.isLoan || (
        uiState.totalInstallments.toIntOrNull()?.let { total ->
            val current = uiState.currentInstallment.toIntOrNull() ?: 0
            val initialPrincipal = uiState.totalLoanAmount.toDoubleOrNull() ?: 0.0
            val rate = if (uiState.interestRate.isEmpty()) 0.0 else uiState.interestRate.toDoubleOrNull()
            val extra = if (uiState.extraPrincipalPaid.isEmpty()) 0.0 else uiState.extraPrincipalPaid.toDoubleOrNull()
            
            total > 0 && current >= 0 && current <= total && initialPrincipal > 0.0 && rate != null && rate >= 0.0 && extra != null && extra >= 0.0
        } == true
    )
    val isFormValid = isNameValid && isPriceValid && isLoanValid

    var showDatePicker by remember { mutableStateOf(false) }
    var showTrialDatePicker by remember { mutableStateOf(false) }

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
            // Service Presets
            if (uiState.id == 0L) {
                Text("Popular Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

            // Main Details
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        label = { Text("Service Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Variable Amount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("For bills with changing amounts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = uiState.isVariablePrice,
                            onCheckedChange = { viewModel.onVariablePriceToggle(it) }
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.onPriceChange(it) },
                        label = { Text(if (uiState.isVariablePrice) "Estimated Price ($currencySymbol) (Optional)" else "Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.price.isNotEmpty() && uiState.price.toDoubleOrNull() == null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                        )
                    )
                }
            }

            // Billing Config
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Billing details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BillingCycle.values().forEach { cycle ->
                            FilterChip(
                                selected = uiState.billingCycle == cycle,
                                onClick = { viewModel.onBillingCycleChange(cycle) },
                                label = { Text(cycle.name.lowercase().replaceFirstChar { it.uppercase() }) },
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
                            label = { Text("Cycle Days") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = uiState.customCycleDays.isNotEmpty() && (uiState.customCycleDays.toIntOrNull() == null || uiState.customCycleDays.toIntOrNull()!! <= 0),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                            )
                        )
                    }

                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

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
                                Text("First Billing Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(dateFormatter.format(Date(uiState.firstBillingDate)), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }

                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

                    Text("Payment Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = uiState.paymentType == PaymentType.AUTO_PAY,
                            onClick = { viewModel.onPaymentTypeChange(PaymentType.AUTO_PAY) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("Auto-Pay")
                        }
                        SegmentedButton(
                            selected = uiState.paymentType == PaymentType.MANUAL,
                            onClick = { viewModel.onPaymentTypeChange(PaymentType.MANUAL) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("Manual")
                        }
                    }
                }
            }

            // Installment / Loan tracking
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Loan / Leasing Payment", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Track finite installments and amortization", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = uiState.isLoan,
                            onCheckedChange = { viewModel.onLoanToggle(it) }
                        )
                    }

                    if (uiState.isLoan) {
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.totalInstallments,
                                onValueChange = { viewModel.onTotalInstallmentsChange(it) },
                                label = { Text("Total Terms (Months)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = uiState.totalInstallments.isNotEmpty() && (uiState.totalInstallments.toIntOrNull() == null || uiState.totalInstallments.toIntOrNull()!! <= 0),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                                )
                            )

                            OutlinedTextField(
                                value = uiState.currentInstallment,
                                onValueChange = { viewModel.onCurrentInstallmentChange(it) },
                                label = { Text("Already Paid Terms") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = uiState.currentInstallment.isNotEmpty() && (
                                    uiState.currentInstallment.toIntOrNull() == null || 
                                    uiState.currentInstallment.toIntOrNull()!! < 0 || 
                                    (uiState.totalInstallments.toIntOrNull()?.let { uiState.currentInstallment.toIntOrNull()!! > it } == true)
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.totalLoanAmount,
                                onValueChange = { viewModel.onTotalLoanAmountChange(it) },
                                label = { Text("Initial Principal") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = uiState.totalLoanAmount.isNotEmpty() && (uiState.totalLoanAmount.toDoubleOrNull() == null || uiState.totalLoanAmount.toDoubleOrNull()!! <= 0.0),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                                )
                            )

                            OutlinedTextField(
                                value = uiState.interestRate,
                                onValueChange = { viewModel.onInterestRateChange(it) },
                                label = { Text("Interest Rate (% APR)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = uiState.interestRate.isNotEmpty() && (uiState.interestRate.toDoubleOrNull() == null || uiState.interestRate.toDoubleOrNull()!! < 0.0),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                                )
                            )
                        }

                        OutlinedTextField(
                            value = uiState.extraPrincipalPaid,
                            onValueChange = { viewModel.onExtraPrincipalPaidChange(it) },
                            label = { Text("Extra Principal Paid (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = uiState.extraPrincipalPaid.isNotEmpty() && (uiState.extraPrincipalPaid.toDoubleOrNull() == null || uiState.extraPrincipalPaid.toDoubleOrNull()!! < 0.0),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }

            // Trial Section
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Trial Period", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Calculate from trial end date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
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
                                    Text("Trial End Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        uiState.trialEndDate?.let { dateFormatter.format(Date(it)) } ?: "Select date",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reminders
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Remind before", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                    text = if (uiState.id == 0L) "Add Subscription" else "Update Subscription",
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
            modifier = Modifier.padding(12.dp).width(70.dp)
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
