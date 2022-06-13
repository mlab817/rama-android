package com.example.rama.api;
import com.example.rama.model.login.Login;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface ApiInterface {
    @GET("androidlogin")
    Call<Login> loginResponse(
            @Query("username") String username,
            @Query("password") String password
    );

    @GET("androidlogin")
    Call<Login> loginQRResponse(
            @Query("scannedQRValue") String scannedQRValue
    );
}

