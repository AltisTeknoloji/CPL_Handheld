package com.altistek.cpl_handheld.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthService {
    private static final String TAG = "-AuthService-";
    private ApiServiceOkHttp apiServiceOkHttp;
    private static Context mContext;

    //  Thats for user infos
    private static SharedPreferences sharedPref;
    private static SharedPreferences.Editor sharedEditor;

    // Constructor
    public AuthService(Context context){
        //Log.d(TAG, "AuthService constructed." );
        if (context != null){
            mContext = context;
        }
        else{
            Log.d(TAG,"Acitivty and/or Context not null");
        }
        sharedPref = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
    }


    public boolean Login (String username, String password)throws Exception {
        Log.d(TAG,"Login & NC - PW -> " + username +" - " + password);
        JSONObject inputDatas = new JSONObject();
        try {
            inputDatas.put("username",username);
            inputDatas.put("password",password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //apiService= new ApiService(mActivity, mContext);
        apiServiceOkHttp = new ApiServiceOkHttp();
        String result = apiServiceOkHttp.POST("/User/Authenticate",inputDatas);
        Log.d(TAG,"RESULT " + result);
        //result = result.substring(1,result.length()-1);
        JSONObject object = null;
        try {
            object = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (object == null){
            Log.d(TAG,"result null " + result);
            return false;
        }
        else {
            Log.d(TAG,"result not null" + result);
            try {
                SetCurrentUser(object);
                SetAccessToken((String) object.get("token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        /*
        try{
            apiService.POST("/User/Authenticate", inputDatas, new ApiCallback() {
                @Override
                public void onGetSuccess(JSONArray jsonArray) {
                    // DO NOTHING
                }

                @Override
                public void onGetSuccess(JSONObject object) {
                    Log.d(TAG, "Result Response In Login Func. -> " + object);
                    try {
                        SetCurrentUser(object);
                        SetAccessToken((String) object.get("token"));
                        callback.onGetSuccess(true,"OK");
                    } catch (JSONException ignored) {
                        callback.onGetSuccess(false,"OK");
                    }
                }
            });

        }catch (Exception ignored){}

         */
    }

    public void Logout (){
        //Log.d(TAG,"Logout");
        RemoveUserandToken();
    }

    // SET CURRENT USER (JSON)
    public static void SetCurrentUser (JSONObject value){
        Log.d("HANDHELD AUTH","setCurrentUser, user is " + value);
        sharedEditor.putString("user", value.toString());
        sharedEditor.apply();
    }

    // GET CURRENT USER (JSON)
    public static JSONObject GetCurrentUser(){
        String temp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE).getString("user", null);
        JSONObject object = null;
        //Log.d(TAG,"getCurrentUser" + temp);
        try {
            if (temp != null){
                object = new JSONObject(temp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return object;
    }

    // SET ACCESS TOKEN
    public static void SetAccessToken (String value){
        Log.d("HANDHELD AUTH","setAccessToken toke is " + value);
        sharedEditor.putString("token", value);
        sharedEditor.apply();
    }

    // GET ACCESS TOKEN
    public static String GetAccessToken(){
        //Log.d(TAG,"getAccessToken");
        String temp;
        temp = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE).getString("token", null);
        return temp;
    }



    // CLEAR USER, REFRESH AND ACCESS TOKEN
    public static void RemoveUserandToken(){
        //Log.d(TAG,"removeUserandToken");
        sharedEditor.clear();
        sharedEditor.apply();
    }

}

