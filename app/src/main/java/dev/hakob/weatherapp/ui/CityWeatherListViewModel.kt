package dev.hakob.weatherapp.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hakob.weatherapp.core.Resource
import dev.hakob.weatherapp.core.SingleEvent
import dev.hakob.weatherapp.core.Status
import dev.hakob.weatherapp.data.WeatherRepository
import dev.hakob.weatherapp.data.entity.UserWeatherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CityWeatherListViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _events = MutableLiveData<SingleEvent<Event>>()
    val events: LiveData<SingleEvent<Event>> = _events

    private val page = MutableStateFlow(1)

    private var noMoreItems = false

    val cityList: LiveData<Resource<List<UserWeatherEntity>>> =
        page.flatMapLatest {
            Timber.d("$it")
            repository.getWeatherList(it)
        }.onEach {
            val size = it.data?.size
            if (size == null) {
                noMoreItems = true
                return@onEach
            }
            noMoreItems = (page.value * 10) > size
        }.asLiveData()

    fun onEndReached() {
        if (noMoreItems) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val nextPage = page.value + 1
            Timber.d("Next page: $nextPage")
            page.emit(nextPage)
        }
    }

    fun removeCity(city: UserWeatherEntity) {
        repository.removeCity(city)
    }

    fun addCity(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (repository.addCityWithName(name)) {
                Status.Success -> {
                    noMoreItems = false
                    _events.postValue(SingleEvent(Event.AddCity(true, name)))
                }
                Status.Fail -> {
                    _events.postValue(SingleEvent(Event.AddCity(false, name)))
                }
                Status.Loading -> Unit
            }
        }
    }

    fun onItemMoved(item: UserWeatherEntity?, fromPos: Int, toPos: Int) {
        if (item == null) {
            return
        }
        repository.updateCitySortOrder(item, fromPos, toPos)
    }

    sealed class Event {
        class AddCity(success: Boolean, name: String) : Event()
    }
}