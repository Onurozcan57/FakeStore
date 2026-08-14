package com.example.apicalling.data.model

import com.google.gson.annotations.SerializedName

/**
 * Kullanıcı teslimat adresi modeli.
 */
data class AddressDto(
    @SerializedName("id") val id: String = java.util.UUID.randomUUID().toString(),
    @SerializedName("title") val title: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("district") val district: String = "",
    @SerializedName("neighborhood") val neighborhood: String = "",
    @SerializedName("street") val street: String = "",
    @SerializedName("building") val building: String = "",
    @SerializedName("floor") val floor: String = "",
    @SerializedName("apartmentNo") val apartmentNo: String = ""
)
