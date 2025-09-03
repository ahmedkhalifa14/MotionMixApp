package com.ahmedkhalifa.motionmix.ui.graphs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.wear.compose.material3.MaterialTheme
import com.ahmedkhalifa.motionmix.ui.main_activity.ScreenContent
import com.ahmedkhalifa.motionmix.ui.screens.home.HomeScreen
import com.ahmedkhalifa.motionmix.ui.screens.inbox.ChatScreen
import com.ahmedkhalifa.motionmix.ui.screens.inbox.ConversationsListScreen
import com.ahmedkhalifa.motionmix.ui.theme.Montserrat

fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    composable(route = Graph.HOME) {
        HomeScreen(navController = navController)
    }

}

sealed class ChatScreen(val route: String) {
    object Conversations : ChatScreen(route = "CONVERSATIONS")
    object Chat : ChatScreen(route = "CHAT/{conversationId}") {
        fun createRoute(conversationId: String) = "CHAT/$conversationId"
    }
    object NewChat : ChatScreen(route = "NEW_CHAT")
}

fun NavGraphBuilder.chatNavGraph(navController: NavHostController) {
    navigation(
        route = Graph.CHAT,
        startDestination = ChatScreen.Conversations.route
    ) {
        composable(route = ChatScreen.Conversations.route) {
            ConversationsListScreen(
                onConversationClick = { conversationId ->
                    navController.navigate(ChatScreen.Chat.createRoute(conversationId))
                },
                onNewChatClick = {
                    navController.navigate(ChatScreen.NewChat.route)
                }
            )
        }
        composable(
            route = ChatScreen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = ChatScreen.NewChat.route) {
            NewChatScreen(
                onChatCreated = { newConversationId ->
                    navController.navigate(ChatScreen.Chat.createRoute(newConversationId)) {
                        popUpTo(ChatScreen.Conversations.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun NewChatScreen(
    onChatCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Start a New Chat",
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Simulate creating a new conversation
                val newConversationId = "new_conversation_${System.currentTimeMillis()}"
                onChatCreated(newConversationId)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Create Chat",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "Back",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
