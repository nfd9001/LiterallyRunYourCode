package com.nfd.literallyrunyourcode;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class RunInfoFragment extends Fragment {
    TextView distance;
    TextView steps;
    TextView next;
    public RunInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_run_info, container, false);
    }

    public void init(){
        distance = (TextView) getView().findViewById(R.id.distanceRanTextView);
        steps = (TextView) getView().findViewById(R.id.stepsRunTextView);
        next = (TextView) getView().findViewById(R.id.distToNextTextView);
    }

    public void updateDistanceRan(float d){
        //linter's overreacting again
        String s = getString(R.string.distance_ran) + String.format("%.1f", d) + " m";
        distance.setText(s);
        Log.i("test", distance.getText().toString() + "aaaa");
    }

    public void updateSteps(int s){
        String t = getString(R.string.distance_ran) + s;
        steps.setText(t);
    }

    public void updateNext(float d){
        String s = getString(R.string.distance_to_next_program_step) + String.format("%.1f", d) + " m";
        next.setText(s);
    }

}
