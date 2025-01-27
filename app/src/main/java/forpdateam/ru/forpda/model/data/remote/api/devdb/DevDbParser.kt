package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class DevDbParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.DevDb

    fun parseBrands(response: String): Brands {
        val letterMap = linkedMapOf<String, List<Brands.Item>>()
        patternProvider
            .getPattern(scope.scope, scope.brands_letters)
            .matcher(response)
            .findAll { matcher ->
                val letter = matcher.group(1)
                val items = patternProvider
                    .getPattern(scope.scope, scope.brands_items_in_letter)
                    .matcher(matcher.group(2))
                    .map { itemsMatcher ->
                        val title = requireNotNull(itemsMatcher.group(2).fromHtml()) {
                            "brands.item.title"
                        }
                        Brands.Item(
                            id = itemsMatcher.group(1),
                            title = title,
                            count = itemsMatcher.group(3).toInt()
                        )
                    }
                letterMap[letter] = items
            }

        val brands = patternProvider
            .getPattern(scope.scope, scope.main_root)
            .matcher(response)
            .mapOnce { matcher ->
                var catId: String? = null
                var catTitle: String? = null
                patternProvider
                    .getPattern(scope.scope, scope.main_breadcrumb)
                    .matcher(matcher.group(1))
                    .findAll { bcMatcher ->
                        if (bcMatcher.group(2) == null) {
                            catId = bcMatcher.group(1)
                            catTitle = bcMatcher.group(3)
                        }
                    }
                Brands(
                    catId = requireNotNull(catId) { "brands.catId" },
                    catTitle = requireNotNull(catTitle) { "brands.catTitle" },
                    actual = matcher.group(5).toInt(),
                    all = matcher.group(6).toInt(),
                    letterMap = letterMap
                )
            }
        return requireNotNull(brands) {
            "Brands not parsed"
        }
    }

    fun parseBrand(response: String): Brand {
        val devices = patternProvider
            .getPattern(scope.scope, scope.brand_devices)
            .matcher(response)
            .map { matcher ->
                val specs = patternProvider
                    .getPattern(scope.scope, scope.main_specs)
                    .matcher(matcher.group(4))
                    .map { Pair(it.group(1), it.group(2)) }
                val title = requireNotNull(matcher.group(3).fromHtml()) {
                    "brand.devices.title"
                }
                Brand.DeviceItem(
                    imageSrc = matcher.group(1),
                    id = matcher.group(2),
                    title = title,
                    price = matcher.group(5),
                    rating = matcher.group(7)?.toInt() ?: 0,
                    specs = specs
                )
            }

        val brand = patternProvider
            .getPattern(scope.scope, scope.main_root)
            .matcher(response)
            .mapOnce { matcher ->
                var catId: String? = null
                var catTitle: String? = null
                var id: String? = null
                var title: String? = null
                patternProvider
                    .getPattern(scope.scope, scope.main_breadcrumb)
                    .matcher(matcher.group(1))
                    .findAll { bcMatcher ->
                        if (bcMatcher.group(2) == null) {
                            catId = bcMatcher.group(1)
                            catTitle = bcMatcher.group(3)
                        } else {
                            id = bcMatcher.group(2)
                            title = bcMatcher.group(3)
                        }
                    }
                title = matcher.group(4) ?: title
                Brand(
                    id = requireNotNull(id) { "brand.id" },
                    title = requireNotNull(title) { "brand.title" },
                    catId = requireNotNull(catId) { "brand.catId" },
                    catTitle = requireNotNull(catTitle) { "brand.catTitle" },
                    actual = matcher.group(5).toInt(),
                    all = matcher.group(6).toInt(),
                    devices = devices
                )
            }

        return requireNotNull(brand) {
            "Brand not parsed"
        }
    }

    fun parseDevice(response: String, argDevId: String): Device = Device().also { data ->
        patternProvider
            .getPattern(scope.scope, scope.device_head)
            .matcher(response)
            .findOnce { matcher ->
                data.title = matcher.group(1)

                patternProvider
                    .getPattern(scope.scope, scope.device_images)
                    .matcher(matcher.group(2))
                    .findAll {
                        data.images.add(Pair(it.group(2), it.group(1)))
                    }

                patternProvider
                    .getPattern(scope.scope, scope.device_specs_titled)
                    .matcher(matcher.group(3))
                    .findAll {
                        val title = it.group(1).fromHtml()
                        val specs = patternProvider
                            .getPattern(scope.scope, scope.main_specs)
                            .matcher(it.group(2))
                            .map {
                                Pair(it.group(1), it.group(2))
                            }
                        data.specs.add(Pair(title.orEmpty(), specs))
                    }
            }

        patternProvider
            .getPattern(scope.scope, scope.main_root)
            .matcher(response)
            .findOnce { matcher ->
                patternProvider
                    .getPattern(scope.scope, scope.main_breadcrumb)
                    .matcher(matcher.group(1))
                    .findAll {
                        if (it.group(2) == null) {
                            data.catId = it.group(1)
                            data.catTitle = it.group(3)
                        } else {
                            data.brandId = it.group(2)
                            data.brandTitle = it.group(3)
                        }
                    }

                matcher.group(2)?.also {
                    data.rating = it.toInt()
                }

                data.title = matcher.group(4)
                data.id = argDevId
            }

        val comments = patternProvider
            .getPattern(scope.scope, scope.device_comments)
            .matcher(response)
            .map { matcher ->
                Device.Comment().apply {
                    id = matcher.group(1).toInt()
                    rating = matcher.group(3).toInt()
                    userId = matcher.group(4).toInt()
                    nick = matcher.group(5).fromHtml()
                    date = matcher.group(6)
                    text = (matcher.group(9) ?: matcher.group(7))?.trim()
                    likes = matcher.group(10).toInt()
                    dislikes = matcher.group(11).toInt()
                }
            }
        data.comments.addAll(comments)

        val news = patternProvider
            .getPattern(scope.scope, scope.device_reviews)
            .matcher(response)
            .map { matcher ->
                Device.PostItem().apply {
                    id = matcher.group(1).toInt()
                    image = matcher.group(2)
                    title = matcher.group(3).fromHtml()
                    date = matcher.group(4)
                    matcher.group(5)?.also {
                        desc = it.fromHtml()
                    }
                }
            }
        data.news.addAll(news)

        patternProvider
            .getPattern(scope.scope, scope.device_discussions)
            .matcher(response)
            .findOnce {
                val discussions = patternProvider
                    .getPattern(scope.scope, scope.device_discuss_and_firm)
                    .matcher(it.group(1))
                    .map { matcher ->
                        Device.PostItem().apply {
                            id = matcher.group(1).toInt()
                            title = matcher.group(2).fromHtml()
                            date = matcher.group(3)
                            matcher.group(4)?.also {
                                desc = it.fromHtml()
                            }
                        }
                    }
                data.discussions.addAll(discussions)
            }

        patternProvider
            .getPattern(scope.scope, scope.device_firmwares)
            .matcher(response)
            .findOnce {
                val firmwares = patternProvider
                    .getPattern(scope.scope, scope.device_discuss_and_firm)
                    .matcher(it.group(1))
                    .map { matcher ->
                        Device.PostItem().apply {
                            id = matcher.group(1).toInt()
                            title = matcher.group(2).fromHtml()
                            date = matcher.group(3)
                            matcher.group(4)?.also {
                                desc = it.fromHtml()
                            }
                        }
                    }
                data.firmwares.addAll(firmwares)
            }
        return data
    }

    fun parseSearch(response: String): BrandSearch {
        val devices = patternProvider
            .getPattern(scope.scope, scope.main_search)
            .matcher(response)
            .map { matcher ->
                val title = requireNotNull(matcher.group(3).fromHtml()) {
                    "brandsearch.devices.title"
                }
                Brand.DeviceItem(
                    id = matcher.group(2),
                    imageSrc = matcher.group(1),
                    title = title,
                    price = null,
                    rating = 0,
                    specs = emptyList()
                )
            }
        return BrandSearch(
            actual = devices.size,
            all = devices.size,
            devices = devices
        )
    }
}
