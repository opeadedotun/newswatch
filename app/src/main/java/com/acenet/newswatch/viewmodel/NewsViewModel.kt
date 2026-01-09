package com.acenet.newswatch.viewmodel

import androidx.lifecycle.viewModelScope
import com.acenet.newswatch.data.NewsItem
import com.acenet.newswatch.data.NewsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val news: List<NewsItem>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NewsRepository()
    private val sharedPrefs = application.getSharedPreferences("newswatch_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _uiState = MutableStateFlow<NewsUiState>(NewsUiState.Loading)
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow(NewsRepository.NewsCategory.WORLD)
    val selectedCategory: StateFlow<NewsRepository.NewsCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _bookmarkedNews = MutableStateFlow<List<NewsItem>>(loadBookmarks())
    val bookmarkedNews: StateFlow<List<NewsItem>> = _bookmarkedNews.asStateFlow()

    // Combined search results
    val filteredNews = combine(uiState, searchQuery) { state, query ->
        if (state is NewsUiState.Success) {
            if (query.isEmpty()) {
                state.news
            } else {
                state.news.filter { item ->
                    item.title.contains(query, ignoreCase = true) ||
                    item.sourceName.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
                }
            }
        } else {
            emptyList()
        }
    }

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

    fun fetchNews(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                _isRefreshing.value = true
            }
            // Only set loading if it's the initial load or a manual forceful refresh that clears content?
            // User experience is better if we keep showing old data while fetching, but for now lets just update state
            if (_uiState.value is NewsUiState.Error || _uiState.value is NewsUiState.Loading) {
                 _uiState.value = NewsUiState.Loading
            }
            // If switching categories we probably want to show loading or keep old content until new one loads?
            // Let's show loading to give feedback that content is changing
            _uiState.value = NewsUiState.Loading
            
            val items = repository.getNewsByCategory(_selectedCategory.value)
            if (items.isNotEmpty()) {
                _uiState.value = NewsUiState.Success(items)
            } else {
                // Keep showing old data if fetch fails, but if empty initially show error
                if (_uiState.value is NewsUiState.Loading) {
                    _uiState.value = NewsUiState.Error("Failed to load news. Check internet connection.")
                }
            }
            _isRefreshing.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
        sharedPrefs.edit().putBoolean("dark_mode", _isDarkMode.value).apply()
    }

    fun toggleBookmark(newsItem: NewsItem) {
        val current = _bookmarkedNews.value.toMutableList()
        val existing = current.find { it.link == newsItem.link }
        if (existing != null) {
            current.remove(existing)
        } else {
            current.add(0, newsItem)
            if (current.size > 20) {
                current.removeAt(current.size - 1)
            }
        }
        _bookmarkedNews.value = current
        saveBookmarks(current)
    }

    fun isBookmarked(newsItem: NewsItem): Boolean {
        return _bookmarkedNews.value.any { it.link == newsItem.link }
    }

    private fun saveBookmarks(bookmarks: List<NewsItem>) {
        val json = gson.toJson(bookmarks)
        sharedPrefs.edit().putString("bookmarks", json).apply()
    }

    private fun loadBookmarks(): List<NewsItem> {
        val json = sharedPrefs.getString("bookmarks", null) ?: return emptyList()
        val type = object : TypeToken<List<NewsItem>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
