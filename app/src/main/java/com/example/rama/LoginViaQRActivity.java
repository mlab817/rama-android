package com.example.rama;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rama.api.ApiClient;
import com.example.rama.api.ApiInterface;
import com.example.rama.model.login.Login;
import com.example.rama.model.login.LoginData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;

public class LoginViaQRActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etUsername, etPassword;
    Button btnLogin,btnBack;
    String Username, Password;
    TextView tvRegister;
    ApiInterface apiInterface;
    SessionManager sessionManager;

    /*For Barcode Scanner*/
    private static final int CAMERA_PERMISSION_CODE=101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_via_qractivity);

        /*Login using QR*/
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        /*For Barcode Scanner*/

        String data_in_code="Hello Bar Code Data";
        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=multiFormatWriter.encode(data_in_code, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
            Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //now let's create barcode scanner

        Button scan_code=findViewById(R.id.button_scan);
        scan_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission(Manifest.permission.CAMERA)){
                        openScanner();
                    }
                    else{
                        requestPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
                    }
                }
                else{
                    openScanner();
                }
            }
        });
    }

    /*For Login*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:
                Username = etUsername.getText().toString();
                Password = etPassword.getText().toString();
                login(Username,Password);
                break;

            case R.id.btnBack:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                break;
        }
    }

    //Barcode Codes Starts Here
    private void openScanner() {
        new IntentIntegrator(LoginViaQRActivity.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null){
            if(result.getContents()==null){
                Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
            }
            else{
                etUsername.setText(result.getContents());
            }
        }
        else{
            Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission(String permission){
        int result= ContextCompat.checkSelfPermission(LoginViaQRActivity.this,permission);
        if(result== PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{
            return false;
        }
    }

    private void requestPermission(String permision,int code){
        if(ActivityCompat.shouldShowRequestPermissionRationale(LoginViaQRActivity.this,permision)){

        }
        else{
            ActivityCompat.requestPermissions(LoginViaQRActivity.this,new String[]{permision},code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openScanner();
                }
        }
    }

    /*Login Using QR*/
    private void login(String username, String password) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<Login> loginCall = apiInterface.loginResponse(username,password);
        loginCall.enqueue(new Callback<Login>() {
            @Override
            public void onResponse(Call<Login> call, Response<Login> response) {
                if(response.body() != null && response.isSuccessful() && response.body().isStatus()){

                    // Ini untuk menyimpan sesi
                    sessionManager = new SessionManager(LoginViaQRActivity.this);
                    LoginData loginData = response.body().getLoginData();
                    sessionManager.createLoginSession(loginData);

                    //Ini untuk pindah
                    Toast.makeText(LoginViaQRActivity.this, response.body().getLoginData().getName(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginViaQRActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginViaQRActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Login> call, Throwable t) {
                Toast.makeText(LoginViaQRActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}