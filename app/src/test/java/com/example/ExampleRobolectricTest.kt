package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Work Tracker", appName)
  }

  @Test
  fun testSplashToDashboardFlow() {
    // Wait for splash screen
    composeTestRule.waitForIdle()
    
    // Advance the compose clock to skip the 2.2s splash delay and trigger navigation
    composeTestRule.mainClock.advanceTimeBy(3000L)
    
    // Wait for dashboard composition
    composeTestRule.waitForIdle()
    
    // Print layout to verify rendering without crashes
    composeTestRule.onRoot().printToLog("ExampleRobolectricTest")
  }
}
