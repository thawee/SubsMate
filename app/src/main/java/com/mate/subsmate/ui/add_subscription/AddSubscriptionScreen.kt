package com.mate.subsmate.ui.add_subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionScreen(
    viewModel: AddSubscriptionViewModel,
    currency: String,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = if (currency == "THB") "฿" else "$"

    if (uiState.isSaved) {
        LaunchedEffect(Unit) {
            onNavigateBack()
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
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    OutlinedTextField(
                        value = uiState.price,
                        onValueChange = { viewModel.onPriceChange(it) },
                        label = { Text("Price ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
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

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

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

            // Reminders
            GlassyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Remind me", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 7).forEach { days ->
                            FilterChip(
                                selected = uiState.reminderDaysBefore == days,
                                onClick = { viewModel.onReminderChange(days) },
                                label = { Text("$days d before") },
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
                onClick = { viewModel.saveSubscription() },
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
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
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
                    Text(
                        text = template.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(template.colorHex))
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
