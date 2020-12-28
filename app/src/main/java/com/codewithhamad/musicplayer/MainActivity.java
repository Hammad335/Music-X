package com.codewithhamad.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE= 1;
    static ArrayList<MusicFiles> musicFiles;
    static boolean shuffleBoolean= false, repeatBoolean= false;
    static ArrayList<MusicFiles> albums= new ArrayList<>();
    private EditText searchBox;
    private long backPressedTime;
    private Toast backToast;
    private Spinner spinner;
    private String MY_SORT_PREF= "SortOrder";
    Toolbar toolbar;


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar= findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        permission();
        searchBox= findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                initSearch(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();

        switch (item.getItemId()) {
            case R.id.by_name:
                Toast.makeText(MainActivity.this, "by name seleted", Toast.LENGTH_SHORT).show();
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_date:
                Toast.makeText(MainActivity.this, "by date seleted", Toast.LENGTH_SHORT).show();
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_size:
                Toast.makeText(MainActivity.this, "by size seleted", Toast.LENGTH_SHORT).show();
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initSearch(CharSequence charSequence) {
        String userInput= searchBox.getText().toString().toLowerCase();
        ArrayList<MusicFiles> myFiles= new ArrayList<>();
        for(MusicFiles song : musicFiles){
            if(song.getTitle().toLowerCase().contains(userInput)){
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void permission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
            , REQUEST_CODE);
        }
        else{
            musicFiles= getAllAudio(this);
            initViewPager();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                musicFiles= getAllAudio(this);
                initViewPager();
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                        , REQUEST_CODE);
            }
        }
    }

    private void initViewPager() {
        ViewPager viewPager= findViewById(R.id.viewpager);
        TabLayout tabLayout= findViewById(R.id.tab_layout);

        ViewPagerAdapter viewPagerAdapter= new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public ArrayList<MusicFiles> getAllAudio(Context context){

        SharedPreferences sharedPreferences= getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);
        String sortOrder= sharedPreferences.getString("sorting", "sortByName");
        ArrayList<String> duplicate= new ArrayList<>();
        albums.clear();
        ArrayList<MusicFiles> tempAudioList= new ArrayList<>();

        String order= null;
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch (sortOrder){
            case "sortByName":
                order= MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;

            case "sortByDate":
                order= MediaStore.MediaColumns.DATE_ADDED + " ASC";

                break;

            case "sortBySize":
                order= MediaStore.MediaColumns.SIZE + " DESC";
                break;
        }
        Log.d("order", "getAllAudio: " + order);

        String[] projection= {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };

        Cursor cursor= context.getContentResolver().query(uri, projection, null, null, order);

        if(cursor != null){
            while(cursor.moveToNext()){
                String album= cursor.getString(0);
                String title= cursor.getString(1);
                String duration= cursor.getString(2);
                String path= cursor.getString(3);
                String artist= cursor.getString(4);
                String id= cursor.getString(5);

                MusicFiles musicFiles= new MusicFiles(path, title, artist, album, duration, id);
                // log.e for checking purpose
                Log.e("path: " + path, "Album: " + album);
                
                tempAudioList.add(musicFiles);

                if(!duplicate.contains(album)){
                    albums.add(musicFiles);
                    duplicate.add(album);

                }
            }
            cursor.close();
        }
//        if(sortOrder.equals("sortByDate")){
//            Log.d("c1", "getAllAudio: "+ "started");
//            ArrayList<MusicFiles> tempList= new ArrayList<>();

//            for(int i=tempAudioList.size()-1; i>=0; i-- ){
//                tempList.add(tempAudioList.get(i));
//            }
//            SongsFragment.musicAdapter.updateList(tempList);
//            return tempList;
//        }
//        else{
            return tempAudioList;
//        }
    }

    @Override
    public void onBackPressed() {

        if(backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            System.exit(0);
        }
        else{
            backToast=Toast.makeText(this, "Press Back Again To Exit.", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime= System.currentTimeMillis();
    }


}