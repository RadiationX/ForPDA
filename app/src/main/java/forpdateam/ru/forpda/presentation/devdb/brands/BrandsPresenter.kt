package forpdateam.ru.forpda.presentation.devdb.brands

import moxy.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class BrandsPresenter(
        private val devDbRepository: DevDbRepository,
        private val router: TabRouter,
        private val errorHandler: IErrorHandler
) : BasePresenter<BrandsView>() {

    companion object {
        const val CATEGORY_PHONES = "phones"
        const val CATEGORY_PAD = "pad"
        const val CATEGORY_EBOOK = "ebook"
        const val CATEGORY_SMARTWATCH = "smartwatch"
    }

    private val categories = arrayOf(
            CATEGORY_PHONES,
            CATEGORY_PAD,
            CATEGORY_EBOOK,
            CATEGORY_SMARTWATCH
    )
    private var currentCategory = categories[0]
    private var currentData: Brands? = null

    fun initCategory(categoryId: String) {
        categories.firstOrNull { it == categoryId }?.let {
            currentCategory = it
        }
    }

    fun selectCategory(position: Int) {
        currentCategory = categories[position]
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.initCategories(categories, categories.indexOf(currentCategory))
        loadBrands()
    }

    fun loadBrands() {
        devDbRepository
                .getBrands(currentCategory)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun openBrand(item: Brands.Item) {
        currentData?.let {
            router.navigateTo(Screen.DevDbDevices().apply {
                categoryId = it.catId
                brandId = item.id
            })
        }
    }

    fun openSearch() {
        router.navigateTo(Screen.DevDbSearch())
    }

}
