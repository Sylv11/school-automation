package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sylvain.projetautomates.DB.User;
import com.example.sylvain.projetautomates.DB.UserAccessDB;
import com.example.sylvain.projetautomates.Network;
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Session;
import com.example.sylvain.projetautomates.ToastService;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private Toolbar toolbar = null;
    private TextView action_bar_title;

    // User session and Network connectivity
    private Session session;
    private Network network;

    private Context context = this;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.action_bar_title = (TextView)findViewById(R.id.action_bar_title);

        this.session = new Session(this);
        this.network = new Network(this);

        // Check network
        if(this.network.checkNetwork()) {
            // Check if the user is connected
            if(this.session.isLogged()) {
                this.action_bar_title.setText("GESTION UTILISATEURS");
                // Display the users and allow to change their rights (RW or R)
                this.initUsersTextViews();
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

    @SuppressLint("SetTextI18n")
    public void initUsersTextViews() {
        // Read in DB to get all users
        UserAccessDB userDB = new UserAccessDB(this);
        userDB.openForRead();
        ArrayList<User> users = userDB.getUsers();
        userDB.Close();

        // For each user, create a TextView and change his rights (R or RW)
        for (User user : users) {
            final User currentUser = user;

            // Get the LinearLayout
            LinearLayout linearLayout = findViewById(R.id.layout_admin_textViews);

            // Make an horizontal line
            View line = new View(this);
            line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            line.setBackgroundColor(Color.rgb(51, 51, 51));
            linearLayout.addView(line);

            LinearLayout linearLayoutVertical = new LinearLayout(this);
            linearLayoutVertical.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayoutVertical.setOrientation(LinearLayout.HORIZONTAL);

            // TextView that contains a user
            TextView dynamicTextView = new TextView(this);
            dynamicTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            dynamicTextView.setGravity(Gravity.START);
            dynamicTextView.setTextSize(18);
            dynamicTextView.setPadding(70,30,100,30);
            dynamicTextView.setText(user.getFirstname() + " " + user.getLastname());

            User userSession = session.getUser();

            if(userSession.getEmail().equals(currentUser.getEmail())) {
                dynamicTextView.setTextColor(getResources().getColor(R.color.colorGreen));
            }

            if(currentUser.getRank() == 2) {
                dynamicTextView.setTypeface(null, Typeface.BOLD);

            }

            linearLayoutVertical.addView(dynamicTextView);


            if(!userSession.getEmail().equals(currentUser.getEmail())) {
                // Delete button
                final Button deleteButton = new Button(this);
                deleteButton.setLayoutParams(new LinearLayout.LayoutParams(240, LinearLayout.LayoutParams.WRAP_CONTENT));
                deleteButton.setCompoundDrawablesWithIntrinsicBounds( R.drawable.delete_user, 0, 0, 0);
                deleteButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
                deleteButton.setTextColor(getResources().getColor(R.color.colorWhite));
                deleteButton.setGravity(Gravity.END);
                linearLayoutVertical.addView(deleteButton);

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View v) {
                        UserAccessDB deleteUserDB = new UserAccessDB(context);
                        deleteUserDB.openForWrite();
                        deleteUserDB.removeUser(currentUser.getEmail());
                        deleteUserDB.Close();

                        // Refresh the activity
                        Intent adminActivity = new Intent(context, AdminActivity.class);
                        adminActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(adminActivity);
                    }
                });
            }

            // Click on the TextView allow us to change his rights
            dynamicTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    UserAccessDB updatedUserDB = new UserAccessDB(context);
                    if(currentUser.getRank() == 1) {
                        // Change the rank
                        User updatedUser = new User(currentUser.getLastname(), currentUser.getFirstname(), currentUser.getEmail(), currentUser.getPassword(), 2);
                        updatedUserDB.openForWrite();
                        updatedUserDB.removeUser(currentUser.getEmail());
                        updatedUserDB.insertUser(updatedUser);
                        updatedUserDB.Close();

                        ToastService.show(context, currentUser.getFirstname() + " est maintenant administrateur (R/W)");
                    }else if(currentUser.getRank() == 2) {
                        // Change the rank
                        User updatedUser = new User(currentUser.getLastname(), currentUser.getFirstname(), currentUser.getEmail(), currentUser.getPassword(), 1);
                        updatedUserDB.openForWrite();
                        updatedUserDB.removeUser(currentUser.getEmail());
                        updatedUserDB.insertUser(updatedUser);
                        updatedUserDB.Close();

                        ToastService.show(context, currentUser.getFirstname() + " est maintenant utilisateur normal (R)");
                    }

                    // Refresh the activity
                    Intent adminActivity = new Intent(context, AdminActivity.class);
                    adminActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(adminActivity);
                }
            });

            linearLayout.addView(linearLayoutVertical);
        }
    }
}