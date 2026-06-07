package app.lensframe.feature.config

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.lensframe.core.data.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
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

    fun toggleRotation(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setRotationEnabled(enabled) }
    }

    fun addFolder(uri: Uri) {
        viewModelScope.launch {
            preferencesRepository.addFolder(uri)
        }
    }

    fun removeFolder(uri: Uri) {
        viewModelScope.launch {
            preferencesRepository.removeFolder(uri)
        }
    }
}