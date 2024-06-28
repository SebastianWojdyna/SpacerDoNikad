package com.example.spacerdonikad.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {
    private static final String BASE_URL = "https://your.api.base.url/";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiEndpoint getApiEndpoint() {
        return retrofit.create(ApiEndpoint.class);
    }
}
