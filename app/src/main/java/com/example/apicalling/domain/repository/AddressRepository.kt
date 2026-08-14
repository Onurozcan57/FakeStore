package com.example.apicalling.domain.repository

import com.example.apicalling.data.model.AddressDto
import kotlinx.coroutines.flow.StateFlow

interface AddressRepository {
    val addresses: StateFlow<List<AddressDto>>
    fun addAddress(address: AddressDto)
    fun getAddresses(): List<AddressDto>
}
