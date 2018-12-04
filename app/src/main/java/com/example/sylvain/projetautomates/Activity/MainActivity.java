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

    SharedPreferences prefs_datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs_datas = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(!this.prefs_datas.contains("super_user_created")) {
            Intent intentSuperUserRegister = new Intent(this,SuperUserRegisterActivity.class);
            startActivity(intentSuperUserRegister);
        }else {
            setContentView(R.layout.activity_main);
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
                            session = new Session(getApplication());
                            session.setUser(user.getLastname(), user.getFirstname(), user.getEmail(), user.getPassword(), user.getRank());

                            Intent DashboardIntent = new Intent(this, DashboardActivity.class);
                            startActivity(DashboardIntent);
                            finish();
                            Toast.makeText(getApplicationContext(),"Vers le dashboard", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Mot de passe incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"Cette adresse email n'est attribuée à aucun compte", Toast.LENGTH_SHORT).show();
                    }

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

