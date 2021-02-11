package dev.hakob.weatherapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.hakob.weatherapp.data.WeatherDao
import dev.hakob.weatherapp.db.WeatherDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    @AppScope
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Singleton
    @Provides
    fun db(@ApplicationContext context: Context): WeatherDb {
        return Room.databaseBuilder(context, WeatherDb::class.java, "weather.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun weatherDao(db: WeatherDb): WeatherDao {
        return db.weatherDao()
    }
}