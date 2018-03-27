package com.nfd.literallyrunyourcode;

import Persistence.Program;
import Persistence.ProgramDatabase;
import android.Manifest;
import android.app.ActivityOptions;
import android.arch.persistence.room.Room;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private TextView CodeTextView;
    private LocationManager lm;
    private double offset_distance = 100;
    private double radius = 30;
    private Location lastLocation;
    private boolean noMapDrawn = true;

    private List<Geofence> geofences = new ArrayList<>();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private boolean paused;

    private int BORDER_COLOR = Color.RED;
    //magic number is 35% transparency in argb (alternatively, a6?)
    private int FILL_COLOR = Color.RED | 0xa6000000;

    private Button pausebutton;
    private Button runbutton;

    ProgramDatabase db;
    private static final String SAVED_PROGRAM_NAME = "session";
    Program persistenceProgram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        setContentView(R.layout.activity_main);
        CodeTextView = (TextView) findViewById(R.id.CodeTextView);
        pausebutton = (Button) findViewById(R.id.pausebutton);
        runbutton = (Button) findViewById(R.id.runbutton);
        runbutton.setOnClickListener(this);
        pausebutton.setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        paused = false;
        db = Room.databaseBuilder(getApplicationContext(), ProgramDatabase.class, "storage").build();
        new LoadProgram().execute();

        requestPerm();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("map ready", "");
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        //setZones(getLocation());
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 1,this);

        paused = false;
    }

    LatLng getLocation(){
        Location l = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        return new LatLng(l.getLatitude(), l.getLongitude());
    }
    //Ref: https://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
    public static LatLng bumpNorth(LatLng l, double meters) {
        //111,111 meters lat ~= 1 degree lat
        return new LatLng(l.latitude + (meters / 111111), l.longitude);
    }

    public static LatLng bumpEast(LatLng l, double meters) {
        return new LatLng(l.latitude, l.longitude + (meters / 111111) * Math.cos(Math.toRadians(l.latitude)) * 2);
    }

    public static LatLng bumpSouth(LatLng l, double meters) {
        return bumpNorth(l, -meters);
    }

    public static LatLng bumpWest(LatLng l, double meters) {
        return bumpEast(l, -meters);
    }

    public static LatLng bumpNE(LatLng l, double meters) {
        return bumpNorth(bumpEast(l, meters), meters);
    }

    public static LatLng bumpNW(LatLng l, double meters) {
        return bumpNorth(bumpWest(l, meters), meters);
    }

    public static LatLng bumpSE(LatLng l, double meters) {
        return bumpSouth(bumpEast(l, meters), meters);
    }

    public static LatLng bumpSW(LatLng l, double meters) {
        return bumpSouth(bumpWest(l, meters), meters);
    }

    private void requestPerm() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    Location fromLatLng(LatLng l){
        Location o = new Location(LocationManager.GPS_PROVIDER);
        o.setLatitude(l.latitude);
        o.setLongitude(l.longitude);
        return o;
    }

    LatLng fromLocation(Location o){
        return new LatLng(o.getLatitude(), o.getLongitude());
    }

    void clearMapAndAlerts() {
        mMap.clear();
        mMap.setMyLocationEnabled(true);
    }

    /*   [ | + | ]
         ---------
         < |   | >
         ---------
         , | - | .

         Zone layout

    */
    void setZones(LatLng l) {
        addProxAlert_(bumpNW(l, offset_distance), "[", R.drawable.open);
        addProxAlert_(bumpNorth(l, offset_distance), "+", R.drawable.plus);
        addProxAlert_(bumpNE(l, offset_distance), "]", R.drawable.close);
        addProxAlert_(bumpWest(l, offset_distance), "<", R.drawable.left);
        addProxAlert_(bumpEast(l, offset_distance), ">", R.drawable.right);
        addProxAlert_(bumpSW(l, offset_distance), ",", R.drawable.comma);
        addProxAlert_(bumpSouth(l, offset_distance), "-", R.drawable.minus);
        addProxAlert_(bumpSE(l, offset_distance), ".", R.drawable.dot);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(l));
    }

    void addProxAlert_(LatLng l, String s, int resid){
        geofences.add(new Geofence(fromLatLng(l), (float) radius, s));
        mMap.addCircle(getStyledCircle(l));
        mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.fromResource(resid)));
    }

    CircleOptions getStyledCircle(LatLng l){
        return new CircleOptions().strokeColor(BORDER_COLOR).radius(radius).center(l);
    }

    @Override
    public void onClick(View view) {
        if (view == runbutton){
            run();
        }
        if (view == pausebutton){
            pause();
        }
    }

    void run(){
        //stop the updates for this activity
        if (!paused){
            pause();
        }
        String program = CodeTextView.getText().toString();
        final Intent intent = new Intent(this, DebuggerActivity.class);
        intent.putExtra("BF_PROGRAM", program);
        if (program.contains(",")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.enter_input);
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    intent.putExtra("BF_INPUT", input.getText().toString());
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getParent()).toBundle());
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.show();
        }
        else{
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }

    }

    void pause(){
        if (paused){
            paused = false;
            pausebutton.setText(R.string.pause);
            setZones(getLocation());
            mMap.setMyLocationEnabled(true);
        }
        else {
            paused = true;
            pausebutton.setText(R.string.resume);
            clearMapAndAlerts();
            mMap.setMyLocationEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if (noMapDrawn){
            noMapDrawn = false;
            setZones(fromLocation(location));
        }
        boolean hitFence = false;
        for (Geofence g : geofences){
            if (g.isIntersected(location)){
                CodeTextView.append(g.c);
                persistenceProgram.setName(CodeTextView.getText().toString());
                new UpdateProgram().execute(persistenceProgram);
                hitFence = true;
                break;
            }
        }
        if (hitFence){
            clearMapAndAlerts();
            geofences = new ArrayList<>();
            setZones(fromLocation(location));
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private class Geofence {
        Location l;
        float radius;
        String c;

        Geofence(Location l, float radius, String c){
            this.l = l;
            this.radius = radius;
            this.c = c;
        }

        boolean isIntersected(Location location){
            return (l.distanceTo(location) < radius);
        }
    }

    private class LoadProgram extends AsyncTask<Void, Void, Program> {
        @Override
        protected Program doInBackground(Void... voids) {
            List<Program> p = db.programDao().getAll();
            Program p_;
            if (p.isEmpty()) {
                p_ = new Program(SAVED_PROGRAM_NAME, "");
                db.programDao().insertAll(p_);
            }
            else{
                p_ = p.get(0);
            }
            return p_;
        }

        @Override
        protected void onPostExecute(Program p) {
            if (p == null){
                return;
            }
            persistenceProgram = p;
            CodeTextView.setText(p.getSource());
        }
    }
    private class UpdateProgram extends AsyncTask<Program, Void, String>{
        @Override
        protected String doInBackground(Program... programs) {
            db.programDao().updatePrograms(programs);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    void clearSavedProgram(){
        CodeTextView.setText("");
        persistenceProgram.setSource("");
        new UpdateProgram().execute(persistenceProgram);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.code_editor_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case(R.id.delete):
               clearSavedProgram();
               return true;
            case(R.id.help):
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.youtubelink)));
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
