package com.example.sylvain.projetautomates;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.sylvain.projetautomates.DB.User;

public class Session
{
    private SharedPreferences prefs;

    public Session(Context cntx) {
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setUser(String lastname, String firstname, String email, String password, int rank) {
        prefs.edit().putString("email", email).commit();
        prefs.edit().putString("firstname", firstname).commit();
        prefs.edit().putString("lastname", lastname).commit();
        prefs.edit().putString("password", password).commit();
        prefs.edit().putInt("rank", rank).commit();
    }

    public User getUser() {
        User user;
        String email = prefs.getString("email","");
        String firstname = prefs.getString("firstname","");
        String lastname = prefs.getString("lastname","");
        String password = prefs.getString("password","");
        int rank = prefs.getInt("rank",0);
        user = new User(lastname, firstname, email, password, rank);

        return user;
    }
}
