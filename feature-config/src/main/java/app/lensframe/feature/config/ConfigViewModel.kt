package app.lensframe.feature.config

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lensframe.core.data.PreferencesRepository
import app.lensframe.core.data.WidgetScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val widgetScheduler: WidgetScheduler
) : ViewModel() {

    val isRotationEnabled: StateFlow<Boolean> = preferencesRepository.isRotationEnabledFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val selectedFolders: StateFlow<List<Uri>> = preferencesRepository.selectedFoldersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            if (preferencesRepository.isRotationEnabledFlow.first()) {
                widgetScheduler.startPeriodicRotation()
            } else {
                widgetScheduler.cancelPeriodicRotation()
            }
        }
    }

    fun toggleRotation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRotationEnabled(enabled)
            if (enabled) {
                widgetScheduler.startPeriodicRotation()
            } else {
                widgetScheduler.cancelPeriodicRotation()
            }
        }
    }

    fun addFolder(uri: Uri) {
        viewModelScope.launch {
            preferencesRepository.addFolder(uri)
            widgetScheduler.updateInstantly()
        }
    }

    fun removeFolder(uri: Uri) {
        viewModelScope.launch {
            preferencesRepository.removeFolder(uri)
            widgetScheduler.updateInstantly()
        }
    }

    fun clearFolders() {
        viewModelScope.launch {
            preferencesRepository.clearFolders()
            widgetScheduler.updateInstantly()
        }
    }
}