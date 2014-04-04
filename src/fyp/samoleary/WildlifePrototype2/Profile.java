package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

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
                wildlifeDB.dropTableRssSighting();
                wildlifeDB.close();
                Toast.makeText(Profile.this, "User Created Sightings Deleted!", Toast.LENGTH_SHORT).show();
                dropTable.setText("User Created Sightings Deleted!");
                dropTable.setEnabled(false);
            }
        });
    }
    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
        SharedPreferences.Editor editor = settings.edit();
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
