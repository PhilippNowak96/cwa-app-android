package de.rki.coronawarnapp.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ui.Country
import kotlinx.android.synthetic.main.view_country_list_entry_flag_container.view.*
import java.text.Collator

class CountryList(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {
    private var attributeSet = attrs
    private var view: View? = null
    private var _list: List<Country>? = null
    var list: List<Country>?
        get() = _list
        set(value) {
            _list = value
            buildAllLists()
        }

    init {
        orientation = HORIZONTAL
    }

    /**
     * Cleans the view and rebuilds the list of countries. Presets already selected countries
     */
    private fun buildAllLists() {
        list
            ?.map { country ->
                context.getString(country.labelRes) to country.iconRes
            }
            ?.sortedWith { a, b ->
                Collator.getInstance().compare(a.first, b.first)
            }

        view = inflate(context, R.layout.view_country_list_entry_flag_container, this)
        val flagNumberOfColumns = 8
        var adapterCountryFlags = CountryAdapterFlags(context, _list)
        context.withStyledAttributes(attributeSet, R.styleable.SimpleStepEntry) {

            flagGrid.layoutManager = GridLayoutManager(context, flagNumberOfColumns)
            flagGrid.adapter = adapterCountryFlags

            var countryNames = ""
            val iterator: Iterator<Country> = list!!.iterator()
            while (iterator.hasNext()) {
                countryNames += resources.getString(iterator.next().labelRes)
                if (iterator.hasNext()) {
                    countryNames += ", "
                }
            }
            country_list_entry_label.text = countryNames
        }
    }
}

/**
 * Adapter for the country flags grid
 */
class CountryAdapterFlags internal constructor(
    context: Context?,
    countryList: List<Country>?
) :
    RecyclerView.Adapter<CountryAdapterFlags.ViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val countryList: List<Country>? = countryList

    // inflates the cell layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater.inflate(R.layout.view_country_list_entry_flag_item, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each cell
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val country = this.countryList!![position]
        holder.flagImage.setImageResource(country.iconRes)
    }

    // total number of cells
    override fun getItemCount(): Int {
        return countryList!!.size
    }

    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var flagImage: ImageView = itemView.findViewById(R.id.country_list_entry_image)
    }
}
