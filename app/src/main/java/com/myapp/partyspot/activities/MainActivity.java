package com.myapp.partyspot.activities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.Firebase;
import com.myapp.partyspot.fragments.ChooseSlaveDialogFragment;
import com.myapp.partyspot.fragments.HostFragment;
import com.myapp.partyspot.fragments.HostSearchResultsFragment;
import com.myapp.partyspot.fragments.NameDialogFragment;
import com.myapp.partyspot.fragments.SlaveFragment;
import com.myapp.partyspot.fragments.SlaveSearchResultsFragment;
import com.myapp.partyspot.fragments.SuggesterFragment;
import com.myapp.partyspot.handlers.FirebaseHandler;
import com.myapp.partyspot.handlers.HTTPFunctions;
import com.myapp.partyspot.R;
import com.myapp.partyspot.handlers.SpotifyHandler;
import com.myapp.partyspot.fragments.ChoosePlaylistHostFragment;
import com.myapp.partyspot.fragments.LoginFragment;
import com.myapp.partyspot.fragments.MainFragment;
import com.myapp.partyspot.spotifyDataClasses.SpotifyPlaylists;
import com.myapp.partyspot.spotifyDataClasses.SpotifyTrack;
import com.myapp.partyspot.spotifyDataClasses.SpotifyTracks;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;

import java.util.ArrayList;

public class MainActivity extends Activity {
    public boolean loggedIn;
    public Spotify spotify;
    public SpotifyHandler spotifyHandler;
    public FirebaseHandler firebaseHandler;
    public String accessToken; // Authentication token from spotify
    public String user; // user's name
    public boolean premiumUser; // true if premium, false otherwise
    public SpotifyTracks suggestedSongs;
    public boolean playing;
    public String playlistName;
    public ArrayList<String> playlists;
    public boolean muted;
    public String userType; //host, slave or suggester
    public String fragment; // current fragment

    public MainActivity() {
        this.playlists = new ArrayList<String>();
        this.loggedIn=false;
        this.spotify = null;
        this.spotifyHandler = null;
        this.firebaseHandler = null;
        this.accessToken = null;
        this.user = null;
        this.premiumUser = false;
        this.suggestedSongs = new SpotifyTracks();
        this.playing = false;
        this.playlistName = "";
        this.muted = false;
        this.userType = "";
        this.fragment = "Login";
    }

    public void setNotMuted() {
        if (this.muted) {
            this.muted = false;
            AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audio.setStreamMute(AudioManager.STREAM_MUSIC, this.muted);
        }
    }

    public void setMuted() {
        if (!this.muted) {
            this.muted = true;
            AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
            audio.setStreamMute(AudioManager.STREAM_MUSIC, this.muted);
        }
    }

    public void changeMutedState() {
        this.muted = !this.muted;
        AudioManager audio = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamMute(AudioManager.STREAM_MUSIC, this.muted);
    }

    public void setPlaylist(String playlist) {
        this.playlists.add(playlist);
        this.playlistName = playlist;
    }

    public void validate(String playlist) {
        Log.v("VALIDATING", "NOW");
        this.firebaseHandler.validatePlaylist(playlist);
    }

