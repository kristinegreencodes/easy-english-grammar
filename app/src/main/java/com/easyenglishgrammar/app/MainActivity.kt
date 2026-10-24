package com.easyenglishgrammar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.easyenglishgrammar.app.data.GrammarRepository
import com.easyenglishgrammar.app.data.ProgressStore
import com.easyenglishgrammar.app.ui.screens.CategoryScreen
import com.easyenglishgrammar.app.ui.screens.HomeScreen
import com.easyenglishgrammar.app.ui.screens.RuleScreen
import com.easyenglishgrammar.app.ui.theme.EasyGrammarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyGrammarTheme {
                val context = LocalContext.current
                val repo = remember { GrammarRepository(context.applicationContext) }
                val progress = remember { ProgressStore(context.applicationContext) }
                val nav = rememberNavController()

                NavHost(navController = nav, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            repo = repo,
                            progress = progress,
                            onCategory = { nav.navigate("category/$it") },
                            onRule = { nav.navigate("rule/$it") },
                        )
                    }
                    composable(
                        "category/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val category = repo.category(entry.arguments?.getString("id").orEmpty())
                        if (category != null) {
                            CategoryScreen(
                                category = category,
                                progress = progress,
                                onBack = { nav.popBackStack() },
                                onRule = { nav.navigate("rule/$it") },
                            )
                        }
                    }
                    composable(
                        "rule/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType }),
                    ) { entry ->
                        val rule = repo.rule(entry.arguments?.getString("id").orEmpty())
                        val category = rule?.let { repo.category(it.categoryId) }
                        if (rule != null && category != null) {
                            val index = category.rules.indexOf(rule)
                            val next = category.rules.getOrNull(index + 1)
                            RuleScreen(
                                rule = rule,
                                category = category,
                                episode = index + 1,
                                progress = progress,
                                onBack = { nav.popBackStack() },
                                onNext = next?.let { n ->
                                    {
                                        nav.navigate("rule/${n.id}") {
                                            popUpTo("rule/{id}") { inclusive = true }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
