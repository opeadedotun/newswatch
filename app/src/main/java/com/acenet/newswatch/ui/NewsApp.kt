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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.acenet.newswatch.R
import com.acenet.newswatch.data.NewsItem
import com.acenet.newswatch.data.NewsRepository
import com.acenet.newswatch.ui.theme.PrimaryRed
import com.acenet.newswatch.viewmodel.NewsUiState
import com.acenet.newswatch.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(viewModel: NewsViewModel = viewModel()) {
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        MainScreen(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: NewsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var selectedNewsItem by remember { mutableStateOf<NewsItem?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    
    // Define tabs
    val tabs = listOf(
        Triple("Local News", NewsRepository.NewsCategory.LOCAL, Icons.Default.Home),
        Triple("Foreign News", NewsRepository.NewsCategory.FOREIGN, Icons.Default.Public),
        Triple("Sports News", NewsRepository.NewsCategory.SPORT, Icons.Default.SportsSoccer)
    )

    if (showInfoDialog) {
        InfoDialog(onDismiss = { showInfoDialog = false })
    }

    Scaffold(
        topBar = {
            if (selectedNewsItem == null) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            // Logo replacing the text "NEWSWATCH"
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .height(60.dp)
                                    .padding(vertical = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showInfoDialog = true }) {
                            Icon(Icons.Default.Info, contentDescription = "App Info")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (selectedNewsItem == null) {
                NavigationBar {
                    tabs.forEach { (label, category, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            selected = selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedNewsItem == null) {
                NewsListScreen(
                    uiState = uiState,
                    onNewsClick = { selectedNewsItem = it }
                )
            } else {
                NewsDetailScreen(
                    newsItem = selectedNewsItem!!,
                    onBack = { selectedNewsItem = null }
                )
            }
        }
    }
}

@Composable
fun NewsListScreen(
    uiState: NewsUiState,
    onNewsClick: (NewsItem) -> Unit
) {
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
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.news) { newsItem ->
                    NewsItemCard(newsItem = newsItem, onClick = { onNewsClick(newsItem) })
                }
            }
        }
    }
}

@Composable
fun NewsItemCard(newsItem: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            // Text only card as requested
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
