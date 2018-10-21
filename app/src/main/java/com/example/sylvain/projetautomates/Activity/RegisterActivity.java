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

public class RegisterActivity extends AppCompatActivity {

    private EditText et_register_lastname;
    private EditText et_register_firstname;
    private EditText et_register_email;
    private EditText et_register_password;

    SharedPreferences prefs_datas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prefs_datas = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        et_register_lastname = (EditText)findViewById(R.id.et_register_lastname);
        et_register_firstname = (EditText)findViewById(R.id.et_register_firstname);
        et_register_email = (EditText)findViewById(R.id.et_register_email);
        et_register_password = (EditText)findViewById(R.id.et_register_password);
    }

    public void onRegisterClickManager(View v){

        switch(v.getId()){
            case R.id.btn_register_toLogin :
                Intent toLoginIntent = new Intent(this,MainActivity.class);
                startActivity(toLoginIntent);
                break;

            case R.id.btn_register_registerAction:
                String lastname = et_register_lastname.getText().toString();
                String firstname = et_register_firstname.getText().toString();
                String email = et_register_email.getText().toString();
                String password = et_register_password.getText().toString();

                checkRegistration(lastname,firstname,email,password);
                break;
        }
    }

    private void checkRegistration(String lastname, String firstname, String email, String password){
        if(!lastname.isEmpty() && !firstname.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if(lastname.length() >= 3){
                if(firstname.length() >= 3){
                    if(email.length() >= 3){
                        if(password.length() >= 3){
                            User user = new User(lastname, firstname, email, password, 1);
                            UserAccessDB userDB = new UserAccessDB(this);
                            userDB.openForWrite();
                            userDB.insertUser(user);
                            userDB.Close();

                            Intent intentToLogin = new Intent(this,MainActivity.class);
                            startActivity(intentToLogin);
                            Toast.makeText(this.getApplicationContext(),"Vous êtes inscrit ! Bienvenue",Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(this.getApplicationContext(),"Votre mot de passe est trop court",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(this.getApplicationContext(),"Votre adresse email est trop courte",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this.getApplicationContext(),"Votre prénom est trop court",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this.getApplicationContext(),"Votre nom est trop court",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this.getApplicationContext(),"Veuillez remplir tous les champs !",Toast.LENGTH_SHORT).show();
        }
    }
}
