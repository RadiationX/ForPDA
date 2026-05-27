package forpdateam.ru.forpda.ui.fragments.devdb.brands

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.extensions.getExtra
import forpdateam.ru.forpda.extensions.putExtra
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsExtra
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsPresenter
import forpdateam.ru.forpda.presentation.devdb.brands.BrandsView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import ru.radiationx.coretypes.DevDbCategoryId

/**
 * Created by radiationx on 08.08.17.
 */

class BrandsFragment : RecyclerFragment(), BrandsView,
    OnItemClickListener<Brands.Item> {

    companion object {
        private const val ARG_ID = "arg_id"
        fun newInstance(categoryId: DevDbCategoryId?) = BrandsFragment().putExtra {
            putParcelable(ARG_ID, categoryId)
        }
    }

    private lateinit var adapter: BrandsAdapter

    private val presenter by quillMoxyPresenter<BrandsPresenter> {
        BrandsExtra(getExtra(ARG_ID))
    }

    init {
        configuration.defaultTitle = getString(R.string.fragment_title_brands)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        refreshLayout.setOnRefreshListener { presenter.loadBrands() }
        titlesWrapper.visibility = View.GONE
        toolbarSpinner.visibility = View.VISIBLE
        setScrollFlagsEnterAlways()

        adapter = BrandsAdapter(this)
        recyclerView.adapter = adapter


        toolbarSpinner.prompt = "Category"
        toolbarSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                presenter.selectCategory(position)
            }

            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }
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

    override fun initCategories(categories: Array<DevDbCategoryId>, position: Int) {
        val spinnerTitles = categories.map { getCategoryTitle(it) }
        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerTitles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        toolbarSpinner.adapter = spinnerAdapter
        toolbarSpinner.setSelection(position)
    }

    override fun showData(data: Brands) {
        setTitle(data.title)
        adapter.bindItems(data)
    }

    override fun onItemClick(item: Brands.Item) {
        presenter.openBrand(item)
    }

    override fun onItemLongClick(item: Brands.Item): Boolean {
        return false
    }

    private fun getCategoryTitle(category: DevDbCategoryId): String? {
        return when (category) {
            BrandsPresenter.CATEGORY_PHONES -> getString(R.string.brands_category_phones)
            BrandsPresenter.CATEGORY_PAD -> getString(R.string.brands_category_tabs)
            BrandsPresenter.CATEGORY_EBOOK -> getString(R.string.brands_category_ebook)
            BrandsPresenter.CATEGORY_SMARTWATCH -> getString(R.string.brands_category_smartwatch)
            else -> null
        }
    }
}
