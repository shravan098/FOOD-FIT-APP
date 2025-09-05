package com.example.foodfit;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApi {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    Call<GeminiRawResponse> analyzeFood(
            @Query("key") String apiKey,   // 👈 API key query param
            @Body GeminiFoodRequest request
    );
}
