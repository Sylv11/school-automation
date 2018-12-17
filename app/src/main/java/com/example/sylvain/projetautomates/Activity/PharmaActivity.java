package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.sylvain.projetautomates.LoadProperties;
import com.example.sylvain.projetautomates.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Session;
import com.example.sylvain.projetautomates.ToastService;
import com.example.sylvain.projetautomates.WriteTaskS7;

public class PharmaActivity extends AppCompatActivity {

    private Toolbar toolbar = null;
    private TextView action_bar_title;
    private CheckBox ch_pharma_convoyeur;
    private Button btn_pharma_connect;
    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    private WriteTaskS7 writeS7;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharma);

        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.action_bar_title = (TextView)findViewById(R.id.action_bar_title);
        this.ch_pharma_convoyeur = (CheckBox)findViewById(R.id.ch_pharma_convoyeur);
        this.btn_pharma_connect = (Button)findViewById(R.id.btn_pharma_connect);

        this.session = new Session(this);
        this.network = new Network(this);

        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                this.action_bar_title.setText("AUTOMATE PHARMACEUTIQUE");
                this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);

                // Load properties file
                try {
                    this.ipAddress = LoadProperties.getProperty("ip_address", this);
                    this.rack = LoadProperties.getProperty("rack", this);
                    this.slot = LoadProperties.getProperty("slot", this);
                }catch(Exception e) {
                    e.printStackTrace();
                }
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
            case R.id.item_pharmaeutical:
                Intent pharmaIntent = new Intent(this, PharmaActivity.class);
                pharmaIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(pharmaIntent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMenu(View view) {
        // Show menu when click on the hamburger
        this.toolbar.showOverflowMenu();
    }

    public void onPharmaClickManager (View v) {
        switch (v.getId()){
            case R.id.ch_pharma_convoyeur :
                // Check if there is a network connectivity
                if(this.network.checkNetwork()) {
                    // Check if the user is connected
                    if(this.session.isLogged()) {
                        writeS7.setWriteBool(1, this.ch_pharma_convoyeur.isChecked() ? 1 : 0);
                    }
                }else {
                    this.session.closeSession();
                    ToastService.show(this, "Action impossible ! Vous n'êtes connecté à aucun réseau");
                }
                break;
        }
    }

    public void connectAutomaton (View v) {
        // Check if there is a network connectivity
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                if(this.btn_pharma_connect.getText().equals("Se connecter à l'automate")) {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.writeS7 = new WriteTaskS7();
                    this.writeS7.start(this.ipAddress, this.rack, this.slot);

                    this.btn_pharma_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
                    this.btn_pharma_connect.setText("Se déconnecter de l'automate");
                    this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop, 0, 0, 0);
                    this.ch_pharma_convoyeur.setVisibility(View.VISIBLE);
                    ToastService.show(this, "Connecté à l'automate");
                }else {
                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.writeS7.stop();

                    this.btn_pharma_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorGreen));
                    this.btn_pharma_connect.setText("Se connecter à l'automate");
                    this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);
                    this.ch_pharma_convoyeur.setVisibility(View.INVISIBLE);
                    ToastService.show(this, "Déconnecté de l'automate");
                }
            }
        }else {
            this.session.closeSession();
            ToastService.show(this, "Action impossible ! Vous n'êtes connecté à aucun réseau");
        }

    }
}