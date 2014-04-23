package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.Intent;import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import fyp.samoleary.WildlifePrototype2.Database.Constants;import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;import fyp.samoleary.WildlifePrototype2.WildlifeGeofencePkg.WildlifeGeofence;

/**
 * Created by ssjoleary on 10/03/14
 */
public class Profile extends Activity {
    private EditText nameText;
    private EditText phoneText;
    private EditText emailText;
    private Boolean isMember;
    private WildlifeDB wildlifeDB;
    private Button dropTable;
    private Switch turnOffBtn;
    private SharedPreferences.Editor editor;

    private LoginButton authButton;
    private static final String TAG = "MainFragment";
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private UiLifecycleHelper uiHelper;

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            authButton.setVisibility(View.VISIBLE);
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
            authButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setVisibility(View.GONE);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        wildlifeDB = new WildlifeDB(this);
        wildlifeDB.open();

        // Restore Preferences
        SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        String name = settings.getString("name", "Click to set up Profile");
        String phone = settings.getString("phone", "Phone");
        String email = settings.getString("email", "Email");
        isMember = settings.getBoolean("isMember", false);

        nameText = (EditText)findViewById(R.id.profile_name);
        phoneText = (EditText) findViewById(R.id.profile_telephone);
        emailText = (EditText) findViewById(R.id.profile_email);
        dropTable = (Button) findViewById(R.id.profile_dropTable);
        if(!name.equals("Click to set up Profile")) {
            nameText.setText(name);
        }
        if(!phone.equals("Phone")){
            phoneText.setText(phone);
        }
        if(!email.equals("Email")){
            emailText.setText(email);
        }
        if (isMember) {
            RadioButton btn = (RadioButton) findViewById(R.id.yes);
            btn.setChecked(true);
        } else {
            RadioButton btn = (RadioButton) findViewById(R.id.no);
            btn.setChecked(true);
        }
        dropTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wildlifeDB.dropTable(Constants.TABLE_NAME_RSS_SIGHTING);
                wildlifeDB.close();
                Toast.makeText(Profile.this, "User Created Sightings Deleted!", Toast.LENGTH_SHORT).show();
                dropTable.setText("User Created Sightings Deleted!");
                dropTable.setEnabled(false);
            }
        });

        SharedPreferences preferences = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        editor = preferences.edit();

        Boolean checked = preferences.getBoolean("isNotificationChecked", true);

        turnOffBtn = (Switch) findViewById(R.id.turnoffBtn);
        turnOffBtn.setVisibility(View.VISIBLE);
        turnOffBtn.setChecked(checked);
        turnOffBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(getApplicationContext(), WildlifeGeofence.class);
                int isCheckedInt;
                if (isChecked) {
                    isCheckedInt = 1;
                } else {
                    isCheckedInt = 0;
                }
                intent.putExtra("isNotificationChecked", isCheckedInt);
                editor.putBoolean("isNotificationChecked", isChecked);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        editor = settings.edit();
        String setName;

        if(nameText.getText().toString().trim().equals("")) {
            setName = "Click to set up Profile";
        } else {
            setName = nameText.getText().toString().trim();
        }

        editor.putString("name", setName);
        editor.putString("phone", phoneText.getText().toString().trim());
        editor.putString("email", emailText.getText().toString().trim());
        editor.putBoolean("isMember", isMember);

        editor.putBoolean("isNotificationChecked", turnOffBtn.isChecked());

        editor.commit();
    }
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.yes:
                isMember = true;
                break;
            case R.id.no:
                isMember = false;
                break;
            default:
                isMember = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // For scenarios where the main activity is launched and user
        // session is not null, the session state change notification
        // may not be triggered. Trigger it if it's open/closed.
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
