package forpdateam.ru.forpda.ui.fragments.devdb.brands

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

import java.util.ArrayList

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsPresenter
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseSectionedAdapter

/**
 * Created by radiationx on 08.08.17.
 */

class BrandsFragment : RecyclerFragment(), BrandsView, BaseSectionedAdapter.OnItemClickListener<Brands.Item> {

    private lateinit var adapter: BrandsAdapter

    @InjectPresenter
    lateinit var presenter: BrandsPresenter

    @ProvidePresenter
    internal fun providePresenter(): BrandsPresenter = BrandsPresenter(
            App.get().Di().devDbRepository,
            App.get().Di().router,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_brands)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            getString(ARG_CATEGORY_ID)?.also {
                presenter.initCategory(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        refreshLayout.setOnRefreshListener { presenter.loadBrands() }
        titlesWrapper.visibility = View.GONE
        toolbarSpinner.visibility = View.VISIBLE
        setScrollFlagsEnterAlways()

        adapter = BrandsAdapter()
        recyclerView.adapter = adapter


        toolbarSpinner.prompt = "Category"
        toolbarSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                presenter.selectCategory(position)
                presenter.loadBrands()
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

        adapter.setOnItemClickListener(this)
    }

    override fun isShadowVisible(): Boolean {
        return true
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.fragment_title_device_search)
                .setIcon(R.drawable.ic_toolbar_search)
                .setOnMenuItemClickListener {
                    presenter.openSearch()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun initCategories(categories: Array<String>, position: Int) {
        val spinnerTitles = categories.map { getCategoryTitle(it) }
        val spinnerAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_item, spinnerTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toolbarSpinner.adapter = spinnerAdapter
        toolbarSpinner.setSelection(position)
    }

    override fun showData(data: Brands) {
        setTitle(data.catTitle)
        adapter.clear()
        for ((key, value) in data.letterMap) {
            adapter.addSection(key, value)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(item: Brands.Item) {
        presenter.openBrand(item)
    }

    override fun onItemLongClick(item: Brands.Item): Boolean {
        return false
    }

    private fun getCategoryTitle(category: String): String? {
        when (category) {
            BrandsPresenter.CATEGORY_PHONES -> return App.get().getString(R.string.brands_category_phones)
            BrandsPresenter.CATEGORY_PAD -> return App.get().getString(R.string.brands_category_tabs)
            BrandsPresenter.CATEGORY_EBOOK -> return App.get().getString(R.string.brands_category_ebook)
            BrandsPresenter.CATEGORY_SMARTWATCH -> return App.get().getString(R.string.brands_category_smartwatch)
        }
        return null
    }

    companion object {
        const val ARG_CATEGORY_ID = "CATEGORY_ID"
    }
}
