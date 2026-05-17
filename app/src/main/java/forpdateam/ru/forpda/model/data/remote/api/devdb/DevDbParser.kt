package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import javax.inject.Inject

class DevDbParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.DevDb

    // todo refactor
    suspend fun parseBrands(response: String): Brands {
        val letterMap = linkedMapOf<String, List<Brands.Item>>()
        patternProvider
            .getRegexParser(scope.scope, scope.brands_letters)
            .findAll(response) { matcher ->
                val letter = matcher.require(1)
                val items = patternProvider
                    .getRegexParser(scope.scope, scope.brands_items_in_letter)
                    .map(matcher.require(2)) { itemsMatcher ->
                        Brands.Item(
                            id = itemsMatcher.require(1),
                            title = itemsMatcher.require(2).fromHtml(),
                            count = itemsMatcher.require(3).toInt()
                        )
                    }
                letterMap[letter] = items
            }

        val brands = patternProvider
            .getRegexParser(scope.scope, scope.main_root)
            .mapOnce(response) { matcher ->
                var catId: String? = null
                var catTitle: String? = null
                patternProvider
                    .getRegexParser(scope.scope, scope.main_breadcrumb)
                    .findAll(matcher.require(1)) { bcMatcher ->
                        if (bcMatcher.get(2) == null) {
                            catId = bcMatcher.require(1)
                            catTitle = bcMatcher.require(3)
                        }
                    }
                Brands(
                    catId = requireNotNull(catId) { "brands.catId" },
                    catTitle = requireNotNull(catTitle) { "brands.catTitle" },
                    actual = matcher.require(5).toInt(),
                    all = matcher.require(6).toInt(),
                    letterMap = letterMap
                )
            }
        return requireNotNull(brands) {
            "Brands not parsed"
        }
    }

    suspend fun parseBrand(response: String): Brand {
        val devices = patternProvider
            .getRegexParser(scope.scope, scope.brand_devices)
            .map(response) { matcher ->
                val specs = patternProvider
                    .getRegexParser(scope.scope, scope.main_specs)
                    .map(matcher.require(4)) {
                        Pair(it.require(1), it.require(2))
                    }
                Brand.DeviceItem(
                    imageSrc = matcher.get(1),
                    id = matcher.require(2),
                    title = matcher.require(3).fromHtml(),
                    price = matcher.get(5),
                    rating = matcher.get(7)?.toInt() ?: 0,
                    specs = specs
                )
            }

        val brand = patternProvider
            .getRegexParser(scope.scope, scope.main_root)
            .mapOnce(response) { matcher ->
                var catId: String? = null
                var catTitle: String? = null
                var id: String? = null
                var title: String? = null
                patternProvider
                    .getRegexParser(scope.scope, scope.main_breadcrumb)
                    .findAll(matcher.require(1)) { bcMatcher ->
                        if (bcMatcher.get(2) == null) {
                            catId = bcMatcher.require(1)
                            catTitle = bcMatcher.require(3)
                        } else {
                            id = bcMatcher.require(2)
                            title = bcMatcher.require(3)
                        }
                    }
                title = matcher.require(4)
                Brand(
                    id = requireNotNull(id) { "brand.id" },
                    title = requireNotNull(title) { "brand.title" },
                    catId = requireNotNull(catId) { "brand.catId" },
                    catTitle = requireNotNull(catTitle) { "brand.catTitle" },
                    actual = matcher.get(5)?.toInt() ?: 0,
                    all = matcher.get(6)?.toInt() ?: 0,
                    devices = devices
                )
            }

        return requireNotNull(brand) {
            "Brand not parsed"
        }
    }

    // todo refactor
    fun parseDevice(response: String, argDevId: String): Device {
        val id: String = argDevId
        var title: String? = null
        var brandId: String? = null
        var brandTitle: String? = null
        var catId: String? = null
        var catTitle: String? = null
        var rating: Int = 0
        val images = mutableListOf<Pair<String, String>>()
        val specsGroups = mutableListOf<Pair<String, List<Pair<String, String>>>>()
        patternProvider
            .getRegexParser(scope.scope, scope.device_head)
            .findOnce(response) { matcher ->
                title = matcher.require(1)

                patternProvider
                    .getRegexParser(scope.scope, scope.device_images)
                    .findAll(matcher.require(2)) {
                        images.add(Pair(it.require(2), it.require(1)))
                    }

                patternProvider
                    .getRegexParser(scope.scope, scope.device_specs_titled)
                    .findAll(matcher.require(3)) {
                        val specTitle = it.require(1).fromHtml()
                        val specs = patternProvider
                            .getRegexParser(scope.scope, scope.main_specs)
                            .map(it.require(2)) {
                                Pair(it.require(1), it.require(2))
                            }
                        specsGroups.add(Pair(specTitle, specs))
                    }
            }

        patternProvider
            .getRegexParser(scope.scope, scope.main_root)
            .findOnce(response) { matcher ->
                patternProvider
                    .getRegexParser(scope.scope, scope.main_breadcrumb)
                    .findAll(matcher.require(1)) {
                        if (it.get(2) == null) {
                            catId = it.require(1)
                            catTitle = it.require(3)
                        } else {
                            brandId = it.require(2)
                            brandTitle = it.require(3)
                        }
                    }

                rating = matcher.get(2)?.toInt() ?: 0
                title = matcher.require(4)
            }

        val comments = patternProvider
            .getRegexParser(scope.scope, scope.device_comments)
            .map(response) { matcher ->
                Device.Comment(
                    id = matcher.require(1).toInt(),
                    rating = matcher.require(3).toInt(),
                    user = User.required(
                        id = matcher.require(4).toInt(),
                        nick = matcher.require(5).fromHtml()
                    ),
                    date = matcher.require(6),
                    text = (matcher.get(9) ?: matcher.get(7))?.trim().orEmpty(),
                    likes = matcher.require(10).toInt(),
                    dislikes = matcher.require(11).toInt()
                )
            }

        val news = patternProvider
            .getRegexParser(scope.scope, scope.device_reviews)
            .map(response) { matcher ->
                Device.PostItem(
                    id = matcher.require(1).toInt(),
                    image = matcher.require(2),
                    title = matcher.require(3).fromHtml(),
                    date = matcher.require(4),
                    desc = matcher.get(5)?.fromHtml()
                )
            }

        val discussions = patternProvider
            .getRegexParser(scope.scope, scope.device_discussions)
            .mapOnce(response) {
                patternProvider
                    .getRegexParser(scope.scope, scope.device_discuss_and_firm)
                    .map(it.require(1)) { matcher ->
                        Device.PostItem(
                            id = matcher.require(1).toInt(),
                            image = null,
                            title = matcher.require(2).fromHtml(),
                            date = matcher.require(3),
                            desc = matcher.get(4)?.fromHtml()
                        )
                    }
            } ?: emptyList()

        val firmwares = patternProvider
            .getRegexParser(scope.scope, scope.device_firmwares)
            .mapOnce(response) {
                patternProvider
                    .getRegexParser(scope.scope, scope.device_discuss_and_firm)
                    .map(it.require(1)) { matcher ->
                        Device.PostItem(
                            id = matcher.require(1).toInt(),
                            image = null,
                            title = matcher.require(2).fromHtml(),
                            date = matcher.require(3),
                            desc = matcher.get(4)?.fromHtml()
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
            .getRegexParser(scope.scope, scope.main_search)
            .map(response) { matcher ->
                Brand.DeviceItem(
                    id = matcher.require(2),
                    imageSrc = matcher.require(1),
                    title = matcher.require(3).fromHtml(),
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
