package com.wavlite.WLAndroid;

import android.os.Bundle;
import android.widget.Toast;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;


/**
 * A sample implementation of a fragment extending YouTubePlayerFragment.
 * This will take care of most of the work necessary to load and play a video
 */
public class YTPlayer extends YouTubePlayerSupportFragment implements YouTubePlayer.OnInitializedListener {
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final String KEY_VIDEO_ID = "KEY_VIDEO_ID";
    private String mVideoId;


    public YTPlayer() {}  // YTPlayer Constructor


    /**
     * Returns a new instance of this Fragment
     * @param videoId The ID of the video to play
     */
    public static YTPlayer newInstance(final String videoId) {
        final YTPlayer youTubeFragment = new YTPlayer();
        final Bundle bundle = new Bundle();
        bundle.putString(KEY_VIDEO_ID, videoId);
        youTubeFragment.setArguments(bundle);
        return youTubeFragment;
    }


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        final Bundle arguments = getArguments();
        if (bundle != null && bundle.containsKey(KEY_VIDEO_ID)) {
            mVideoId = bundle.getString(KEY_VIDEO_ID);
        }
        else if (arguments != null && arguments.containsKey(KEY_VIDEO_ID)) {
            mVideoId = arguments.getString(KEY_VIDEO_ID);
        }
        initialize(DeveloperKey.DEVELOPER_KEY, this);
    }  // onCreate


    /**
     * Set the video id and initialize the player
     * This can be used when including the Fragment in an XML layout
     * @param videoId The ID of the video to play
     */
    public void setVideoId(final String videoId) {
        mVideoId = videoId;
        initialize(DeveloperKey.DEVELOPER_KEY, this);
    }  // setVideoId

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean restored) {
        if (mVideoId != null) {
            if (restored) {
                youTubePlayer.play();
            }
            else {
                youTubePlayer.loadVideo(mVideoId);
            }
        }
    }  // onInitializationSuccess


    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
        }
        else {
            Toast.makeText(getActivity(), youTubeInitializationResult.toString(), Toast.LENGTH_LONG).show();
        }
    }  // onInitializationFailure


    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putString(KEY_VIDEO_ID, mVideoId);
    }  // onSaveInstanceState
}  // YTPlayer