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
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.example.sylvain.projetautomates.LoadProperties;
import com.example.sylvain.projetautomates.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.ReadTaskS7;
import com.example.sylvain.projetautomates.Session;
import com.example.sylvain.projetautomates.ToastService;
import com.example.sylvain.projetautomates.TogglePLCStatusTask;

public class DashboardActivity extends AppCompatActivity {

    private TextView tv_dashboard_numCPU;
    private TextView tv_dashboard_statusCPU;
    private TextView tv_dashboard_modelPU;
    private TextView tv_dashboard_error;
    private Button btn_dashboard_powerPLC;
    private Toolbar toolbar = null;

    private boolean CPLstatus;

    private Session session;
    private Network network;

    private String ipAddress;
    private String rack;
    private String slot;

    private ReadTaskS7 readS7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);

        this.tv_dashboard_numCPU = (TextView)findViewById(R.id.tv_dashboard_numCPU);
        this.tv_dashboard_modelPU = (TextView)findViewById(R.id.tv_dashboard_modelPU);
        this.tv_dashboard_statusCPU = (TextView)findViewById(R.id.tv_dashboard_statusCPU);
        this.tv_dashboard_error = (TextView)findViewById(R.id.tv_dashboard_error);
        this.btn_dashboard_powerPLC = (Button)findViewById(R.id.btn_dashboard_powerPLC);

        this.session = new Session(this);
        this.network = new Network(this);

        if(this.network.checkNetwork()) {
            if(this.session.isLogged()) {

                try {
                    this.ipAddress = LoadProperties.getProperty("ip_address", this);
                    this.rack = LoadProperties.getProperty("rack", this);
                    this.slot = LoadProperties.getProperty("slot", this);
                }catch(Exception e) {
                    e.printStackTrace();
                }

                this.readS7 = new ReadTaskS7(this.tv_dashboard_numCPU, this.tv_dashboard_statusCPU, this.tv_dashboard_error, this.tv_dashboard_modelPU, this.btn_dashboard_powerPLC, this);
                this.readS7.start(this.ipAddress,this.rack, this.slot);

            }
        }else {
            this.session.closeSession();
            ToastService.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_dashboard_logout :
                this.session.closeSession();
                ToastService.show(this,"Déconnecté");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMenu(View view) {
        this.toolbar.showOverflowMenu();
    }

    public void refreshInfo (View v) {
        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(dashboardIntent);

    }

    public void togglePLCStatus (View v) {

        if(this.tv_dashboard_statusCPU.getText().equals("En fonctionnement")) {
            this.CPLstatus = true;

            this.setRunButton();
        }
        if(this.tv_dashboard_statusCPU.getText().equals("Stoppé")) {
            this.CPLstatus = false;

            this.setStopButton();
        }
        TogglePLCStatusTask togglePLCStatusTask = new TogglePLCStatusTask(this.CPLstatus, this.tv_dashboard_statusCPU);
        togglePLCStatusTask.start(this.ipAddress, this.rack, this.slot);

    }

    public void setRunButton () {
        this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorGreen));
        this.btn_dashboard_powerPLC.setText("Run");
        this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);
    }

    public void setStopButton () {
        this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
        this.btn_dashboard_powerPLC.setText("Stop");
        this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop, 0, 0, 0);
    }

}
