package com.acenet.newswatch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.acenet.newswatch.data.NewsItem
import com.acenet.newswatch.data.NewsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val news: List<NewsItem>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()
    
    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow(NewsRepository.NewsCategory.LOCAL)
    val selectedCategory: StateFlow<NewsRepository.NewsCategory> = _selectedCategory.asStateFlow()
    
    // For keeping track of the last update time, potentially
    private var lastUpdatedTime = System.currentTimeMillis()

    init {
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (isActive) {
                fetchNews()
                // Wait 15 minutes
                delay(15 * 60 * 1000L)
            }
        }
    }
    
    fun onCategorySelected(category: NewsRepository.NewsCategory) {
        if (_selectedCategory.value != category) {
            _selectedCategory.value = category
            fetchNews()
        }
    }

    fun fetchNews() {
        viewModelScope.launch {
            // Only set loading if it's the initial load or a manual forceful refresh that clears content?
            // User experience is better if we keep showing old data while fetching, but for now lets just update state
            if (_uiState.value is NewsUiState.Error || _uiState.value is NewsUiState.Loading) {
                 _uiState.value = NewsUiState.Loading
            }
            // If switching categories we probably want to show loading or keep old content until new one loads?
            // Let's show loading to give feedback that content is changing
            _uiState.value = NewsUiState.Loading
            
            val items = repository.getLatestNews(_selectedCategory.value)
            if (items.isNotEmpty()) {
                _uiState.value = NewsUiState.Success(items)
                lastUpdatedTime = System.currentTimeMillis()
            } else {
                // Keep showing old data if fetch fails, but if empty initially show error
                if (_uiState.value is NewsUiState.Loading) {
                    _uiState.value = NewsUiState.Error("Failed to load news. Check internet connection.")
                }
            }
        }
    }
}
