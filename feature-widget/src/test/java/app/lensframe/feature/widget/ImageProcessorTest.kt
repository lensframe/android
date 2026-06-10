package app.lensframe.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ImageProcessorTest {

    private lateinit var context: Context
    private lateinit var imageProcessor: ImageProcessor

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        imageProcessor = ImageProcessor(context)
    }

    @Test
    fun `calculateInSampleSize calculates correct downsample ratio for massive images`() {
        val options = BitmapFactory.Options().apply {
            outWidth = 4000
            outHeight = 3000
        }
        val targetMaxSize = 400

        val sampleSize = imageProcessor.calculateInSampleSize(options, targetMaxSize)

        assertEquals(4, sampleSize)
    }

    @Test
    fun `calculateInSampleSize ignores small images`() {
        val options = BitmapFactory.Options().apply {
            outWidth = 300
            outHeight = 300
        }

        val sampleSize = imageProcessor.calculateInSampleSize(options, 400)

        assertEquals(1, sampleSize)
    }

    @Test
    fun `rotateBitmap successfully swaps width and height on 90 degree rotation`() {
        val originalBitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)

        val rotatedBitmap = imageProcessor.rotateBitmap(originalBitmap, 90)

        assertEquals(100, rotatedBitmap.width)
        assertEquals(200, rotatedBitmap.height)
        assertNotEquals(originalBitmap, rotatedBitmap)
    }

    @Test
    fun `rotateBitmap ignores 0 degree rotation`() {
        val originalBitmap = Bitmap.createBitmap(200, 100, Bitmap.Config.ARGB_8888)

        val rotatedBitmap = imageProcessor.rotateBitmap(originalBitmap, 0)

        assertEquals(originalBitmap, rotatedBitmap)
    }
}