package app.lensframe.feature.config

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor() : ViewModel() {
    private val _isRotationEnabled = MutableStateFlow(true)
    val isRotationEnabled: StateFlow<Boolean> = _isRotationEnabled.asStateFlow()

    private val _selectedFolderUri = MutableStateFlow<Uri?>(null)
    val selectedFolderUri: StateFlow<Uri?> = _selectedFolderUri.asStateFlow()

    fun toggleRotation(enabled: Boolean) {
        _isRotationEnabled.value = enabled
    }

    fun updateSelectedFolder(uri: Uri?) {
        _selectedFolderUri.value = uri

    }
}