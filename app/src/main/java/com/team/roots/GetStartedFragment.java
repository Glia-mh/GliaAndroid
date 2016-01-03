package com.team.roots;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * Created by adityaaggarwal on 12/15/15.
 */
public class GetStartedFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.get_started, container, false);
        // Inflate the layout for this fragment
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
            Button get_started = (Button) getView().findViewById(R.id.getstarted);
            get_started.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isNetworkAvailable()) {
                        MainActivity ma = (MainActivity) getActivity();
                        ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);


                    } else {
                        MainActivity ma = (MainActivity) getActivity();
                        ma.getWelcomeAlertDialog(R.string.no_internet_connection).show();
                    }
                }
            });
    }




    //Network Check
    public boolean isNetworkAvailable(){
        RootsApp rootsAppInstance=(RootsApp)(getActivity().getApplication());
        return rootsAppInstance.isNetworkAvailable();
    }



}
