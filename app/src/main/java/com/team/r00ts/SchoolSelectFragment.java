package com.team.r00ts;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adityaaggarwal on 12/15/15.
 */
public class SchoolSelectFragment extends android.support.v4.app.Fragment {

    private School currentSchool;
    private List<SchoolSelector> schools=new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

            View convertView;
            if(savedInstanceState==null) {
                Log.d("recreating","recreating view");

                convertView =inflater.inflate(R.layout.school_selection, container, false);

            }else
                convertView= super.onCreateView(inflater, container, savedInstanceState);
        if(currentSchool==null){

            if(convertView!=null) {
                Button next = (Button) convertView.findViewById(R.id.schoolselectionnext);
                Drawable nextShape = next.getBackground();
                nextShape.setColorFilter(getResources().getColor(R.color.roots_green_unselected), PorterDuff.Mode.MULTIPLY);
            }
        }
        return convertView;

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible()) {
            if (isVisibleToUser) {
                MainActivity ma=(MainActivity)getActivity();
                if(ma.getSchools()!=null){
                    populateSchoolListUI();
                }

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
        if(schools.size()==0) {
            for (School school : ma.getSchools()) {
                schools.add(new SchoolSelector(school, false));
            }
        }

        final SchoolListAdapter schoolListAdapter = new SchoolListAdapter(getActivity(), R.id.list_view_schools, schools);
        Log.d("school list", "school list view" + ma.getSchools().toString() + "size" + ma.getSchools().size());
        final ListView schoolListView = (ListView) getView().findViewById(R.id.list_view_schools);

        //enable the school list
        schoolListView.setVisibility(View.VISIBLE);

        schoolListView.setAdapter(schoolListAdapter);
        schoolListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity ma = (MainActivity) getActivity();
                    if(currentSchool!=null) {
                        schools.get(ma.getSchools().indexOf(currentSchool)).setSelected(false);
                    }
                    currentSchool=ma.getSchools().get(position);
                    schools.get(position).setSelected(true);

                //next button
                    Button next = (Button) getView().findViewById(R.id.schoolselectionnext);
                    Drawable nextShape = next.getBackground();
                    nextShape.setColorFilter(getResources().getColor(R.color.roots_green), PorterDuff.Mode.MULTIPLY);

                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            getActivity().getIntent().putExtra("schoolname", currentSchool.getSchoolName());
                            getActivity().getIntent().putExtra("schoolobjectid", currentSchool.getObjectId());
                            getActivity().getIntent().putExtra("schoolemail", currentSchool.getEmail());
                            MainActivity ma = (MainActivity) getActivity();
                            ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);

                        }
                    });
                    schoolListAdapter.notifyDataSetChanged();
                }
        });
        if(currentSchool!=null){
            Button next = (Button) getView().findViewById(R.id.schoolselectionnext);
            Drawable nextShape=next.getBackground();

            nextShape.setColorFilter(getResources().getColor(R.color.roots_green), PorterDuff.Mode.MULTIPLY);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    getActivity().getIntent().putExtra("schoolname", currentSchool.getSchoolName());
                    getActivity().getIntent().putExtra("schoolobjectid", currentSchool.getObjectId());
                    getActivity().getIntent().putExtra("schoolemail", currentSchool.getEmail());
                    MainActivity ma = (MainActivity) getActivity();
                    ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);
                }
            });
        }
    }

                    private class SchoolSelector {
                        private School school;
                        private boolean selected;
                        SchoolSelector(School school, boolean selected) {
                            this.school=school;
                            this.selected=selected;
                        }
                        public boolean getSelected() {
                            return selected;
                        }
                        public void setSelected(boolean selected){
                            this.selected=selected;
                        }
                        public School getSchool(){
                            return school;
                        }
                    }

                    public class SchoolListAdapter extends ArrayAdapter<SchoolSelector> {

                        public SchoolListAdapter(Context context, int resource, List<SchoolSelector> items) {
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

                            School school = getItem(position).getSchool();

                            if (school != null) {
                                TextView tt1 = (TextView) v.findViewById(R.id.school_name_text);

                                if (tt1 != null) {
                                    tt1.setText(school.getSchoolName());
                                }


                            }
                            if(getItem(position).getSelected()) {
                                    v.findViewById(R.id.schoolcheck).setVisibility(View.VISIBLE);
                            } else {
                                    v.findViewById(R.id.schoolcheck).setVisibility(View.GONE);
                            }

                            return v;
                        }




                    }


                }