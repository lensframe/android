package app.lensframe.feature.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.lensframe.core.data.WidgetScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetScheduler {

    override fun startPeriodicRotation() {
        val periodicRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        ).setInputData(workDataOf(WidgetUpdateWorker.IS_PERIODIC to true)).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
    }

    override fun cancelPeriodicRotation() {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME_PERIODIC)
    }

    override fun updateInstantly() {
        val instantRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WIDGET_UPDATE_WORK_NAME_INSTANT,
            ExistingWorkPolicy.REPLACE,
            instantRequest
        )
    }

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME_PERIODIC = "WidgetUpdateWork_Periodic"
        private const val WIDGET_UPDATE_WORK_NAME_INSTANT = "WidgetUpdateWork_Instant"
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetSchedulerBindingModule {
    @Binds
    abstract fun bindWidgetScheduler(
        impl: WidgetSchedulerImpl
    ): WidgetScheduler
}