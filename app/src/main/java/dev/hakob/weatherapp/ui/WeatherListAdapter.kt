package dev.hakob.weatherapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.hakob.weatherapp.R
import dev.hakob.weatherapp.data.entity.UserWeatherEntity
import dev.hakob.weatherapp.utils.toCelsius

class WeatherListAdapter(
    private val clickHandler: (UserWeatherEntity) -> Unit,
    private val longClickHandler: (position: Int) -> Unit
) : ListAdapter<UserWeatherEntity, WeatherViewHolder>(WeatherDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        return WeatherViewHolder(view).apply {
            view.setOnClickListener {
                clickHandler(getItem(adapterPosition))
            }
            view.setOnLongClickListener {
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

class WeatherDiffUtil : DiffUtil.ItemCallback<UserWeatherEntity>() {
    override fun areItemsTheSame(oldItem: UserWeatherEntity,
                                 newItem: UserWeatherEntity) = oldItem.cityId == newItem.cityId

    override fun areContentsTheSame(oldItem: UserWeatherEntity,
                                    newItem: UserWeatherEntity): Boolean {
        // don't compare sort order as touch helper already does animations.
        return oldItem == newItem
    }
}

class WeatherViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val temperatureView = view.findViewById<TextView>(R.id.temperature)
    private val cityNameView = view.findViewById<TextView>(R.id.cityName)

    fun bind(userWeatherEntity: UserWeatherEntity) {
        temperatureView.text = userWeatherEntity.temperature.temp?.toCelsius()
        cityNameView.text = userWeatherEntity.cityName
    }
}