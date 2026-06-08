package app.lensframe.core.data

interface WidgetScheduler {
    fun scheduleWidgetUpdate()
    fun cancelWidgetUpdate()
}