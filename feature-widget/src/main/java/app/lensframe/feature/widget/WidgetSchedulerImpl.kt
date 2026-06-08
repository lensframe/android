package app.lensframe.feature.widget

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
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

    override fun scheduleWidgetUpdate() {
        val periodicRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        val instantRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(instantRequest)
    }

    override fun cancelWidgetUpdate() {
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "WidgetUpdateWork_Periodic"
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