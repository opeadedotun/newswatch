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
        "Pulse Sports" to "https://www.pulsesports.ng/feeds/rss",
        "Complete Sports" to "https://www.completesports.com/feed",
        "Goal.com" to "https://www.goal.com/feeds/en-ng/news"
    )

    private val techSources = mapOf(
        "TechCrunch" to "https://techcrunch.com/feed/",
        "The Verge" to "https://www.theverge.com/rss/index.xml",
        "Wired" to "https://www.wired.com/feed/rss",
        "Engadget" to "https://www.engadget.com/rss.xml"
    )

    private val movieSources = mapOf(
        "Variety" to "https://variety.com/feed/",
        "Hollywood Reporter" to "https://www.hollywoodreporter.com/feed/",
        "Vanguard Entertainment" to "https://www.vanguardngr.com/category/entertainment/feed/",
        "Premium Times Entertainment" to "https://www.premiumtimesng.com/category/entertainment/feed/"
    )

    enum class NewsCategory {
        LATEST, FOREIGN, SPORT, TECH, MOVIE
    }

    suspend fun getLatestNews(category: NewsCategory): List<NewsItem> = withContext(Dispatchers.IO) {
        if (category == NewsCategory.LATEST) {
            val localNews = fetchCategoryNews(localSources)
            val foreignNews = fetchCategoryNews(foreignSources)
            
            // Take 8 local and 2 foreign for 80% ratio
            return@withContext (localNews.take(8) + foreignNews.take(2))
                .sortedByDescending { parseDate(it.pubDate) }
        }

        val selectedSources = when (category) {
            NewsCategory.LATEST -> localSources // Should not hit this due to if above
            NewsCategory.FOREIGN -> foreignSources
            NewsCategory.SPORT -> sportSources
            NewsCategory.TECH -> techSources
            NewsCategory.MOVIE -> movieSources
        }

        fetchCategoryNews(selectedSources)
    }

    private suspend fun fetchCategoryNews(sources: Map<String, String>): List<NewsItem> = coroutineScope {
        try {
            // Fetch all feeds in parallel
            val deferreds = sources.map { (name, url) ->
                async {
                    try {
                        val feed = service.getFeed(url)
                        feed.channel?.items?.map { item ->
                            item.apply { 
                                sourceName = name 
                                // Prioritize enclosure if it's an image
                                imageUrl = if (enclosure?.type?.startsWith("image") == true) {
                                    enclosure?.url
                                } else {
                                    extractImage(description)
                                }
                            }
                        } ?: emptyList()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        emptyList<NewsItem>()
                    }
                }
            }

            val allNews = deferreds.awaitAll().flatten()
            allNews.sortedByDescending { parseDate(it.pubDate) }.take(10)

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
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
