package com.example.sylvain.projetautomates.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sylvain.projetautomates.DB.User;
import com.example.sylvain.projetautomates.DB.UserAccessDB;
import com.example.sylvain.projetautomates.Utils.EmailValidator;
import com.example.sylvain.projetautomates.R;

public class RegisterActivity extends AppCompatActivity {

    // EditText user informations

    private EditText et_register_lastname;
    private EditText et_register_firstname;
    private EditText et_register_email;
    private EditText et_register_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.et_register_lastname = (EditText)findViewById(R.id.et_register_lastname);
        this.et_register_firstname = (EditText)findViewById(R.id.et_register_firstname);
        this.et_register_email = (EditText)findViewById(R.id.et_register_email);
        this.et_register_password = (EditText)findViewById(R.id.et_register_password);
    }

    public void onRegisterClickManager(View v){

        switch(v.getId()){
            // Redirect to login
            case R.id.btn_register_toLogin :
                Intent toLoginIntent = new Intent(this,MainActivity.class);
                startActivity(toLoginIntent);
                finish();
                break;

            // User registration
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
            if(lastname.trim().length() >= 3){
                if(firstname.trim().length() >= 3){
                    if(email.trim().length() >= 3){
                        if(password.length() >= 4){
                            if(EmailValidator.isValidEmail(email)) {
                                // Check user informations and if user already exists
                                UserAccessDB userDB = new UserAccessDB(this);
                                userDB.openForRead();
                                User userCheckExist = userDB.getUser(email);
                                userDB.Close();

                                if(userCheckExist == null){
                                    // Store new user
                                    User user = new User(lastname, firstname, email, password, 1);
                                    userDB.openForWrite();
                                    userDB.insertUser(user);
                                    userDB.Close();

                                    // Redirect to login
                                    Intent intentToLogin = new Intent(this,MainActivity.class);
                                    startActivity(intentToLogin);
                                    finish();
                                    Toast.makeText(this,"Vous êtes inscrit ! Bienvenue",Toast.LENGTH_LONG).show();
                                }else {
                                    Toast.makeText(this, "Cette adresse email est déjà attribuée à un compte", Toast.LENGTH_SHORT).show();
                                }
                            }else {
                                Toast.makeText(this, "Cette adresse email est non conforme", Toast.LENGTH_SHORT).show();
                            }
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
