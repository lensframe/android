package app.lensframe.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.documentfile.provider.DocumentFile
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
                val folders = preferencesRepository.selectedFoldersFlow.first()
                if (folders.isEmpty()) {
                    return@withContext Result.success()
                }

                val randomFolderUri = folders.random()
                val folderDocument = DocumentFile.fromTreeUri(applicationContext, randomFolderUri)
                val images = folderDocument?.listFiles()?.filter {
                    it.type?.startsWith("image/") == true
                }
                if (images.isNullOrEmpty()) {
                    return@withContext Result.success()
                }
                val selectedImageUri = images.random().uri

                val scaledBitmap =
                    decodeSampledBitmapFromUri(selectedImageUri) ?: return@withContext Result.failure()

                val cacheFile = File(applicationContext.filesDir, "widget_cache.jpg")
                FileOutputStream(cacheFile).use { out ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
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

    companion object {
        private const val MAX_SIZE = 800
    }
}