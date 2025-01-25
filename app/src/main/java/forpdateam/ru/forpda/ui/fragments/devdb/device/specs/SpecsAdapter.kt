package forpdateam.ru.forpda.ui.fragments.devdb.device.specs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import forpdateam.ru.forpda.App.Companion.getColorFromAttr
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.coloredFromHtml

/**
 * Created by radiationx on 08.08.17.
 */
class SpecsAdapter : RecyclerView.Adapter<SpecsAdapter.ViewHolder>() {
    private val list = ArrayList<Pair<String, List<Pair<String, String>>>>()


    @JvmOverloads
    fun addAll(
        results: Collection<Pair<String, List<Pair<String, String>>>>,
        clearList: Boolean = true
    ) {
        if (clearList) clear()
        list.addAll(results)
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.device_spec_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.title.text = item.first
        val builder = StringBuilder()

        for (i in item.second.indices) {
            val pair = item.second[i]
            val strColor = String.format(
                "#%06X",
                0xFFFFFF and getColorFromAttr(holder.itemView.context, R.attr.second_text_color)
            )
            builder.append("<small style=\"font-size:10px\"><span style=\"color: ").append(strColor)
                .append("\">").append(pair.first).append("</span></small><br>").append(pair.second)
            if (i + 1 < item.second.size) {
                builder.append("<br><br>")
            }
        }

        holder.desc.text = coloredFromHtml(builder.toString())

        /*holder.price.setVisibility(item.getPrice() == null ? View.GONE : View.VISIBLE);
        if (item.getPrice() != null) {
            holder.price.setText(item.getPrice());
        }*/
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun getItem(position: Int): Pair<String, List<Pair<String, String>>> {
        return list[position]
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var title: TextView = v.findViewById(R.id.item_title)
        var desc: TextView = v.findViewById(R.id.item_desc)
    }
}
