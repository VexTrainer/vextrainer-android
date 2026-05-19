package com.vextrainer.android.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vextrainer.android.presentation.MainViewModel
import com.vextrainer.android.presentation.ui.auth.login.LoginScreen
import com.vextrainer.android.presentation.ui.auth.register.RegisterScreen
import com.vextrainer.android.presentation.ui.info.AboutScreen
import com.vextrainer.android.presentation.ui.info.ContactUsScreen
import com.vextrainer.android.presentation.ui.info.DeleteAccountScreen
import com.vextrainer.android.presentation.ui.info.DonateScreen
import com.vextrainer.android.presentation.ui.info.PrivacyScreen
import com.vextrainer.android.presentation.ui.lessons.list.LessonListScreen
import com.vextrainer.android.presentation.ui.lessons.modules.ModulesScreen
import com.vextrainer.android.presentation.ui.lessons.topics.TopicListScreen
import com.vextrainer.android.presentation.ui.lessons.viewer.TopicViewerScreen
import com.vextrainer.android.presentation.ui.profile.ProfileScreen
import com.vextrainer.android.presentation.ui.quiz.categories.QuizCategoryScreen
import com.vextrainer.android.presentation.ui.quiz.detail.QuizDetailScreen
import com.vextrainer.android.presentation.ui.quiz.history.QuizHistoryScreen
import com.vextrainer.android.presentation.ui.quiz.list.QuizListScreen
import com.vextrainer.android.presentation.ui.quiz.results.QuizResultScreen
import com.vextrainer.android.presentation.ui.quiz.session.QuizSessionScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    // isLoggedIn is now expiry-aware in SecurePreferences, so an outdated token
    // on disk will correctly produce `false` here and send the user to Login.
    val startDestination = if (mainViewModel.isLoggedIn) Screen.QuizCategories.route
                           else Screen.Login.route

    // ── Session-expired observer ───────────────────────────────────────────────
    // AuthInterceptor calls SessionManager.notifySessionExpired() on a background
    // thread whenever a 401 is received and the refresh token is also invalid.
    // We collect that signal here and navigate to Login, clearing the entire
    // back stack so the user cannot press Back to return to authenticated screens.
    LaunchedEffect(navController) {
        mainViewModel.sessionExpired.collect {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val showBottomBar  = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) VexBottomNavBar(navController = navController)
        }
    ) { scaffoldPadding ->

        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(scaffoldPadding)
        ) {

            // ── Auth — Login ──────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess       = {
                        navController.navigate(Screen.QuizCategories.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { email ->
                        navController.navigate(Screen.Register.createRoute(email))
                    }
                )
            }

            // ── Auth — Register ───────────────────────────────────────────
            composable(
                route     = Screen.Register.route,
                arguments = listOf(
                    navArgument(Screen.Register.ARG_EMAIL) {
                        type         = NavType.StringType
                        defaultValue = ""
                        nullable     = true
                    }
                )
            ) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }

            // ── Quiz — Categories ─────────────────────────────────────────
            composable(Screen.QuizCategories.route) {
                QuizCategoryScreen(
                    onCategoryClick = { id, name ->
                        navController.navigate(Screen.QuizList.createRoute(id, name))
                    },
                    onHistoryClick = { navController.navigate(Screen.QuizHistory.route) }
                )
            }

            // ── Quiz — List ───────────────────────────────────────────────
            composable(
                route     = Screen.QuizList.route,
                arguments = listOf(
                    navArgument(Screen.QuizList.ARG_CATEGORY_ID) { type = NavType.IntType },
                    navArgument(Screen.QuizList.ARG_CATEGORY_NAME) {
                        type = NavType.StringType; defaultValue = ""
                    }
                )
            ) {
                QuizListScreen(
                    onQuizClick = { quizId ->
                        navController.navigate(Screen.QuizDetail.createRoute(quizId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Quiz — Detail ─────────────────────────────────────────────
            composable(
                route     = Screen.QuizDetail.route,
                arguments = listOf(
                    navArgument(Screen.QuizDetail.ARG_QUIZ_ID) { type = NavType.IntType }
                )
            ) {
                QuizDetailScreen(
                    onStartQuiz = { attemptId ->
                        navController.navigate(Screen.QuizSession.createRoute(attemptId)) {
                            popUpTo(Screen.QuizDetail.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Quiz — Session ────────────────────────────────────────────
            composable(
                route     = Screen.QuizSession.route,
                arguments = listOf(
                    navArgument(Screen.QuizSession.ARG_ATTEMPT_ID) { type = NavType.IntType }
                )
            ) {
                QuizSessionScreen(
                    onNavigateToResults = { attemptId ->
                        navController.navigate(Screen.QuizResult.createRoute(attemptId)) {
                            popUpTo(Screen.QuizSession.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Quiz — Results ────────────────────────────────────────────
            composable(
                route     = Screen.QuizResult.route,
                arguments = listOf(
                    navArgument(Screen.QuizResult.ARG_ATTEMPT_ID) { type = NavType.IntType }
                )
            ) {
                QuizResultScreen(
                    onRetakeQuiz = {
                        navController.navigate(Screen.QuizCategories.route) {
                            popUpTo(Screen.QuizCategories.route) { inclusive = false }
                        }
                    },
                    onDone = {
                        navController.navigate(Screen.QuizCategories.route) {
                            popUpTo(Screen.QuizCategories.route) { inclusive = false }
                        }
                    }
                )
            }

            // ── Quiz — History ────────────────────────────────────────────
            composable(Screen.QuizHistory.route) {
                QuizHistoryScreen(
                    onAttemptClick = { attemptId ->
                        navController.navigate(Screen.QuizResult.createRoute(attemptId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Lessons — Modules ─────────────────────────────────────────
            composable(Screen.Modules.route) {
                ModulesScreen(
                    onModuleClick = { moduleId, moduleName ->
                        navController.navigate(Screen.LessonList.createRoute(moduleId, moduleName))
                    }
                )
            }

            // ── Lessons — Lesson list ─────────────────────────────────────
            composable(
                route     = Screen.LessonList.route,
                arguments = listOf(
                    navArgument(Screen.LessonList.ARG_MODULE_ID) { type = NavType.IntType },
                    navArgument(Screen.LessonList.ARG_MODULE_NAME) {
                        type = NavType.StringType; defaultValue = ""
                    }
                )
            ) {
                LessonListScreen(
                    onLessonClick = { lessonId, lessonTitle ->
                        navController.navigate(Screen.TopicList.createRoute(lessonId, lessonTitle))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Lessons — Topic list ──────────────────────────────────────
            composable(
                route     = Screen.TopicList.route,
                arguments = listOf(
                    navArgument(Screen.TopicList.ARG_LESSON_ID) { type = NavType.IntType },
                    navArgument(Screen.TopicList.ARG_LESSON_TITLE) {
                        type = NavType.StringType; defaultValue = ""
                    }
                )
            ) {
                TopicListScreen(
                    onTopicClick = { topicId ->
                        navController.navigate(Screen.TopicViewer.createRoute(topicId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Lessons — Topic viewer ────────────────────────────────────
            composable(
                route     = Screen.TopicViewer.route,
                arguments = listOf(
                    navArgument(Screen.TopicViewer.ARG_TOPIC_ID) { type = NavType.IntType }
                )
            ) {
                TopicViewerScreen(
                    onPrevious = { prevTopicId ->
                        navController.navigate(Screen.TopicViewer.createRoute(prevTopicId))
                    },
                    onNext = { nextTopicId ->
                        navController.navigate(Screen.TopicViewer.createRoute(nextTopicId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Profile ───────────────────────────────────────────────────
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout        = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onAbout         = { navController.navigate(Screen.About.route) },
                    onContactUs     = { navController.navigate(Screen.ContactUs.route) },
                    onPrivacy       = { navController.navigate(Screen.Privacy.route) },
                    onDonate        = { navController.navigate(Screen.Donate.route) },
                    onDeleteAccount = { navController.navigate(Screen.DeleteAccount.route) }
                )
            }

            // ── Info screens ──────────────────────────────────────────────
            composable(Screen.About.route) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.ContactUs.route) {
                ContactUsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Privacy.route) {
                PrivacyScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Donate.route) {
                DonateScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.DeleteAccount.route) {
                DeleteAccountScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