    public void validateHost(String playlist) {this.firebaseHandler.validatePlaylistHost(playlist);}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this); // required to use Firebase
        this.firebaseHandler = new FirebaseHandler(this);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    // starts the app by prompting user to login
                    .add(R.id.container, new LoginFragment(), "LoginFragment")
                    .commit();
        }

        // temporary suggested songs
        //suggestedSongs.addTrack(new SpotifyTrack("Whoa Whoa Whoa", "spotify:track:3tpdc8zHIOXy8rYhuI9car", "Watsky"));
        //suggestedSongs.addTrack(new SpotifyTrack("3005", "spotify:track:3Z2sglqDj1rDRMF5x0Sz2R", "Childish Gambino"));
        //suggestedSongs.addTrack(new SpotifyTrack("Handyman", "spotify:track:31Fw4CZistkNF4Uo3S39Md", "Seven"));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // this method gets called when user logs in with spotify
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            // gets access token to create spotify class and for web api requests.
            this.accessToken = response.getAccessToken();
            this.spotify = new Spotify(this.accessToken);
            this.loggedIn = true;
            this.spotifyHandler = new SpotifyHandler(this);
        }

        getUser(); // gets and sets user from the web api
        this.fragment = "Main";
        changeToMainFragment();
    }

    public void setPlayingTracks(SpotifyTracks tracks) {
        this.spotifyHandler.playingTracks = tracks;
        shuffleTracks();
    }

    public void setPremiumUser() {
        this.premiumUser = true;
    }

    public void shuffleTracks() {
        this.spotifyHandler.playingTracks.shuffleTracks();
    }

    public void setUser(String name) {
        this.user = name;
    }

    public void getUser() {
        try {
            HTTPFunctions functions = new HTTPFunctions(this);
            functions.getUser();
        } catch (Exception e) { // needed by volley
            e.printStackTrace();
        }
    }

    public void getPlaylistTracks(String playlistOwner, String playlistId) {
        try {
            HTTPFunctions functions = new HTTPFunctions(this);
            functions.getPlaylistTracks(playlistOwner, playlistId);
        } catch (Exception e) { // needed by volley
            e.printStackTrace();
        }
    }

    public void getPlaylists() {
        try {
            HTTPFunctions functions = new HTTPFunctions(this);
            functions.getPlaylists(this.user);
        } catch (Exception e) { // needed by volley
            e.printStackTrace();
        }
    }

    public void displayPlaylists(final SpotifyPlaylists playlists) {
        // called after the httpFunctions gets the users playlists
        ArrayList<String> list = playlists.makeNameArray();

        // displays the list of playlists
        ArrayAdapter<String> myListAdapter = new ArrayAdapter<String>(this, R.layout.playlist_view, list);
        final ListView myListView = (ListView) this.findViewById(R.id.playlist_list);
        myListView.setAdapter(myListAdapter);

        //create an onItemClickListener for the user to choose playlist to play
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String s = (String) myListView.getItemAtPosition(position);
                String playlistOwner = playlists.getOwnerFromTitle(s);
                String playlistId = playlists.getUriFromTitle(s);
                getPlaylistTracks(playlistOwner, playlistId); // gets playlist tracks to play
                MainActivity.this.spotifyHandler.setPlaylist(playlistOwner, playlistId); // sets variables for spotifyHandler
            }
        });
    }

    public void changeToHostFragment() {
        this.fragment = "Host";
        HostFragment fragment = new HostFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    public void setPlaylistsLoaded() {
        // changes from the progress bar view to the list of playlists
        findViewById(R.id.loadingBar).setVisibility(View.GONE);
    }

    public void changeToChoosePlaylistToHostFragment() {
        this.fragment = "ChoosePlaylistHost";
        ChoosePlaylistHostFragment fragment = new ChoosePlaylistHostFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, fragment, "choosePlaylist");
        transaction.commit();

        getPlaylists();
    }

    public void setMainFragmentLoaded() {
        findViewById(R.id.loadingBar).setVisibility(View.GONE);
        if (this.premiumUser) {
            findViewById(R.id.host_playlist).setVisibility(View.VISIBLE);
            findViewById(R.id.listen_playlist).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.suggest_playlist).setVisibility(View.VISIBLE);
    }

    public void changeToMainFragment() {
        this.fragment = "Main";
        this.spotifyHandler.setNotHostOrSlave();
        this.spotifyHandler.songIndex = 0;
        this.spotifyHandler.onPlaylist = false;
        this.playing = false;
        this.playlistName ="";
        this.spotifyHandler.pause();
        this.setNotMuted();
        MainFragment fragment = new MainFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    public void displayHostSearchResults(SpotifyTracks tracks) {
        HostSearchResultsFragment frag = (HostSearchResultsFragment) getFragmentManager().findFragmentByTag("host_search");
        frag.displaySearchResults(tracks);
    }

    public void displaySlaveSearchResults(SpotifyTracks tracks) {
        SlaveSearchResultsFragment frag = (SlaveSearchResultsFragment) getFragmentManager().findFragmentByTag("slave_search");
        frag.displaySearchResults(tracks);
    }

    public void changeToHostSearchResults() {
        if (!this.fragment.equals("HostSearchResults")) {
            this.fragment = "HostSearchResults";
            DialogFragment newFragment = new HostSearchResultsFragment();
            newFragment.show(getFragmentManager(), "host_search");
        }
    }

    public void changeToSlaveSearchResults() {
        if (!this.fragment.equals("SlaveSearchResults")) {
            this.fragment = "SlaveSearchResults";
            DialogFragment newFragment = new SlaveSearchResultsFragment();
            newFragment.show(getFragmentManager(), "slave_search");
        }
    }

    public void namePlaylist() {
        DialogFragment newFragment = new NameDialogFragment();
        newFragment.show(getFragmentManager(), "missiles");
    }

    public void choosePlaylistSlave() {
        DialogFragment newFragment = new ChooseSlaveDialogFragment();
        newFragment.show(getFragmentManager(), "missiles");
    }

    public void changeToSlaveFragment() {
        this.fragment = "Slave";
        SlaveFragment fragment = new SlaveFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, fragment, "Slave");
        transaction.commit();
    }

    public void changeToSuggesterFragment() {
        this.fragment = "Suggester";
        SuggesterFragment fragment = new SuggesterFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.container, fragment, "Suggester");
        transaction.commit();
    }

    public Spotify getSpotify() {
        return this.spotify;
    }

    @Override
    public void onDestroy() {
        this.spotifyHandler.destroy();
    }
}