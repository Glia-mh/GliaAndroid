package com.team.roots;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by adityaaggarwal on 12/15/15.
 */
public class SchoolSelectFragment extends android.support.v4.app.Fragment {

    private School currentSchool;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


            if(savedInstanceState==null) {
                Log.d("recreating","recreating view");

                return inflater.inflate(R.layout.school_selection, container, false);

            }else
                return super.onCreateView(inflater, container, savedInstanceState);

    }
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        if(savedInstanceState==null){
            MainActivity ma=(MainActivity)getActivity();
            if(ma.getSchools()!=null){
                populateSchoolListUI();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

            TextView back = (TextView) getView().findViewById(R.id.back_school_selection);
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity ma = (MainActivity) getActivity();
                    ma.pager.setCurrentItem(ma.pager.getCurrentItem() - 1, true);
                }
            });







      //  }
    }
    public void populateSchoolListUI(){

        //At this point, all the schools are known and can be accessed using MainActivity
        MainActivity ma = (MainActivity) getActivity();

        //Turn off loading sign
        ProgressBar pb = (ProgressBar)getActivity().findViewById(R.id.loading_sign_for_schools);
        pb.setVisibility(View.GONE);


        SchoolListAdapter schoolListAdapter = new SchoolListAdapter(getActivity(), R.id.list_view_schools, ma.getSchools());
        Log.d("school list", "school list view" + ma.getSchools().toString() + "size" + ma.getSchools().size());
        final ListView schoolListView = (ListView) getView().findViewById(R.id.list_view_schools);

        //enable the school list
        schoolListView.setVisibility(View.VISIBLE);

        schoolListView.setAdapter(schoolListAdapter);
        schoolListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity ma = (MainActivity) getActivity();
                if (currentSchool != null) {
                    schoolListView.getChildAt(currentSchool.getPositioninList()).findViewById(R.id.schoolcheck).setVisibility(View.GONE);

                } else {
                    Button next = (Button) getView().findViewById(R.id.schoolselectionnext);
                    next.setBackgroundColor(getResources().getColor(R.color.roots_green_darker));
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            getActivity().getIntent().putExtra("schoolname", currentSchool.getSchoolName());
                            getActivity().getIntent().putExtra("schoolobjectid", currentSchool.getObjectId());
                            MainActivity ma = (MainActivity) getActivity();
                            ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);
                        }
                    });
                }
                schoolListView.getChildAt(position).findViewById(R.id.schoolcheck).setVisibility(View.VISIBLE);

                currentSchool = ma.getSchools().get(position);
                currentSchool.setPositioninList(position);


            }
        });
        if(currentSchool!=null){
            Button next = (Button) getView().findViewById(R.id.schoolselectionnext);
            next.setBackgroundColor(getResources().getColor(R.color.roots_green_darker));
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getActivity().getIntent().putExtra("schoolname", currentSchool.getSchoolName());
                    getActivity().getIntent().putExtra("schoolobjectid", currentSchool.getObjectId());
                    MainActivity ma = (MainActivity) getActivity();
                    ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);
                }
            });
        }
    }

                    public class SchoolListAdapter extends ArrayAdapter<School> {

                        public SchoolListAdapter(Context context, int resource, List<School> items) {
                            super(context, resource, items);
                        }

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {

                            View v = convertView;

                            if (v == null) {
                                LayoutInflater vi;
                                vi = LayoutInflater.from(getContext());
                                v = vi.inflate(R.layout.schoollistrow, null);
                            }

                            School school = getItem(position);

                            if (school != null) {
                                TextView tt1 = (TextView) v.findViewById(R.id.school_name_text);

                                if (tt1 != null) {
                                    tt1.setText(school.getSchoolName());
                                }


                            }
                            if(currentSchool!=null) {
                                if (currentSchool.equals(school)) {
                                    v.findViewById(R.id.schoolcheck).setVisibility(View.VISIBLE);
                                }
                            }

                            return v;
                        }




                    }


                }