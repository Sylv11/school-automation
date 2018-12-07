package com.example.sylvain.projetautomates;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ToastService
{
    public static void show(final Context context, final String toast) {
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,toast, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }
}
