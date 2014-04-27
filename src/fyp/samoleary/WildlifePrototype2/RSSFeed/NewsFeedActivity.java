package fyp.samoleary.WildlifePrototype2.RSSFeed;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import fyp.samoleary.WildlifePrototype2.GMap.GMapActivity;
import fyp.samoleary.WildlifePrototype2.GetConnectivityStatus;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.NavDrawer.NavDrawer;
import fyp.samoleary.WildlifePrototype2.Profile;
import fyp.samoleary.WildlifePrototype2.R;
import fyp.samoleary.WildlifePrototype2.SpeciesGuide.SpeciesGuide;

import java.util.Random;

public class NewsFeedActivity extends NavDrawer {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rssfeed_listview);

        FragmentManager fragmentManager = getFragmentManager();
        GetConnectivityStatus isConnected = new GetConnectivityStatus();

        if (isConnected.isConnected(getApplicationContext())) {
            if (fragmentManager.findFragmentById(R.id.rssfeedfrag) == null) {
                RSSFeedActivityListFrag rssFeedActivityListFrag = new RSSFeedActivityListFrag();
                fragmentManager.beginTransaction().add(R.id.rssfeedfrag, rssFeedActivityListFrag).commit();
            }
        } else {
            setContentView(R.layout.no_internet_access);
            TextView joke = (TextView) findViewById(R.id.joke);
            Random r = new Random();
            int i = r.nextInt(4 - 1 + 1) + 1;
            switch (i){
                case 1:
                    joke.setText(R.string.joke_one);
                    break;
                case 2:
                    joke.setText(R.string.joke_two);
                    break;
                case 3:
                    joke.setText(R.string.joke_three);
                    break;
                case 4:
                    joke.setText(R.string.joke_four);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void selectItem(int groupPosition, int childPosition) {
        Log.d(LocationUtils.APPTAG, groupPosition + " : " + childPosition);
        switch (groupPosition) {
            case 0:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoSpeciesGuide();
                        break;
                    default:
                        break;
                }
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
                    case 3:
                        closeDrawer();
                        //gotoSearchActivity();
                        gotoGMapActivity(groupPosition, childPosition);
                        break;
                    default:
                        break;
                }
                break;
            case 2:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoNewsFeed(getString(R.string.rssfeed_news));
                        break;
                    case 1:
                        closeDrawer();
                        gotoNewsFeed(getString(R.string.rssfeed_sightings));
                        break;
                    case 2:
                        closeDrawer();
                        gotoNewsFeed(getString(R.string.rssfeed_strandings));
                        break;
                }
                break;
            case 3:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoWildlifeGeofence(groupPosition, childPosition);
                        break;
                    case 1:
                        closeDrawer();
                        gotoWildlifeGeofence(groupPosition, childPosition);
                        break;
                    case 2:
                        closeDrawer();
                        gotoWildlifeGeofence(groupPosition, childPosition);
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

    @Override
    public void selectGroup(int groupPosition) {
        switch (groupPosition) {
            case 0:
                closeDrawer();
                gotoSpeciesGuide();
                break;
            default:
                break;
        }
    }

    /*private void gotoSpeciesGuide() {
        Intent intent = new Intent(this, SpeciesGuide.class);
        startActivity(intent);
    }

    private void gotoProfile() {
        Intent i = new Intent(this, Profile.class);
        startActivity(i);
    }

    /*private void gotoGMapActivity(int groupPosition, int childPosition) {
        Intent intent = new Intent(this, GMapActivity.class);
        intent.putExtra("groupPosition", groupPosition);
        intent.putExtra("childPosition", childPosition);
        startActivity(intent);
    }

    private void gotoNewsFeed(String rssUrl) {
        Intent intent = new Intent(this, NewsFeedActivity.class);
        intent.putExtra("rssUrl", rssUrl);
        startActivity(intent);
    }*/
}
