package net.ienlab.sogangassist.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import net.ienlab.sogangassist.ui.navigation.RootNavigationGraph
import net.ienlab.sogangassist.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        setContent {
            AppTheme {
                enableEdgeToEdge(
                    statusBarStyle = if (!isSystemInDarkTheme()) {
                        SystemBarStyle.light(MaterialTheme.colorScheme.surface.toArgb(), MaterialTheme.colorScheme.surface.toArgb())
                    } else {
                        SystemBarStyle.dark(MaterialTheme.colorScheme.surface.toArgb())
                    }
                )
                val windowSize = calculateWindowSizeClass(this)
                val navController = rememberNavController()
                RootNavigationGraph(
                    windowSize = windowSize,
                    navController = navController,
                    bundle = Bundle()
                )
            }
        }
    }
}