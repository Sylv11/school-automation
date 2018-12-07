package com.example.sylvain.projetautomates;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.sylvain.projetautomates.Activity.MainActivity;
import com.example.sylvain.projetautomates.DB.User;
import com.example.sylvain.projetautomates.DB.UserAccessDB;

public class Session
{
    private SharedPreferences prefs;
    private UserAccessDB userDB;
    private Context context;
    private SharedPreferences.Editor editor;

    public Session(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.userDB = new UserAccessDB(context);
        this.context = context;
        this.editor = prefs.edit();
    }

    public void setUser(String email) {
        this.editor.putBoolean("IS_LOGGED", true);
        this.editor.putString("email", email);

        this.editor.apply();
    }

    public User getUser() {

        if(this.isLogged()) {
            String email = this.prefs.getString("email","");

            if(email != null) {
                this.userDB.openForRead();
                User user = userDB.getUser(email);
                this.userDB.Close();
                user = new User(user.getLastname(), user.getFirstname(), user.getEmail(), user.getPassword(), user.getRank());

                return user;
            }
            return null;
        }
        return null;
    }

    public void closeSession() {
        this.editor.remove("email");
        this.editor.remove("IS_LOGGED");

        this.editor.apply();

        Intent intentLogin = new Intent(this.context, MainActivity.class);
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.context.startActivity(intentLogin);

        ((Activity)this.context).finish();
    }

    public boolean isLogged() {
        boolean logged = this.prefs.getBoolean("IS_LOGGED",false);

        if(logged) {
            return true;
        }
        return false;
    }
}
