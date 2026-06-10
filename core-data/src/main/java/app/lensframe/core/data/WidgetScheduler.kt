package app.lensframe.core.data

interface WidgetScheduler {
    fun startPeriodicRotation()
    fun cancelPeriodicRotation()
    fun updateInstantly()
}