package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import com.example.api.models.ClusterResource
import com.example.ui.screens.NodeUsageTrendsChart

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockNode = ClusterResource(
        id = "node/pve-01",
        type = "node",
        node = "pve-01",
        name = "Production Node 1",
        status = "online",
        cpu = 0.45,
        maxcpu = 16.0,
        mem = 17179869184L, // 16 GB
        maxmem = 34359738368L // 32 GB
    )

    composeTestRule.setContent { 
        MyApplicationTheme { 
            NodeUsageTrendsChart(resource = mockNode) 
        } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
