package forpdateam.ru.forpda.presentation.devdb.brands

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDevicesId
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 11.11.17.
 */
data class BrandsExtra(
    val categoryId: DevDbCategoryId?
) : QuillExtra

@InjectViewState
class BrandsPresenter(
    private val argExtra: BrandsExtra,
    private val devDbRepository: DevDbRepository,
    private val router: TabRouter,
    private val errorHandler: ErrorHandler
) : BasePresenter<BrandsView>() {

    companion object {
        val CATEGORY_PHONES = DevDbCategoryId("phones")
        val CATEGORY_PAD = DevDbCategoryId("pad")
        val CATEGORY_EBOOK = DevDbCategoryId("ebook")
        val CATEGORY_SMARTWATCH = DevDbCategoryId("smartwatch")
    }

    private val categories = arrayOf(
        CATEGORY_PHONES,
        CATEGORY_PAD,
        CATEGORY_EBOOK,
        CATEGORY_SMARTWATCH
    )
    private var currentCategory = argExtra.categoryId?.let { id ->
        categories.firstOrNull { it == id }
    } ?: categories.first()

    private var currentData: Brands? = null

    fun selectCategory(position: Int) {
        currentCategory = categories[position]
        loadBrands()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.initCategories(categories, categories.indexOf(currentCategory))
        loadBrands()
    }

    fun loadBrands() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                devDbRepository.getBrands(currentCategory)
            }.onSuccess {
                currentData = it
                viewState.showData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun openBrand(item: Brands.Item) {
        val data = currentData ?: return
        val id = DevDbDevicesId(data.id, item.id)
        router.navigateTo(Screen.DevDbDevices(devicesId = id))
    }

    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch(text = null))
    }

}
