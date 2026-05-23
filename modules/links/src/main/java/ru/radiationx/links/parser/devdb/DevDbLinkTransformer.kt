package ru.radiationx.links.parser.devdb

import ru.radiationx.coretypes.DevDbBrandId
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.links.Link
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder

//https://4pda.to/devdb/
//https://4pda.to/devdb/phones
//https://4pda.to/devdb/pad
//https://4pda.to/devdb/ebook
//https://4pda.to/devdb/smartwatch
//https://4pda.to/devdb/phones/all
//https://4pda.to/devdb/phones/all#letter-M
//https://4pda.to/devdb/phones/select/
//https://4pda.to/devdb/phones/select/all
//https://4pda.to/devdb/phones/apple
//https://4pda.to/devdb/phones/apple/all
//https://4pda.to/devdb/phones/apple?sort=year
//https://4pda.to/devdb/phones/apple?sort=year&sort-year=desc
//https://4pda.to/devdb/phones/apple?sort=year&sort-year=asc
//https://4pda.to/devdb/phones/apple?sort=rating
//https://4pda.to/devdb/phones/apple?sort=rating&sort-rating=desc
//https://4pda.to/devdb/phones/apple?sort=rating&sort-rating=asc
//https://4pda.to/devdb/phones/apple?sort=title
//https://4pda.to/devdb/phones/apple?sort=title&sort-title=desc
//https://4pda.to/devdb/phones/apple?sort=title&sort-title=asc
//https://4pda.to/devdb/xiaomi_mi_note_10_lite
//https://4pda.to/devdb/xiaomi_mi_note_10_lite:8_128_256
//https://4pda.to/devdb/xiaomi_mi_note_10_lite#specification
//https://4pda.to/devdb/xiaomi_mi_note_10_lite#comments
//https://4pda.to/devdb/xiaomi_mi_note_10_lite#discussions
//https://4pda.to/devdb/xiaomi_mi_note_10_lite#reviews
//https://4pda.to/devdb/xiaomi_mi_note_10_lite#firmware
//https://4pda.to/devdb/xiaomi_mi_note_10_lite#prices
//https://4pda.to/devdb/search?s=nothing
internal object DevDbLinkTransformer {

    private val categoryIds = setOf("phones", "pad", "ebook", "smartwatch")
    private val letterRegex = Regex("letter-(\\w)")
    private val forbiddenBrandId = setOf("all", "select")
    private val sortFields = setOf("year", "rating", "title")

    fun build(builder: LinkUrlBuilder, link: Link.DevDb): LinkUrl {
        with(builder) {
            segment("devdb")
            when (link) {
                is Link.DevDb.Brands -> {
                    segment(link.categoryId.id)
                    if (link.letter != null) {
                        fragment("letter-${link.letter}")
                    }
                }

                is Link.DevDb.Devices -> {
                    segment(link.brandId.categoryId.id)
                    segment(link.brandId.brand)
                    if (link.sort != null) {
                        query("sort", link.sort.field)
                        if (link.sort.order != null) {
                            val order = when (link.sort.order) {
                                Link.DevDb.Devices.Sort.Order.Asc -> "asc"
                                Link.DevDb.Devices.Sort.Order.Desc -> "desc"
                            }
                            query("sort-${link.sort.field}", order)
                        }
                    }
                }

                is Link.DevDb.Device -> {
                    segment(link.deviceId.id)
                    if (link.tab != null) {
                        fragment(link.tab)
                    }
                }

                is Link.DevDb.Search -> {
                    segment("search")
                    query("s", link.text)
                }

                Link.DevDb.Categories -> Unit
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrl): Link.DevDb? {
        if (url.segment(0) != "devdb") {
            return null
        }
        val segment1 = url.segment(1) ?: return Link.DevDb.Categories

        val withCategory = parseWithCategory(url, segment1)
        if (withCategory != null) {
            return withCategory
        }

        val search = parseSearch(url, segment1)
        if (search != null) {
            return search
        }

        return parseDevice(url, segment1)
    }

    private fun parseWithCategory(url: LinkUrl, segment1: String): Link.DevDb? {
        if (segment1 !in categoryIds) return null
        val categoryId = DevDbCategoryId(segment1)
        val brand = parseDevices(url, categoryId)
        if (brand != null) {
            return brand
        }
        return parseBrands(url, categoryId)
    }

    private fun parseBrands(url: LinkUrl, categoryId: DevDbCategoryId): Link.DevDb.Brands {
        val letter = url.fragment?.let {
            letterRegex.find(it)?.groupValues[1]
        }
        return Link.DevDb.Brands(categoryId = categoryId, letter)
    }

    private fun parseDevices(url: LinkUrl, categoryId: DevDbCategoryId): Link.DevDb.Devices? {
        val segment2 = url.segment(2) ?: return null
        if (segment2 in forbiddenBrandId) return null
        val brandId = DevDbBrandId(categoryId = categoryId, brand = segment2)
        val querySortField = url.query("sort").takeIf { it in sortFields }
        val sort = querySortField?.let {
            val querySortOrder = url.query("sort-$it")
            val order = when (querySortOrder) {
                "asc" -> Link.DevDb.Devices.Sort.Order.Asc
                "desc" -> Link.DevDb.Devices.Sort.Order.Desc
                else -> null
            }
            Link.DevDb.Devices.Sort(field = it, order = order)
        }
        return Link.DevDb.Devices(brandId = brandId, sort = sort)
    }

    private fun parseSearch(url: LinkUrl, segment1: String): Link.DevDb.Search? {
        if (segment1 != "search") return null
        return Link.DevDb.Search(text = url.query("s").orEmpty())
    }

    private fun parseDevice(url: LinkUrl, segment1: String): Link.DevDb.Device {
        return Link.DevDb.Device(deviceId = DevDbDeviceId(id = segment1), tab = url.fragment)
    }
}