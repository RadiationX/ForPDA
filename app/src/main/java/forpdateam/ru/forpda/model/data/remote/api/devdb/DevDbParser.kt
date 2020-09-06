package forpdateam.ru.forpda.model.data.remote.api.devdb

import android.util.Pair
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class DevDbParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.DevDb

    fun parseBrands(response: String): Brands = Brands().also { data ->
        patternProvider
                .getPattern(scope.scope, scope.brands_letters)
                .matcher(response)
                .findAll { matcher ->
                    val letter = matcher.group(1)
                    val items = patternProvider
                            .getPattern(scope.scope, scope.brands_items_in_letter)
                            .matcher(matcher.group(2))
                            .map { itemsMatcher ->
                                Brands.Item().apply {
                                    id = itemsMatcher.group(1)
                                    title = itemsMatcher.group(2).fromHtml()
                                    count = itemsMatcher.group(3).toInt()
                                }
                            }
                    data.letterMap[letter] = items
                }

        patternProvider
                .getPattern(scope.scope, scope.main_root)
                .matcher(response)
                .findOnce { matcher ->
                    patternProvider
                            .getPattern(scope.scope, scope.main_breadcrumb)
                            .matcher(matcher.group(1))
                            .findAll { bcMatcher ->
                                if (bcMatcher.group(2) == null) {
                                    data.catId = bcMatcher.group(1)
                                    data.catTitle = bcMatcher.group(3)
                                }
                            }
                    data.actual = matcher.group(5).toInt()
                    data.all = matcher.group(6).toInt()
                }
        return data
    }

    fun parseBrand(response: String): Brand = Brand().also { data ->
        val list = patternProvider
                .getPattern(scope.scope, scope.brand_devices)
                .matcher(response)
                .map { matcher ->
                    Brand.DeviceItem().apply {
                        imageSrc = matcher.group(1)
                        id = matcher.group(2)
                        title = matcher.group(3).fromHtml()

                        patternProvider
                                .getPattern(scope.scope, scope.main_specs)
                                .matcher(matcher.group(4))
                                .findOnce {
                                    specs.add(Pair(it.group(1), it.group(2)))
                                }

                        matcher.group(5)?.also {
                            price = it
                        }
                        matcher.group(7)?.also {
                            rating = it.toInt()
                        }
                    }
                }
        data.devices.addAll(list)

        patternProvider
                .getPattern(scope.scope, scope.main_root)
                .matcher(response)
                .findOnce { matcher ->
                    patternProvider
                            .getPattern(scope.scope, scope.main_breadcrumb)
                            .matcher(matcher.group(1))
                            .findAll { bcMatcher ->
                                if (bcMatcher.group(2) == null) {
                                    data.catId = bcMatcher.group(1)
                                    data.catTitle = bcMatcher.group(3)
                                } else {
                                    data.id = bcMatcher.group(2)
                                    data.title = bcMatcher.group(3)
                                }
                            }
                    data.title = matcher.group(4)
                    data.actual = matcher.group(5).toInt()
                    data.all = matcher.group(6).toInt()
                }
        return data
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

    fun parseSearch(response: String): Brand = Brand().also { data ->
        val devices = patternProvider
                .getPattern(scope.scope, scope.main_search)
                .matcher(response)
                .map { matcher ->
                    Brand.DeviceItem().apply {
                        imageSrc = matcher.group(1)
                        id = matcher.group(2)
                        title = matcher.group(3).fromHtml()
                    }
                }

        data.devices.addAll(devices)
        data.all = data.devices.size
        data.actual = data.all
    }
}
