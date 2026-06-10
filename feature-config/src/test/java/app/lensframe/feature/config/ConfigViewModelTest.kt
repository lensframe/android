package app.lensframe.feature.config

import android.net.Uri
import app.lensframe.core.data.PreferencesRepository
import app.lensframe.core.data.WidgetScheduler
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConfigViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: PreferencesRepository
    private lateinit var widgetScheduler: WidgetScheduler
    private lateinit var viewModel: ConfigViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        widgetScheduler = mockk(relaxed = true)
    }

    @Test
    fun `init starts periodic rotation if enabled in database`() = runTest {
        every { repository.isRotationEnabledFlow } returns flowOf(true)
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())

        viewModel = ConfigViewModel(repository, widgetScheduler)

        verify { widgetScheduler.startPeriodicRotation() }
        verify(exactly = 0) { widgetScheduler.cancelPeriodicRotation() }
    }

    @Test
    fun `init cancels periodic rotation if disabled in database`() = runTest {
        every { repository.isRotationEnabledFlow } returns flowOf(false)
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())

        viewModel = ConfigViewModel(repository, widgetScheduler)

        verify { widgetScheduler.cancelPeriodicRotation() }
        verify(exactly = 0) { widgetScheduler.startPeriodicRotation() }
    }

    @Test
    fun `toggleRotation updates database and scheduler`() = runTest {
        every { repository.isRotationEnabledFlow } returns flowOf(true)
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())
        viewModel = ConfigViewModel(repository, widgetScheduler)

        viewModel.toggleRotation(false)

        coVerify { repository.setRotationEnabled(false) }
        verify { widgetScheduler.cancelPeriodicRotation() }
    }

    @Test
    fun `addFolder saves to database and forces instant widget update`() = runTest {
        val mockUri = mockk<Uri>()
        every { repository.isRotationEnabledFlow } returns flowOf(true)
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())
        viewModel = ConfigViewModel(repository, widgetScheduler)

        viewModel.addFolder(mockUri)

        coVerify { repository.addFolder(mockUri) }
        verify { widgetScheduler.updateInstantly() }
    }

    @Test
    fun `removeFolder removes from database and forces instant widget update`() = runTest {
        val mockUri = mockk<Uri>()
        every { repository.isRotationEnabledFlow } returns flowOf(true)
        every { repository.selectedFoldersFlow } returns flowOf(listOf(mockUri))
        viewModel = ConfigViewModel(repository, widgetScheduler)

        viewModel.removeFolder(mockUri)

        coVerify { repository.removeFolder(mockUri) }
        verify { widgetScheduler.updateInstantly() }
    }

    @Test
    fun `clearFolders wipes database and forces instant widget update`() = runTest {
        val mockUri = mockk<Uri>()
        every { repository.isRotationEnabledFlow } returns flowOf(true)
        every { repository.selectedFoldersFlow } returns flowOf(listOf(mockUri))
        viewModel = ConfigViewModel(repository, widgetScheduler)

        viewModel.clearFolders()

        coVerify { repository.clearFolders() }
        verify { widgetScheduler.updateInstantly() }
    }

}