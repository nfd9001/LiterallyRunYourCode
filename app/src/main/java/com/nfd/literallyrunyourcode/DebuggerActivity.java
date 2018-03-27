package com.nfd.literallyrunyourcode;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import BFModel.BFModel;
import BFModel.InvalidModelStateException;

/**
 * @author Alexander Ronsse-Tucherov
 * Activity hosting a BF debugger.
 */
public class DebuggerActivity extends AppCompatActivity implements LocationListener {

    private BFModel model;
    private RecyclerView m;
    private TextView output;
    private TextView codeView;
    private String program;
    private Location previous;
    private float totalDistance;
    private LocationManager lm;
    private RunInfoFragment rif;

    private float distanceToStep = 20;
    private float remainingDistUntilStep = distanceToStep;
    private boolean modelDied = false;
    private boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Explode());
        getWindow().setExitTransition(new Explode());
        getWindow().setAllowEnterTransitionOverlap(true);
        setContentView(R.layout.activity_debugger);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        output = findViewById(R.id.outputTextView);
        codeView = findViewById(R.id.codeView);
        rif = (RunInfoFragment) getFragmentManager().findFragmentById(R.id.RunInfoFragment);
        rif.init();

        output.setMovementMethod(new ScrollingMovementMethod());
        codeView.setMovementMethod(new ScrollingMovementMethod());


        m = findViewById(R.id.CellRecyclerView);
        m.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Intent intent = getIntent();
        String programNonCleaned = intent.getStringExtra("BF_PROGRAM");
        String inputNonCleaned = intent.getStringExtra("BF_INPUT");
        if (inputNonCleaned == null){
            inputNonCleaned = "";
        }
        program = BFModel.stripComments(programNonCleaned);
        //strips non-ascii chars
        String input = inputNonCleaned.replaceAll("[^\\p{ASCII}]", "");

        try {
            model = new BFModel(program, input);
        } catch (Exception e) {
            modelDied(e);
            finish();
        }

        m.setAdapter(new CellAdapter(model));
        m.getAdapter().notifyDataSetChanged();
        setCodeViewText();

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            finish();
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
    }

    public void modelDied(Exception e){
        String s = e.getMessage();
        if (s == null || s.isEmpty()){
            s = "Some error occurred.";
        }
        modelDied = true;
        if (running){
            running = false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(s)
                .setTitle(R.string.error_dialog_title);

        Log.w("mdeath", e);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void step(View view){
        Log.i("step", "called");
        try {
            if (!modelDied){
                modelDied = model.step();
                output.setText(model.getOutput());
                m.getAdapter().notifyDataSetChanged();
                m.scrollToPosition(model.getCurrentCellPosition());
                setCodeViewText();
                int sndid = (modelDied) ? R.raw.crash : R.raw.click;
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), sndid);
                mp.start();
                rif.updateSteps(model.getStepCount());
            }else {
                new AlertDialog.Builder(this).setTitle("Notice").setMessage("Program halted.").create().show();
            }
        } catch (InvalidModelStateException e) {
            modelDied(e);
        } catch (Exception e){
            e.printStackTrace();
            modelDied(e);
        }
    }


    public void setCodeViewText(){
        SpannableString s = new SpannableString(program);
        int startIndex = model.getCurrentInstructionIndex();
        int endIndex = startIndex + 1;
        if (startIndex < program.length()){
            //getColor deprecated, but the linter suggests this anyway
            s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), startIndex, endIndex, 0);
        }
        codeView.setText(s);
    }

    public void checkIfStepNeeded(float diff){
        Log.i("step", "entered check: diff = " + diff + ", remaining = " + remainingDistUntilStep);
        while (diff >= remainingDistUntilStep){
            Log.i("step", "loop head: diff = " + diff + ", remaining = " + remainingDistUntilStep);
            diff -= remainingDistUntilStep;
            step(null);
            remainingDistUntilStep = distanceToStep;
            Log.i("step", "after subtract: diff = " + diff + ", remaining = " + remainingDistUntilStep);
        }
        remainingDistUntilStep -= diff;
        Log.i("step", "after else: diff = " + diff + ", remaining = " + remainingDistUntilStep);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("loc", "got update");
        if (location == null){
            Log.i("loc", "update was null");
            return;
        }
        if (previous == null){
            previous = location;
            rif.updateNext(distanceToStep);
            rif.updateSteps(0);
            rif.updateDistanceRan(0);
            Log.i("loc", "set initial debugger loc");
            return;
        }
        float diff = location.distanceTo(previous);
        totalDistance += diff;
        previous = location;
        checkIfStepNeeded(diff);
        rif.updateNext(remainingDistUntilStep);
        rif.updateDistanceRan(totalDistance);
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
}
