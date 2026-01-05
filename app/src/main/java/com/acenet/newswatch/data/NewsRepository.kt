package com.acenet.newswatch.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

interface RssService {
    @GET
    suspend fun getFeed(@Url url: String): RssFeed
}

class NewsRepository {

    private val client = okhttp3.OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.google.com/") 
        .client(client)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build()

    private val service = retrofit.create(RssService::class.java)

    private val localSources = mapOf(
        "Vanguard" to "https://www.vanguardngr.com/feed",
        "The Guardian" to "https://guardian.ng/feed",
        "Premium Times" to "https://www.premiumtimesng.com/feed",
        "Punch" to "https://punchng.com/feed",
        "Daily Post" to "https://dailypost.ng/feed",
        "Tribune" to "https://tribuneonlineng.com/feed"
    )

    private val foreignSources = mapOf(
        "BBC World" to "https://feeds.bbci.co.uk/news/world/rss.xml",
        "CNN" to "http://rss.cnn.com/rss/edition_world.rss",
        "Al Jazeera" to "https://www.aljazeera.com/xml/rss/all.xml"
    )

    private val sportSources = mapOf(
        "Complete Sports (NG)" to "https://www.completesports.com/feed",
        "BBC Football" to "https://feeds.bbci.co.uk/sport/football/rss.xml",
        "Sky Sports Football" to "https://www.skysports.com/rss/11095",
        "Goal" to "https://www.goal.com/feeds/en/news"
    )

    private val techSources = mapOf(
        "TechCrunch" to "https://techcrunch.com/feed/",
        "The Verge" to "https://www.theverge.com/rss/index.xml",
        "Wired" to "https://www.wired.com/feed/rss",
        "Vanguard Tech" to "https://www.vanguardngr.com/category/technology/feed/"
    )

    private val movieSources = mapOf(
        "Variety" to "https://variety.com/feed/",
        "Hollywood Reporter" to "https://www.hollywoodreporter.com/feed/",
        "Vanguard Entertainment" to "https://www.vanguardngr.com/category/entertainment/feed/",
        "Punch Entertainment" to "https://punchng.com/topics/entertainment/feed/"
    )

    enum class NewsCategory {
        WORLD, FOREIGN, SPORT, TECH, ENTERTAINMENT
    }

    suspend fun getNewsByCategory(category: NewsCategory): List<NewsItem> = withContext(Dispatchers.IO) {
        val rawItems = when (category) {
            NewsCategory.WORLD -> {
                val localNews = fetchCategoryNews(localSources, limit = 60)
                val foreignNews = fetchCategoryNews(foreignSources, limit = 30)
                val filteredLocal = filterWorldNews(localNews)
                val filteredForeign = filterWorldNews(foreignNews)
                (filteredLocal.take(14) + filteredForeign.take(6))
            }
            NewsCategory.FOREIGN -> fetchCategoryNews(foreignSources, limit = 20)
            NewsCategory.SPORT -> {
                val news = fetchCategoryNews(sportSources, limit = 100)
                filterByCategory(news, NewsCategory.SPORT)
            }
            NewsCategory.TECH -> {
                val news = fetchCategoryNews(techSources, limit = 100)
                filterByCategory(news, NewsCategory.TECH)
            }
            NewsCategory.ENTERTAINMENT -> {
                val news = fetchCategoryNews(movieSources, limit = 100)
                filterByCategory(news, NewsCategory.ENTERTAINMENT)
            }
        }

        return@withContext rawItems.sortedByDescending { parseDate(it.pubDate) }.take(20)
    }

    private fun filterWorldNews(news: List<NewsItem>): List<NewsItem> {
        val excludeKeywords = listOf(
            "tech", "technology", "software", "app", "gadget", "smartphone", "iphone", "android",
            "sport", "football", "soccer", "basketball", "tennis", "golf", "match", "league", "cup",
            "movie", "entertainment", "cinema", "celebrity", "music", "song", "album", "artist", "actor", "actress", "hollywood", "nollywood", "box office",
            "gaming", "nintendo", "playstation", "xbox"
        )
        return news.filter { item ->
            val content = (item.title + " " + item.description).lowercase()
            excludeKeywords.none { content.contains(it) }
        }
    }

    private fun filterByCategory(news: List<NewsItem>, category: NewsCategory): List<NewsItem> {
        val includeKeywords = when (category) {
            NewsCategory.SPORT -> listOf(
                "football", "soccer", "league", "club", "premier league", "champions league", "afcon", 
                "super eagles", "npfl", "nigerian league", "nations cup", "world cup", "coach", "striker",
                "manchester", "chelsea", "liverpool", "arsenal", "real madrid", "barcelona", "bayern",
                "psg", "italy", "spain", "germany", "france", "ucl", "uel", "transfers"
            )
            NewsCategory.TECH -> listOf(
                "tech", "technology", "ai", "artificial intelligence", "software", "hardware", 
                "app", "startup", "silicon", "semiconductor", "robot", "computing", "digital",
                "smartphone", "mobile", "internet", "google", "apple", "microsoft", "meta", "tesla"
            )
            NewsCategory.ENTERTAINMENT -> listOf(
                "movie", "film", "cinema", "entertainment", "celebrity", "music", "song", "album",
                "artist", "singer", "actor", "actress", "hollywood", "nollywood", "showbiz", "award",
                "series", "streaming", "netflix", "theatre", "tv"
            )
            else -> emptyList()
        }

        return news.filter { item ->
            val content = (item.title + " " + item.description).lowercase()
            includeKeywords.any { content.contains(it) }
        }
    }

    private suspend fun fetchCategoryNews(sources: Map<String, String>, limit: Int = 20): List<NewsItem> = coroutineScope {
        try {
            // Fetch all feeds in parallel
            val deferreds = sources.map { (name, url) ->
                async {
                    try {
                        val feed = service.getFeed(url)
                        feed.channel?.items?.map { item ->
                            item.apply { 
                                sourceName = name 
                                imageUrl = if (enclosure?.type?.startsWith("image") == true) {
                                    enclosure?.url
                                } else {
                                    extractImage(description)
                                }
                            }
                        } ?: emptyList()
                    } catch (e: Exception) {
                        emptyList<NewsItem>()
                    }
                }
            }

            val allNews = deferreds.awaitAll().flatten()
            val result = allNews.sortedByDescending { parseDate(it.pubDate) }.take(limit)
            return@coroutineScope result

        } catch (e: Exception) {
            return@coroutineScope emptyList<NewsItem>()
        }
    }

    private fun parseDate(dateString: String): Date {
        // RSS Date Fmt: Wed, 02 Oct 2002 13:00:00 GMT
        val formats = listOf(
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
            SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH),
             SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH) // Fallback for Atom-ish
        )
        
        for (format in formats) {
            try {
                return format.parse(dateString) ?: Date(0)
            } catch (e: Exception) {
                // Try next
            }
        }
        return Date(0)
    }

    private fun extractImage(description: String): String? {
        val regex = "src\\s*=\\s*['\"]([^'\"]+)['\"]".toRegex()
        val matchResult = regex.find(description)
        return matchResult?.groupValues?.get(1)
    }
}
