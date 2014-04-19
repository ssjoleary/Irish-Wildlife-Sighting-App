package fyp.samoleary.WildlifePrototype2.SpeciesGuide;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import fyp.samoleary.WildlifePrototype2.GMap.GMapActivity;
import fyp.samoleary.WildlifePrototype2.WildlifeGeofencePkg.WildlifeGeofence;
import fyp.samoleary.WildlifePrototype2.NavDrawer.NavDrawer;
import fyp.samoleary.WildlifePrototype2.Profile;
import fyp.samoleary.WildlifePrototype2.R;
import fyp.samoleary.WildlifePrototype2.RSSFeed.NewsFeedActivity;

/**
 * Created by ssjoleary on 17/03/2014
 */
public class SpeciesGuide extends NavDrawer {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speciesguide_listview);

        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.findFragmentById(R.id.speciesfrag) == null) {
            SpeciesGuideListfrag speciesGuideListfrag = new SpeciesGuideListfrag();
            fragmentManager.beginTransaction().add(R.id.speciesfrag, speciesGuideListfrag).commit();
        }
    }

    @Override
    public void selectItem(int groupPosition, int childPosition) {
        switch (groupPosition) {
            case 0:
                closeDrawer();
                break;
            case 1:
                switch (childPosition) {
                case 0:
                    closeDrawer();
                    //getRecentSightings();
                    gotoGMapActivity(groupPosition, childPosition);
                    break;
                case 1:
                    closeDrawer();
                    //createReportDialog();
                    gotoGMapActivity(groupPosition, childPosition);
                    break;
                case 2:
                    closeDrawer();
                    //gotoSearchActivity();
                    gotoGMapActivity(groupPosition, childPosition);
                    break;
                default:
                    break;
            }
                /*switch (childPosition) {
                    case 0:
                        closeDrawer();
                        break;
                    default:
                        break;
                }*/
                break;
            case 2:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoRSSFeed("http://www.iwdg.ie/index.php?option=com_k2&view=itemlist&task=category&id=1&Itemid=93&format=feed");
                        break;
                    case 1:
                        closeDrawer();
                        gotoRSSFeed("http://www.iwdg.ie/_customphp/iscope/rss_sightings.php");
                        break;
                    case 2:
                        closeDrawer();
                        gotoRSSFeed("http://www.iwdg.ie/_customphp/iscope/rss_strandings.php");
                        break;
                }
                break;
            case 3:
                switch (childPosition) {
                    case 0:
                        break;
                    case 1:
                        gotoWildlifeGeofence();
                        break;
                }
                break;
            case 10:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoProfile();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private void gotoWildlifeGeofence() {
        Intent intent = new Intent(this, WildlifeGeofence.class);
        startActivity(intent);
    }

    @Override
    public void selectGroup(int groupPosition) {
        switch (groupPosition) {
            case 0:
                closeDrawer();
                break;
            default:
                break;
        }
    }

    private void gotoGMapActivity(int groupPosition, int childPosition) {
        Intent intent = new Intent(this, GMapActivity.class);
        intent.putExtra("groupPosition", groupPosition);
        intent.putExtra("childPosition", childPosition);
        startActivity(intent);
    }

    private void gotoProfile() {
        Intent i = new Intent(this, Profile.class);
        startActivity(i);
    }

    private void gotoRSSFeed(String rssUrl) {
        Intent intent = new Intent(this, NewsFeedActivity.class);
        intent.putExtra("rssUrl", rssUrl);
        startActivity(intent);
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

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState){
            super.onViewCreated(view, savedInstanceState);

            getListView().setDivider(null);
        }
    }


}
