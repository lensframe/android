package app.lensframe.core.data

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreferencesRepositoryTest {
    private lateinit var repository: PreferencesRepository

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        repository = PreferencesRepository(FakePreferencesDataStore())
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `default values are correct`() = runTest {
        val isRotationEnabled = repository.isRotationEnabledFlow.first()
        val selectedFolders = repository.selectedFoldersFlow.first()

        assertTrue("Rotation should be true by default", isRotationEnabled)
        assertTrue("Folders should be empty by default", selectedFolders.isEmpty())
    }

    @Test
    fun `setRotationEnabled to false saves correctly`() = runTest {
        repository.setRotationEnabled(false)

        val isRotationEnabled = repository.isRotationEnabledFlow.first()
        assertFalse(isRotationEnabled)
    }

    @Test
    fun `setRotationEnabled to true saves correctly`() = runTest {
        repository.setRotationEnabled(true)

        val isRotationEnabled = repository.isRotationEnabledFlow.first()
        assertTrue(isRotationEnabled)
    }

    @Test
    fun `addFolder adds unique URIs and prevents duplicates`() = runTest {
        val uri1 = createMockUri("content://folder1")
        val uri2 = createMockUri("content://folder2")

        repository.addFolder(uri1)
        repository.addFolder(uri2)
        repository.addFolder(uri1)

        val folders = repository.selectedFoldersFlow.first()
        assertEquals(2, folders.size)
        assertTrue(folders.containsAll(listOf(uri1, uri2)))
    }

    @Test
    fun `removeFolder deletes specific URI`() = runTest {
        val uri1 = createMockUri("content://folder1")
        val uri2 = createMockUri("content://folder2")
        repository.addFolder(uri1)
        repository.addFolder(uri2)

        repository.removeFolder(uri1)

        val folders = repository.selectedFoldersFlow.first()
        assertEquals(1, folders.size)
        assertTrue(folders.containsAll(listOf(uri2)))
    }

    @Test
    fun `clearFolders wipes all URIs`() = runTest {
        val uri1 = createMockUri("content://folder1")
        val uri2 = createMockUri("content://folder2")
        repository.addFolder(uri1)
        repository.addFolder(uri2)

        repository.clearFolders()

        val folders = repository.selectedFoldersFlow.first()
        assertTrue(folders.isEmpty())
    }

    private fun createMockUri(path: String): Uri {
        val mockUri = mockk<Uri>()
        every { mockUri.toString() } returns path
        every { Uri.parse(path) } returns mockUri
        return mockUri
    }
}