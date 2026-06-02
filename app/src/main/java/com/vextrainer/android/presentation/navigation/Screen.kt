package com.vextrainer.android.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {

    // Auth
    object Login    : Screen("auth/login")

    object Register : Screen("auth/register?email={email}") {
        const val ARG_EMAIL = "email"
        fun createRoute(email: String = "") =
            if (email.isBlank()) "auth/register?email="
            else "auth/register?email=${Uri.encode(email)}"
    }

    // Dashboard
    object Dashboard : Screen("dashboard")

    // Quiz
    object QuizCategories : Screen("quiz/categories")

    object QuizList : Screen("quiz/categories/{categoryId}/quizzes?categoryName={categoryName}") {
        const val ARG_CATEGORY_ID   = "categoryId"
        const val ARG_CATEGORY_NAME = "categoryName"
        fun createRoute(categoryId: Int, categoryName: String) =
            "quiz/categories/$categoryId/quizzes?categoryName=${Uri.encode(categoryName)}"
    }

    object QuizDetail : Screen("quiz/quizzes/{quizId}") {
        const val ARG_QUIZ_ID = "quizId"
        fun createRoute(quizId: Int) = "quiz/quizzes/$quizId"
    }

    object QuizSession : Screen("quiz/attempts/{attemptId}/session") {
        const val ARG_ATTEMPT_ID = "attemptId"
        fun createRoute(attemptId: Int) = "quiz/attempts/$attemptId/session"
    }

    object QuizResult : Screen("quiz/attempts/{attemptId}/results") {
        const val ARG_ATTEMPT_ID = "attemptId"
        fun createRoute(attemptId: Int) = "quiz/attempts/$attemptId/results"
    }

    object QuizHistory : Screen("quiz/history")

    // Lessons
    object Modules : Screen("lessons/modules")

    object LessonList : Screen("lessons/modules/{moduleId}/lessons?moduleName={moduleName}") {
        const val ARG_MODULE_ID   = "moduleId"
        const val ARG_MODULE_NAME = "moduleName"
        fun createRoute(moduleId: Int, moduleName: String) =
            "lessons/modules/$moduleId/lessons?moduleName=${Uri.encode(moduleName)}"
    }

    object TopicList : Screen(
        "lessons/lessons/{lessonId}/topics?lessonTitle={lessonTitle}&moduleName={moduleName}"
    ) {
        const val ARG_LESSON_ID    = "lessonId"
        const val ARG_LESSON_TITLE = "lessonTitle"
        const val ARG_MODULE_NAME  = "moduleName"
        fun createRoute(lessonId: Int, lessonTitle: String, moduleName: String) =
            "lessons/lessons/$lessonId/topics" +
            "?lessonTitle=${Uri.encode(lessonTitle)}" +
            "&moduleName=${Uri.encode(moduleName)}"
    }

    object TopicViewer : Screen("lessons/topics/{topicId}") {
        const val ARG_TOPIC_ID = "topicId"
        fun createRoute(topicId: Int) = "lessons/topics/$topicId"
    }

    // Activity Report
    object ActivityReport : Screen("activity/report")

    // Profile & Info
    object Profile       : Screen("profile")
    object About         : Screen("info/about")
    object ContactUs     : Screen("info/contact")
    object Privacy       : Screen("info/privacy")
    object Donate        : Screen("info/donate")
    object DeleteAccount : Screen("info/delete-account")
}
