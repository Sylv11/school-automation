package com.example.sylvain.projetautomates.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sylvain.projetautomates.DB.User;
import com.example.sylvain.projetautomates.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Session;
import com.example.sylvain.projetautomates.ToastService;

import java.util.Timer;

public class DashboardActivity extends AppCompatActivity {

    private TextView tv_dashboard_welcome;
    private Button btn_dashboard_logout;

    private Session session;
    private Network network;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        tv_dashboard_welcome = (TextView)findViewById(R.id.tv_dashboard_welcome);
        btn_dashboard_logout = (Button)findViewById(R.id.btn_dashboard_logout);

        session = new Session(this);
        network = new Network(this);

        if(this.network.checkNetwork()) {
            if(session.isLogged()) {
                User user = session.getUser();
                tv_dashboard_welcome.setText("Salut " + user.getFirstname() + " !" );
            }
        }else {
            session.closeSession();
            ToastService.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    public void onDashboardClickManager(View v) {
        switch(v.getId()){

            case R.id.btn_dashboard_logout:
                session.closeSession();

                ToastService.show(this, "Déconnecté");
                break;
        }
    }
}
