package com.example.spacerdonikad.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiEndpoint {
    // Przykład endpointu
    @GET("someEndpoint")
    Call<SomeResponse> getSomeData(@Query("param") String param);
}
