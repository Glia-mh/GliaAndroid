package com.team.roots;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by adityaaggarwal on 12/15/15.
 */
public class GetStartedFragment extends android.support.v4.app.Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View getStartedView=inflater.inflate(R.layout.get_started, container, false);
        // Inflate the layout for this fragment
        Button get_started=(Button)getStartedView.findViewById(R.id.getstarted);
        TextView mini_desc=(TextView)getStartedView.findViewById(R.id.mini_desc);
        //font setting

        Drawable nextShape=(Drawable)get_started.getBackground();
        nextShape.setColorFilter(getResources().getColor(R.color.roots_green), PorterDuff.Mode.MULTIPLY);
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
        return getStartedView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }




    //Network Check
    public boolean isNetworkAvailable(){
        RootsApp rootsAppInstance=(RootsApp)(getActivity().getApplication());
        return rootsAppInstance.isNetworkAvailable();
    }



}
