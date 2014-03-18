package fyp.samoleary.WildlifePrototype2;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

/**
 * Created by ssjoleary on 17/03/2014
 */
public class SpeciesGuide extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] values = getResources().getStringArray(R.array.species_array_id);
        SpeciesGuideArrayAdapter adapter = new SpeciesGuideArrayAdapter(this, values);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Irish", "Clicked!");
                Click(position);
            }
        });
    }

    protected void Click(int position) {
        Intent intent = new Intent(this, SpeciesGuideDialog.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

   /* @Override
    public void selectItem(int position) {
        switch (position) {
            case 0:
                gotoProfile();
                break;
            case 1:
                gotoSightings();
                break;
            default:
                break;
        }
    }

    private void gotoSightings() {
        Intent intent = new Intent(this, GMapActivity.class);
        startActivity(intent);
    }

    private void gotoProfile() {
        Intent i = new Intent(this, Profile.class);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }*/
}
