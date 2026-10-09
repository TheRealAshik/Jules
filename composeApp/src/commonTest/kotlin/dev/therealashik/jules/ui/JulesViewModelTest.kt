package dev.therealashik.jules.ui

import dev.therealashik.jules.sdk.JulesApiClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JulesViewModelTest {

    @Test
    fun testInitialScreenWhenApiKeyIsBlank() {
        val client = JulesApiClient("")
        val viewModel = JulesViewModel(client, initialApiKey = "")
        assertEquals(Screen.Welcome, viewModel.state.value.screen)
        assertEquals("", viewModel.state.value.apiKey)
    }

    @Test
    fun testInitialScreenWhenApiKeyIsPresent() {
        val client = JulesApiClient("valid_test_key")
        val viewModel = JulesViewModel(client, initialApiKey = "valid_test_key")
        assertEquals(Screen.SessionList, viewModel.state.value.screen)
        assertEquals("valid_test_key", viewModel.state.value.apiKey)
    }

    @Test
    fun testNavigationBetweenWelcomeAndApiKeySetup() {
        val client = JulesApiClient("")
        val viewModel = JulesViewModel(client, initialApiKey = "")

        viewModel.navigate(Screen.ApiKeySetup)
        assertEquals(Screen.ApiKeySetup, viewModel.state.value.screen)

        viewModel.navigate(Screen.Welcome)
        assertEquals(Screen.Welcome, viewModel.state.value.screen)
    }

    @Test
    fun testNavigationToSettingsAndPromptGallery() {
        val client = JulesApiClient("valid_key")
        val viewModel = JulesViewModel(client, initialApiKey = "valid_key")

        viewModel.navigate(Screen.Settings)
        assertEquals(Screen.Settings, viewModel.state.value.screen)

        viewModel.navigate(Screen.PromptGallery)
        assertEquals(Screen.PromptGallery, viewModel.state.value.screen)
    }

    @Test
    fun testThemePreferenceSaving() {
        val client = JulesApiClient("valid_key")
        val viewModel = JulesViewModel(client, initialApiKey = "valid_key")

        viewModel.saveThemePreference(ThemePreference.DARK)
        assertEquals(ThemePreference.DARK, viewModel.state.value.themePreference)

        viewModel.saveThemePreference(ThemePreference.LIGHT)
        assertEquals(ThemePreference.LIGHT, viewModel.state.value.themePreference)
    }

    @Test
    fun testPageSizeSaving() {
        val client = JulesApiClient("valid_key")
        val viewModel = JulesViewModel(client, initialApiKey = "valid_key")

        viewModel.savePageSize(50)
        assertEquals(50, viewModel.state.value.pageSize)
    }

    @Test
    fun testClearError() {
        val client = JulesApiClient("")
        val viewModel = JulesViewModel(client, initialApiKey = "")

        viewModel.saveApiKey("   ")
        assertEquals(Strings.INVALID_API_KEY, viewModel.state.value.error)

        viewModel.clearError()
        assertNull(viewModel.state.value.error)
    }
}
