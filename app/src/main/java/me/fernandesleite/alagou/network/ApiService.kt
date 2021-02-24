package me.fernandesleite.alagou.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import me.fernandesleite.alagou.models.Flooding
import me.fernandesleite.alagou.models.FloodingPost
import me.fernandesleite.alagou.models.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://alagou-api.herokuapp.com/"

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

interface ApiService {
    @GET("floodings")
    fun getFloodings(@Query("minlat") minLat: Double,
                     @Query("maxlat") maxLat: Double,
                     @Query("minlng") minLng: Double,
                     @Query("maxlng") maxLng: Double): Call<List<Flooding>>

    @POST("floodings")
    fun createFlooding(@Body flooding: FloodingPost): Call<FloodingPost>

    @GET("floodings/{id}")
    fun getFlooding(@Path("id") id: String): Call<Flooding>

    @POST("user")
    fun createUser(@Body user: User): Call<User>
}

object Api {
    val retrofitService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}