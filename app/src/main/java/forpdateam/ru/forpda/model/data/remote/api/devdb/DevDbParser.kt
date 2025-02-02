package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.findAll
import forpdateam.ru.forpda.extensions.findOnce
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
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
                        Brands.Item(
                            id = itemsMatcher.group(1),
                            title = itemsMatcher.group(2).fromHtml().orEmpty(),
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
                Brand.DeviceItem(
                    imageSrc = matcher.group(1),
                    id = matcher.group(2),
                    title = matcher.group(3).fromHtml().orEmpty(),
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
                title = matcher.group(4)
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

    fun parseDevice(response: String, argDevId: String): Device {
        var id: String? = null
        var title: String? = null
        var brandId: String? = null
        var brandTitle: String? = null
        var catId: String? = null
        var catTitle: String? = null
        var rating: Int = 0
        val images = mutableListOf<Pair<String, String>>()
        val specsGroups = mutableListOf<Pair<String, List<Pair<String, String>>>>()
        patternProvider
            .getPattern(scope.scope, scope.device_head)
            .matcher(response)
            .findOnce { matcher ->
                title = matcher.group(1)

                patternProvider
                    .getPattern(scope.scope, scope.device_images)
                    .matcher(matcher.group(2))
                    .findAll {
                        images.add(Pair(it.group(2), it.group(1)))
                    }

                patternProvider
                    .getPattern(scope.scope, scope.device_specs_titled)
                    .matcher(matcher.group(3))
                    .findAll {
                        val specTitle = it.group(1).fromHtml()
                        val specs = patternProvider
                            .getPattern(scope.scope, scope.main_specs)
                            .matcher(it.group(2))
                            .map {
                                Pair(it.group(1), it.group(2))
                            }
                        specsGroups.add(Pair(specTitle.orEmpty(), specs))
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
                            catId = it.group(1)
                            catTitle = it.group(3)
                        } else {
                            brandId = it.group(2)
                            brandTitle = it.group(3)
                        }
                    }

                rating = matcher.group(2)?.toInt() ?: 0

                title = matcher.group(4)
                id = argDevId
            }

        val comments = patternProvider
            .getPattern(scope.scope, scope.device_comments)
            .matcher(response)
            .map { matcher ->
                Device.Comment(
                    id = matcher.group(1).toInt(),
                    rating = matcher.group(3).toInt(),
                    user = User.required(
                        id = matcher.group(4).toInt(),
                        nick = matcher.group(5).fromHtml()
                    ),
                    date = matcher.group(6),
                    text = (matcher.group(9) ?: matcher.group(7))?.trim().orEmpty(),
                    likes = matcher.group(10).toInt(),
                    dislikes = matcher.group(11).toInt()
                )
            }

        val news = patternProvider
            .getPattern(scope.scope, scope.device_reviews)
            .matcher(response)
            .map { matcher ->
                Device.PostItem(
                    id = matcher.group(1).toInt(),
                    image = matcher.group(2),
                    title = matcher.group(3).fromHtml().orEmpty(),
                    date = matcher.group(4),
                    desc = matcher.group(5).fromHtml()
                )
            }

        val discussions = patternProvider
            .getPattern(scope.scope, scope.device_discussions)
            .matcher(response)
            .mapOnce {
                patternProvider
                    .getPattern(scope.scope, scope.device_discuss_and_firm)
                    .matcher(it.group(1))
                    .map { matcher ->
                        Device.PostItem(
                            id = matcher.group(1).toInt(),
                            image = null,
                            title = matcher.group(2).fromHtml().orEmpty(),
                            date = matcher.group(3),
                            desc = matcher.group(4).fromHtml()
                        )
                    }
            } ?: emptyList()

        val firmwares = patternProvider
            .getPattern(scope.scope, scope.device_firmwares)
            .matcher(response)
            .mapOnce {
                patternProvider
                    .getPattern(scope.scope, scope.device_discuss_and_firm)
                    .matcher(it.group(1))
                    .map { matcher ->
                        Device.PostItem(
                            id = matcher.group(1).toInt(),
                            image = null,
                            title = matcher.group(2).fromHtml().orEmpty(),
                            date = matcher.group(3),
                            desc = matcher.group(4).fromHtml()
                        )
                    }
            } ?: emptyList()
        return Device(
            id = requireNotNull(id),
            title = requireNotNull(title),
            brandId = requireNotNull(brandId),
            brandTitle = requireNotNull(brandTitle),
            catId = requireNotNull(catId),
            catTitle = requireNotNull(catTitle),
            rating = rating,
            specs = specsGroups,
            images = images,
            comments = comments,
            discussions = discussions,
            firmwares = firmwares,
            news = news
        )
    }

    fun parseSearch(response: String): BrandSearch {
        val devices = patternProvider
            .getPattern(scope.scope, scope.main_search)
            .matcher(response)
            .map { matcher ->
                Brand.DeviceItem(
                    id = matcher.group(2),
                    imageSrc = matcher.group(1),
                    title = matcher.group(3).fromHtml().orEmpty(),
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
