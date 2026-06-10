package app.lensframe.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.lensframe.core.data.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val isPeriodic = inputData.getBoolean(IS_PERIODIC, false)
                if (isPeriodic && !preferencesRepository.isRotationEnabledFlow.first()) {
                    return@withContext Result.success()
                }

                val folders = preferencesRepository.selectedFoldersFlow.first()
                if (folders.isEmpty()) {
                    clearWidgetCache()
                    return@withContext Result.success()
                }

                val randomFolderUri = folders.random()
                val folderDocument = DocumentFile.fromTreeUri(applicationContext, randomFolderUri)
                val images = folderDocument?.listFiles()?.filter {
                    it.type?.startsWith("image/") == true
                }
                if (images.isNullOrEmpty()) {
                    clearWidgetCache()
                    return@withContext Result.success()
                }
                val selectedImageUri = images.random().uri

                val scaledBitmap =
                    decodeSampledBitmapFromUri(selectedImageUri) ?: return@withContext Result.failure()

                val rotationDegrees = getRotationDegrees(selectedImageUri)
                val finalBitmap = rotateBitmap(scaledBitmap, rotationDegrees)

                val cacheFile = File(applicationContext.filesDir, CACHED_FILE_NAME)
                FileOutputStream(cacheFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }

                LensFrameWidget().updateAll(applicationContext)

                return@withContext Result.success()
            } catch (e: SecurityException) {
                Log.e("WidgetWorker", "Lost permission to access the photo folder.", e)
                Result.failure()

            } catch (e: java.io.IOException) {
                Log.e("WidgetWorker", "Failed to read or write the image file.", e)
                Result.retry()

            } catch (e: OutOfMemoryError) {
                Log.e("WidgetWorker", "Out of memory trying to load the image.", e)
                Result.failure()

            } catch (e: Exception) {
                Log.e("WidgetWorker", "Unexpected error updating the widget.", e)
                Result.failure()
            }
        }
    }

    private suspend fun clearWidgetCache() {
        val cacheFile = File(applicationContext.filesDir, CACHED_FILE_NAME)
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
        LensFrameWidget().updateAll(applicationContext)
    }

    private fun decodeSampledBitmapFromUri(uri: android.net.Uri): Bitmap? {
        val resolver = applicationContext.contentResolver

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options)

        options.inJustDecodeBounds = false

        return resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > MAX_SIZE || width > MAX_SIZE) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= MAX_SIZE && halfWidth / inSampleSize >= MAX_SIZE) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getRotationDegrees(uri: Uri): Int {
        return try {
            applicationContext.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            Log.e("WidgetWorker", "Failed to read EXIF data", e)
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) {
            return bitmap
        }

        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object {
        const val IS_PERIODIC = "is_periodic"
        private const val MAX_SIZE = 400
        private const val CACHED_FILE_NAME = "widget_cache.jpg"
    }
}