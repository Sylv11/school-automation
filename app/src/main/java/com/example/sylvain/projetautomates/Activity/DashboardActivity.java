package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.example.sylvain.projetautomates.Utils.LoadProperties;
import com.example.sylvain.projetautomates.Utils.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Tasks.ReadTaskS7;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastUtil;
import com.example.sylvain.projetautomates.Tasks.TogglePLCStatusTask;


/* This activity displays all the useful information's
 * of automaton and the CPU */


public class DashboardActivity extends AppCompatActivity {

    // Basic rank
    private final static int BASIC_RANK = 1;

    // Textview, Button and Toolbar (information's about CPU)
    private TextView tv_dashboard_numCPU;
    private TextView tv_dashboard_statusCPU;
    private TextView tv_dashboard_modelPU;
    private TextView tv_dashboard_error;
    private Button btn_dashboard_powerPLC;
    private Toolbar toolbar = null;
    private TextView tv_dashboard_ipAddress;
    private TextView tv_dashboard_rackNum;
    private TextView tv_dashboard_slotNum;
    private LinearLayout ll_dashboard_container;

    private boolean CPLstatus;

    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    // Task to read the information's
    private ReadTaskS7 readS7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        this.init();

        this.session = new Session(this);
        this.network = new Network(this);

        // Check if there is a network connectivity
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {

                this.ll_dashboard_container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in1s));

                // Load properties file
                try {
                    this.ipAddress = LoadProperties.getProperty("ip_address", this);
                    this.rack = LoadProperties.getProperty("rack", this);
                    this.slot = LoadProperties.getProperty("slot", this);

                    // Show properties
                    this.tv_dashboard_ipAddress.setText(this.ipAddress);
                    this.tv_dashboard_ipAddress.setTextColor(Color.GRAY);

                    this.tv_dashboard_rackNum.setText(this.rack);
                    this.tv_dashboard_rackNum.setTextColor(Color.GRAY);

                    this.tv_dashboard_slotNum.setText(this.slot);
                    this.tv_dashboard_slotNum.setTextColor(Color.GRAY);

                }catch(Exception e) {
                    e.printStackTrace();
                }

                // Start the read task to put the information's in the different TextView's
                this.readS7 = new ReadTaskS7(this.tv_dashboard_numCPU,
                                             this.tv_dashboard_statusCPU,
                                             this.tv_dashboard_error,
                                             this.tv_dashboard_modelPU,
                                             this.btn_dashboard_powerPLC, this);
                this.readS7.start(this.ipAddress, this.rack, this.slot);

                // Run and stop button only for the superuser
                if(session.getUser().getRank() == BASIC_RANK) {
                    this.btn_dashboard_powerPLC.setVisibility(View.GONE);
                }
            }else {
                session.closeSession();
                ToastUtil.show(this, "Vous n'êtes pas connecté");
            }
        }else {
            this.session.closeSession();
            ToastUtil.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    private void init(){
        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.tv_dashboard_numCPU = findViewById(R.id.tv_dashboard_numCPU);
        this.tv_dashboard_modelPU = findViewById(R.id.tv_dashboard_modelPU);
        this.tv_dashboard_statusCPU = findViewById(R.id.tv_dashboard_statusCPU);
        this.tv_dashboard_error = findViewById(R.id.tv_dashboard_error);
        this.btn_dashboard_powerPLC = findViewById(R.id.btn_dashboard_powerPLC);
        this.tv_dashboard_ipAddress = findViewById(R.id.tv_dashboard_ipAddress);
        this.tv_dashboard_rackNum = findViewById(R.id.tv_dashboard_rackNum);
        this.tv_dashboard_slotNum = findViewById(R.id.tv_dashboard_slotNum);
        this.ll_dashboard_container = findViewById(R.id.ll_dashboard_container);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Show the action bar
        if (menu instanceof MenuBuilder) ((MenuBuilder) menu).setOptionalIconsVisible(true);

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // In the case of a click on a navigation item, we redirect to the right activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //  Redirect to dashboard activity
            case R.id.item_dashboard:
                Intent dashboardIntent = new Intent(this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
                break;

            // Logout and close user session
            case R.id.item_logout :
                this.session.closeSession();
                ToastUtil.show(this,"Déconnecté");
                break;

            //  Redirect to pharmaceutic activity
            case R.id.item_pharmaceutical:
                Intent pharmaIntent = new Intent(this, PharmaActivity.class);
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

            // Redirect to manual activity
            case R.id.item_manual_settings:
                Intent manualIntent = new Intent(this, ManualActivity.class);
                manualIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(manualIntent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Show menu when click on the hamburger
    public void openMenu(View view) {
        this.toolbar.showOverflowMenu();
    }

    // Refresh dashboard
    public void refreshInfo (View v) {
        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(dashboardIntent);
        finish();
        overridePendingTransition(0, 0);
    }

    // This method allows us to toggle the PLC status from run to stop (or the opposite)
    public void togglePLCStatus (View v) {

        // Check if there is a network connectivity
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                // Synchronize the run/stop button with the PLC status
                if(this.tv_dashboard_statusCPU.getText().equals("En fonctionnement")) {
                    this.CPLstatus = true;

                    this.setRunButton();
                }
                if(this.tv_dashboard_statusCPU.getText().equals("Stoppé")) {
                    this.CPLstatus = false;

                    this.setStopButton();
                }
                // Task for toggle PLC status
                TogglePLCStatusTask togglePLCStatusTask = new TogglePLCStatusTask(this.CPLstatus, this.tv_dashboard_statusCPU);
                togglePLCStatusTask.start(this.ipAddress, this.rack, this.slot);
            }
        }else {
            ToastUtil.show(this, "Action impossible ! Vous n'êtes connecté à aucun réseau");
        }
    }

    @SuppressLint("SetTextI18n")
    public void setRunButton () {
        // Change the run/stop button properties
        this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorGreen));
        this.btn_dashboard_powerPLC.setText("Run");
        this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);
    }

    @SuppressLint("SetTextI18n")
    public void setStopButton () {
        // Change the run/stop button properties
        this.btn_dashboard_powerPLC.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
        this.btn_dashboard_powerPLC.setText("Stop");
        this.btn_dashboard_powerPLC.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop, 0, 0, 0);
    }

}
