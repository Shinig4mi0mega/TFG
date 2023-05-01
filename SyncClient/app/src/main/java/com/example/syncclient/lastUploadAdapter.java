package com.example.syncclient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;


public class lastUploadAdapter extends BaseAdapter {
    private  LayoutInflater inflater;
    private  Context context;
    ArrayList<String> parsedUsers;
    ArrayList<String>  parsedDate;


    lastUploadAdapter(Context context,String data){
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        parsedUsers = new ArrayList<String>();
        parsedDate = new ArrayList<String>();



        String[] pipeSplit = data.split(";");
        int i = 0;
        for(String s : pipeSplit){
            if(s.contains("=")){
                String[] equalSplit = s.split("=");
                Log.d("TAG", ""+s);
                parsedUsers.add(equalSplit[0]);
                parsedDate.add(equalSplit[1]);
                i++;
            }

        }
    }


    @Override
    public int getCount() {
        return parsedUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.lastuploads, viewGroup, false);
        }

        TextView user = (TextView) view.findViewById(R.id.LastUploadsUser);
        TextView date = (TextView) view.findViewById(R.id.LastUploadsDate);
        Log.d("TAG",parsedUsers.get(i) + "=" + parsedDate.get(i));
        user.setText(parsedUsers.get(i));
        date.setText(parsedDate.get(i));
        return view;
    }
}
