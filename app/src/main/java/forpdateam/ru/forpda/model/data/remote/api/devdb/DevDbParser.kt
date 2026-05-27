package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.coretypes.DevDbBrandId
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbCommentId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.coretypes.UserId
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
                            id = DevDbBrandId(itemsMatcher.require(1)),
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
                requireNotNull(catId) { "brands.catId" }
                requireNotNull(catTitle) { "brands.catTitle" }
                Brands(
                    id = DevDbCategoryId(catId),
                    title = catTitle,
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
                        Device.Spec(it.require(1), it.require(2))
                    }
                Brand.DeviceItem(
                    imageSrc = matcher.get(1),
                    id = DevDbDeviceId(matcher.require(2)),
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
                requireNotNull(id) { "brand.id" }
                requireNotNull(title) { "brand.title" }
                requireNotNull(catId) { "brand.catId" }
                requireNotNull(catTitle) { "brand.catTitle" }
                Brand(
                    id = DevDbDevicesId(DevDbCategoryId(catId), DevDbBrandId(id)),
                    title = title,
                    catTitle = catTitle,
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
    fun parseDevice(response: String, argDevId: DevDbDeviceId): Device {
        val id: DevDbDeviceId = argDevId
        var title: String? = null
        var brandId: String? = null
        var brandTitle: String? = null
        var catId: String? = null
        var catTitle: String? = null
        var rating: Int = 0
        val images = mutableListOf<Device.Image>()
        val specsGroups = mutableListOf<Device.Specs>()
        patternProvider
            .getRegexParser(scope.scope, scope.device_head)
            .findOnce(response) { matcher ->
                title = matcher.require(1)

                patternProvider
                    .getRegexParser(scope.scope, scope.device_images)
                    .map(matcher.require(2)) {
                        Device.Image(it.require(2), it.require(1))
                    }
                    .also { images.addAll(it) }

                patternProvider
                    .getRegexParser(scope.scope, scope.device_specs_titled)
                    .map(matcher.require(3)) {
                        val specTitle = it.require(1).fromHtml()
                        val specs = patternProvider
                            .getRegexParser(scope.scope, scope.main_specs)
                            .map(it.require(2)) {
                                Device.Spec(it.require(1), it.require(2))
                            }
                        Device.Specs(specTitle, specs)
                    }
                    .also { specsGroups.addAll(it) }
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
                    id = DevDbCommentId(matcher.require(1).toInt()),
                    rating = matcher.require(3).toInt(),
                    user = User(
                        id = UserId(matcher.require(4).toInt()),
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
                Device.Article(
                    id = ArticleId(matcher.require(1).toInt()),
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
                        Device.Topic(
                            id = TopicId(matcher.require(1).toInt()),
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
                        Device.Topic(
                            id = TopicId(matcher.require(1).toInt()),
                            title = matcher.require(2).fromHtml(),
                            date = matcher.require(3),
                            desc = matcher.get(4)?.fromHtml()
                        )
                    }
            } ?: emptyList()
        requireNotNull(title) { "device.title" }
        requireNotNull(brandId) { "device.brandId" }
        requireNotNull(brandTitle) { "device.brandTitle" }
        requireNotNull(catId) { "device.catId" }
        requireNotNull(catTitle) { "device.catTitle" }
        return Device(
            id = id,
            devicesId = DevDbDevicesId(DevDbCategoryId(catId), DevDbBrandId(brandId)),
            title = title,
            brandTitle = brandTitle,
            catTitle = catTitle,
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
                    id = DevDbDeviceId(matcher.require(2)),
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
