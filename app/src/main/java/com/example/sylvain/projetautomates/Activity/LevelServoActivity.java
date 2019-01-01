package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Tasks.ReadServoDBTask;
import com.example.sylvain.projetautomates.Utils.LoadProperties;
import com.example.sylvain.projetautomates.Utils.Network;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastUtil;
import com.example.sylvain.projetautomates.Tasks.WriteTaskS7;

public class LevelServoActivity extends AppCompatActivity {
    // Ranks
    private final static int BASIC_RANK = 1;
    private final static int ADMIN_RANK = 2;

    // View components
    private Toolbar toolbar = null;
    private TextView action_bar_title;
    private Button btn_servo_connect;
    private LinearLayout linear_servo_container;
    private CheckBox ch_servo_sluicegate1;
    private CheckBox ch_servo_sluicegate2;
    private CheckBox ch_servo_sluicegate3;
    private CheckBox ch_servo_sluicegate4;
    private CheckBox ch_servo_auto_manual;
    private CheckBox ch_servo_local_remote;
    private LinearLayout linear_servo_write_container;
    private EditText et_servo_level;
    private EditText et_servo_auto;
    private EditText et_servo_manual;
    private EditText et_servo_sluicegate_word;
    private TextView tv_servo_status_sluicegate_1;
    private TextView tv_servo_status_sluicegate_2;
    private TextView tv_servo_status_sluicegate_3;
    private TextView tv_servo_status_sluicegate_4;
    private TextView tv_servo_read_auto_manual;
    private ProgressBar pb_servo_liquid_level;
    private TextView tv_servo_read_auto_order;
    private TextView tv_servo_read_manual_order;
    private TextView tv_servo_read_sluicegate_word;
    private TextView tv_servo_read_local_remote;

    // Read task
    private ReadServoDBTask readS7DBB0;

