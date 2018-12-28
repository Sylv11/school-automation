package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Utils.LoadProperties;
import com.example.sylvain.projetautomates.Utils.Network;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastService;

public class LevelServoActivity extends AppCompatActivity {

    private Toolbar toolbar = null;
    private TextView action_bar_title;
    private Button btn_servo_connect;

    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_servo);

        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.action_bar_title = (TextView)findViewById(R.id.action_bar_title);
        this.btn_servo_connect = (Button)findViewById(R.id.btn_servo_connect);

        this.session = new Session(this);
        this.network = new Network(this);

        // Check network
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                this.action_bar_title.setText("AUTOMATE D'ASSERVISSEMENT");
                this.btn_servo_connect.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);

                // Load properties file
                try {
                    this.ipAddress = LoadProperties.getProperty("ip_address", this);
                    this.rack = LoadProperties.getProperty("rack", this);
                    this.slot = LoadProperties.getProperty("slot", this);
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }else {
                session.closeSession();
                ToastService.show(this, "Vous n'êtes pas connecté");
            }
        }else {
            this.session.closeSession();
            ToastService.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Show the action bar
        if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Redirect to dashboard activity
            case R.id.item_dashboard:
                Intent dashboardIntent = new Intent(this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
                break;
            // Logout and close user session
            case R.id.item_logout :
                this.session.closeSession();
                ToastService.show(this,"Déconnecté");
                break;
            // Redirect to pharma activity
            case R.id.item_pharmaceutical:
                Intent pharmaIntent = new Intent(this, PharmaActivity.class);
                pharmaIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(pharmaIntent);
                finish();
                break;
            // Redirect to servo level activity
            case R.id.item_servo_level:
                Intent servoIntent = new Intent(this, LevelServoActivity.class);
                servoIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(servoIntent);
                finish();
                break;
            // Redirect to admin activity
            case R.id.item_admin :
                Intent adminIntent = new Intent(this, AdminActivity.class);
                adminIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(adminIntent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMenu(View view) {
        // Show menu when click on the hamburger
        this.toolbar.showOverflowMenu();
    }


}
