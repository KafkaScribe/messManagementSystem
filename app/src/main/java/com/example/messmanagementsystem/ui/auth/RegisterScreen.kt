package com.example.messmanagementsystem.ui.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            // Header
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Join your mess today",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Full Name
            MessOutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 100) name = it },
                label = "Full Name",
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = (hasAttemptedSubmit && name.isBlank()) || nameError != null,
                supportingText = {
                    when {
                        hasAttemptedSubmit && name.isBlank() -> Text("Name is required", color = MaterialTheme.colorScheme.error)
                        nameError != null -> Text(nameError, color = MaterialTheme.colorScheme.error)
                        name.isNotEmpty() -> Text("${name.length}/100", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Email
            MessOutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = (hasAttemptedSubmit && email.isBlank()) || emailError != null,
                supportingText = {
                    when {
                        hasAttemptedSubmit && email.isBlank() -> Text("Email is required", color = MaterialTheme.colorScheme.error)
                        emailError != null -> Text(emailError, color = MaterialTheme.colorScheme.error)
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
                        contentPadding = PaddingValues(horizontal = 12.dp)
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
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = (hasAttemptedSubmit && phoneNumber.isBlank()) || phoneError != null,
                    supportingText = {
                        when {
                            hasAttemptedSubmit && phoneNumber.isBlank() -> Text("Phone number is required", color = MaterialTheme.colorScheme.error)
                            phoneError != null -> Text(phoneError, color = MaterialTheme.colorScheme.error)
                            phoneNumber.isNotEmpty() -> Text("${phoneNumber.length}/11 digits", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide" else "Show"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = hasAttemptedSubmit && !isPasswordValid && password.isNotEmpty(),
                supportingText = if (hasAttemptedSubmit && password.isBlank()) {
                    { Text("Password is required", color = MaterialTheme.colorScheme.error) }
                } else null
            )

            // Password Strength Indicator
            if (password.isNotEmpty()) {
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

            Spacer(modifier = Modifier.height(8.dp))

            if (authState is AuthState.Error) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
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

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onNavigateLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Login",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
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
        0, 1 -> Color(0xFFD32F2F)
        2 -> Color(0xFFF57C00)
        3 -> Color(0xFFFBC02D)
        4 -> Color(0xFF388E3C)
        5 -> Color(0xFF1B5E20)
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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                strengthLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(MaterialTheme.shapes.small),
            color = animatedColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
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
        targetValue = if (isMet) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = color
        )
    }
}
