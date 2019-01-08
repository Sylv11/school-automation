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
import com.example.sylvain.projetautomates.Utils.EmailValidator;
import com.example.sylvain.projetautomates.R;

/* When it is the first time that a user run the application, We have to define a superuser.
 * The superuser have to register. Then, he can manage the rights of the others users */


public class SuperUserRegisterActivity extends AppCompatActivity {

    // EditText user information's

    private EditText et_super_user_register_lastname;
    private EditText et_super_user_register_firstname;
    private EditText et_super_user_register_email;
    private EditText et_super_user_register_password;

    // SharedPreferences to store superuser
    SharedPreferences prefs_datas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_user_register);

        this.prefs_datas = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        this.et_super_user_register_lastname = findViewById(R.id.et_super_user_register_lastname);
        this.et_super_user_register_firstname = findViewById(R.id.et_super_user_register_firstname);
        this.et_super_user_register_email = findViewById(R.id.et_super_user_register_email);
        this.et_super_user_register_password = findViewById(R.id.et_super_user_register_password);
    }

    public void onSuperUserRegisterClickManager(View v) {

        switch (v.getId()) {
            // Superuser registration
            case R.id.btn_super_user_register:
                String lastname = this.et_super_user_register_lastname.getText().toString();
                String firstname = this.et_super_user_register_firstname.getText().toString();
                String email = this.et_super_user_register_email.getText().toString();
                String password = this.et_super_user_register_password.getText().toString();

                checkRegistration(lastname, firstname, email, password);
                break;
        }
    }

    // This method checks the input value given by the user
    private void checkRegistration(String lastname, String firstname, String email, String password) {
        if (!lastname.isEmpty() && !firstname.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
            if (lastname.trim().length() >= 3) {
                if (firstname.trim().length() >= 3) {
                    if (email.trim().length() >= 3) {
                        if (password.length() >= 4) {
                            if (EmailValidator.isValidEmail(email)) {
                                // Store superuser in SharedPreferences
                                User user = new User(lastname, firstname, email, password, 2);
                                UserAccessDB userDB = new UserAccessDB(this);
                                userDB.openForWrite();
                                userDB.insertUser(user);
                                userDB.Close();

                                SharedPreferences.Editor editeur_datas = this.prefs_datas.edit();

                                editeur_datas.putBoolean("super_user_created", true).apply();

                                // Redirect to login
                                Intent intentToLogin = new Intent(this, MainActivity.class);
                                startActivity(intentToLogin);
                                Toast.makeText(this, "Vous êtes inscrit ! Bienvenue", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Cette adresse email est non conforme", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Votre mot de passe est trop court", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Votre adresse email est trop courte", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Votre prénom est trop court", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Votre nom est trop court", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Veuillez remplir tous les champs !", Toast.LENGTH_SHORT).show();
        }
    }
}
