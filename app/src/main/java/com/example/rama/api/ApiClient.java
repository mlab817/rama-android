package com.example.rama.api;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;
public class ApiClient {
    private static final String BASE_URL = "https://edsabuswaymonitoring.online/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {

        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
