package dev.hakob.weatherapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.hakob.weatherapp.R
import dev.hakob.weatherapp.data.entity.CityWeather
import dev.hakob.weatherapp.databinding.WeatherItemBinding
import dev.hakob.weatherapp.utils.toCelsius

class CityWeatherListAdapter(
    private val clickHandler: (CityWeather) -> Unit,
    private val longClickHandler: (position: Int) -> Unit
) : ListAdapter<CityWeather, WeatherViewHolder>(WeatherDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = WeatherItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherViewHolder(binding).apply {
            binding.root.setOnClickListener {
                clickHandler(getItem(adapterPosition))
            }
            binding.root.setOnLongClickListener {
                longClickHandler(adapterPosition)
                return@setOnLongClickListener true
            }
        }
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)
    }
}

class WeatherViewHolder(private val binding: WeatherItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(cityWeather: CityWeather) {
        binding.temperature.text = cityWeather.temperature.temp?.toCelsius()
        binding.cityName.text = cityWeather.cityName
    }
}

class WeatherDiffUtil : DiffUtil.ItemCallback<CityWeather>() {
    override fun areItemsTheSame(
        oldItem: CityWeather,
        newItem: CityWeather
    ) = oldItem.cityId == newItem.cityId

    override fun areContentsTheSame(
        oldItem: CityWeather,
        newItem: CityWeather
    ): Boolean {
        return oldItem == newItem
    }
}