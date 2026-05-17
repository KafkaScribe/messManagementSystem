package com.example.messmanagementsystem.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.messmanagementsystem.ui.components.MessOutlinedTextField
import com.example.messmanagementsystem.ui.components.MessPrimaryButton
import com.example.messmanagementsystem.ui.theme.*

data class CountryCode(val country: String, val code: String, val flag: String)

val countryCodes = listOf(
    CountryCode("Bangladesh", "+880", "🇧🇩"),
    CountryCode("India", "+91", "🇮🇳"),
    CountryCode("Pakistan", "+92", "🇵🇰"),
    CountryCode("Nepal", "+977", "🇳🇵"),
    CountryCode("Sri Lanka", "+94", "🇱🇰"),
    CountryCode("Myanmar", "+95", "🇲🇲"),
    CountryCode("USA", "+1", "🇺🇸"),
    CountryCode("UK", "+44", "🇬🇧"),
    CountryCode("Malaysia", "+60", "🇲🇾"),
    CountryCode("Saudi Arabia", "+966", "🇸🇦"),
    CountryCode("UAE", "+971", "🇦🇪"),
    CountryCode("Qatar", "+974", "🇶🇦"),
    CountryCode("Kuwait", "+965", "🇰🇼"),
    CountryCode("Oman", "+968", "🇴🇲"),
    CountryCode("Singapore", "+65", "🇸🇬")
)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf(countryCodes[0]) }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    // --- Validation ---
    val nameError = when {
        name.isEmpty() -> null // Don't show error on empty until submit
        name.length > 100 -> "Name must be 100 characters or less"
        !name.matches(Regex("^[a-zA-Z\\s.'-]+$")) -> "Name can only contain letters, spaces, dots, hyphens"
        name.trim().length < 2 -> "Name must be at least 2 characters"
        else -> null
    }
    val isNameValid = name.isNotBlank() && nameError == null

    val emailError = when {
        email.isEmpty() -> null
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Enter a valid email address"
        else -> null
    }
    val isEmailValid = email.isNotBlank() && emailError == null

    val phoneError = when {
        phoneNumber.isEmpty() -> null
        !phoneNumber.matches(Regex("^[0-9]+$")) -> "Phone number can only contain digits"
        phoneNumber.length != 11 -> "Phone number must be exactly 11 digits"
        else -> null
    }
    val isPhoneValid = phoneNumber.isNotBlank() && phoneError == null

    // Password strength checks
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val strengthChecks = listOf(hasMinLength, hasUppercase, hasLowercase, hasDigit, hasSpecial)
    val strengthScore = strengthChecks.count { it }
    val isPasswordValid = strengthScore == 5

    val isFormValid = isNameValid && isEmailValid && isPhoneValid && isPasswordValid

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Gradient header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .padding(top = 56.dp, bottom = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(34.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "Create Account",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Join your mess today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }

            // ── Form card ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 20.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Full Name
                    MessOutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 100) name = it },
                        label = "Full Name",
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        isError = (hasAttemptedSubmit && name.isBlank()) || nameError != null,
                        supportingText = {
                            when {
                                hasAttemptedSubmit && name.isBlank() -> Text("Name is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                nameError != null -> Text(nameError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                name.isNotEmpty() -> Text("${name.length}/100", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Email
                    MessOutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = "Email Address",
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        isError = (hasAttemptedSubmit && email.isBlank()) || emailError != null,
                        supportingText = {
                            when {
                                hasAttemptedSubmit && email.isBlank() -> Text("Email is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                emailError != null -> Text(emailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Phone Number with Country Code
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Country code dropdown
                        Box(modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedButton(
                                onClick = { countryDropdownExpanded = true },
                                modifier = Modifier
                                    .height(56.dp)
                                    .widthIn(min = 110.dp),
                                shape = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(
                                    "${selectedCountryCode.flag} ${selectedCountryCode.code}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select country code",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = countryDropdownExpanded,
                                onDismissRequest = { countryDropdownExpanded = false }
                            ) {
                                countryCodes.forEach { cc ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${cc.flag} ${cc.country} (${cc.code})",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            selectedCountryCode = cc
                                            countryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        MessOutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }
                                if (digitsOnly.length <= 11) phoneNumber = digitsOnly
                            },
                            label = "Phone Number",
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = (hasAttemptedSubmit && phoneNumber.isBlank()) || phoneError != null,
                            supportingText = {
                                when {
                                    hasAttemptedSubmit && phoneNumber.isBlank() -> Text("Phone number is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    phoneError != null -> Text(phoneError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    phoneNumber.isNotEmpty() -> Text("${phoneNumber.length}/11 digits", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))

                    // Password
                    MessOutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide" else "Show",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = hasAttemptedSubmit && !isPasswordValid && password.isNotEmpty(),
                        supportingText = if (hasAttemptedSubmit && password.isBlank()) {
                            { Text("Password is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        } else null
                    )

                    // Password Strength Indicator
                    AnimatedVisibility(visible = password.isNotEmpty()) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            PasswordStrengthIndicator(strengthScore = strengthScore)
                            Spacer(modifier = Modifier.height(8.dp))
                            PasswordRequirementsList(
                                hasMinLength = hasMinLength,
                                hasUppercase = hasUppercase,
                                hasLowercase = hasLowercase,
                                hasDigit = hasDigit,
                                hasSpecial = hasSpecial
                            )
                        }
                    }

                    // Error
                    AnimatedVisibility(
                        visible = authState is AuthState.Error,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        if (authState is AuthState.Error) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    (authState as AuthState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    MessPrimaryButton(
                        text = "Create Account",
                        onClick = {
                            hasAttemptedSubmit = true
                            if (isFormValid) {
                                val fullPhone = "${selectedCountryCode.code}$phoneNumber"
                                viewModel.register(name.trim(), email.trim(), password, fullPhone)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isLoading = authState == AuthState.Loading,
                        enabled = !hasAttemptedSubmit || isFormValid
                    )
                }
            }

            // ── Bottom link ──
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onNavigateLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(strengthScore: Int) {
    val strengthLabel = when (strengthScore) {
        0, 1 -> "Very Weak"
        2 -> "Weak"
        3 -> "Fair"
        4 -> "Strong"
        5 -> "Very Strong"
        else -> ""
    }
    val strengthColor = when (strengthScore) {
        0, 1 -> Color(0xFFDC2626)
        2 -> Color(0xFFEA580C)
        3 -> Color(0xFFCA8A04)
        4 -> Color(0xFF16A34A)
        5 -> Color(0xFF059669)
        else -> Color.Gray
    }

    val animatedProgress by animateFloatAsState(
        targetValue = strengthScore / 5f,
        label = "strength_progress"
    )
    val animatedColor by animateColorAsState(
        targetValue = strengthColor,
        label = "strength_color"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Password Strength",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                strengthLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = animatedColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(MaterialTheme.shapes.extraSmall),
            color = animatedColor,
            trackColor = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun PasswordRequirementsList(
    hasMinLength: Boolean,
    hasUppercase: Boolean,
    hasLowercase: Boolean,
    hasDigit: Boolean,
    hasSpecial: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        PasswordRequirementItem("At least 8 characters", hasMinLength)
        PasswordRequirementItem("One uppercase letter (A-Z)", hasUppercase)
        PasswordRequirementItem("One lowercase letter (a-z)", hasLowercase)
        PasswordRequirementItem("One digit (0-9)", hasDigit)
        PasswordRequirementItem("One special character (!@#\$%^&*)", hasSpecial)
    }
}

@Composable
private fun PasswordRequirementItem(text: String, isMet: Boolean) {
    val color by animateColorAsState(
        targetValue = if (isMet) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        label = "req_color"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 1.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = color
        )
    }
}