    // Write task
    private WriteTaskS7 writeS7DBB2;

    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    // Thread running flag
    boolean isRunning = false;

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_servo);

        this.init();

        this.session = new Session(this);
        this.network = new Network(this);

        // Check network
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                this.action_bar_title.setText("AUTOMATE D'ASSERVISSEMENT");
                this.btn_servo_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.run, 0, 0, 0);

                // If the connected user is not admin, don't show components to write in DB
                if (session.getUser().getRank() == BASIC_RANK) {
                    this.linear_servo_write_container.setVisibility(View.GONE);
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

    private void init(){
        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.action_bar_title = findViewById(R.id.action_bar_title);
        this.btn_servo_connect = findViewById(R.id.btn_servo_connect);
        this.linear_servo_container = findViewById(R.id.linear_servo_container);
        this.ch_servo_sluicegate1 = findViewById(R.id.ch_servo_sluicegate1);
        this.ch_servo_sluicegate2 = findViewById(R.id.ch_servo_sluicegate2);
        this.ch_servo_sluicegate3 = findViewById(R.id.ch_servo_sluicegate3);
        this.ch_servo_sluicegate4 = findViewById(R.id.ch_servo_sluicegate4);
        this.ch_servo_auto_manual = findViewById(R.id.ch_servo_auto_manual);
        this.linear_servo_write_container = findViewById(R.id.linear_servo_write_container);
        this.et_servo_level = findViewById(R.id.et_servo_level);
        this.et_servo_auto = findViewById(R.id.et_servo_auto);
        this.et_servo_manual = findViewById(R.id.et_servo_manual);
        this.et_servo_sluicegate_word = findViewById(R.id.et_servo_sluicegate_word);
        this.tv_servo_status_sluicegate_1 = findViewById(R.id.tv_servo_status_sluicegate_1);
        this.tv_servo_status_sluicegate_2 = findViewById(R.id.tv_servo_status_sluicegate_2);
        this.tv_servo_status_sluicegate_3 = findViewById(R.id.tv_servo_status_sluicegate_3);
        this.tv_servo_status_sluicegate_4 = findViewById(R.id.tv_servo_status_sluicegate_4);
        this.tv_servo_read_auto_manual = findViewById(R.id.tv_servo_read_auto_manual);
        this.pb_servo_liquid_level = findViewById(R.id.pb_servo_liquid_level);
        this.tv_servo_read_auto_order = findViewById(R.id.tv_servo_read_auto_order);
        this.tv_servo_read_manual_order = findViewById(R.id.tv_servo_read_manual_order);
        this.tv_servo_read_sluicegate_word = findViewById(R.id.tv_servo_read_sluicegate_word);
        this.ch_servo_local_remote = findViewById(R.id.ch_servo_local_remote);
        this.tv_servo_read_local_remote = findViewById(R.id.tv_servo_read_local_remote);
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
            case R.id.item_logout:
                this.session.closeSession();
                ToastUtil.show(this, "Déconnecté");
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

    public void openMenu(View view) {
        // Show menu when click on the hamburger
        this.toolbar.showOverflowMenu();
    }

    public void onServoClickManager(View v) {
        // Check network
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                // Check if admin
                if (session.getUser().getRank() == ADMIN_RANK) {
                    switch (v.getId()) {
                        case R.id.ch_servo_sluicegate1:
                            if (this.writeS7DBB2 != null) {
                                // Write in DB to active the sluicegate 1
                                this.writeS7DBB2.setWriteBool(2, this.ch_servo_sluicegate1.isChecked() ? 1 : 0);
                            }
                            break;
                        case R.id.ch_servo_sluicegate2:
                            if (this.writeS7DBB2 != null) {
                                // Write in DB to active the sluicegate 2
                                this.writeS7DBB2.setWriteBool(4, this.ch_servo_sluicegate2.isChecked() ? 1 : 0);
                            }
                            break;
                        case R.id.ch_servo_sluicegate3:
                            if (this.writeS7DBB2 != null) {
                                // Write in DB to active the sluicegate 3
                                this.writeS7DBB2.setWriteBool(8, this.ch_servo_sluicegate3.isChecked() ? 1 : 0);
                            }
                            break;
                        case R.id.ch_servo_sluicegate4:
                            if (this.writeS7DBB2 != null) {
                                // Write in DB to active the sluicegate 4
                                this.writeS7DBB2.setWriteBool(16, this.ch_servo_sluicegate4.isChecked() ? 1 : 0);
                            }
                            break;
                        case R.id.ch_servo_auto_manual:
                            if (this.writeS7DBB2 != null) {
                                // Write in DB to active the auto or manual mode
                                this.writeS7DBB2.setWriteBool(32, this.ch_servo_auto_manual.isChecked() ? 1 : 0);
                            }
                            break;
                        case R.id.ch_servo_local_remote:
                            if (this.writeS7DBB2 != null) {
                                // Write in DB to active the auto or manual mode
                                this.writeS7DBB2.setWriteBool(64, this.ch_servo_local_remote.isChecked() ? 1 : 0);
                            }
                            break;
                        case R.id.btn_servo_level:
                            if (!(this.et_servo_level.getText().toString().trim().length() == 0)) {

                                if (this.writeS7DBB2 != null) {
                                    // Write in DB to set the liquid level
                                    try {
                                        Integer level = Integer.parseInt(this.et_servo_level.getText().toString());
                                        if (level >= 0 && level <= 1000)
                                            this.writeS7DBB2.setLevel(level);
                                        else
                                            ToastUtil.show(this, "Veuillez entrer un nombre compris entre 0 et 1000");
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }else {
                                ToastUtil.show(this,"Veuillez entrer un niveau de liquide");
                            }
                            break;
                        case R.id.btn_servo_auto:
                            if (!(this.et_servo_auto.getText().toString().trim().length() == 0)) {

                                if (this.writeS7DBB2 != null) {
                                    // Write in DB to set auto
                                    try {
                                        Integer auto = Integer.parseInt(this.et_servo_auto.getText().toString());
                                        if (auto >= 0 && auto <= 1000)
                                            this.writeS7DBB2.setAuto(auto);
                                        else
                                            ToastUtil.show(this, "Veuillez entrer un nombre compris entre 0 et 1000");
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }else{
                                ToastUtil.show(this, "Veuillez entrer une consigne auto");
                            }
                            break;
                        case R.id.btn_servo_manual:
                            if (!(this.et_servo_manual.getText().toString().trim().length() == 0)) {
                                if (this.writeS7DBB2 != null) {
                                    // Write in DB to set manual
                                    try {
                                        Integer manual = Integer.parseInt(this.et_servo_manual.getText().toString());
                                        if (manual >= 0 && manual <= 100)
                                            this.writeS7DBB2.setManual(Integer.parseInt(this.et_servo_manual.getText().toString()));
                                        else
                                            ToastUtil.show(this, "Veuillez entrer un nombre compris entre 0 et 100");
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }else {
                                ToastUtil.show(this,"Veuillez entrer une consigne manuelle");
                            }
                            break;
                        case R.id.btn_servo_sluicegate_word:
                            if (!(this.et_servo_sluicegate_word.getText().toString().trim().length() == 0)) {
                                if (this.writeS7DBB2 != null) {
                                    // Write in DB to set the sluicegate word
                                    try {
                                        Integer sluicegateWord = Integer.parseInt(this.et_servo_sluicegate_word.getText().toString());
                                        if (sluicegateWord >= 0 && sluicegateWord <= 100)
                                            this.writeS7DBB2.setSluicegateWord(sluicegateWord);
                                        else
                                            ToastUtil.show(this, "Veuillez entrer un nombre compris entre 0 et 100");
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }else {
                                ToastUtil.show(this,"Veuillez entrer un mot de pilotage des vannes");
                            }
                            break;
                    }
                }
            }
        } else {
            this.session.closeSession();
            ToastUtil.show(this, "Vous n'êtes connecté à aucun réseau");
        }
    }

    public void connectToAutomaton(View v) {
        // Check if there is a network connectivity
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                if (this.btn_servo_connect.getText().equals("Se connecter à l'automate")) {

                    // If admin
                    if (session.getUser().getRank() == ADMIN_RANK) {
                        // WriteTask for DB5.DBB2
                        this.writeS7DBB2 = new WriteTaskS7(2);
                        // Connection to the automaton
                        this.writeS7DBB2.start(this.ipAddress, this.rack, this.slot);
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // ReadTask for DB5.DBB0
                    this.readS7DBB0 = new ReadServoDBTask(0, this,
                            this.tv_servo_status_sluicegate_1,
                            this.tv_servo_status_sluicegate_2,
                            this.tv_servo_status_sluicegate_3,
                            this.tv_servo_status_sluicegate_4,
                            this.tv_servo_read_auto_manual,
                            this.pb_servo_liquid_level,
                            this.tv_servo_read_auto_order,
                            this.tv_servo_read_manual_order,
                            this.tv_servo_read_sluicegate_word,
                            this.tv_servo_read_local_remote);
                    // Connection to the automaton
                    this.readS7DBB0.start(this.ipAddress, this.rack, this.slot);

                    // Running flag to true
                    this.isRunning = true;

                    // Reset value in DB
                    this.resetDB();

                    // Set components visible
                    this.connectDisplay();

                    ToastUtil.show(this, "Connecté à l'automate");
                } else {
                    // Stop thread
                    if (this.writeS7DBB2 != null) {
                        this.writeS7DBB2.stop();
                        // Wait 0.1 second
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Stop thread
                    if (this.readS7DBB0 != null) {
                        this.readS7DBB0.stop();
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

        // Stop threads when activity is destroyed
        if (isRunning) {
            if (this.writeS7DBB2 != null) {
                this.writeS7DBB2.stop();
                // Wait 0.1 second
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (this.readS7DBB0 != null) {
                this.readS7DBB0.stop();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void connectDisplay() {
        this.btn_servo_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
        this.btn_servo_connect.setText("Se déconnecter de l'automate");
        this.btn_servo_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.stop, 0, 0, 0);
        this.linear_servo_container.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SetTextI18n")
    private void disconnectDisplay() {
        this.btn_servo_connect.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorGreen));
        this.btn_servo_connect.setText("Se connecter à l'automate");
        this.btn_servo_connect.setCompoundDrawablesWithIntrinsicBounds(R.drawable.run, 0, 0, 0);
        this.linear_servo_container.setVisibility(View.INVISIBLE);
    }

    // Reset DB values when connect to automaton
    private void resetDB() {
        if (this.writeS7DBB2 != null) {
            this.writeS7DBB2.setLevel(0);
            this.writeS7DBB2.setAuto(0);
            this.writeS7DBB2.setManual(0);
            this.writeS7DBB2.setSluicegateWord(0);
        }
    }
}
