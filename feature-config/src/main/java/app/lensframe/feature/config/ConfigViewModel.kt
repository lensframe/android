package app.lensframe.feature.config

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor() : ViewModel() {
    private val _isRotationEnabled = MutableStateFlow(true)
    val isRotationEnabled: StateFlow<Boolean> = _isRotationEnabled.asStateFlow()
    private val _selectedFolders = MutableStateFlow<List<Uri>>(emptyList())
    val selectedFolders: StateFlow<List<Uri>> = _selectedFolders.asStateFlow()

    fun toggleRotation(enabled: Boolean) {
        _isRotationEnabled.value = enabled
    }

    fun addFolder(uri: Uri) {
        _selectedFolders.update { currentList ->
            if (currentList.contains(uri)) currentList else currentList + uri
        }
    }

    fun removeFolder(uri: Uri) {
        _selectedFolders.update { currentList ->
            currentList - uri
        }
    }
}