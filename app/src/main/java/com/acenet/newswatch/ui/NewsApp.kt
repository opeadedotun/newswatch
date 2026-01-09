package com.acenet.newswatch.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.acenet.newswatch.R
import com.acenet.newswatch.data.NewsItem
import com.acenet.newswatch.data.NewsRepository
import com.acenet.newswatch.ui.theme.NewsWatchTheme
import com.acenet.newswatch.ui.theme.PrimaryRed
import com.acenet.newswatch.viewmodel.NewsUiState
import com.acenet.newswatch.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(viewModel: NewsViewModel = viewModel()) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    var showSplash by remember { mutableStateOf(true) }

    NewsWatchTheme(darkTheme = isDarkMode) {
        if (showSplash) {
            SplashScreen(onTimeout = { showSplash = false })
        } else {
            MainScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NewsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val filteredNews by viewModel.filteredNews.collectAsState(initial = emptyList())
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val bookmarkedNews by viewModel.bookmarkedNews.collectAsState()
    
    var selectedNewsItem by remember { mutableStateOf<NewsItem?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var infoDialogType by remember { mutableStateOf(InfoDialogType.ABOUT) }
    var showBookmarks by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    
    val uriHandler = LocalUriHandler.current
    
    // Define tabs
    val tabs = listOf(
        Triple("World News", NewsRepository.NewsCategory.WORLD, Icons.Default.Newspaper),
        Triple("Tech News", NewsRepository.NewsCategory.TECH, Icons.Default.Computer),
        Triple("Entertainment", NewsRepository.NewsCategory.ENTERTAINMENT, Icons.Default.PlayArrow),
        Triple("Sports News", NewsRepository.NewsCategory.SPORT, Icons.Default.SportsSoccer)
    )

    if (showInfoDialog) {
        InfoDialog(
            type = infoDialogType,
            onDismiss = { showInfoDialog = false }
        )
    }

    Scaffold(
        topBar = {
            if (selectedNewsItem == null) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .height(45.dp)
                                    .padding(top = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "All the headlines. One place",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (viewModel.isDarkMode.value) Color.White else Color.Black,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Bookmarks") },
                                    onClick = {
                                        menuExpanded = false
                                        showBookmarks = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (viewModel.isDarkMode.value) "Light Mode" else "Dark Mode") },
                                    onClick = {
                                        menuExpanded = false
                                        viewModel.toggleTheme()
                                    },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Check for update") },
                                    onClick = {
                                        menuExpanded = false
                                        uriHandler.openUri("https://upload-apk.com/JtcYNqF7u86gd36")
                                    },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Privacy Policy") },
                                    onClick = {
                                        menuExpanded = false
                                        infoDialogType = InfoDialogType.PRIVACY_POLICY
                                        showInfoDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Open Source License") },
                                    onClick = {
                                        menuExpanded = false
                                        infoDialogType = InfoDialogType.LICENSES
                                        showInfoDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                                )
                                DropdownMenuItem(
                                    text = { Text("About NewsWatch") },
                                    onClick = {
                                        menuExpanded = false
                                        infoDialogType = InfoDialogType.ABOUT
                                        showInfoDialog = true
                                    },
                                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (selectedNewsItem == null && !showBookmarks) {
                NavigationBar {
                    tabs.forEach { (label, category, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp) },
                            selected = selectedCategory == category,
                            onClick = { 
                                showBookmarks = false
                                viewModel.onCategorySelected(category) 
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedNewsItem == null) {
                if (showBookmarks) {
                    BackHandler { showBookmarks = false }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Back", color = PrimaryRed, modifier = Modifier.clickable { showBookmarks = false })
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "Bookmarks", style = MaterialTheme.typography.titleLarge)
                        }
                        if (bookmarkedNews.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No bookmarks yet.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(bookmarkedNews) { newsItem ->
                                    NewsItemCard(
                                        newsItem = newsItem, 
                                        isBookmarked = true,
                                        onBookmarkClick = { viewModel.toggleBookmark(newsItem) },
                                        onClick = { selectedNewsItem = newsItem }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    NewsListScreen(
                        uiState = uiState,
                        news = filteredNews,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                        onNewsClick = { selectedNewsItem = it },
                        onBookmarkClick = { viewModel.toggleBookmark(it) },
                        isBookmarked = { viewModel.isBookmarked(it) },
                        isRefreshing = viewModel.isRefreshing.collectAsState().value,
                        onRefresh = { viewModel.fetchNews(isManual = true) }
                    )
                }
            } else {
                NewsDetailScreen(
                    newsItem = selectedNewsItem!!,
                    onBack = { selectedNewsItem = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen(
    uiState: NewsUiState,
    news: List<NewsItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewsClick: (NewsItem) -> Unit,
    onBookmarkClick: (NewsItem) -> Unit,
    isBookmarked: (NewsItem) -> Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()
    
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }

    Column {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(50.dp),
            placeholder = { 
                Text(
                    text = "Search...", 
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 0.dp)
                ) 
            },
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Default.Search, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp)
                ) 
            },
            singleLine = true,
            shape = RoundedCornerShape(25.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        )

        Box(modifier = Modifier.fillMaxSize().nestedScroll(pullToRefreshState.nestedScrollConnection)) {
            when (uiState) {
                is NewsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryRed)
                    }
                }
                is NewsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = uiState.message, color = Color.Red, textAlign = TextAlign.Center)
                        }
                    }
                }
                is NewsUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(news) { newsItem ->
                            NewsItemCard(
                                newsItem = newsItem, 
                                isBookmarked = isBookmarked(newsItem),
                                onBookmarkClick = { onBookmarkClick(newsItem) },
                                onClick = { onNewsClick(newsItem) }
                            )
                        }
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun NewsItemCard(
    newsItem: NewsItem, 
    isBookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = newsItem.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = newsItem.sourceName, style = MaterialTheme.typography.labelSmall, color = PrimaryRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "• ${parseDateForDisplay(newsItem.pubDate)}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            IconButton(onClick = onBookmarkClick, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) Color.Yellow else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NewsDetailScreen(newsItem: NewsItem, onBack: () -> Unit) {
    BackHandler { onBack() }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Back", color = PrimaryRed, modifier = Modifier.clickable { onBack() })
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "Details", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
        }
        AndroidView(factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadUrl(newsItem.link)
            }
        }, modifier = Modifier.fillMaxSize())
    }
}

fun parseDateForDisplay(dateString: String): String {
    return try {
        if (dateString.length > 22) dateString.substring(0, 22) else dateString
    } catch (e: Exception) {
        dateString
    }
}
