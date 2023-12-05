package com.altistek.cpl_handheld.helpers;


import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.altistek.cpl_handheld.R;

public class MyToast {

    public static void showToast(Context context, String msg, int duration) {
        Toast toast = Toast.makeText(context, msg, duration);
        View view = toast.getView();
        view.setBackgroundResource(android.R.drawable.toast_frame);
        view.setBackgroundColor(Color.TRANSPARENT);
        TextView text = view.findViewById(android.R.id.message);
        text.setBackground(context.getResources().getDrawable(R.drawable.custom_toast));
        text.setTextColor(context.getResources().getColor(R.color.white));
        toast.show();
    }
}

