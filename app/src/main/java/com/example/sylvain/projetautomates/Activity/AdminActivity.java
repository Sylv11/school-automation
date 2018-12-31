package com.example.sylvain.projetautomates.Activity;

import android.annotation.SuppressLint;
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
import com.example.sylvain.projetautomates.R;
import com.example.sylvain.projetautomates.Utils.Session;
import com.example.sylvain.projetautomates.Utils.ToastUtil;

import java.util.ArrayList;

public class AdminActivity extends AppCompatActivity {

    private final static int BASIC_RANK = 1;
    private final static int ADMIN_RANK = 2;

    private Toolbar toolbar = null;
    private TextView action_bar_title;

    // User session and Network connectivity
    private Session session;

    private Context context = this;
    private User userSession;

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        this.toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);
        this.getLayoutInflater().inflate(R.layout.action_bar, null);
        this.action_bar_title = findViewById(R.id.action_bar_title);

        this.session = new Session(this);

        // Check if the user is connected
        if (this.session.isLogged()) {
            this.action_bar_title.setText("GESTION UTILISATEURS");
            // Current user session
            userSession = session.getUser();

            // Display the users and allow to change their rights (RW or R) or to delete them
            this.initUsersTextViews();
        } else {
            session.closeSession();
            ToastUtil.show(this, "Vous n'êtes pas connecté");
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void openMenu(View view) {
        // Show menu when click on the hamburger
        this.toolbar.showOverflowMenu();
    }

    @SuppressLint("SetTextI18n")
    public void initUsersTextViews() {
        int i = 0;

        // Read in DB to get all users
        UserAccessDB userDB = new UserAccessDB(this);
        userDB.openForRead();
        ArrayList<User> users = userDB.getUsers();
        userDB.Close();

        // For each user, create a TextView and change his rights (R or RW)
        for (User user : users) {
            final User currentUser = user;
            i++;

            // Get the LinearLayout from activity_admin
            LinearLayout linearLayout = findViewById(R.id.layout_admin_textViews);

            // Create a line
            this.createLine(i, linearLayout);

            // Create a linearLayout to display the user and the buttons
            LinearLayout linearLayoutVertical = new LinearLayout(this);
            linearLayoutVertical.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayoutVertical.setOrientation(LinearLayout.HORIZONTAL);

            // Create the user TextView
            this.createUserTextView(currentUser, linearLayoutVertical);

            // If it is the connected user or if it is not an admin, we don't show the buttons
            if (!userSession.getEmail().equals(currentUser.getEmail()) && !(userSession.getRank() == BASIC_RANK)) {

                // Create the delete Button
                this.createDeleteButton(linearLayoutVertical, currentUser);

                // Create the change rights Button
                this.createRightsButton(linearLayoutVertical, currentUser);
            }
            linearLayout.addView(linearLayoutVertical);
        }
    }

    private void createDeleteButton(LinearLayout linearLayoutVertical, final User currentUser) {
        // Delete button
        final Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(240, 200));
        deleteButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.delete_user, 0, 0, 0);
        deleteButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorRed));
        deleteButton.setTextColor(getResources().getColor(R.color.colorWhite));
        deleteButton.setGravity(Gravity.END);
        linearLayoutVertical.addView(deleteButton);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if it is an admin
                if (userSession.getRank() == ADMIN_RANK) {
                    UserAccessDB deleteUserDB = new UserAccessDB(context);
                    deleteUserDB.openForWrite();
                    deleteUserDB.removeUser(currentUser.getEmail());
                    deleteUserDB.Close();

                    ToastUtil.show(context, currentUser.getFirstname() + " a été supprimé");

                    // Refresh the activity
                    Intent adminActivity = new Intent(context, AdminActivity.class);
                    adminActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(adminActivity);
                } else {
                    ToastUtil.show(context, "Vous n'avez pas les droits pour supprimer un utilisateur");
                }
            }
        });
    }

    private void createRightsButton(LinearLayout linearLayoutVertical, final User currentUser) {
        // Change rights button
        final Button rightsButton = new Button(this);
        rightsButton.setLayoutParams(new LinearLayout.LayoutParams(240, 200));
        rightsButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.change_rights, 0, 0, 0);
        rightsButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorBlue));
        rightsButton.setTextColor(getResources().getColor(R.color.colorWhite));
        rightsButton.setGravity(Gravity.END);
        linearLayoutVertical.addView(rightsButton);

        // Click on the TextView allow us to change his rights
        rightsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if it is an admin
                if (userSession.getRank() == ADMIN_RANK) {
                    UserAccessDB updatedUserDB = new UserAccessDB(context);
                    if (currentUser.getRank() == BASIC_RANK) {
                        // Change the rank
                        User updatedUser = new User(currentUser.getLastname(), currentUser.getFirstname(), currentUser.getEmail(), currentUser.getPassword(), 2);
                        updatedUserDB.openForWrite();
                        updatedUserDB.removeUser(currentUser.getEmail());
                        updatedUserDB.insertUser(updatedUser);
                        updatedUserDB.Close();

                        ToastUtil.show(context, currentUser.getFirstname() + " est maintenant administrateur (R/W)");
                    } else if (currentUser.getRank() == ADMIN_RANK) {
                        // Change the rank
                        User updatedUser = new User(currentUser.getLastname(), currentUser.getFirstname(), currentUser.getEmail(), currentUser.getPassword(), 1);
                        updatedUserDB.openForWrite();
                        updatedUserDB.removeUser(currentUser.getEmail());
                        updatedUserDB.insertUser(updatedUser);
                        updatedUserDB.Close();

                        ToastUtil.show(context, currentUser.getFirstname() + " est maintenant utilisateur normal (R)");
                    }

                    // Refresh the activity
                    Intent adminActivity = new Intent(context, AdminActivity.class);
                    adminActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(adminActivity);
                } else {
                    ToastUtil.show(context, "Vous n'avez pas les droits pour modifier les droits des utilisateurs");
                }
            }
        });
    }

    private void createLine(int i, LinearLayout linearLayout) {
        // Make an horizontal line
        View line = new View(this);

        // Alternate the color
        if (!(i % 2 == 0)) {
            line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3));
            line.setBackgroundColor(Color.rgb(65, 23, 99));
        } else {
            line.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            line.setBackgroundColor(Color.rgb(100, 13, 171));
        }
        linearLayout.addView(line);
    }

    @SuppressLint("SetTextI18n")
    private void createUserTextView(User currentUser, LinearLayout linearLayoutVertical) {
        // TextView that contains a user
        TextView dynamicTextView = new TextView(this);
        dynamicTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        dynamicTextView.setGravity(Gravity.START);
        dynamicTextView.setTextSize(18);
        dynamicTextView.setPadding(70, 30, 100, 30);
        if (currentUser.getRank() == ADMIN_RANK) {
            dynamicTextView.setText("[Admin] ");
            dynamicTextView.setTypeface(null, Typeface.BOLD);

        }
        dynamicTextView.append(currentUser.getFirstname() + " " + currentUser.getLastname());

        // If the user displayed is the connected user, his name is in green
        if (userSession.getEmail().equals(currentUser.getEmail())) {
            dynamicTextView.setTextColor(getResources().getColor(R.color.colorGreen));
        }
        linearLayoutVertical.addView(dynamicTextView);
    }
}