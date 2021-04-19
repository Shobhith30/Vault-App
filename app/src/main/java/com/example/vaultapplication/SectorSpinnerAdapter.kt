package com.example.vaultapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class SectorSpinnerAdapter(context: Context,private val sectorList : List<SectorCount>) : ArrayAdapter<SectorCount>(context,0,sectorList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.sector_custom_spinner,parent,false)

        val sectorItem = sectorList[position]
        val sectorName = view.findViewById<TextView>(R.id.sector_name)
        val sectorCount = view.findViewById<TextView>(R.id.sector_count)
        sectorName.text = sectorItem.getSectorName()
        val sectorCountValue = sectorItem.getSectorCount()
        sectorCount.text = if(sectorCountValue<10) "0$sectorCountValue" else "$sectorCountValue"
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sector_spinner_dropdown,parent,false)

        val sectorItem = sectorList[position]
        val sectorName = view.findViewById<TextView>(R.id.sector_name)
        val sectorCount = view.findViewById<TextView>(R.id.sector_count)
        sectorName.text = sectorItem.getSectorName()
        val sectorCountValue = sectorItem.getSectorCount()
        sectorCount.text = if(sectorCountValue<10) "0$sectorCountValue" else "$sectorCountValue"
        return view
    }

}