package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Created by ssjoleary on 10/03/14.
 */
public class Profile extends Activity {
    //public static final String PREFS_NAME = "MyPrefsFile";
    private EditText nameText;
    private EditText phoneText;
    private EditText emailText;
    private String name;
    private String phone;
    private String email;
    private Boolean isMember;
    private Button dropTable;
    private WildlifeDB wildlifeDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        wildlifeDB = new WildlifeDB(this);
        wildlifeDB.open();

        // Restore Preferences
        SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        name = settings.getString("name", "Name");
        phone = settings.getString("phone", "Phone");
        email = settings.getString("email", "Email");
        isMember = settings.getBoolean("isMember", false);

        nameText = (EditText)findViewById(R.id.profile_name);
        phoneText = (EditText) findViewById(R.id.profile_telephone);
        emailText = (EditText) findViewById(R.id.profile_email);
        dropTable = (Button) findViewById(R.id.profile_dropTable);
        nameText.setText(name);
        phoneText.setText(phone);
        emailText.setText(email);
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
                wildlifeDB.dropTable();
                wildlifeDB.close();
                Toast.makeText(Profile.this, "User Created Sightings Deleted!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("name", nameText.getText().toString().trim());
        editor.putString("phone", phoneText.getText().toString().trim());
        editor.putString("email", emailText.getText().toString().trim());
        editor.putBoolean("isMember", isMember);

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
}
