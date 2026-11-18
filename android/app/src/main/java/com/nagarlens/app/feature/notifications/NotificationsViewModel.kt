package com.nagarlens.app.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagarlens.app.core.model.NotificationItem
import com.nagarlens.app.core.model.NotificationSeverity
import com.nagarlens.app.core.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val selectedFilter: String = "ALL",
    val activeFilters: List<String> = listOf("ALL", "UNREAD", "CRITICAL", "WARNING"),
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
            notificationRepository.observeAllNotifications().collectLatest { list ->
                _uiState.update { it.copy(notifications = list) }
            }
        }
        viewModelScope.launch {
            notificationRepository.observeUnreadCount().collectLatest { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }
    }

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
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
