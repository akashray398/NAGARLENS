package com.nagarlens.app.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.NotificationItem
import com.nagarlens.app.core.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notificationRepository.observeAllNotifications().collect { list ->
                _uiState.value = _uiState.value.copy(notifications = list)
            }
        }
        viewModelScope.launch {
            notificationRepository.observeUnreadCount().collect { count ->
                _uiState.value = _uiState.value.copy(unreadCount = count)
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }
}
