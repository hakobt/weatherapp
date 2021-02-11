package dev.hakob.weatherapp.data.response

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

@Keep
data class Temperature(

	@field:SerializedName("temp")
	@ColumnInfo(name = "temp")
	val temp: Double? = null,

	@field:SerializedName("temp_min")
	@ColumnInfo(name = "temp_min")
	val tempMin: Double? = null,

	@field:SerializedName("temp_max")
	@ColumnInfo(name = "temp_max")
	val tempMax: Double? = null
)