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

    // EditText of login and password of the user

    private EditText et_main_email;
    private EditText et_main_password;

    // User session
    private Session session;

    // SharedPreferences to store superuser creation
    private SharedPreferences prefs_datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.prefs_datas = PreferenceManager.getDefaultSharedPreferences(this);
        this.session = new Session(this);

        // Check if superuser is already created

        if(!this.prefs_datas.contains("super_user_created")) {
            Intent intentSuperUserRegister = new Intent(this,SuperUserRegisterActivity.class);
            startActivity(intentSuperUserRegister);
        }else {
            //Check if connected

            if(this.session.isLogged()) {
                // to Dashboard

                Intent dashboardIntent = new Intent(this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
            }else {
                setContentView(R.layout.activity_main);
            }
        }

        this.et_main_email = (EditText)findViewById(R.id.et_main_email);
        this.et_main_password = (EditText)findViewById(R.id.et_main_password);

    }

    public void onMainClickManager(View v){

        switch(v.getId()){

            // User check password

            case R.id.btn_main_login:
                String email = et_main_email.getText().toString();
                String password = et_main_password.getText().toString();

                checkLogin(email,password);
                break;

            // User registration

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
                if(password.length() >= 4){
                    // Get user by email
                    UserAccessDB userDB = new UserAccessDB(this);
                    userDB.openForRead();
                    User user = userDB.getUser(email);
                    userDB.Close();

                    if(user != null){
                        if(password.equals(user.getPassword())){
                            // Start user session and redirect to dashboard
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

