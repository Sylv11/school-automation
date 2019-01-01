package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Tasks.WriteTaskS7;
import com.example.sylvain.projetautomates.Utils.DataBlock;
import com.example.sylvain.projetautomates.Utils.LoadProperties;
import com.example.sylvain.projetautomates.Utils.Network;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastUtil;

public class ManualActivity extends AppCompatActivity {

    // Ranks
    private final static int BASIC_RANK = 1;
    private final static int ADMIN_RANK = 2;

    // View components
    private Toolbar toolbar = null;
    private TextView action_bar_title;
    private EditText et_manual_DB;
    private Button btn_manual_DB;
    private LinearLayout ll_manual;
    private EditText et_manual_address;
    private EditText et_manual_value;
    private WriteTaskS7 writeS7byte;
    private Button btn_manual_send;

    // User session and Network connectivity
    private Session session;
    private Network network;

    // Information to communicate with the automaton
    private String ipAddress;
    private String rack;
    private String slot;

    private Integer oldValue = 0;
    private Integer addressNumber;
    private Integer value;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        // Init components
        this.init();

        this.session = new Session(this);
        this.network = new Network(this);

        // Check network
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                this.action_bar_title.setText("CONFIGURATION MANUELLE");
                this.et_manual_DB.setHint(String.valueOf(DataBlock.DB));

                // If the connected user is not admin
                if (this.session.getUser().getRank() == BASIC_RANK) {
                    this.et_manual_DB.setEnabled(false);
                    this.btn_manual_DB.setEnabled(false);
                    this.btn_manual_DB.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorDarkGray));
                    this.ll_manual.setVisibility(View.GONE);
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
        this.et_manual_DB = findViewById(R.id.et_manual_DB);
        this.btn_manual_DB = findViewById(R.id.btn_manual_DB);
        this.ll_manual = findViewById(R.id.ll_manual);
        this.et_manual_address = findViewById(R.id.et_manual_address);
        this.et_manual_value = findViewById(R.id.et_manual_value);
        this.btn_manual_send = findViewById(R.id.btn_manual_send);
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

    public void onManualClickManager(View v) {
        // Check network
        if (this.network.checkNetwork()) {
            // Check if the user is connected
            if (this.session.isLogged()) {
                // Check if admin
                if (session.getUser().getRank() == ADMIN_RANK) {
                    switch (v.getId()) {
                        case R.id.btn_manual_DB:
                            // If the EditText is not empty
                            if (!(this.et_manual_DB.getText().toString().trim().length() == 0)) {

                                // Set the DB number
                                Integer DBValue = Integer.valueOf(this.et_manual_DB.getText().toString());
                                DataBlock.setDB(DBValue);

                                // Refresh activity
                                Intent manualIntent = new Intent(this, ManualActivity.class);
                                manualIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(manualIntent);
                                finish();
                                overridePendingTransition(0, 0);
                            } else {
                                ToastUtil.show(this, "Veuillez entrer un numéro de DB");
                            }
                            break;
                        case R.id.btn_manual_send:
                            // If the EditTexts are not empty
                            if (!(this.et_manual_address.getText().toString().trim().length() == 0 || this.et_manual_value.getText().toString().trim().length() == 0)) {
                                // If admin
                                if (session.getUser().getRank() == ADMIN_RANK) {
                                    this.addressNumber = Integer.valueOf(et_manual_address.getText().toString());
                                    this.value = Integer.valueOf(et_manual_value.getText().toString());

                                    // Address value between 0 and 34
                                    if (addressNumber >= 0 && addressNumber <= 34) {

                                        // WriteTask
                                        this.writeS7byte = new WriteTaskS7(addressNumber);
                                        // Connection to the automaton
                                        this.writeS7byte.start(this.ipAddress, this.rack, this.slot);

                                        // Write the value in DB in correct address
                                        if (this.writeS7byte != null) {
                                            this.writeS7byte.setWriteBool(oldValue, 0);
                                            this.writeS7byte.setWriteBool(value, 1);
                                            this.oldValue = value;
                                        }

                                        // Wait 0.2 second
                                        try {
                                            Thread.sleep(200);
                                        } catch (InterruptedException ignored) {
                                        }

                                        // Stop the thread
                                        if (this.writeS7byte != null) {
                                            this.writeS7byte.stop();
                                        }

                                    } else {
                                        ToastUtil.show(this, "Veuillez entrer une adresse comprise entre 0 et 34");
                                    }
                                }

                            } else {
                                ToastUtil.show(this, "Veuillez entrer des valeurs");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this.writeS7byte != null) {
            this.writeS7byte.stop();
        }
    }

}
