package com.example.syncclient;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class saveFileAdapter extends BaseAdapter {

    Context context;
    Map folders;

    LayoutInflater inflater;

    String[] names;

    saveFileAdapter(Context context, Map<String, ?> folders) {
        this.context = context;
        this.folders = folders;
        this.inflater = LayoutInflater.from(context);
        names = (String[]) folders.keySet().toArray(new String[folders.keySet().size()]);
    }


    @Override
    public int getCount() {
        return folders.size();
    }

    @Override
    public String getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.saving_folders_layout, viewGroup, false);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(i);
                }
            }
        });

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.selectedToSave);
        TextView folderName = (TextView) view.findViewById(R.id.folderName);
        //Log.d("TAG",""+checkBox.isChecked());
        checkBox.setChecked(checkBox.isChecked());
        folderName.setText(names[i]);
        return view;

    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }



}
