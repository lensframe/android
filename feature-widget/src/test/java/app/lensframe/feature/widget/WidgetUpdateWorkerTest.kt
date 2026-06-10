package app.lensframe.feature.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import app.lensframe.core.data.PreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WidgetUpdateWorkerTest {

    private lateinit var context: Context
    private lateinit var repository: PreferencesRepository
    private lateinit var imageProcessor: ImageProcessor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = mockk(relaxed = true)
        imageProcessor = ImageProcessor(context)
    }

    @Test
    fun `when folders are empty, worker clears cache and returns success`() = runTest {
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())
        val cacheFile = File(context.filesDir, "widget_cache.jpg")
        cacheFile.writeText("stale-but-should-survive")
        val worker = buildWorker()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(cacheFile.exists())
    }

    @Test
    fun `periodic run with rotation disabled is a no-op and leaves cache untouched`() = runTest {
        every { repository.isRotationEnabledFlow } returns flowOf(false)
        val cacheFile = File(context.filesDir, "widget_cache.jpg")
        cacheFile.writeText("stale-but-should-survive")
        val worker = buildWorker(true)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(cacheFile.exists())
    }

    @Test
    fun `periodic run with rotation enabled proceeds past the guard`() = runTest {
        every { repository.isRotationEnabledFlow } returns flowOf(true)
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())
        val cacheFile = File(context.filesDir, "widget_cache.jpg")
        cacheFile.writeText("stale-but-should-survive")
        val worker = buildWorker(true)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(cacheFile.exists())
    }

    @Test
    fun `instant run is not gated by the rotation toggle`() = runTest {
        every { repository.isRotationEnabledFlow } returns flowOf(false)
        every { repository.selectedFoldersFlow } returns flowOf(emptyList())
        val cacheFile = File(context.filesDir, "widget_cache.jpg")
        cacheFile.writeText("stale-but-should-survive")
        val worker = buildWorker()

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertFalse(cacheFile.exists())
    }

    private fun buildWorker(isPeriodic: Boolean = false): WidgetUpdateWorker {
        return TestListenableWorkerBuilder<WidgetUpdateWorker>(context)
            .setInputData(workDataOf(WidgetUpdateWorker.IS_PERIODIC to isPeriodic))
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker {
                    return WidgetUpdateWorker(appContext, workerParameters, repository, imageProcessor)
                }
            })
            .build()
    }
}