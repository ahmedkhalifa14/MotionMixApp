package com.ahmedkhalifa.motionmix.ui.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.ahmedkhalifa.motionmix.ui.screens.ScreenContent
import com.ahmedkhalifa.motionmix.ui.screens.auth.login.LoginScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.select_country.SelectCountryScreenContent
import com.ahmedkhalifa.motionmix.ui.screens.auth.signup.SignupScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.tabs.CreatePasswordScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.tabs.EmailPhoneTab
import com.ahmedkhalifa.motionmix.ui.screens.auth.tabs.EmailTabScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.tabs.EnterPasswordScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.tabs.OtpScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.tabs.PhoneTabScreen
import com.ahmedkhalifa.motionmix.ui.screens.auth.userProfile.UserProfileFormScreen


fun NavGraphBuilder.authNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.AUTHENTICATION,
        startDestination = AuthScreen.SignUp.route
    ) {
        composable(route = AuthScreen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = AuthScreen.SignUp.route) {
            SignupScreen(navController = navController)
        }
        composable(route = AuthScreen.Forgot.route) {
            ScreenContent(name = AuthScreen.Forgot.route) {}
        }
        composable(
            route = AuthScreen.EmailPhoneTab.route,
            arguments = listOf(
                navArgument("initialMode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val initialMode = backStackEntry.arguments?.getString("initialMode") ?: "Login"
            EmailPhoneTab(navController, initialMode)
        }

        composable(
            route = AuthScreen.Email.route,
            arguments = listOf(
                navArgument("mode") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "Login"
            EmailTabScreen(navController, mode)
        }
        composable(route = AuthScreen.Phone.route) {
            PhoneTabScreen(navController)
        }

        composable(
            route = AuthScreen.EnterPassword.route,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                }
            )
        ) {
            EnterPasswordScreen(navController, it)
        }
    }
    composable(
        route = AuthScreen.Password.route,
        arguments = listOf(
            navArgument("email") {
                type = NavType.StringType
            }
        )
    ) {
        CreatePasswordScreen(navController, it)
    }
    composable(route = AuthScreen.SelectCountryCode.route) {
        SelectCountryScreenContent(navController)
    }
    composable(
        route = AuthScreen.Otp.route,
        arguments = listOf(
            navArgument("verificationId") {
                type = NavType.StringType
            }
        )
    ) {
        OtpScreen(navController, it)
    }
    composable (route= AuthScreen.UserProfileForm.route){
        UserProfileFormScreen(navController)
    }
}


sealed class AuthScreen(val route: String) {
    object Login : AuthScreen(route = "LOGIN")
    object SignUp : AuthScreen(route = "SIGN_UP")
    object Forgot : AuthScreen(route = "FORGOT")
    object EmailPhoneTab : AuthScreen(route = "EMAIL_PHONE_TAB/{initialMode}")
    object Phone : AuthScreen(route = "PHONE")
    object Email : AuthScreen(route = "EMAIL/{mode}")
    object Password : AuthScreen(route = "PASSWORD/{email}")
    object EnterPassword : AuthScreen(route = "ENTER_PASSWORD/{email}")
    object SelectCountryCode : AuthScreen(route = "SELECT_COUNTRY_CODE")
    object Otp : AuthScreen(route = "OTP/{verificationId}")
    object UserProfileForm: AuthScreen (route="USER_PROFILE_SCREEN")
}