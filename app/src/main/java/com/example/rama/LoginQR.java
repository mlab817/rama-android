package com.example.rama;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.rama.api.ApiClient;
import com.example.rama.api.ApiInterface;
import com.example.rama.model.login.Login;
import com.example.rama.model.login.LoginData;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginQR extends AppCompatActivity {
    ApiInterface apiInterface;
    SessionManager sessionManager;
    private static final int CAMERA_PERMISSION_CODE = 101;
    String scannedQRValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission(Manifest.permission.CAMERA)) {
                openScanner();
            } else {
                requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            }
        } else {
            openScanner();
        }
    }

    private void openScanner() {
        new IntentIntegrator(LoginQR.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                scannedQRValue = result.getContents();
                loginQR(scannedQRValue);
            }
        } else {
            Toast.makeText(this, "Scan cancelled!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(LoginQR.this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String permision, int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(LoginQR.this, permision)) {

        } else {
            ActivityCompat.requestPermissions(LoginQR.this, new String[]{permision}, code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openScanner();
                }
        }
    }

    private void loginQR(String scannedQRValue) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Login> loginCall = apiInterface.loginQRResponse(scannedQRValue);
        loginCall.enqueue(new Callback<Login>() {
            @Override
            public void onResponse(Call<Login> call, Response<Login> response) {
                if(response.body() != null && response.isSuccessful() && response.body().isStatus()){
                    Toast.makeText(LoginQR.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    sessionManager = new SessionManager(LoginQR.this);
                    LoginData loginData = response.body().getLoginData();
                    sessionManager.createLoginSession(loginData);

                    Intent intent = new Intent(LoginQR.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if(response.body().getMessage() == null)
                    {
                        Toast.makeText(LoginQR.this, "Database Error", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginQR.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginQR.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
            @Override
            public void onFailure(Call<Login> call, Throwable t) {
                Toast.makeText(LoginQR.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}