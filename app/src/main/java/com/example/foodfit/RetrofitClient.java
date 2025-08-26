    package com.example.foodfit;

    import retrofit2.Retrofit;
    import retrofit2.converter.gson.GsonConverterFactory;

    public class RetrofitClient {

        // 🔹 USDA API
        private static final String USDA_BASE_URL = "https://api.nal.usda.gov/fdc/v1/";
        private static Retrofit usdaRetrofit = null;

        // 🔹 Gemini Backend API
        private static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"; // 🔁 Replace with actual backend URL
        private static Retrofit geminiRetrofit = null;

        public static USDAApiService getUSDAService() {
            if (usdaRetrofit == null) {
                usdaRetrofit = new Retrofit.Builder()
                        .baseUrl(USDA_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return usdaRetrofit.create(USDAApiService.class);
        }

        public static GeminiApi getGeminiService() {
            if (geminiRetrofit == null) {
                geminiRetrofit = new Retrofit.Builder()
                        .baseUrl(GEMINI_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
            }
            return geminiRetrofit.create(GeminiApi.class);
        }
    }