package com.acenet.newswatch.data

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root
import java.io.Serializable

@Root(name = "item", strict = false)
data class NewsItem(
    @field:Element(name = "title", required = false)
    var title: String = "",

    @field:Element(name = "link", required = false)
    var link: String = "",

    @field:Element(name = "description", required = false)
    var description: String = "",

    @field:Element(name = "pubDate", required = false)
    var pubDate: String = "",

    @field:Element(name = "enclosure", required = false)
    var enclosure: Enclosure? = null,
    
    // We will populate this manually after fetching
    var sourceName: String = "",
    var imageUrl: String? = null
) : Serializable

@Root(name = "enclosure", strict = false)
data class Enclosure(
    @field:org.simpleframework.xml.Attribute(name = "url", required = false)
    var url: String = "",
    
    @field:org.simpleframework.xml.Attribute(name = "type", required = false)
    var type: String = ""
) : Serializable

@Root(name = "rss", strict = false)
data class RssFeed(
    @field:Element(name = "channel")
    var channel: Channel? = null
)

@Root(name = "channel", strict = false)
data class Channel(
    @field:org.simpleframework.xml.ElementList(name = "item", inline = true, required = false)
    var items: List<NewsItem>? = null
)
