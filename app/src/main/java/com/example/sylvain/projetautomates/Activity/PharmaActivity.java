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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.sylvain.projetautomates.Utils.LoadProperties;
import com.example.sylvain.projetautomates.Utils.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.ReadDBTask;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastService;
import com.example.sylvain.projetautomates.WriteTaskS7;

import org.w3c.dom.Text;

public class PharmaActivity extends AppCompatActivity {

    private Toolbar toolbar = null;
    private TextView action_bar_title;
    private CheckBox ch_pharma_convoyeur;
    private Button btn_pharma_connect;
    private RadioButton rb_pharma_pills5;
    private RadioButton rb_pharma_pills10;
    private RadioButton rb_pharma_pills15;
    private CheckBox ch_pharma_bottle;
    private CheckBox ch_pharma_resetCounter;
    private LinearLayout linear_pharma_container;
    private TextView tv_pharma_conveyor_state;
    private TextView tv_pharma_read_number_tablets;
    private TextView tv_pharma_read_bottles_status;
    private TextView tv_pharma_read_live_tablets;
    private TextView tv_pharma_read_live_bottles;

    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    private WriteTaskS7 writeS7DBB5;
    private WriteTaskS7 writeS7DBB6;

    private ReadDBTask readS7DBB0;
    private ReadDBTask readS7DBB4;
    private ReadDBTask readS7DBB1;

    boolean isRunning = false;

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
        this.rb_pharma_pills5 = (RadioButton)findViewById(R.id.rb_pharma_pills5);
        this.rb_pharma_pills10 = (RadioButton)findViewById(R.id.rb_pharma_pills10);
        this.rb_pharma_pills15 = (RadioButton)findViewById(R.id.rb_pharma_pills15);
        this.ch_pharma_bottle = (CheckBox)findViewById(R.id.ch_pharma_bottle);
        this.ch_pharma_resetCounter = (CheckBox) findViewById(R.id.ch_pharma_resetCounter);
        this.tv_pharma_conveyor_state = (TextView)findViewById(R.id.tv_pharma_conveyor_state);
        this.tv_pharma_read_number_tablets = (TextView)findViewById(R.id.tv_pharma_read_number_tablets);
        this.tv_pharma_read_bottles_status = (TextView)findViewById(R.id.tv_pharma_read_bottles_status);
        this.tv_pharma_read_live_tablets = (TextView)findViewById(R.id.tv_pharma_read_live_tablets);
        this.tv_pharma_read_live_bottles = (TextView)findViewById(R.id.tv_pharma_read_live_bottles);

        this.linear_pharma_container = (LinearLayout)findViewById(R.id.linear_pharma_container);

        this.session = new Session(this);
        this.network = new Network(this);

        // Check network
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

