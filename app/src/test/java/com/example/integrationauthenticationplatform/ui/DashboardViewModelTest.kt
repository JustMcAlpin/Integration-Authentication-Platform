package com.example.integrationauthenticationplatform.ui

import app.cash.turbine.test
import com.example.integrationauthenticationplatform.MainDispatcherRule
import com.example.integrationauthenticationplatform.data.CredentialRepo
import com.example.integrationauthenticationplatform.model.ProviderGroup
import com.example.integrationauthenticationplatform.model.SERVICES
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle


class DashboardViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onOAuthSuccessForService saves once and connects that service`() = runTest {
        val repo = mockk<CredentialRepo>(relaxed = true)

        // VM init calls repo.all(); give it "empty DB"
        coEvery { repo.all() } returns emptyList()

        val vm = DashboardViewModel(repo)
        val target = SERVICES.first { it.id == "facebook" }

        vm.onOAuthSuccessForService("facebook", """{"access_token":"t"}""")
        advanceUntilIdle()

        // verify repo save
        coVerify {
            repo.save(service = target.displayName, authType = "oauth", credentialJson = any())
        }

        // verify state
        vm.services.test {
            val list = awaitItem()
            val ui = list.first { it.def.id == "facebook" }
            assertTrue(ui.connected)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onOAuthSuccess fans out to group and connects all`() = runTest {
        val repo = mockk<CredentialRepo>(relaxed = true)
        coEvery { repo.all() } returns emptyList()

        val vm = DashboardViewModel(repo)

        vm.onOAuthSuccess(ProviderGroup.Google, """{"access_token":"t"}""")
        advanceUntilIdle()

        val googleServices = SERVICES.filter { it.group == ProviderGroup.Google }
        // each service saved once
        googleServices.forEach { s ->
            coVerify {
                repo.save(service = s.displayName, authType = "oauth", credentialJson = any())
            }
        }

        vm.services.test {
            val list = awaitItem()
            googleServices.forEach { s ->
                assertTrue(list.first { it.def.id == s.id }.connected)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `saveApiKey stores api_key and marks connected`() = runTest {
        val repo = mockk<CredentialRepo>(relaxed = true)
        coEvery { repo.all() } returns emptyList()

        val vm = DashboardViewModel(repo)
        val sendgrid = SERVICES.first { it.id == "sendgrid" }

        vm.saveApiKey(sendgrid, "SG.x")
        advanceUntilIdle()

        coVerify {
            repo.save(service = sendgrid.displayName, authType = "api_key", credentialJson = match { it.contains("api_key") })
        }

        vm.services.test {
            val list = awaitItem()
            val ui = list.first { it.def.id == "sendgrid" }
            assertTrue(ui.connected)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
