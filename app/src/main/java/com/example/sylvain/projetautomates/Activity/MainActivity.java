package com.example.sylvain.projetautomates.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sylvain.projetautomates.R;

public class MainActivity extends AppCompatActivity {

    // On déclare des attributs correspondants au layout main

    private EditText et_main_email;
    private EditText et_main_password;

    // Objet SharedPreferences pour la persistance des données
    // Je l'utilise ici pour vérifier si c'est le premier utilisateur que l'on crée (superutilisateur)

    SharedPreferences prefs_datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs_datas = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // On récupère les composants du layout main
        et_main_email = (EditText)findViewById(R.id.et_main_email);
        et_main_password = (EditText)findViewById(R.id.et_main_password);

        if(!this.prefs_datas.contains("super_user_created")) {
            Intent intentSuperUserRegister = new Intent(this,SuperUserRegisterActivity.class);
            startActivity(intentSuperUserRegister);
        }else {
            setContentView(R.layout.activity_main);
        }


    }

    public void onMainClickManager(View v){

        switch(v.getId()){

            case R.id.btn_main_login:
                String email = et_main_email.getText().toString();
                String password = et_main_password.getText().toString();

                checkLogin(email,password);
                break;

            case R.id.btn_main_register:
                Intent intentRegister = new Intent(this,RegisterActivity.class);
                startActivity(intentRegister);
                break;
        }
    }

    private void checkLogin(String email, String password){
        if(!email.isEmpty() && !password.isEmpty()) {
            if(email.length() >= 3){
                if(password.length() >= 3){
                    Toast.makeText(getApplicationContext(),"Vers le dashboard", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Votre mot de passe est trop court", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"Votre adresse email est trop courte", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show();
        }
    }

}

