package fyp.samoleary.WildlifePrototype2;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GetConnectivityStatus {

    public GetConnectivityStatus() {
    }

    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
