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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.sylvain.projetautomates.Utils.LoadProperties;
import com.example.sylvain.projetautomates.Utils.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Tasks.ReadPharmaDBTask;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastUtil;
import com.example.sylvain.projetautomates.Tasks.WriteTaskS7;

/* This activity allow us to manage the pharmaceutic system.
 * We are able to send data from the application to the automaton
 * to control the system, for example, the conveyor.
 * It also allow us to read useful information's of the running system. */


public class PharmaActivity extends AppCompatActivity {

    // Ranks
    private final static int BASIC_RANK = 1;
    private final static int ADMIN_RANK = 2;

    // View components
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
    private EditText et_pharma_tablets_number;
    private LinearLayout linear_pharma_write_container;
    private EditText et_pharma_bottles_number;
    private LinearLayout ll_pharma_read_container;

    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    // Write tasks
    private WriteTaskS7 writeS7DBB5;
    private WriteTaskS7 writeS7DBB6;
    private WriteTaskS7 writeS7DBB8;

    // Read tasks
    private ReadPharmaDBTask readS7DBB0;
    private ReadPharmaDBTask readS7DBB4;
    private ReadPharmaDBTask readS7DBB1;

    // Old value for tablets number
    Integer oldValueTablets = 0;

    // Thread running flag
    boolean isRunning = false;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pharma);

        this.init();

        this.session = new Session(this);
        this.network = new Network(this);

        // Check network
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                this.action_bar_title.setText("AUTOMATE PHARMACEUTIQUE");
                this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.run, 0, 0, 0);

                // If the connected user is not admin, don't show components to write in DB
                if (session.getUser().getRank() == BASIC_RANK) {
                    this.linear_pharma_write_container.setVisibility(View.GONE);
                }

                // Load properties file
                try {
                    this.ipAddress = LoadProperties.getProperty("ip_address", this);
                    this.rack = LoadProperties.getProperty("rack", this);
                    this.slot = LoadProperties.getProperty("slot", this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                session.closeSession();
                ToastUtil.show(this, "Vous n'êtes pas connecté");
            }
        } else {
            this.session.closeSession();
            ToastUtil.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    private void init() {
        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.action_bar_title = findViewById(R.id.action_bar_title);
        this.ch_pharma_convoyeur = findViewById(R.id.ch_pharma_convoyeur);
        this.btn_pharma_connect = findViewById(R.id.btn_pharma_connect);
        this.rb_pharma_pills5 = findViewById(R.id.rb_pharma_pills5);
        this.rb_pharma_pills10 = findViewById(R.id.rb_pharma_pills10);
        this.rb_pharma_pills15 = findViewById(R.id.rb_pharma_pills15);
        this.ch_pharma_bottle = findViewById(R.id.ch_pharma_bottle);
        this.ch_pharma_resetCounter = findViewById(R.id.ch_pharma_resetCounter);
        this.tv_pharma_conveyor_state = findViewById(R.id.tv_pharma_conveyor_state);
        this.tv_pharma_read_number_tablets = findViewById(R.id.tv_pharma_read_number_tablets);
        this.tv_pharma_read_bottles_status = findViewById(R.id.tv_pharma_read_bottles_status);
        this.tv_pharma_read_live_tablets = findViewById(R.id.tv_pharma_read_live_tablets);
        this.tv_pharma_read_live_bottles = findViewById(R.id.tv_pharma_read_live_bottles);
        this.linear_pharma_container = findViewById(R.id.linear_pharma_container);
        this.et_pharma_tablets_number = findViewById(R.id.et_pharma_tablets_number);
        this.linear_pharma_write_container = findViewById(R.id.linear_pharma_write_container);
        this.et_pharma_bottles_number = findViewById(R.id.et_pharma_bottles_number);
        this.ll_pharma_read_container = findViewById(R.id.ll_pharma_read_container);
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
            // Redirect to dashboard activity
            case R.id.item_dashboard:
                Intent dashboardIntent = new Intent(this, DashboardActivity.class);
                startActivity(dashboardIntent);
                finish();
                break;

            // Logout and close user session
            case R.id.item_logout:
                this.session.closeSession();
                ToastUtil.show(this, "Déconnecté");
                break;

            // Redirect to pharmaceutic activity
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
            case R.id.item_admin:
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

    /* This method listens when the user click on one of these item. Then, it calls a method from the WriteTaskS7
     * to write in the data block */

    public void onPharmaClickManager(View v) {
        // Check network
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                switch (v.getId()) {
                    case R.id.ch_pharma_convoyeur:
                        if (this.writeS7DBB5 != null) {
                            // Write in DB to active the conveyor
                            this.writeS7DBB5.setWriteBool(1, this.ch_pharma_convoyeur.isChecked() ? 1 : 0);
                        }
                        break;

                    case R.id.rb_pharma_pills5:
                        if (this.writeS7DBB5 != null) {
                            // Set the correct value in the DB for 5 tablets
                            this.writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
                            this.writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
                            this.writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
                        }
                        break;

                    case R.id.rb_pharma_pills10:
                        if (this.writeS7DBB5 != null) {
                            // Set the correct value in the DB for 10 tablets
                            this.writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
                            this.writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
                            this.writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
                        }
                        break;
                    case R.id.rb_pharma_pills15:
                        if (this.writeS7DBB5 != null) {
                            // Set the correct value in the DB for 15 tablets
                            this.writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
                            this.writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
                            this.writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
                        }
                        break;
                    case R.id.ch_pharma_bottle:
                        if (this.writeS7DBB6 != null) {
                            // Set the value in DB to active the bottles arrival
                            this.writeS7DBB6.setWriteBool(8, this.ch_pharma_bottle.isChecked() ? 1 : 0);
                        }
                        break;
                    case R.id.ch_pharma_resetCounter:
                        if (this.writeS7DBB6 != null) {
                            // Set the value in DB to reset the bottle counter
                            this.writeS7DBB6.setWriteBool(4, this.ch_pharma_resetCounter.isChecked() ? 1 : 0);
                        }
                        break;
                    case R.id.btn_pharma_tablets_number:
                        if (!(this.et_pharma_tablets_number.getText().toString().trim().length() == 0)) {
                            if (this.writeS7DBB8 != null) {
                                // Set the tablets number in DB
                                try {
                                    Integer tabletsNumber = Integer.parseInt(this.et_pharma_tablets_number.getText().toString());
                                    this.writeS7DBB8.setWriteBool(oldValueTablets, 0);
                                    this.writeS7DBB8.setWriteBool(tabletsNumber, 1);
                                    this.oldValueTablets = tabletsNumber;
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        } else {
                            ToastUtil.show(this, "Veuillez entrer un nombre de comprimés");
                        }
                        break;
                    case R.id.btn_pharma_bottles_number:
                        if (!(this.et_pharma_bottles_number.getText().toString().trim().length() == 0)) {

                            if (this.writeS7DBB8 != null) {
                                // Set the bottles number in DB
                                try {
                                    this.writeS7DBB8.setBottles(Integer.parseInt(this.et_pharma_bottles_number.getText().toString()));
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        } else {
                            ToastUtil.show(this, "Veuillez entrer un nombre de flacons");
                        }
                        break;
                }
            }
        } else {
            this.session.closeSession();
            ToastUtil.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    public void connectAutomaton(View v) {
        // Check if there is a network connectivity
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                if (this.btn_pharma_connect.getText().equals("Se connecter à l'automate")) {
                    // Check if admin
                    if (session.getUser().getRank() == ADMIN_RANK) {

                        this.ll_pharma_read_container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in2s));
                        this.linear_pharma_write_container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in1s));

                        // WriteTask for DB5.DBB5
                        this.writeS7DBB5 = new WriteTaskS7(5);
                        // Connection to the automaton
                        this.writeS7DBB5.start(this.ipAddress, this.rack, this.slot);

                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // WriteTask for DB5.DBB6
                        this.writeS7DBB6 = new WriteTaskS7(6);
                        // Connection to the automaton
                        this.writeS7DBB6.start(this.ipAddress, this.rack, this.slot);

                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // WriteTask for DB5.DBB8
                        this.writeS7DBB8 = new WriteTaskS7(8);
                        // Connection to the automaton
                        this.writeS7DBB8.start(this.ipAddress, this.rack, this.slot);

                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    this.ll_pharma_read_container.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in1s));

                    // ReadTask for DB5.DBB0 and set components of the view with the information's
                    this.readS7DBB0 = new ReadPharmaDBTask(0, this,
                            this.tv_pharma_conveyor_state,
                            this.tv_pharma_read_number_tablets,
                            this.tv_pharma_read_bottles_status,
                            this.tv_pharma_read_live_tablets,
                            this.tv_pharma_read_live_bottles);
                    // Connection to the automaton
                    this.readS7DBB0.start(this.ipAddress, this.rack, this.slot);

                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // ReadTask for DB5.DBB4 and set components of the view with the information's
                    this.readS7DBB4 = new ReadPharmaDBTask(4, this,
                            this.tv_pharma_conveyor_state,
                            this.tv_pharma_read_number_tablets,
                            this.tv_pharma_read_bottles_status,
                            this.tv_pharma_read_live_tablets,
                            this.tv_pharma_read_live_bottles);
                    // Connection to the automaton
                    this.readS7DBB4.start(this.ipAddress, this.rack, this.slot);

                    // Wait 0.1 second
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // ReadTask for DB5.DBB1
                    this.readS7DBB1 = new ReadPharmaDBTask(1, this,
                            this.tv_pharma_conveyor_state,
                            this.tv_pharma_read_number_tablets,
                            this.tv_pharma_read_bottles_status,
                            this.tv_pharma_read_live_tablets,
                            this.tv_pharma_read_live_bottles);
                    // Connection to the automaton
                    this.readS7DBB1.start(this.ipAddress, this.rack, this.slot);

                    // Set running flag to true
                    this.isRunning = true;

                    // Reset the values in DB
                    this.resetDB();

                    // Set components visible
                    this.connectDisplay();

                    ToastUtil.show(this, "Connecté à l'automate");
                } else {

                    // Stop threads
                    if (this.writeS7DBB5 != null) {
                        this.writeS7DBB5.stop();
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (this.writeS7DBB6 != null) {
                        this.writeS7DBB6.stop();
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (this.writeS7DBB8 != null) {
                        this.writeS7DBB8.stop();
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (this.readS7DBB0 != null) {
                        this.readS7DBB0.stop();
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (this.readS7DBB4 != null) {
                        this.readS7DBB4.stop();
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (this.readS7DBB1 != null) {
                        this.readS7DBB1.stop();
                    }

                    // Set running flag to false
                    this.isRunning = false;

                    // Set components invisible
                    this.disconnectDisplay();

                    ToastUtil.show(this, "Déconnecté de l'automate");
                }
            }
        } else {
            this.session.closeSession();
            ToastUtil.show(this, "Action impossible ! Vous n'êtes connecté à aucun réseau");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop the threads when activity is destroyed
        if (isRunning) {
            if (this.writeS7DBB5 != null) {
                this.writeS7DBB5.stop();
            }
            if (this.writeS7DBB6 != null) {
                this.writeS7DBB6.stop();
            }
            if (this.writeS7DBB8 != null) {
                this.writeS7DBB8.stop();
            }
            if (this.readS7DBB0 != null) {
                this.readS7DBB0.stop();
            }
            if (this.readS7DBB4 != null) {
                this.readS7DBB4.stop();
            }
            if (this.readS7DBB1 != null) {
                this.readS7DBB1.stop();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void connectDisplay() {
        this.btn_pharma_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
        this.btn_pharma_connect.setText("Se déconnecter de l'automate");
        this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stop, 0, 0, 0);
        this.linear_pharma_container.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void disconnectDisplay() {
        this.btn_pharma_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorGreen));
        this.btn_pharma_connect.setText("Se connecter à l'automate");
        this.btn_pharma_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.run, 0, 0, 0);
        this.linear_pharma_container.setVisibility(View.INVISIBLE);
    }

    // Reset values in DB when connect to the automaton
    private void resetDB() {
        this.ch_pharma_convoyeur.setChecked(false);
        this.rb_pharma_pills5.setChecked(false);
        this.rb_pharma_pills10.setChecked(false);
        this.rb_pharma_pills15.setChecked(false);
        this.ch_pharma_bottle.setChecked(false);
        this.ch_pharma_resetCounter.setChecked(false);

        if (this.writeS7DBB5 != null) {
            this.writeS7DBB5.setWriteBool(1, this.ch_pharma_convoyeur.isChecked() ? 1 : 0);
            this.writeS7DBB5.setWriteBool(2, this.rb_pharma_pills5.isChecked() ? 1 : 0);
            this.writeS7DBB5.setWriteBool(4, this.rb_pharma_pills10.isChecked() ? 1 : 0);
            this.writeS7DBB5.setWriteBool(8, this.rb_pharma_pills15.isChecked() ? 1 : 0);
        }

        if (this.writeS7DBB6 != null) {
            this.writeS7DBB6.setWriteBool(8, this.ch_pharma_bottle.isChecked() ? 1 : 0);
            this.writeS7DBB6.setWriteBool(4, this.ch_pharma_resetCounter.isChecked() ? 1 : 0);
            this.writeS7DBB8.setBottles(0);
        }
    }
}
