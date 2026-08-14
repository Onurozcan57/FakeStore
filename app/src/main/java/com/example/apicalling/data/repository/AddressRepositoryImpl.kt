package com.example.apicalling.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.apicalling.data.model.AddressDto
import com.example.apicalling.domain.repository.AddressRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : AddressRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("address_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _addresses = MutableStateFlow<List<AddressDto>>(loadAddressesFromDisk())
    override val addresses: StateFlow<List<AddressDto>> = _addresses.asStateFlow()

    override fun addAddress(address: AddressDto) {
        val currentList = _addresses.value.toMutableList()
        currentList.add(address)
        _addresses.value = currentList
        saveAddressesToDisk(currentList)
    }

    override fun getAddresses(): List<AddressDto> = _addresses.value

    private fun saveAddressesToDisk(list: List<AddressDto>) {
        val json = gson.toJson(list)
        prefs.edit().putString("saved_addresses", json).apply()
    }

    private fun loadAddressesFromDisk(): List<AddressDto> {
        val json = prefs.getString("saved_addresses", null)
        return if (json != null) {
            val type = object : TypeToken<List<AddressDto>>() {}.type
            gson.fromJson(json, type)
        } else emptyList()
    }
}
