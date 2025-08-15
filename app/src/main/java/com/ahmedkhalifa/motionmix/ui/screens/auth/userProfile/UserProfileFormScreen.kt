package com.ahmedkhalifa.motionmix.ui.screens.auth.userProfile

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.EventObserver
import com.ahmedkhalifa.motionmix.common.utils.LocationResult
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.services.LocationService
import com.ahmedkhalifa.motionmix.ui.composable.CustomTextField
import com.ahmedkhalifa.motionmix.ui.composable.LocationTextField
import com.ahmedkhalifa.motionmix.ui.composable.ProfilePictureSection
import com.ahmedkhalifa.motionmix.ui.screens.profile.UserProfileViewModel
import com.ahmedkhalifa.motionmix.ui.theme.AppMainColor
import kotlinx.coroutines.launch

@Composable
fun UserProfileFormScreen(
    navController: NavController,
    userProfileViewModel: UserProfileViewModel = hiltViewModel()
) {
    val saveUserProfileState = userProfileViewModel.saveUserProfileState.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val saveUserProfileEventObserver = EventObserver<Unit>(
        onLoading = {
            isLoading = true
        },
        onSuccess = {
            isLoading = false
            Toast.makeText(
                context,
                context.getString(R.string.user_profile_data_saved_successfully), Toast.LENGTH_SHORT
            ).show()
            // navController.navigate(Graph.HOME) {
            //     popUpTo(AuthScreen.UserProfileForm.route) { inclusive = true }
            // }
        },
        onError = { errorMessage ->
            isLoading = false
            Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
        }
    )

    LaunchedEffect(saveUserProfileState.value) {
        saveUserProfileEventObserver.emit(saveUserProfileState.value)
    }
    UserProfileFormScreenContent(
        isLoading = isLoading,
        onNavigateBack = { navController.popBackStack() },
        onSaveData = { user, imgUrl ->
            userProfileViewModel.saveUserProfileData(user, imgUrl, context)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileFormScreenContent(
    isLoading: Boolean,
    onNavigateBack: () -> Unit = {},
    onSaveData: (User, Uri?) -> Unit
) {
    var userData by remember { mutableStateOf(User()) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val locationService = remember { LocationService(context) }

    // Validate all required fields
    val areFieldsValid by remember(userData) {
        mutableStateOf(
            userData.firstName.isNotBlank() &&
                    userData.lastName.isNotBlank() &&
                    userData.email.isNotBlank() &&
                    userData.phoneNumber.isNotBlank() &&
                    userData.location.isNotBlank()
        )
    }

    // KeyboardController
    val firstNameFocusRequester = remember { FocusRequester() }
    val lastnameFocusRequester = remember { FocusRequester() }
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val locationFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasPermission = permissions.values.any { it }
        if (hasPermission) {
            isLoadingLocation = true
            locationError = null
            coroutineScope.launch {
                when (val result = locationService.getCurrentLocationAddress()) {
                    is LocationResult.Success -> {
                        userData = userData.copy(location = result.address)
                        locationError = null
                    }
                    is LocationResult.Error -> {
                        locationError = result.message
                    }
                }
                isLoadingLocation = false
            }
        } else {
            locationError = "Location permission denied"
            isLoadingLocation = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.complete_your_profile),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val userWithImage = userData.copy(
                            profilePictureLink = profilePictureUri?.toString() ?: ""
                        )
                        onSaveData(userWithImage, profilePictureUri)
                    },
                    enabled = areFieldsValid && !isLoading
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (areFieldsValid) AppMainColor else Color.Gray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ProfilePictureSection(
                profilePictureUri = profilePictureUri,
                onProfilePictureChange = { uri ->
                    profilePictureUri = uri
                    userData = userData.copy(profilePictureLink = uri?.toString() ?: "")
                    Log.d("imageUrl", profilePictureUri.toString() + "  s")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // First Name
                CustomTextField(
                    value = userData.firstName,
                    onValueChange = { userData = userData.copy(firstName = it) },
                    label = stringResource(R.string.first_name),
                    placeholder = stringResource(R.string.enter_your_first_name),
                    leadingIcon = Icons.Default.Person,
                    focusRequester = firstNameFocusRequester,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            lastnameFocusRequester.requestFocus()
                        }
                    )
                )

                // Last Name
                CustomTextField(
                    value = userData.lastName,
                    onValueChange = { userData = userData.copy(lastName = it) },
                    label = stringResource(R.string.last_name),
                    placeholder = stringResource(R.string.enter_your_last_name),
                    leadingIcon = Icons.Default.Person,
                    focusRequester = lastnameFocusRequester,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            emailFocusRequester.requestFocus()
                        }
                    )
                )

                // Email
                CustomTextField(
                    value = userData.email,
                    onValueChange = { userData = userData.copy(email = it) },
                    label = stringResource(R.string.email),
                    placeholder = stringResource(R.string.enter_your_email_address),
                    leadingIcon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    focusRequester = emailFocusRequester,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            phoneNumberFocusRequester.requestFocus()
                        }
                    )
                )

                // Phone Number
                CustomTextField(
                    value = userData.phoneNumber,
                    onValueChange = { input ->
                        if (input.matches(Regex("[0-9+()-]*"))) {
                            userData = userData.copy(phoneNumber = input)
                        }
                    },
                    label = stringResource(R.string.phone_number),
                    placeholder = stringResource(R.string.enter_your_phone_number),
                    leadingIcon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone,
                    focusRequester = phoneNumberFocusRequester,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            locationFocusRequester.requestFocus()
                        }
                    )
                )

                // Location with Auto-Detection
                LocationTextField(
                    value = userData.location,
                    onValueChange = { userData = userData.copy(location = it) },
                    isLoading = isLoadingLocation,
                    error = locationError,
                    onGetCurrentLocation = {
                        locationError = null
                        if (locationService.hasLocationPermission()) {
                            isLoadingLocation = true
                            coroutineScope.launch {
                                when (val result = locationService.getCurrentLocationAddress()) {
                                    is LocationResult.Success -> {
                                        userData = userData.copy(location = result.address)
                                        locationError = null
                                    }
                                    is LocationResult.Error -> {
                                        locationError = result.message
                                    }
                                }
                                isLoadingLocation = false
                            }
                        } else {
                            locationPermissionLauncher.launch(locationService.getRequiredPermissions())
                        }
                    },
                    focusRequester = locationFocusRequester,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    )
                )
                // Error message for location
                if (locationError != null) {
                    Text(
                        text = locationError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button
                Button(
                    onClick = {
                        val userWithImage = userData.copy(
                            profilePictureLink = profilePictureUri?.toString() ?: ""
                        )
                        onSaveData(userWithImage, profilePictureUri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppMainColor,
                        disabledContainerColor = Color.Gray
                    ),
                    enabled = areFieldsValid && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(
                            text = stringResource(R.string.complete_profile),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (areFieldsValid) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Skip Button
                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Text(
                        text = stringResource(R.string.skip_for_now),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserDataInputScreenPreview() {
    MaterialTheme {
        UserProfileFormScreen(rememberNavController())
    }
}