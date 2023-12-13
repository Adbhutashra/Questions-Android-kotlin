package com.example.myapplication
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @Multipart
    @POST("Question/Savequestions/")
    fun uploadFile(
        @Part("userId") userId: RequestBody,
        @Part("levelId") levelId: RequestBody,
        @Part("que") que: RequestBody,
        @Part("choice") choice: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<Void>

}