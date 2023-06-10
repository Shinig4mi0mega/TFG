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

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class saveFileAdapter extends BaseAdapter {

    private  LayoutInflater inflater;
    private  Context context;
    //HashMap<String, Boolean> files = new HashMap<String,Boolean>();
    ArrayList<String> files = new ArrayList<>();
    ArrayList<Boolean> state = new ArrayList<>();

    saveFileAdapter(Context context, DocumentFile fileToList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        makeFileList(fileToList);
    }

    private void makeFileList(DocumentFile fileToList) {

        if (fileToList.isDirectory()) {
            for (DocumentFile file : fileToList.listFiles()) {
                makeFileList(file);
            }
        }else{
            //files.put(fileToList.getName(),false);
            files.add(fileToList.getName());
            state.add(false);
        }
    }

    public boolean  setTrue(String key){
        if(!files.contains(key))
            return false;

        //files.put(key,true);
        state.set(files.indexOf(key),true);
        return true;
    }


    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public String getItem(int position) {
        return files.get(position);
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


        String fileToChange = files.get(i);
        Boolean fileState = state.get(i);
        Log.d("TAG","File State " + state.get(i));


        if(fileState){
            CheckBox cb = (CheckBox) view.findViewById(R.id.isUploaded);
            Log.d("TAG","This cant be false:  " + state.get(i));
            Log.d("TAG","updating check true" + files.get(i));
            cb.setChecked(true);
        }else{
            CheckBox cb = (CheckBox) view.findViewById(R.id.isUploaded);
            cb.setChecked(false);
        }


        TextView folderName = (TextView) view.findViewById(R.id.folderName);
        folderName.setText(files.get(i));
        return view;

    }




}
