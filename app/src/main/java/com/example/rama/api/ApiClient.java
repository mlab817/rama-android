package com.example.rama.api;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Retrofit;
public class ApiClient {
//    private static final String BASE_URL = "http://192.168.31.205/rama-main/public/assets/androidStudio/";
    private static final String BASE_URL = "http://192.168.31.205/rama-main/public/";
//    private static final String BASE_URL = "https://edsabuswaymonitoring.online/assets/androidStudio/";

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
