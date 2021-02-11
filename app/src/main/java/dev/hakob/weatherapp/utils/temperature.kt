package dev.hakob.weatherapp.utils

fun Double.toCelsius(): String = "${(this - 273.15).toInt()}°"