package com.example.foodfit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface USDAApiService {

    @GET("foods/search")
    Call<FoodSearchResponse> searchFoods(
            @Query("query") String query,
            @Query("api_key") String apiKey
    );
}
