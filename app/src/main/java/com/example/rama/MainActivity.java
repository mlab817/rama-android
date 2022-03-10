package com.example.rama;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    ArrayList<String> locationList = new ArrayList<>();
    ArrayAdapter<String> locationAdapter;
    RequestQueue requestQueue;
    /*
     * this is the url to our webservice
     * make sure you are using the ip instead of localhost
     * it will not work if you are using localhost
     * */
    public static final String URL_SAVE_NAME = "https://edsabuswaymonitoring.online/assets/androidStudio/saveName.php/";

    //database helper object
    private DatabaseHelper db;

    //View objects
    private Button buttonSave;
    private Spinner spinnerTextName;
    private ListView listViewNames;

    //List to store all the names
    private List<Name> names;

    //1 means data is synced and 0 means data is not synced
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //a broadcast to know weather the data is synced or not
    public static final String DATA_SAVED_BROADCAST = "net.simplifiedcoding.datasaved";

    //Broadcast receiver to know the sync status
    private BroadcastReceiver broadcastReceiver;

    //adapterobject for list view
    private NameAdapter nameAdapter;

    /*Login Information*/
    TextView etUsername, etName;
    SessionManager sessionManager;
    String username, fullname, finalValue;

    /*BarCode & QRCode Scanner*/
    private static final int CAMERA_PERMISSION_CODE = 101;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sessionManager = new SessionManager(MainActivity.this);
        if (!sessionManager.isLoggedIn()) {
            moveToLogin();
        }

        /*Login Information*/
        etUsername = findViewById(R.id.etUsername);
        etName = findViewById(R.id.etMainName);

        username = sessionManager.getUserDetail().get(SessionManager.USERNAME);
        fullname = sessionManager.getUserDetail().get(SessionManager.FULLNAME);

        etUsername.setText(username);
        etName.setText(fullname);

        /*BarCode & QRCode Scanner*/
        textView = findViewById(R.id.txtPUVDetails);

        String data_in_code = "Hello Bar Code Data";

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(data_in_code, BarcodeFormat.QR_CODE, 200, 200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //now let's create barcode scanner
        Button scan_code = findViewById(R.id.button_scan);
        scan_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        /*For Saving and Retrieving Data*/
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //initializing views and objects
        db = new DatabaseHelper(this);
        names = new ArrayList<>();

        buttonSave = (Button) findViewById(R.id.buttonSave);
        spinnerTextName = (Spinner) findViewById(R.id.spinnerTextName);
        listViewNames = (ListView) findViewById(R.id.listViewNames);

        //adding click listener to button
        buttonSave.setOnClickListener(this);

        //calling the method to load all the stored names
        loadNames();

        //the broadcast receiver to update sync status
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //loading the names again
                loadNames();
            }
        };

        //registering the broadcast receiver to update sync status
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));

        /*For Getting Location*/
        requestQueue = Volley.newRequestQueue(this);
        spinnerTextName = findViewById(R.id.spinnerTextName);
//        String url = "http://192.168.31.205/sample_android_spinnerv2/scan_location.php?username="+selectedCountry;
        String url = "http://192.168.31.205/sample_android_spinnerv2/scan_location.php?username="+ "0001";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("scan_location");
                    for(int i=0; i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String locationName = jsonObject.optString("location");
                        locationList.add(locationName);
                        locationAdapter = new ArrayAdapter<>(MainActivity.this,
                                android.R.layout.simple_spinner_item, locationList);
                        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerTextName.setAdapter(locationAdapter);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
        spinnerTextName.setOnItemSelectedListener(this);
    }

    private void moveToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionLogout:
                sessionManager.logoutSession();
                moveToLogin();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openScanner() {
        new IntentIntegrator(MainActivity.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
            } else {
                textView.setText(result.getContents());
                finalValue = result.getContents();
            }
        } else {
            Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(String permision, int code) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permision)) {

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permision}, code);
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

    /*For Retrieving and Saving Data*/
    /*
     * this method will
     * load the names from the database
     * with updated sync status
     * */
    private void loadNames() {
        names.clear();
        Cursor cursor = db.getNames();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") Name name = new Name(
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NAME)),
                        cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_STATUS))
                );
                names.add(name);
            } while (cursor.moveToNext());
        }

        nameAdapter = new NameAdapter(this, R.layout.names, names);
        listViewNames.setAdapter(nameAdapter);
    }

    /*
     * this method will simply refresh the list
     * */
    private void refreshList() {
        nameAdapter.notifyDataSetChanged();
        listViewNames.invalidateViews();
        listViewNames.refreshDrawableState();
    }

    /*
     * this method is saving the name to ther server
     * */
    private void saveNameToServer() {

        final String spinner = spinnerTextName.getSelectedItem().toString().trim();
//        final String platenumber = finalValue;
        if (spinner.matches("")) {
            Toast.makeText(MainActivity.this, "Location is Empty...", Toast.LENGTH_SHORT).show();
        }

        else if (textView.getText().toString().length() == 0) {
            Toast.makeText(MainActivity.this, "Scan Vehicle To Continue", Toast.LENGTH_SHORT).show();

        } else {
//            Toast.makeText(MainActivity.this, "Submitted", Toast.LENGTH_SHORT).show();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Saving Vehicle Details...");
            progressDialog.show();
            String displayTextView = null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd/HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());

            final String name = spinnerTextName.getSelectedItem().toString().trim() + "/" + username + "/" + fullname + "/" + finalValue + "/" + currentDateandTime;

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_NAME,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.getBoolean("error")) {
                                    //if there is a success
                                    //storing the name to sqlite with status synced
                                    saveNameToLocalStorage((name), NAME_SYNCED_WITH_SERVER);
                                } else {
                                    //if there is some error
                                    //saving the name to sqlite with status unsynced
                                    saveNameToLocalStorage(name, NAME_NOT_SYNCED_WITH_SERVER);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            //on error storing the name to sqlite with status unsynced
                            saveNameToLocalStorage(name, NAME_NOT_SYNCED_WITH_SERVER);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    return params;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

            textView.setText("");
        }
    }

    //saving the name to local storage
    private void saveNameToLocalStorage(String name, int status) {
        db.addName(name, status);
        Name n = new Name(name, status);
        names.add(n);
        refreshList();
    }

    @Override
    public void onClick(View view) {
        saveNameToServer();
    }

    /*For Getting Location*/
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
