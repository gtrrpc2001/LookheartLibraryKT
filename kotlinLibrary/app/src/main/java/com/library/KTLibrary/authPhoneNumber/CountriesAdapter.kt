package com.library.KTLibrary.authPhoneNumber

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.library.KTLibrary.R
import java.util.Locale

data class CountryInfo(val icon: Drawable?, val name: String, val code: Int)

class CountriesAdapter(private val context: Context, private val countriesList: List<CountryInfo?>, private val listener: OnCountryClickListener) :

    RecyclerView.Adapter<CountriesAdapter.CountryViewHolder>() {

    class CountryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val countryIconImageView: ImageView = view.findViewById(R.id.countryIcon)
        val countryNameTextView: TextView = view.findViewById(R.id.countryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.auth_phone_country_item, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
//        val country = countriesList[position]

//        if (country.icon != null && country.name.isNotEmpty()) {
//            val countryName = getCountryName(country.name)
//
//            holder.countryIconImageView.setImageDrawable(country.icon)
//            holder.countryNameTextView.text = "$countryName (${country.code})"
//
//            // 클릭 리스너
//            holder.itemView.setOnClickListener {
//                listener.onCountryClick(country.name, country.code)
//            }
//        }

        countriesList[position]?.let {
            if (it.icon != null && it.name.isNotEmpty()) {
                val countryName = getCountryName(it.name)

                holder.countryIconImageView.setImageDrawable(it.icon)
                holder.countryNameTextView.text = context.getString(R.string.country_code, countryName, it.code)

                // 클릭 리스너
                holder.itemView.setOnClickListener { _ ->
                    listener.onCountryClick(it.name, it.code)
                }
            }
        }
    }

    private fun getCountryName(countryCode: String): String {
        return Locale("", countryCode).displayCountry
    }

    override fun getItemCount() = countriesList.size
}