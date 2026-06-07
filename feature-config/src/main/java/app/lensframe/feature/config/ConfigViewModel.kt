package app.lensframe.feature.config

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

    fun toggleRotation(enabled: Boolean) {
        _isRotationEnabled.value = enabled
    }
}