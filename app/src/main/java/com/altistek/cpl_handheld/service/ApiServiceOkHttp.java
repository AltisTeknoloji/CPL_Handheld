package com.altistek.cpl_handheld.service;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import android.widget.Toast;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ApiServiceOkHttp {
    private final String TAG = "-ApiService-";
    private OkHttpClient okHttpClient;
    private AuthService authService;
    //private final String BASE_URL = "http://192.168.1.113:5046";
    private final String BASE_URL = "http://192.168.1.20:7000";
    //private final String BASE_URL = "http://192.168.1.118:5000";
    private final String REFRESH_URL = BASE_URL + "/User/Refresh";
    OkHttpClient.Builder builder =new OkHttpClient.Builder();

    public ApiServiceOkHttp() {
        builder.connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5,TimeUnit.MINUTES)
                .readTimeout(5,TimeUnit.MINUTES);

//        okHttpClient = builder.build();
        okHttpClient = new OkHttpClient();
        //authService = new AuthService(getApll);
        machineName();
    }

    public String GET(final String URL) {
        Log.d(TAG, "URL : " + BASE_URL + URL);
        String jsonData = null;
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + AuthService.GetAccessToken())
                .header("Accept", "application/json")
                .url(BASE_URL + URL)
                .get()
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception e) {
            Log.d(TAG, "Exception is " + e);
        }

        if (response == null) {
            return "null";
        }

        int responseCode = response.networkResponse().code();
        boolean isSuccess = response.isSuccessful();
        if (isSuccess) {
            if (responseCode == 200) {
                try {
                    jsonData = response.body().string();

                } catch (IOException ignored) {
                }
                Log.d(TAG, "No error, success");
                //return jsonData;
            } else {
                Log.d(TAG, "Something else error");
                return "null";

                //jsonData = response.body().bytes();
            }
        } else {
            Log.d(TAG, "response is not successful, code is " + responseCode);
            if (responseCode == 401) {
                Log.d(TAG, "Auth error");

                boolean res = isUpdateToken();
                if (res) {
                    //
                    Log.d(TAG, "token updated");
                    return GET(URL);
                } else {
                    Log.d(TAG, "no update token");
                    return "no token";
                }
            } else {
                return "null";
            }
        }
        return jsonData;
    }

    public enum AddPalletResultType {
        Success,
        NotFound,
        Error,
        NoToken
    }

    public AddPalletResultType AddPalletMovement(final String URL) {
        Log.d(TAG, "URL : " + BASE_URL + URL);
        String jsonData = null;
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + AuthService.GetAccessToken())
                .header("Accept", "application/json")
                .url(BASE_URL + URL)
                .get()
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception e) {
            Log.d(TAG, "Exception is " + e);
        }

        if (response == null) {
            return AddPalletResultType.Error;
        }

        int responseCode = response.networkResponse().code();
        boolean isSuccess = response.isSuccessful();
        if (isSuccess) {
            if (responseCode == 200) {
                try {
                    jsonData = response.body().string();
                } catch (IOException ignored) {
                }
                Log.d(TAG, "No error, success");
                //return jsonData;
            } else {
                Log.d(TAG, "Something else error");
                //return null;

                //jsonData = response.body().bytes();
            }
        } else {
            Log.d(TAG, "response is not successful, code is " + responseCode);
            if (responseCode == 401) {
                Log.d(TAG, "Auth error");

                boolean res = isUpdateToken();
                if (res) {
                    //
                    Log.d(TAG, "token updated");
                    return AddPalletMovement(URL);
                } else {
                    Log.d(TAG, "no update token");
                    return AddPalletResultType.NoToken;
                    //return null;
                }
            } else if (responseCode == 404) {
                return AddPalletResultType.NotFound;
            } else {
                return AddPalletResultType.Error;
            }
        }
        return AddPalletResultType.Success;
    }

    public AddPalletResultType AddPalletMovements(final String URL, String data) {
        Log.d(TAG, "URL : " + BASE_URL + URL);
        String jsonData = null;
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), data);
        final Request request = new Request.Builder()
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Accept", "application/json")
                .url(BASE_URL + URL)
                .post(body)
                .build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception e) {
            Log.d(TAG, "Exception is " + e);
        }

        if (response == null) {
            return AddPalletResultType.Error;
        }

        int responseCode = response.networkResponse().code();
        boolean isSuccess = response.isSuccessful();
        if (isSuccess) {
            if (responseCode == 200) {
                try {
                    jsonData = response.body().string();
                } catch (IOException ignored) {
                }
                Log.d(TAG, "No error, success");
                //return jsonData;
            } else {
                Log.d(TAG, "Something else error");
                //return null;

                //jsonData = response.body().bytes();
            }
        } else {
            Log.d(TAG, "response is not successful, code is " + responseCode);
            if (responseCode == 401) {
                Log.d(TAG, "Auth error");

                boolean res = isUpdateToken();
                if (res) {
                    //
                    Log.d(TAG, "token updated");
                    return AddPalletMovements(URL, data);
                } else {
                    Log.d(TAG, "no update token");
                    return AddPalletResultType.NoToken;
                    //return null;
                }
            } else if (responseCode == 404) {
                return AddPalletResultType.NotFound;
            } else {
                return AddPalletResultType.Error;
            }
        }
        return AddPalletResultType.Success;
    }

    public String POST(String URL, JSONObject object)  throws IOException {
        Log.d(TAG, "POST");
        String result = null;
        Response response = null;
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),  object.toString());
        Request request = new Request.Builder()
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + AuthService.GetAccessToken())
                .url(BASE_URL + URL)
                .post(body)
                .build();

        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception ignored) {
            Log.d(TAG, "response null");
            return null;
        }
        if (response == null) {
            return "null";
        }
        int responseCode = response.code();
        if (response.isSuccessful()) {

            Log.d(TAG, "error code is " + responseCode);
            if (responseCode == 200) {
                try {
                    result = response.body().string();
                } catch (IOException ignored) {
                }
                Log.d(TAG, "No error, success");
            } else if (responseCode == 401) {
                Log.d(TAG, "Auth error");
            } else {
                Log.d(TAG, "Something else error");
            }

        } else {
            Log.d(TAG, "response is not successful, code is " + responseCode);
            if (responseCode == 401) {
                Log.d(TAG, "Auth error");

                boolean res = isUpdateToken();
                if (res) {
                    //
                    Log.d(TAG, "token updated");
                    return POST(URL, object);
                } else {
                    Log.d(TAG, "no update token");
                    //return null;
                }
            } else {
                return "null";
            }
        }
        return result;
    }
    public Response POSTArray(String URL, Object object)  throws IOException {
        Log.d(TAG, "POST");
        String result = null;
        Response response = null;
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),  object.toString());
        Request request = new Request.Builder()
                .header("Content-Type", "application/json;charset=utf-8")
                .url(BASE_URL + URL)
                .post(body)
                .build();

        try {
            OkHttpClient deneme1 = new OkHttpClient();
            Call deneme2 = deneme1.newCall(request);
            response = deneme2.execute();
            if (response.code() == 200) {
                    ResponseBody responseBody = response.body();
                    return response;
            }
            else if(response.code() == 400){
                ResponseBody responseBody = response.body();
                return response;
            }
            else{
                return null;
            }
        } catch (Exception ignored) {
            Log.d(TAG, "response null");
            return null;
        }
    }
    private boolean isUpdateToken() {
        Log.d(TAG, "updateToken");
        JSONObject object = new JSONObject();
        JSONObject currentUser = AuthService.GetCurrentUser();
        try {
            object.put("id", currentUser.get("id"));
            object.put("refreshToken", currentUser.get("refreshToken"));

        } catch (Exception ignored) {

        }
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), String.valueOf(currentUser));
        //Log.d(TAG, "Get Current User : " + currentUser);
        final Request request = new Request.Builder()
                .url(REFRESH_URL)
                .post(body)
                .build();
        boolean result;
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception ignored) {

        }

        if (response.isSuccessful()) {
            int responseCode = response.networkResponse().code();
            Log.d(TAG, "error code is " + responseCode);
            if (responseCode == 200) {
                Log.d(TAG, "No error, success");
                byte[] temp2 = new byte[0];
                try {
                    temp2 = response.body().bytes();
                } catch (Exception ignored) {

                }
                Log.d(TAG, "Temp result : " + temp2);

                JSONObject json = byteToJSON(temp2);
                try {

                    AuthService.RemoveUserandToken();
                    AuthService.SetAccessToken((String) json.get("token"));
                    AuthService.SetCurrentUser(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                result = true;
            } else {
                result = false;
            }
        } else {
            Log.d(TAG, "Request not successfull");
            result = false;
        }
        if (!result) {
            Log.d(TAG, "removed user");
            AuthService.RemoveUserandToken();
        }
        Log.d(TAG, "token result" + result);
        return result;
    }

    public String PUT(String URL, JSONObject object) {
        Log.d(TAG, "PUT");
        String result = null;
        Response response = null;
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), String.valueOf(object));
        Request request = new Request.Builder()
                .header("Content-Type", "application/json;charset=utf-8")
                .header("Authorization", "Bearer " + AuthService.GetAccessToken())
                .url(BASE_URL + URL)
                .put(body)
                .build();

        try {
            response = okHttpClient.newCall(request).execute();
        } catch (Exception ignored) {
            Log.d(TAG, "response null");
            return null;
        }
        if (response == null) {
            return "null";
        }
        int responseCode = response.networkResponse().code();
        if (response.isSuccessful()) {

            Log.d(TAG, "code is " + responseCode);
            if (responseCode == 200) {
                try {
                    result = response.body().string();
                } catch (IOException ignored) {
                }
                result = "success";
                Log.d(TAG, "No error, success");
            } else if (responseCode == 401) {
                Log.d(TAG, "Auth error");
            } else {
                Log.d(TAG, "Something else error");
            }

        } else {
            Log.d(TAG, "response is not successful, code is " + responseCode);
            if (responseCode == 401) {
                Log.d(TAG, "Auth error");

                boolean res = isUpdateToken();
                if (res) {
                    //
                    Log.d(TAG, "token updated");
                    return PUT(URL, object);
                } else {
                    Log.d(TAG, "no update token");
                    AuthService.RemoveUserandToken();

                    //return null;
                }
            } else {
                return "null";
            }
        }
        return result;
    }

    private JSONObject byteToJSON(byte[] byteArray) {
        JSONObject temp = new JSONObject();
        try {
            if (byteArray != null)
                temp = new JSONObject(new String(byteArray));
        } catch (Exception ignored) {
        }
        return temp;
    }

    private String byteToString(byte[] bytes) {
        return new String(bytes);
    }

    private String machineName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String id = Build.ID;
        if (model.startsWith(manufacturer)) {
            return model.toUpperCase();
        }
        return manufacturer.toUpperCase() + "_" + model + "_" + id;
    }

}
