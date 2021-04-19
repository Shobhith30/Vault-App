package com.example.vaultapplication


data class SectorCount(
    private val sectorName : String,
    private val sectorCount : Int) {

    fun getSectorName() = sectorName
    fun getSectorCount() = sectorCount
}
