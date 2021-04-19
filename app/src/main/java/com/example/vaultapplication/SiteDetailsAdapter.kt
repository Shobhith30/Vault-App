package com.example.vaultapplication

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vaultapplication.databinding.ItemSingleSiteBinding
import com.example.vaultapplication.encryption.SiteEncryption
import com.example.vaultapplication.model.Site
import java.net.MalformedURLException
import java.net.URL
import java.util.*


class SiteDetailsAdapter : RecyclerView.Adapter<SiteDetailsAdapter.SiteViewHolder>(),Filterable {

    private var siteList: MutableList<Site> = mutableListOf()
    private var siteFilterList : MutableList<Site> = mutableListOf()

    fun setSiteData(siteList: List<Site>) {
        this.siteList = siteList as MutableList<Site>
        siteFilterList = siteList.toMutableList()
        notifyDataSetChanged()
    }

    inner class SiteViewHolder(val adapterBinding: ItemSingleSiteBinding) :
        RecyclerView.ViewHolder(adapterBinding.root) {
        init {
            openSiteUrl()
            copySitePassword()
            openSiteDetailsActivity()
        }

        private fun openSiteDetailsActivity() {
           adapterBinding.root.setOnClickListener {
               val siteDetailsIntent = Intent(it.context,SiteDetailsActivity::class.java)
               val bundle = Bundle()
               bundle.putParcelable(Constants.PARCELABLE_SITE_KEY,siteList[adapterPosition])
               siteDetailsIntent.putExtras(bundle)
               it.context.startActivity(siteDetailsIntent)
           }
        }

        private fun copySitePassword() {
            adapterBinding.copyPassword.setOnClickListener {
                val clipboard: ClipboardManager? =
                    it.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText(
                    Constants.CLIPBOARD_LABEL,
                    SiteEncryption.decrypt(siteList[adapterPosition].getPassword())
                )
                clipboard?.setPrimaryClip(clip)
                adapterBinding.copyPassword.text = it.context.getString(R.string.password_copied)
                Toast.makeText(it.context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
            }

        }

        private fun openSiteUrl() {
            adapterBinding.siteUrl.setOnClickListener {
                try {
                    val urlIntent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(siteList[adapterPosition].getUrl()))
                    it.context.startActivity(urlIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        it.context,
                        "No Browser Detected or Check your URL",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiteViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val adapterBinding = ItemSingleSiteBinding.inflate(layoutInflater, parent, false)
        return SiteViewHolder(adapterBinding)
    }

    override fun onBindViewHolder(holder: SiteViewHolder, position: Int) {
        val siteValue = siteList[position]
        holder.adapterBinding.siteName.text = siteValue.getSiteName()
        holder.adapterBinding.siteUrl.text = siteValue.getUrl()
        try {
            val url = URL(siteValue.getUrl())
            val baseUrl: String = url.protocol.toString() + "://" + url.host
            val favIconUrl = "$baseUrl/favicon.ico"
            Glide.with(holder.adapterBinding.siteLogo).load(favIconUrl).into(holder.adapterBinding.siteLogo)
        } catch (e: MalformedURLException) { }

    }

    override fun getItemCount(): Int {
        Log.e("data-count",siteList.size.toString())
        return siteList.size

    }

    override fun getFilter(): Filter {
        return recentSearchFilter
    }

    private val recentSearchFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val filteredList: MutableList<Site> = mutableListOf()
            if (constraint.isEmpty()) {
                filteredList.addAll(siteFilterList)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim { it <= ' ' }
                for (item in siteFilterList) {
                    if (item.getSiteName().toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            siteList.clear()
            siteList.addAll(results.values as Collection<Site>)
            notifyDataSetChanged()
        }
    }
}