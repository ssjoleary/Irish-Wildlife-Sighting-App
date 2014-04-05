package fyp.samoleary.WildlifePrototype2.SpeciesGuide;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.google.android.youtube.player.YouTubeStandalonePlayer;

/**
 * Created by ssjoleary on 17/03/2014.
 */
public class VideoFragment extends YouTubePlayerSupportFragment {
    public VideoFragment() { }

    public static VideoFragment newInstance(String url) {

        VideoFragment f = new VideoFragment();

        Bundle b = new Bundle();
        b.putString("url", url);

        f.setArguments(b);
        f.init();

        return f;
    }

    private void init() {

        initialize("AIzaSyABWZiR0Wx4Qlt-BBJ-C4hDKzQ0a9Qy77M", new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) { }

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {
                player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                if (!wasRestored) {
                    //Tell the player you want to control the fullscreen change
                    player.setFullscreenControlFlags(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
                    //Tell the player how to control the change
                    player.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener(){
                        @Override
                        public void onFullscreen(boolean arg0) {
                            Intent intent = YouTubeStandalonePlayer.createVideoIntent(getActivity(), "oCwq9oPOfNk", "AIzaSyABWZiR0Wx4Qlt-BBJ-C4hDKzQ0a9Qy77M");
                            startActivity(intent);
                        }});
                    player.cueVideo(getArguments().getString("url"));
                }
            }
        });
    }
}
