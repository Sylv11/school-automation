package com.example.sylvain.projetautomates.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sylvain.projetautomates.DB.User;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Session;

public class DashboardActivity extends AppCompatActivity {

    private TextView tv_dashboard_welcome;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        tv_dashboard_welcome = (TextView)findViewById(R.id.tv_dashboard_welcome);

        session = new Session(getApplication());
        User user = session.getUser();

        tv_dashboard_welcome.setText("Salut " + user.getFirstname() + " !" );
    }
}
