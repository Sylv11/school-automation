package com.example.sylvain.projetautomates.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sylvain.projetautomates.DB.User;
import com.example.sylvain.projetautomates.DB.UserAccessDB;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Session;

public class MainActivity extends AppCompatActivity {

    // On déclare des attributs correspondants au layout main

    private EditText et_main_email;
    private EditText et_main_password;

    private Session session;
    // Objet SharedPreferences pour la persistance des données
    // Je l'utilise ici pour vérifier si c'est le premier utilisateur que l'on crée (superutilisateur)

    private SharedPreferences prefs_datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.prefs_datas = PreferenceManager.getDefaultSharedPreferences(this);
        this.session = new Session(this);

        if(!this.prefs_datas.contains("super_user_created")) {
            Intent intentSuperUserRegister = new Intent(this,SuperUserRegisterActivity.class);
            startActivity(intentSuperUserRegister);
        }else {
            if(this.session.isLogged()) {
                Intent dashboardIntent = new Intent(this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
            }else {
                setContentView(R.layout.activity_main);
            }
        }

        // On récupère les composants du layout main
        et_main_email = (EditText)findViewById(R.id.et_main_email);
        et_main_password = (EditText)findViewById(R.id.et_main_password);

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
                finish();
                break;
        }
    }

    private void checkLogin(String email, String password){
        if(!email.isEmpty() && !password.isEmpty()) {
            if(email.length() >= 3){
                if(password.length() >= 3){
                    UserAccessDB userDB = new UserAccessDB(this);
                    userDB.openForRead();
                    User user = userDB.getUser(email);
                    userDB.Close();

                    if(user != null){
                        if(password.equals(user.getPassword())){
                            this.session.setUser(user.getEmail());

                            Intent dashboardIntent = new Intent(this, DashboardActivity.class);
                            startActivity(dashboardIntent);
                            finish();
                        }else{
                            Toast.makeText(this,"Mot de passe incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this,"Cette adresse email n'est attribuée à aucun compte", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(this,"Votre mot de passe est trop court", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this,"Votre adresse email est trop courte", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this,"Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show();
        }
    }

}

