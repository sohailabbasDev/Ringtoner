package com.inflexionlabs.ringtoner.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.connectivity.Downloader;
import com.inflexionlabs.ringtoner.model.Favourite;
import com.inflexionlabs.ringtoner.model.Song;
import com.inflexionlabs.ringtoner.player.RingtonePlayer;
import com.inflexionlabs.ringtoner.util.Util;
import com.inflexionlabs.ringtoner.viewModel.AppViewModel;
import com.inflexionlabs.ringtoner.viewModel.FavouriteViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayActivity extends AppCompatActivity {

    private TextView songName;
    private ImageButton playButton;
    private ImageButton nextButton;
    private ImageButton prevButton;

    private CheckBox favouritesCheckBox;

    private RingtonePlayer ringtonePlayer;

    private CardView ringText;
    private CardView setAsAlarmText;
    private CardView setAsNotificationTone;
    private CardView setAsContact;
    private CardView downloadText;

    private ImageView downImage;
    private ImageView ringImage;
    private ImageView alarmImage;
    private ImageView contactImage;
    private ImageView notifyImage;

    private ProgressBar ringProgress;
    private ProgressBar alarmProgress;
    private ProgressBar contactProgress;
    private ProgressBar notifyProgress;
    private ProgressBar downProgress;

    private TextView ringTextView;
    private TextView alarmTextView;
//    private TextView contactTextView;
    private TextView notifyTextView;
    private TextView downTextView;

    private AppViewModel appViewModel;
    private FavouriteViewModel favViewModel;

    private ArrayList<Song> songArrayList;
    private ArrayList<Song> catArrayList;

    private Animation anim;
    private Animation animation;
    private Animation prevAnim;

    private int pos;

    private String category;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private Song song = new Song();
    private Favourite favourite = new Favourite();

    private boolean fromFav = false;
    private boolean fromCat = false;

    private Downloader downloader;

    private File sFile;

    final ActivityResultLauncher<Intent> contactActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri contactUri;

                    // There are no request codes
                    Intent data = result.getData();
                    if (data != null){
                        contactUri = data.getData();
                        if (contactUri != null && sFile != null){

                            Uri insertedUri = getFilteredUri(sFile);

                            // Apply the custom ringtone
                            final ContentValues values = new ContentValues(1);
                            values.put(ContactsContract.Contacts.CUSTOM_RINGTONE, insertedUri.toString());
                            getContentResolver().update(contactUri, values, null, null);

                            Snackbar.make(findViewById(android.R.id.content),"Contact Ringtone set!", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        downloader = new Downloader();

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbarPlay);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_keyboard_backspace_24,null));
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        //initialization of ringtone player
        ringtonePlayer = new RingtonePlayer();

        favViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(FavouriteViewModel.class);

        executorService.execute(() -> Util.FAVOURITE_STRING_ARRAYLIST = (ArrayList<String>) favViewModel.getAllUid());

        //component initialization
        songName = findViewById(R.id.songTextView);

        //Card views
        ringText = findViewById(R.id.ringCard);
        setAsAlarmText = findViewById(R.id.alarmCard);
        setAsNotificationTone = findViewById(R.id.notificationCard);
        setAsContact = findViewById(R.id.contactCard);
        downloadText = findViewById(R.id.downloadCard);

        //ImageViews
        ringImage = findViewById(R.id.callImage);
        alarmImage = findViewById(R.id.alarmImage);
        notifyImage = findViewById(R.id.notificationImage);
        contactImage = findViewById(R.id.contactImage);
        downImage = findViewById(R.id.downloadImage);

        //ProgressBars
        ringProgress = findViewById(R.id.ringProgress);
        alarmProgress = findViewById(R.id.alarmProgress);
        notifyProgress = findViewById(R.id.notProgress);
        contactProgress = findViewById(R.id.contactProgress);
        downProgress = findViewById(R.id.downloadProgress);

        //Main textViews
        ringTextView = findViewById(R.id.setAsRingtone);
        alarmTextView = findViewById(R.id.setAsAlarm);
        notifyTextView = findViewById(R.id.setAsNotification);
//        contactTextView = findViewById(R.id.setAsContactTone);
        downTextView = findViewById(R.id.downloadText);

        favouritesCheckBox = findViewById(R.id.favouriteToolbarButton);

        //Main buttons
        playButton = findViewById(R.id.playButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.previousButton);

        //loading animations
        anim = AnimationUtils.loadAnimation(this,R.anim.rotate);
        animation = AnimationUtils.makeInAnimation(this,false);
        prevAnim = AnimationUtils.makeInAnimation(this,true);

        //setting animations
        ringText.setAnimation(animation);
        setAsAlarmText.setAnimation(animation);
        setAsNotificationTone.setAnimation(animation);
        setAsContact.setAnimation(animation);
        downloadText.setAnimation(animation);

        //initialization view models

        appViewModel = new ViewModelProvider(PlayActivity.this).get(AppViewModel.class);

        //app view model
        appViewModel.initSongsData();
        appViewModel.getSongsData().observe(PlayActivity.this,songs -> songArrayList = songs);

        if (getIntent().hasExtra(Util.POSITION)){

            if (getIntent().hasExtra(Util.BOOLEAN)){

                favViewModel.getAllFavourites().observe(this, favourites -> {
                    fromFav = getIntent().getBooleanExtra(Util.BOOLEAN,true);
                    pos = getIntent().getIntExtra(Util.POSITION,0);
                    favourite = favourites.get(pos);
                    song.setName(favourite.getText());
                    song.setUri(favourite.getUrl());
                    songName.setText(song.getName());
                    updateCheckBox();
                });

            }else if(getIntent().hasExtra(Util.CODE_CATEGORY)){
                category = getIntent().getStringExtra(Util.CODE_CATEGORY);
                pos = getIntent().getIntExtra(Util.POSITION,0);
                song.setName(getIntent().getStringExtra(Util.CATEGORY_NAME_TEXT));
                song.setUri(getIntent().getStringExtra(Util.CATEGORY_URL));
                fromCat = true;
                appViewModel.getCategorySongData(category).observe(PlayActivity.this,songs -> catArrayList = songs);
            }else{
                //position variable
                pos = getIntent().getIntExtra(Util.POSITION,0);
                song = Objects.requireNonNull(appViewModel.getSongsData().getValue()).get(pos);
            }
        }else{
            pos = 0;
            if (getIntent().hasExtra(Util.CATEGORY_NAME_TEXT)){
                song.setName(getIntent().getStringExtra(Util.CATEGORY_NAME_TEXT));
                song.setUri(getIntent().getStringExtra(Util.CATEGORY_URL));
            }
        }

        songName.setText(song.getName());

        updateCheckBox();

        nextButton.setOnClickListener(view -> {
            resetViews();

            songName.startAnimation(animation);

            ringText.startAnimation(animation);
            setAsAlarmText.startAnimation(animation);
            setAsNotificationTone.startAnimation(animation);
            setAsContact.startAnimation(animation);
            downloadText.startAnimation(animation);

            playNext(fromFav,fromCat);

        });

        prevButton.setOnClickListener(view -> {
            resetViews();

            songName.startAnimation(prevAnim);

            ringText.startAnimation(prevAnim);
            setAsAlarmText.startAnimation(prevAnim);
            setAsNotificationTone.startAnimation(prevAnim);
            setAsContact.startAnimation(prevAnim);
            downloadText.startAnimation(prevAnim);

            playPrevious(fromFav, fromCat);
        });

        playButton.setOnClickListener(view -> {
            if (!ringtonePlayer.isPlaying()){
                if (fromFav){
                    playRingtone(Objects.requireNonNull(favViewModel.getAllFavourites().getValue()).get(pos).getUrl());
                }else{
                    playRingtone(song.getUri());
                }
            }else {
                ringtonePlayer.stopPlay();
                playButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_play_circle_filled_24,null));
            }
        });

        favouritesCheckBox.setOnClickListener(view -> {
            Favourite favourite1 = new Favourite();
            favourite1.setText(song.getName());
            favourite1.setUrl(song.getUri());
            if (favouritesCheckBox.isChecked()){
                favViewModel.insert(favourite1);
                Toast.makeText(PlayActivity.this,Util.ADDED_TO_FAVOURITES, Toast.LENGTH_SHORT).show();
            }else{
                favViewModel.delete(favourite.getText());
                Toast.makeText(PlayActivity.this,Util.REMOVE_FROM_FAVOURITES, Toast.LENGTH_SHORT).show();
            }
        });

        downloadText.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (checkWritePermission()){
                    if (Util.isConnected(this)){
                        downImage.setVisibility(View.INVISIBLE);
                        downProgress.setVisibility(View.VISIBLE);

                        Toast.makeText(PlayActivity.this,"You can find downloads in your music folder", Toast.LENGTH_LONG).show();

                        if (sFile == null){
                            nextButton.setVisibility(View.INVISIBLE);
                            prevButton.setVisibility(View.INVISIBLE);

                            downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                @Override
                                public void onDownloaded(boolean downloaded, File file) {
                                    if (downloaded) {
                                        sFile = file;
                                        Snackbar.make(findViewById(android.R.id.content), "DOWNLOADED!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onDownloadFailed(boolean failed) {
                                    if (failed) {
                                        Snackbar.make(findViewById(android.R.id.content), "FAILED!", Snackbar.LENGTH_LONG).show();
                                        downTextView.setText(R.string.downloaded_text);
                                        downloadText.setClickable(true);
                                    }
                                }

                                @Override
                                public void uiChange(boolean done, File file) {
                                    downTextView.setText(R.string.downloading);
                                    if (done){
                                        downImage.setVisibility(View.VISIBLE);
                                        downProgress.setVisibility(View.GONE);
                                        downImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_file_download_done_24,null));
                                        downTextView.setText(R.string.downloaded_text);
                                        downloadText.setClickable(false);

                                        nextButton.setVisibility(View.VISIBLE);
                                        prevButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }else{
                            downImage.setVisibility(View.VISIBLE);
                            downProgress.setVisibility(View.GONE);
                            downImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_file_download_done_24,null));
                            downTextView.setText(R.string.downloaded_text);
                            downloadText.setClickable(false);

                            nextButton.setVisibility(View.VISIBLE);
                            prevButton.setVisibility(View.VISIBLE);
                        }

                    }else{
                        Util.alert(PlayActivity.this);
                    }
                }else{
                    askWritePermission();
                }
            }else{
                if (checkWritePermitForLowerSDK()){
                    if (checkWritePermission()){
                        if (Util.isConnected(this)){
                            downImage.setVisibility(View.INVISIBLE);
                            downProgress.setVisibility(View.VISIBLE);

                            if (sFile == null){
                                nextButton.setVisibility(View.INVISIBLE);
                                prevButton.setVisibility(View.INVISIBLE);

                                downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                    @Override
                                    public void onDownloaded(boolean downloaded, File file) {
                                        if (downloaded) {
                                            sFile = file;
                                            Snackbar.make(findViewById(android.R.id.content), "DOWNLOADED!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onDownloadFailed(boolean failed) {
                                        if (failed) {
                                            Snackbar.make(findViewById(android.R.id.content), "FAILED!", Snackbar.LENGTH_LONG).show();
                                            downloadText.setClickable(true);
                                        }
                                    }

                                    @Override
                                    public void uiChange(boolean done, File file) {
                                        if (done){
                                            downImage.setVisibility(View.VISIBLE);
                                            downProgress.setVisibility(View.GONE);
                                            downImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_file_download_done_24,null));
                                            downTextView.setText(R.string.downloaded_text);
                                            downloadText.setClickable(false);

                                            nextButton.setVisibility(View.VISIBLE);
                                            prevButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }else{
                                downImage.setVisibility(View.VISIBLE);
                                downProgress.setVisibility(View.GONE);
                                downImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_file_download_done_24,null));
                                downTextView.setText(R.string.downloaded_text);
                                downloadText.setClickable(false);

                                nextButton.setVisibility(View.VISIBLE);
                                prevButton.setVisibility(View.VISIBLE);
                            }

                        }else{
                            Util.alert(PlayActivity.this);
                        }
                    }else {
                        askWritePermission();
                    }
                }else{
                    askWriteExternalPermission();
                }
            }
        });

        ringText.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (checkWritePermission()){
                    if (Util.isConnected(this)){
                        ringImage.setVisibility(View.INVISIBLE);
                        ringProgress.setVisibility(View.VISIBLE);

                        if (sFile == null){
                            nextButton.setVisibility(View.INVISIBLE);
                            prevButton.setVisibility(View.INVISIBLE);

                            downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                @Override
                                public void onDownloaded(boolean downloaded, File file) {

                                    if (downloaded){
                                        sFile = file;

                                        RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_RINGTONE, getFilteredUri(file));

                                        Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();

                                    }
                                }

                                @Override
                                public void onDownloadFailed(boolean failed) {
                                    if (failed) {
                                        Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                        ringText.setClickable(true);
                                    }
                                }

                                @Override
                                public void uiChange(boolean done, File file) {
                                    if (done){
                                        ringImage.setVisibility(View.VISIBLE);
                                        ringProgress.setVisibility(View.GONE);
                                        ringImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                        ringTextView.setText(R.string.ringtone_set_text);
                                        ringText.setClickable(false);

                                        nextButton.setVisibility(View.VISIBLE);
                                        prevButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }else{

                            RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_RINGTONE, getFilteredUri(sFile));

                            Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();

                            ringImage.setVisibility(View.VISIBLE);
                            ringProgress.setVisibility(View.GONE);
                            ringImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                            ringTextView.setText(R.string.ringtone_set_text);
                            ringText.setClickable(false);
                        }

                    }else {
                        Util.alert(PlayActivity.this);
                    }
                }else{
                    askWritePermission();
                }
            }else{
                if (checkWritePermitForLowerSDK()){
                    if (checkWritePermission()){
                        if (Util.isConnected(this)){
                            ringImage.setVisibility(View.INVISIBLE);
                            ringProgress.setVisibility(View.VISIBLE);

                            if (sFile == null){
                                nextButton.setVisibility(View.INVISIBLE);
                                prevButton.setVisibility(View.INVISIBLE);

                                downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                    @Override
                                    public void onDownloaded(boolean downloaded, File file) {

                                        if (downloaded){
                                            sFile = file;

                                            RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_RINGTONE, getFilteredUri(file));

                                            Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();

                                        }
                                    }

                                    @Override
                                    public void onDownloadFailed(boolean failed) {
                                        if (failed) {
                                            Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                            ringText.setClickable(true);
                                        }
                                    }

                                    @Override
                                    public void uiChange(boolean done, File file) {
                                        if (done){
                                            ringImage.setVisibility(View.VISIBLE);
                                            ringProgress.setVisibility(View.GONE);
                                            ringImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                            ringTextView.setText(R.string.ringtone_set_text);
                                            ringText.setClickable(false);

                                            nextButton.setVisibility(View.VISIBLE);
                                            prevButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }else{

                                RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_RINGTONE, getFilteredUri(sFile));

                                Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();

                                ringImage.setVisibility(View.VISIBLE);
                                ringProgress.setVisibility(View.GONE);
                                ringImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                ringTextView.setText(R.string.ringtone_set_text);
                                ringText.setClickable(false);
                            }

                        }else {
                            Util.alert(PlayActivity.this);
                        }
                    }else{
                        askWritePermission();
                    }
                }else{
                    askWriteExternalPermission();
                }
            }

        });

        setAsNotificationTone.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (checkWritePermission()){
                    if (Util.isConnected(PlayActivity.this)){
                        notifyImage.setVisibility(View.INVISIBLE);
                        notifyProgress.setVisibility(View.VISIBLE);

                        if (sFile == null){

                            nextButton.setVisibility(View.INVISIBLE);
                            prevButton.setVisibility(View.INVISIBLE);
                            downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                @Override
                                public void onDownloaded(boolean downloaded, File file) {

                                    if (downloaded){

                                        sFile = file;

                                        RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_NOTIFICATION, getFilteredUri(file));

                                        Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onDownloadFailed(boolean failed) {
                                    if (failed) {
                                        Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                        setAsNotificationTone.setClickable(true);
                                    }
                                }

                                @Override
                                public void uiChange(boolean done, File file) {
                                    if (done){
                                        notifyImage.setVisibility(View.VISIBLE);
                                        notifyProgress.setVisibility(View.GONE);
                                        notifyImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                        notifyTextView.setText(R.string.notif_tone_set);
                                        setAsNotificationTone.setClickable(false);

                                        nextButton.setVisibility(View.VISIBLE);
                                        prevButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }else{

                            RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_NOTIFICATION, getFilteredUri(sFile));

                            Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();

                            notifyImage.setVisibility(View.VISIBLE);
                            notifyProgress.setVisibility(View.GONE);
                            notifyImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                            notifyTextView.setText(R.string.notif_tone_set);
                            setAsNotificationTone.setClickable(false);
                        }
                    }else{
                        Util.alert(PlayActivity.this);
                    }
                }else{
                    askWritePermission();
                }
            }else{
                if (checkWritePermitForLowerSDK()){
                    if (checkWritePermission()){
                        if (Util.isConnected(PlayActivity.this)){
                            notifyImage.setVisibility(View.INVISIBLE);
                            notifyProgress.setVisibility(View.VISIBLE);

                            if (sFile == null){

                                nextButton.setVisibility(View.INVISIBLE);
                                prevButton.setVisibility(View.INVISIBLE);
                                downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                    @Override
                                    public void onDownloaded(boolean downloaded, File file) {

                                        if (downloaded){

                                            sFile = file;

                                            RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_NOTIFICATION, getFilteredUri(file));

                                            Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onDownloadFailed(boolean failed) {
                                        if (failed) {
                                            Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                            setAsNotificationTone.setClickable(true);
                                        }
                                    }

                                    @Override
                                    public void uiChange(boolean done, File file) {
                                        if (done){
                                            notifyImage.setVisibility(View.VISIBLE);
                                            notifyProgress.setVisibility(View.GONE);
                                            notifyImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                            notifyTextView.setText(R.string.notif_tone_set);
                                            setAsNotificationTone.setClickable(false);

                                            nextButton.setVisibility(View.VISIBLE);
                                            prevButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }else{

                                RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_NOTIFICATION, getFilteredUri(sFile));

                                Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();

                                notifyImage.setVisibility(View.VISIBLE);
                                notifyProgress.setVisibility(View.GONE);
                                notifyImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                notifyTextView.setText(R.string.notif_tone_set);
                                setAsNotificationTone.setClickable(false);
                            }
                        }else{
                            Util.alert(PlayActivity.this);
                        }
                    }else{
                        askWritePermission();
                    }
                }else{
                    askWriteExternalPermission();
                }
            }

        });

        setAsContact.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (checkWritePermission()){
                    if (checkContactPermission()){
                        if (Util.isConnected(PlayActivity.this)){

                            if (sFile == null){
                                contactImage.setVisibility(View.INVISIBLE);
                                contactProgress.setVisibility(View.VISIBLE);
                                setAsContact.setClickable(false);

                                downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                    @Override
                                    public void onDownloaded(boolean downloaded, File file) {}

                                    @Override
                                    public void onDownloadFailed(boolean failed) {
                                        if (failed) {
                                            setAsContact.setClickable(true);
                                            Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void uiChange(boolean done, File file) {
                                        if (done){
                                            contactImage.setVisibility(View.VISIBLE);
                                            contactProgress.setVisibility(View.INVISIBLE);
                                            setAsContact.setClickable(true);
                                            sFile = file;
                                            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                                    ContactsContract.Contacts.CONTENT_URI);
                                            contactActivityResultLauncher.launch(contactPickerIntent);
                                        }
                                    }
                                });
                            }else{
                                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                        ContactsContract.Contacts.CONTENT_URI);
                                contactActivityResultLauncher.launch(contactPickerIntent);
                            }
                        }else{
                            Util.alert(PlayActivity.this);
                        }
                    }else{
                        askContactPermission();
                    }
                }else{
                    askWritePermission();
                }
            }else{
                if (checkContactPermission()){
                    if (checkWritePermitForLowerSDK()){
                        if (checkWritePermission()){
                            if (Util.isConnected(PlayActivity.this)){

                                if (sFile == null){
                                    contactImage.setVisibility(View.INVISIBLE);
                                    contactProgress.setVisibility(View.VISIBLE);
                                    setAsContact.setClickable(false);

                                    downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                        @Override
                                        public void onDownloaded(boolean downloaded, File file) {}

                                        @Override
                                        public void onDownloadFailed(boolean failed) {
                                            if (failed) {
                                                Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                                setAsContact.setClickable(true);
                                            }
                                        }

                                        @Override
                                        public void uiChange(boolean done, File file) {
                                            if (done){
                                                sFile = file;
                                                contactImage.setVisibility(View.VISIBLE);
                                                contactProgress.setVisibility(View.INVISIBLE);
                                                setAsContact.setClickable(true);
                                                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                                        ContactsContract.Contacts.CONTENT_URI);
                                                contactActivityResultLauncher.launch(contactPickerIntent);
                                            }
                                        }
                                    });
                                }else{
                                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                                            ContactsContract.Contacts.CONTENT_URI);
                                    contactActivityResultLauncher.launch(contactPickerIntent);
                                }
                            }else{
                                Util.alert(PlayActivity.this);
                            }
                        }else{
                            askWritePermission();
                        }
                    }else{
                        askWriteExternalPermission();
                    }
                }else{
                    askContactPermission();
                }
            }

        });

        setAsAlarmText.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                if (checkWritePermission()){
                    if (Util.isConnected(PlayActivity.this)){
                        alarmImage.setVisibility(View.INVISIBLE);
                        alarmProgress.setVisibility(View.VISIBLE);

                        if (sFile == null){
                            nextButton.setVisibility(View.INVISIBLE);
                            prevButton.setVisibility(View.INVISIBLE);

                            downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                @Override
                                public void onDownloaded(boolean downloaded, File file) {

                                    if (downloaded){

                                        sFile = file;

                                        RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_ALARM, getFilteredUri(file));

                                        Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onDownloadFailed(boolean failed) {
                                    if (failed) {
                                        Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                        setAsAlarmText.setClickable(true);
                                    }
                                }

                                @Override
                                public void uiChange(boolean done, File file) {
                                    if (done){
                                        alarmImage.setVisibility(View.VISIBLE);
                                        alarmProgress.setVisibility(View.GONE);
                                        alarmImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                        alarmTextView.setText(R.string.alarm_tone_set);
                                        setAsAlarmText.setClickable(false);

                                        nextButton.setVisibility(View.VISIBLE);
                                        prevButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }else{

                            RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_ALARM, getFilteredUri(sFile));

                            Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();
                            alarmImage.setVisibility(View.VISIBLE);
                            alarmProgress.setVisibility(View.GONE);
                            alarmImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                            alarmTextView.setText(R.string.alarm_tone_set);
                            setAsAlarmText.setClickable(false);
                        }

                    }else{
                        Util.alert(PlayActivity.this);
                    }
                }else{
                    askWritePermission();
                }
            }else{
                if (checkWritePermitForLowerSDK()){
                    if (checkWritePermission()){
                        if (Util.isConnected(PlayActivity.this)){
                            alarmImage.setVisibility(View.INVISIBLE);
                            alarmProgress.setVisibility(View.VISIBLE);

                            if (sFile == null){
                                nextButton.setVisibility(View.INVISIBLE);
                                prevButton.setVisibility(View.INVISIBLE);

                                downloader.downloadFromURl(song.getUri(), song.getName(), new Downloader.DownloadListener() {
                                    @Override
                                    public void onDownloaded(boolean downloaded, File file) {

                                        if (downloaded){

                                            sFile = file;

                                            RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_ALARM, getFilteredUri(file));

                                            Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onDownloadFailed(boolean failed) {
                                        if (failed) {
                                            Snackbar.make(findViewById(android.R.id.content), "Failed to set!", Snackbar.LENGTH_LONG).show();
                                            setAsAlarmText.setClickable(true);
                                        }
                                    }

                                    @Override
                                    public void uiChange(boolean done, File file) {
                                        if (done){
                                            alarmImage.setVisibility(View.VISIBLE);
                                            alarmProgress.setVisibility(View.GONE);
                                            alarmImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                            alarmTextView.setText(R.string.alarm_tone_set);
                                            setAsAlarmText.setClickable(false);

                                            nextButton.setVisibility(View.VISIBLE);
                                            prevButton.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                            }else{

                                RingtoneManager.setActualDefaultRingtoneUri(PlayActivity.this, RingtoneManager.TYPE_ALARM, getFilteredUri(sFile));

                                Snackbar.make(findViewById(android.R.id.content),"Ringtone set!", Snackbar.LENGTH_LONG).show();
                                alarmImage.setVisibility(View.VISIBLE);
                                alarmProgress.setVisibility(View.GONE);
                                alarmImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_done_24,null));
                                alarmTextView.setText(R.string.alarm_tone_set);
                                setAsAlarmText.setClickable(false);
                            }

                        }else{
                            Util.alert(PlayActivity.this);
                        }
                    }else{
                        askWritePermission();
                    }
                }else{
                    askWriteExternalPermission();
                }
            }
        });
    }

    private void askContactPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, Util.WRITE_CONTACTS_CODE);
    }

    private void resetViews(){
        if (sFile != null){

            alarmImage.setVisibility(View.VISIBLE);
            alarmImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_access_alarm_24,null));
            alarmTextView.setText(R.string.set_as_alarm_tone);
            setAsAlarmText.setClickable(true);

            notifyImage.setVisibility(View.VISIBLE);
            notifyImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_notifications_active_24,null));
            notifyTextView.setText(R.string.set_as_notification_sound);
            setAsNotificationTone.setClickable(true);

            ringImage.setVisibility(View.VISIBLE);
            ringImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_phone_in_talk_24,null));
            ringTextView.setText(R.string.set_as_ringtone);
            ringText.setClickable(true);

            downImage.setVisibility(View.VISIBLE);
            downImage.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_download_24,null));
            downTextView.setText(R.string.download);
            downloadText.setClickable(true);

            sFile = null;
        }
    }

    private void askWritePermission() {
        new AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is required to set the ringtone, sms tone, contact tone")
                .setPositiveButton("ALLOW", (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .create()
                .show();
    }

    private void askWriteExternalPermission(){
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Util.WRITE_SETTINGS_CODE);
    }

    private Uri getFilteredUri(File file){
        // check if file already exists in MediaStore
        String[] projection = {MediaStore.Audio.Media._ID};
        String selectionClause = MediaStore.Audio.Media.DATA + " = ? ";
        String[] selectionArgs = {file.getAbsolutePath()};
        Cursor cursor = PlayActivity.this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selectionClause, selectionArgs, null);
        Uri insertedUri;
        if (cursor == null || cursor.getCount() < 1) {
            // not exist, insert into MediaStore
            ContentValues cv = new ContentValues();
            cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
            cv.put(MediaStore.MediaColumns.TITLE, file.getName());
            cv.put(MediaStore.Audio.Media.IS_ALARM, true);
            insertedUri = PlayActivity.this.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cv);
        } else {
            // already exist
            cursor.moveToNext();
            long id = cursor.getLong(0);
            insertedUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        }
        if (cursor!=null) cursor.close();

        return insertedUri;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Util.WRITE_SETTINGS_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (!checkWritePermission()){
                    askWritePermission();
                }
            }
        }else if (Util.WRITE_CONTACTS_CODE == requestCode){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                    if (!checkWritePermitForLowerSDK()){
                        askWriteExternalPermission();
                    }
                }
            }
        }
    }

    private void playRingtone(String url){

        ringtonePlayer.playWithDataSource(url, new RingtonePlayer.OnPrepareListener() {
            @Override
            public void isPreparing(boolean isPreparing) {
                if (isPreparing){
                    playButton.startAnimation(anim);
                }else{
                    playButton.setAnimation(null);
                    playButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.main_stop_circle_24,null));
                }
            }

            @Override
            public void completed(boolean completed) {
                if (completed){
                    playButton.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_play_circle_filled_24,null));
                }
            }

            @Override
            public void onNext() {

            }
        });
    }


    private void playNext(boolean fromFav, boolean fromCat){
        nextButton.setEnabled(false);
        if (fromFav){
            favViewModel.getAllFavourites().observe(this, favourites -> {
                ringtonePlayer.stopPlay();
                pos = (pos+1) % favourites.size();

                songName.setText(favourites.get(pos).getText());
                playRingtone(favourites.get(pos).getUrl());
            });
            updateCheckBox();
        }else if (fromCat){
            ringtonePlayer.stopPlay();
            pos = (pos+1) % catArrayList.size();
            song = Objects.requireNonNull(appViewModel.getCategorySongData(category).getValue()).get(pos);
            songName.setText(song.getName());
            updateCheckBox();
            playRingtone(song.getUri());
        }else{
            ringtonePlayer.stopPlay();
            pos = (pos+1) % songArrayList.size();
            song = Objects.requireNonNull(appViewModel.getSongsData().getValue()).get(pos);
            songName.setText(song.getName());
            updateCheckBox();

            playRingtone(song.getUri());
        }
        nextButton.setEnabled(true);
    }

    private void playPrevious(boolean fromFav, boolean fromCat){
        prevButton.setEnabled(false);
        if (fromFav){
            ringtonePlayer.stopPlay();
            favViewModel.getAllFavourites().observe(this, favourites -> {
                if (pos>0){
                    pos = (pos-1) % favourites.size();
                }else {
                    pos = favourites.size()-1;
                }
                songName.setText(favourites.get(pos).getText());
                playRingtone(favourites.get(pos).getUrl());
            });
        }else if (fromCat){
            ringtonePlayer.stopPlay();
            if (pos>0){
                pos = (pos-1) % catArrayList.size();
            }else {
                pos = catArrayList.size()-1;
            }
            song = Objects.requireNonNull(appViewModel.getCategorySongData(category).getValue()).get(pos);
            songName.setText(song.getName());
            updateCheckBox();
            playRingtone(song.getUri());
        }else{
            ringtonePlayer.stopPlay();
            if (pos>0){
                pos = (pos-1) % songArrayList.size();
            }else {
                pos = songArrayList.size()-1;
            }
            song = Objects.requireNonNull(appViewModel.getSongsData().getValue()).get(pos);
            songName.setText(song.getName());
            updateCheckBox();
            playRingtone(song.getUri());
        }
        prevButton.setEnabled(true);
    }

    public void updateCheckBox() {
        favouritesCheckBox.setChecked(Util.FAVOURITE_STRING_ARRAYLIST.contains(song.getName()));
    }

    @Override
    public void onPause() {
        super.onPause();
        ringtonePlayer.stopPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        ringtonePlayer.stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ringtonePlayer.destroy();
    }

    private boolean checkContactPermission(){
        return checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkWritePermitForLowerSDK(){
        return checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private boolean checkWritePermission(){
        return Settings.System.canWrite(this);
    }

}