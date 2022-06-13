package com.example.rama;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.example.rama.model.login.LoginData;
import java.util.HashMap;
public class SessionManager {

    private Context _context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public static final String IS_LOGGED_IN = "isLoggedIn";
    public static final String USERID = "userid";
    public static final String USERNAME = "username";
    public static final String FULLNAME = "name";
    public static final String IS_UPDATED = "updated";

    public SessionManager (Context context){
        this._context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(LoginData user){
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putString(USERID, user.getUserid());
        editor.putString(USERNAME, user.getUsername());
        editor.putString(FULLNAME, user.getName());
        editor.putBoolean(IS_UPDATED, user.getUpdated());
        editor.commit();
    }

    public HashMap<String,String> getUserDetail(){
        HashMap<String,String> user = new HashMap<>();
        user.put(USERID, sharedPreferences.getString(USERID,null));
        user.put(USERNAME, sharedPreferences.getString(USERNAME,null));
        user.put(FULLNAME, sharedPreferences.getString(FULLNAME,null));
        return user;
    }

    public void logoutSession(){
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    public boolean isUpdated(){
        return sharedPreferences.getBoolean(IS_UPDATED, false);
    }
}
