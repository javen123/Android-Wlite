package appsneva.com.wliteandroid;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayerView;

import java.util.logging.LogRecord;

/**
 * Created by javen on 6/22/15.
 */
public class YouTubeActivity extends YouTubeBaseActivity implements OnInitializedListener {

    public static final String TAG = YouTubeActivity.class.getSimpleName();
    public static final String GOOGlE_API_KEY = "\n" + "\n" +
            "AIzaSyDpxxO0kCBvNKpldc0H3w_Ci2th5Tdhq60";

    public static final String YOUTUBE_VIDEO_ID = "HO3oSMF4nDM"; //TODO need correct video ID from Wavlite


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.youtube);
        YouTubePlayerView youTubePlayView = (YouTubePlayerView)findViewById(R.id.youtube_player);
        youTubePlayView.initialize(GOOGlE_API_KEY, this);
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.setPlayerStateChangeListener(listener);
        youTubePlayer.setPlaybackEventListener(playbackEventListener);

        if (!b) {
            youTubePlayer.cueVideo(YOUTUBE_VIDEO_ID);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(YouTubeActivity.this,"Cannot initialize YouTubePlayer", Toast.LENGTH_LONG).show();
        Log.d("myYouTube", youTubeInitializationResult.toString());
    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private YouTubePlayer.PlayerStateChangeListener listener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };

}
