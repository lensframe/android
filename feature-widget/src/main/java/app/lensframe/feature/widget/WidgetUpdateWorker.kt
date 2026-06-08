package app.lensframe.feature.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.documentfile.provider.DocumentFile
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.lensframe.core.data.PreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val preferencesRepository: PreferencesRepository
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        try {
            val folders = preferencesRepository.selectedFoldersFlow.first()
            if (folders.isEmpty()) {
                return Result.success()
            }

            val randomFolderUri = folders.random()
            val folderDocument = DocumentFile.fromTreeUri(applicationContext, randomFolderUri)
            val images = folderDocument?.listFiles()?.filter {
                it.type?.startsWith("image/") == true
            }
            if (images.isNullOrEmpty()) {
                return Result.success()
            }
            val selectedImageUri = images.random().uri

            val inputStream = applicationContext.contentResolver.openInputStream(selectedImageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val scaledBitmap = scaleBitmapToWidgetSize(originalBitmap)

            val cacheFile = File(applicationContext.filesDir, "widget_cache.jpg")
            FileOutputStream(cacheFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            LensFrameWidget().updateAll(applicationContext)

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun scaleBitmapToWidgetSize(bitmap: Bitmap): Bitmap {
        val maxSize = 800
        val ratio =
            (maxSize.toFloat() / bitmap.width).coerceAtMost(maxSize.toFloat() / bitmap.height)
        if (ratio >= 1.0) return bitmap

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        return bitmap.scale(newWidth, newHeight)
    }
}