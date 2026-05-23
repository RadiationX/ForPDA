package ru.radiationx.links.parser.devdb

import ru.radiationx.coretypes.DevDbBrandId
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkBuilderAdapter
import ru.radiationx.links.url.LinkUrlAdapter

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
class DevDbLinkTransformer {

    private companion object {
        private val categoryIds = setOf("phones", "pad", "ebook", "smartwatch")
        private val letterRegex = Regex("letter-(\\w)")
        private val forbiddenBrandId = setOf("all", "select")
        private val sortFields = setOf("year", "rating", "title")
    }

    fun build(builder: LinkBuilderAdapter, link: Links.DevDb): LinkUrlAdapter {
        with(builder) {
            segment("devdb")
            when (link) {
                is Links.DevDb.Brands -> {
                    segment(link.categoryId.id)
                    if (link.letter != null) {
                        fragment("letter-${link.letter}")
                    }
                }

                is Links.DevDb.Brand -> {
                    segment(link.brandId.categoryId.id)
                    segment(link.brandId.brandId)
                    if (link.sort != null) {
                        query("sort", link.sort.field)
                        if (link.sort.order != null) {
                            val order = when (link.sort.order) {
                                Links.DevDb.Brand.Sort.Order.Asc -> "asc"
                                Links.DevDb.Brand.Sort.Order.Desc -> "desc"
                            }
                            query("sort-${link.sort.field}", order)
                        }
                    }
                }

                is Links.DevDb.Device -> {
                    segment(link.deviceId.id)
                    if (link.tab != null) {
                        fragment(link.tab)
                    }
                }

                is Links.DevDb.Search -> {
                    segment("search")
                    query("s", link.text)
                }

                Links.DevDb.Categories -> Unit
            }
        }
        return builder.build()
    }

    fun parse(url: LinkUrlAdapter): Links.DevDb? {
        if (url.segment(0) != "devdb") {
            return null
        }
        val segment1 = url.segment(1) ?: return Links.DevDb.Categories

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

    private fun parseWithCategory(url: LinkUrlAdapter, segment1: String): Links.DevDb? {
        if (segment1 !in categoryIds) return null
        val categoryId = DevDbCategoryId(segment1)
        val brand = parseBrand(url, categoryId)
        if (brand != null) {
            return brand
        }
        return parseBrands(url, categoryId)
    }

    private fun parseBrands(url: LinkUrlAdapter, categoryId: DevDbCategoryId): Links.DevDb.Brands {
        val letter = url.fragment?.let {
            letterRegex.find(it)?.groupValues[1]
        }
        return Links.DevDb.Brands(categoryId = categoryId, letter)
    }

    private fun parseBrand(url: LinkUrlAdapter, categoryId: DevDbCategoryId): Links.DevDb.Brand? {
        val segment2 = url.segment(2) ?: return null
        if (segment2 in forbiddenBrandId) return null
        val brandId = DevDbBrandId(categoryId = categoryId, brandId = segment2)
        val querySortField = url.query("sort").takeIf { it in sortFields }
        val sort = querySortField?.let {
            val querySortOrder = url.query("sort-$it")
            val order = when (querySortOrder) {
                "asc" -> Links.DevDb.Brand.Sort.Order.Asc
                "desc" -> Links.DevDb.Brand.Sort.Order.Desc
                else -> null
            }
            Links.DevDb.Brand.Sort(field = it, order = order)
        }
        return Links.DevDb.Brand(brandId = brandId, sort = sort)
    }

    private fun parseSearch(url: LinkUrlAdapter, segment1: String): Links.DevDb.Search? {
        if (segment1 != "search") return null
        return Links.DevDb.Search(text = url.query("s").orEmpty())
    }

    private fun parseDevice(url: LinkUrlAdapter, segment1: String): Links.DevDb.Device {
        return Links.DevDb.Device(deviceId = DevDbDeviceId(id = segment1), tab = url.fragment)
    }
}