    public void onPharmaClickManager (View v) {
        // Check network
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                switch (v.getId()){
                    case R.id.ch_pharma_convoyeur :
                        // Write in DB to active the conveyor
                        writeS7DBB5.setWriteBool(1, this.ch_pharma_convoyeur.isChecked() ? 1 : 0);
                        break;

                    case R.id.rb_pharma_pills5 :
                        // Set the correct value in the DB for 5 tablets
                        writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
                        writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
                        writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
                        break;

                    case R.id.rb_pharma_pills10 :
                        // Set the correct value in the DB for 10 tablets
                        writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
                        writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
                        writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
                        break;

                    case R.id.rb_pharma_pills15 :
                        // Set the correct value in the DB for 15 tablets
                        writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
                        writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
                        writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
                        break;
                    case R.id.ch_pharma_bottle :
                        // Set the value in DB to active the bottles arrival
                        writeS7DBB6.setWriteBool(8, this.ch_pharma_bottle.isChecked() ? 1 : 0);
                        break;
                    case R.id.ch_pharma_resetCounter :
                        // Set the value in DB to reset the bottle counter
                        writeS7DBB6.setWriteBool(4, this.ch_pharma_resetCounter.isChecked() ? 1 : 0);
                        break;
                }
            }
        }else {
            this.session.closeSession();
            ToastService.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    public void connectAutomaton (View v) {
        // Check if there is a network connectivity
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                if(this.btn_pharma_connect.getText().equals("Se connecter à l'automate")) {

                    // WriteTask for DB5.DBB5
                    this.writeS7DBB5 = new WriteTaskS7(5);

                    // WriteTask for DB5.DBB6
                    this.writeS7DBB6 = new WriteTaskS7(6);

                    // ReadTask for DB5.DBB0
                    this.readS7DBB0 = new ReadDBTask(0,this, tv_pharma_conveyor_state, tv_pharma_read_number_tablets, tv_pharma_read_bottles_status, tv_pharma_read_live_tablets, tv_pharma_read_live_bottles);

                    // ReadTask for DB5.DBB4
                    this.readS7DBB4 = new ReadDBTask(4,this, tv_pharma_conveyor_state, tv_pharma_read_number_tablets, tv_pharma_read_bottles_status, tv_pharma_read_live_tablets, tv_pharma_read_live_bottles);

                    // ReadTask for DB5.DBB1
                    this.readS7DBB1 = new ReadDBTask(1,this, tv_pharma_conveyor_state, tv_pharma_read_number_tablets, tv_pharma_read_bottles_status, tv_pharma_read_live_tablets, tv_pharma_read_live_bottles);

                    // Connection to the automaton
                    this.writeS7DBB5.start(this.ipAddress, this.rack, this.slot);

                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Connection to the automaton
                    this.writeS7DBB6.start(this.ipAddress, this.rack, this.slot);

                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.readS7DBB0.start(this.ipAddress, this.rack, this.slot);

                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.readS7DBB4.start(this.ipAddress, this.rack, this.slot);

                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.readS7DBB1.start(this.ipAddress, this.rack, this.slot);

                    this.isRunning = true;
                    // Reinitiate the values in DB
                    this.reinitiateDB();

                    // Set components visible
                    this.connectDisplay();

                    ToastService.show(this, "Connecté à l'automate");
                }else {

                    this.writeS7DBB5.stop();
                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.writeS7DBB6.stop();
                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.readS7DBB0.stop();
                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.readS7DBB4.stop();
                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    this.readS7DBB1.stop();

                    this.isRunning = false;

                    // Set components invisible
                    this.disconnectDisplay();

                    ToastService.show(this, "Déconnecté de l'automate");
                }
            }
        }else {
            this.session.closeSession();
            ToastService.show(this, "Action impossible ! Vous n'êtes connecté à aucun réseau");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(isRunning) {
            this.writeS7DBB5.stop();
            this.writeS7DBB6.stop();
            this.readS7DBB0.stop();
            this.readS7DBB4.stop();
            this.readS7DBB1.stop();
        }
    }

    @SuppressLint("SetTextI18n")
    public void connectDisplay() {
        this.btn_pharma_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
        this.btn_pharma_connect.setText("Se déconnecter de l'automate");
        this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds( R.drawable.stop, 0, 0, 0);
        this.linear_pharma_container.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    public void disconnectDisplay() {
        this.btn_pharma_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorGreen));
        this.btn_pharma_connect.setText("Se connecter à l'automate");
        this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds( R.drawable.run, 0, 0, 0);
        this.linear_pharma_container.setVisibility(View.INVISIBLE);
    }

    public void reinitiateDB() {
        this.ch_pharma_convoyeur.setChecked(false);
        this.rb_pharma_pills5.setChecked(false);
        this.rb_pharma_pills10.setChecked(false);
        this.rb_pharma_pills15.setChecked(false);
        this.ch_pharma_bottle.setChecked(false);
        this.ch_pharma_resetCounter.setChecked(false);

        writeS7DBB5.setWriteBool(1, this.ch_pharma_convoyeur.isChecked() ? 1 : 0);
        writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
        writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
        writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
        writeS7DBB6.setWriteBool(8, this.ch_pharma_bottle.isChecked() ? 1 : 0);
        writeS7DBB6.setWriteBool(4, this.ch_pharma_resetCounter.isChecked() ? 1 : 0);
    }
}
