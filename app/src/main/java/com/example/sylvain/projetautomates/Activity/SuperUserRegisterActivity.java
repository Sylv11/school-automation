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

public class SuperUserRegisterActivity extends AppCompatActivity {

    private EditText et_super_user_register_lastname;
    private EditText et_super_user_register_firstname;
    private EditText et_super_user_register_email;
    private EditText et_super_user_register_password;

    SharedPreferences prefs_datas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_user_register);

        prefs_datas = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        et_super_user_register_lastname = (EditText)findViewById(R.id.et_super_user_register_lastname);
        et_super_user_register_firstname = (EditText)findViewById(R.id.et_super_user_register_firstname);
        et_super_user_register_email = (EditText)findViewById(R.id.et_super_user_register_email);
        et_super_user_register_password = (EditText)findViewById(R.id.et_super_user_register_password);
    }

    public void onSuperUserRegisterClickManager(View v){

        switch(v.getId()){
            case R.id.btn_super_user_register:
                String lastname = et_super_user_register_lastname.getText().toString();
                String firstname = et_super_user_register_firstname.getText().toString();
                String email = et_super_user_register_email.getText().toString();
                String password = et_super_user_register_password.getText().toString();

                checkRegistration(lastname, firstname, email, password);
                break;
        }
    }

    private void checkRegistration(String lastname, String firstname, String email, String password){
        if(!lastname.isEmpty() && !firstname.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if(lastname.length() >= 3){
                if(firstname.length() >= 3){
                    if(email.length() >= 3){
                        if(password.length() >= 4){
                            User user = new User(lastname, firstname, email, password, 2);
                            UserAccessDB userDB = new UserAccessDB(this);
                            userDB.openForWrite();
                            userDB.insertUser(user);
                            userDB.Close();

                            SharedPreferences.Editor editeur_datas = prefs_datas.edit();

                            // Les valeurs à transmettre de type String
                            editeur_datas.putBoolean("super_user_created", true).commit();

                            Intent intentToLogin = new Intent(this,MainActivity.class);
                            startActivity(intentToLogin);
                            Toast.makeText(this,"Vous êtes inscrit ! Bienvenue",Toast.LENGTH_LONG).show();
                            finish();
                    }else{
                            Toast.makeText(this,"Votre mot de passe est trop court",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this,"Votre adresse email est trop courte",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this,"Votre prénom est trop court",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this,"Votre nom est trop court",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this,"Veuillez remplir tous les champs !",Toast.LENGTH_SHORT).show();
        }
    }
}
