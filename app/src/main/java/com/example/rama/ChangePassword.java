package com.example.rama;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {
    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_CHANGE_PASSWORD = "https://edsabuswaymonitoring.online/assets/androidStudio/changePassword.php/";

    Button btnSubmit, btnCancel;
    EditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    SessionManager sessionManager;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        sessionManager = new SessionManager(ChangePassword.this);

        userid = sessionManager.getUserDetail().get(SessionManager.USERID);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        etCurrentPassword = (EditText) findViewById(R.id.etCurrentPassword);
        etNewPassword = (EditText) findViewById(R.id.etNewPassword);
        etConfirmNewPassword = (EditText) findViewById(R.id.etConfirmNewPassword);

        int length = 25;
        etNewPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(length)});
        etConfirmNewPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(length)});

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = etCurrentPassword.getText().toString();
                String newPassword = etNewPassword.getText().toString();
                String confirmNewPassword = etConfirmNewPassword.getText().toString();

                if (TextUtils.isEmpty(currentPassword)) {
                    etCurrentPassword.setError("The current password cannot be empty.");
                }

                if (TextUtils.isEmpty(newPassword)) {
                    etNewPassword.setError("The new password cannot be empty.");
                }

                if (TextUtils.isEmpty(confirmNewPassword)) {
                    etConfirmNewPassword.setError("The confirm new password cannot be empty.");
                }

                if (etNewPassword.getText().toString().length() < 4) {
                    etNewPassword.setError("The new password is too short, 4 characters minimum.");
                }

                if (newPassword.compareTo(confirmNewPassword)!=0) {
                    etConfirmNewPassword.setError("The password confirmation does not match.");
                }

                if (newPassword.compareTo(currentPassword)==0) {
                    etNewPassword.setError("The new password cannot be the same as your current password.");
                }

                if (!TextUtils.isEmpty(currentPassword) && !TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(confirmNewPassword) && etNewPassword.getText().toString().length() > 3 && newPassword.compareTo(confirmNewPassword)==0 && newPassword.compareTo(currentPassword)!=0) {
                    changePasswordToServer(currentPassword, newPassword);
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /*
     * this method is saving the name to ther server
     * */
    private void changePasswordToServer(String currentPassword, String newPassword) {
        final ProgressDialog progressDialog = new ProgressDialog(ChangePassword.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CHANGE_PASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject obj = new JSONObject(response);
                            String name = obj.getString("message");

                            if (!obj.getBoolean("error")) {
                                if (obj.has("message")) {
                                    name = obj.getString("message") + " Please log in again.";
                                }
                                finish();
                                sessionManager.logoutSession();
                                moveToLogin();
                            } else {
                                if (obj.has("message")) {
                                    name = obj.getString("message");
                                }
                            }

                            Toast.makeText(ChangePassword.this, name, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ChangePassword.this, "Password update failed.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userid", userid);
                params.put("password", currentPassword);
                params.put("new_password", newPassword);
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void moveToLogin() {
        Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

}