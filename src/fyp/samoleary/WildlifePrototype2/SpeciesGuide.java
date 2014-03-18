package fyp.samoleary.WildlifePrototype2;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by ssjoleary on 17/03/2014
 */
public class SpeciesGuide extends NavDrawer {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speciesguide_listview);

        FragmentManager fragmentManager= getFragmentManager();

        if (fragmentManager.findFragmentById(R.id.speciesfrag) == null) {
            SpeciesGuideListfrag speciesGuideListfrag = new SpeciesGuideListfrag();
            fragmentManager.beginTransaction().add(R.id.speciesfrag, speciesGuideListfrag).commit();
        }
    }

    @Override
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
    }

    public static class SpeciesGuideListfrag extends ListFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            String[] values = getResources().getStringArray(R.array.species_array_id);
            SpeciesGuideArrayAdapter adapter = new SpeciesGuideArrayAdapter(inflater.getContext(), values);
            setListAdapter(adapter);

            return super.onCreateView(inflater, container, savedInstanceState);
        }

        public void onListItemClick(ListView l, View v, int position, long id) {
                Intent intent = new Intent(v.getContext(), SpeciesGuideDialog.class);
                intent.putExtra("position", position);
                startActivity(intent);
        }
    }


}
