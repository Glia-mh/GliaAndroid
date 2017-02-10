package com.team.r00ts;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adityaaggarwal on 12/15/15.
 */
public class AccountTypeSelectFragment extends Fragment {

    private AccountType currentAccountType;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View convertView;
        if(savedInstanceState==null)
                convertView=inflater.inflate(R.layout.account_type, container, false);
            else
                convertView=super.onCreateView(inflater, container, savedInstanceState);
        Button next=(Button)convertView.findViewById(R.id.accounttypeselectionnext);
        Drawable nextShape=next.getBackground();
        nextShape.setColorFilter(getResources().getColor(R.color.roots_green_unselected), PorterDuff.Mode.MULTIPLY);
        return convertView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView back= (TextView) getView().findViewById(R.id.backaccounttype);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity ma = (MainActivity) getActivity();
                ma.pager.setCurrentItem(ma.pager.getCurrentItem() - 1, true);
            }
        });


        final List<AccountType> accountTypeList = new ArrayList<>();
        accountTypeList.add(new AccountType(0, "Student"));
        accountTypeList.add(new AccountType(1, "Counselor"));
        AccountTypeListAdapter accountTypeListAdapter = new AccountTypeListAdapter(getActivity(), R.id.list_view_account_types, accountTypeList);
        final ListView accountTypeListView = (ListView) getView().findViewById(R.id.list_view_account_types);
        accountTypeListView.setAdapter(accountTypeListAdapter);
        accountTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentAccountType != null) {
                    accountTypeListView.getChildAt(currentAccountType.getPositioninList()).findViewById(R.id.schoolcheck).setVisibility(View.GONE);
                } else {
                    Button next = (Button) getView().findViewById(R.id.accounttypeselectionnext);
                    Drawable nextShape=next.getBackground();
                    nextShape.setColorFilter(getResources().getColor(R.color.roots_green), PorterDuff.Mode.MULTIPLY);
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().getIntent().putExtra("accountTypeNumber", currentAccountType.getObjectId());
                            MainActivity ma = (MainActivity) getActivity();
                            ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);
                        }
                    });
                }
                accountTypeListView.getChildAt(position).findViewById(R.id.schoolcheck).setVisibility(View.VISIBLE);

                currentAccountType = accountTypeList.get(position);
                currentAccountType.setPositioninList(position);


            }
        });


    }

    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        if(savedInstanceState==null){
                if(currentAccountType!=null){
                    Button next = (Button) getView().findViewById(R.id.accounttypeselectionnext);
                    Drawable nextShape=next.getBackground();
                    nextShape.setColorFilter(getResources().getColor(R.color.roots_green), PorterDuff.Mode.MULTIPLY);
                    next.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().getIntent().putExtra("accountTypeNumber", currentAccountType.getObjectId());
                            MainActivity ma = (MainActivity) getActivity();
                            ma.pager.setCurrentItem(ma.pager.getCurrentItem() + 1, true);
                        }
                    });
                }
            }
        }


    public class AccountTypeListAdapter extends ArrayAdapter<AccountType> {

        public AccountTypeListAdapter(Context context, int resource, List<AccountType> items) {
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

            AccountType accountType = getItem(position);

            if (accountType != null) {
                TextView tt1 = (TextView) v.findViewById(R.id.school_name_text);

                if (tt1 != null) {
                    tt1.setText(accountType.getaccountType());
                }


            }
            if(currentAccountType!=null) {
                if (currentAccountType.equals(accountType)) {
                    v.findViewById(R.id.schoolcheck).setVisibility(View.VISIBLE);
                }
            }


            return v;
        }




    }



}