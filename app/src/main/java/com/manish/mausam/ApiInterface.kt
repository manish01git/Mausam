package com.manish.mausam


import com.manish.mausam.modals.WeatherApp
import retrofit2.Call

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

//    @POST("weather")
//    fun postWeather(
//        @Field("q") city:String,
//        @Field("aapid") aapId:String,
//        @Field("units") units:String
//    ):Call<WeatherApp>
    @GET("weather")
    fun getWeatherData(
        @Query("q") city:String,
        @Query("appid") aapid:String,
        @Query("units") units:String
    ) :Call<WeatherApp>

}