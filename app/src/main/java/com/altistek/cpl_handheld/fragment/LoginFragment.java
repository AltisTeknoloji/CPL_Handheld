package com.altistek.cpl_handheld.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.altistek.cpl_handheld.MainActivity;
import com.altistek.cpl_handheld.R;
import com.altistek.cpl_handheld.service.AuthService;

public class LoginFragment extends Fragment {

    private final String TAG = "-LoginFragment-";

    private EditText etUsername;
    private EditText etPassword;

    private Button btnLogin;

    private AuthService authService;
    private Context mContext;

    public static LoginFragment newInstance() {return new LoginFragment();}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.login_fragment,container,false);

        mContext = inflater.getContext();
        authService = new AuthService(mContext);
        etUsername = (EditText) v.findViewById(R.id.et_username);
        etPassword = (EditText) v.findViewById(R.id.et_password);

        btnLogin = (Button) v.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "btn Login Click");
                if (etUsername.getText().toString().equals("") && etPassword.getText().toString().equals("")){
                    Log.d(TAG, "username and password null");
                    Toast.makeText(mContext,getString(R.string.nc_or_pw_not_null),Toast.LENGTH_SHORT).show();
                }
                else if (etUsername.getText().toString().equals("") || etPassword.getText().toString().equals("")){
                    Log.d(TAG, "username or password null");
                    Toast.makeText(mContext,getString(R.string.nc_or_pw_not_null),Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG, "username and password not null");
                    new WebApiTask().execute();
                }
            }
        });
        return v;
    }

    class WebApiTask extends AsyncTask<String, Void, Boolean> {
        private Exception exception;
        private boolean result;
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                Log.d(TAG, "before");
                result = authService.Login(etUsername.getText().toString(), etPassword.getText().toString());
                Log.d(TAG, "resullt is " + result);
                if (result){
                    // Reload activity for route
                    Activity activity = getActivity();
                    activity.finish();
                    Intent temp = new Intent(mContext, MainActivity.class);
                    activity.startActivity(temp);
                }
                else {
                    Log.d(TAG, "Error login");
                }

            }
            catch (Exception e) {
                Log.e(TAG, "Error occurred: " + e.getMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            // Check exception
            if (!result){
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(getString(R.string.login));
                builder.setMessage(getString(R.string.error_occured));
                builder.setPositiveButton(getString(R.string.okey_str),null);
                builder.show();
            }
            else {
                Log.d(TAG,"success login");
            }

        }
    }
}

