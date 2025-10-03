package com.example.foodfit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {


    private static final String USDA_BASE_URL = "https://api.nal.usda.gov/fdc/v1/";
    private static Retrofit usdaRetrofit = null;

    public static USDAApiService getUSDAService() {
        if (usdaRetrofit == null) {
            usdaRetrofit = new Retrofit.Builder()
                    .baseUrl(USDA_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return usdaRetrofit.create(USDAApiService.class);
    }
}
