package com.example.syncclient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class folderSaveList extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1001;
    String savingName;
    String savingPath;
    SharedPreferences prefs;
    SharedPreferences connectionprefs;


    BufferedReader input;
    BufferedWriter output;
    String user;
    String ip;
    int port;

    Uri fileSource;

    ListView folderList;
    saveFileAdapter adapter;

    int cont;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_save_list2);

        folderList = findViewById(R.id.folderList);
        prefs = getSharedPreferences("folders", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("");
        editor.apply();

        connectionprefs = getSharedPreferences("conection", Context.MODE_PRIVATE);


        Button addButton = findViewById(R.id.add_input);

        folderList = findViewById(R.id.folderList);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MiApp:WakeLockTag");

        while(!wakeLock.isHeld()) {
            Log.d("TAG", "Requesting wakeLock");
            wakeLock.acquire();
        }


        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            Uri uri = data.getData();
                            DocumentFile documentFile = DocumentFile.fromTreeUri(folderSaveList.this, uri);
                            fileSource = uri;

                            adapter = new saveFileAdapter(folderSaveList.this , documentFile);
                            folderList.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                            send();
                        }
                    }
                }
        );
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
                filePickerLauncher.launch(i);


            }
        });


    }



    private void send() {
        user = connectionprefs.getString("user", "");
        ip = connectionprefs.getString("ip", "");
        port = Integer.parseInt(connectionprefs.getString("port", "9001"));

        CheckBox checkBox = findViewById(R.id.dateCheckBox);
        boolean isChecked = checkBox.isChecked();

        if(isChecked){
            user = addDateToUser(user);
        }

        Log.d("TAG", "Valor de user: " + user);
        Log.d("TAG", "Valor de ip: " + ip);
        Log.d("TAG", "Valor de port: " + port);

        DocumentFile source = DocumentFile.fromTreeUri(folderSaveList.this, fileSource);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = new Socket(ip, port)) {
                    Log.d("TAG","conection stablished");
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    custompacket uploadSYN = new custompacket("UPLOAD_SYN", user, "");
                    uploadSYN.send(output);
                    custompacket response = new custompacket(input);

                    if (response.PacketMethod.equals(method.UPLOAD_ACK.getMethod())) {
                        Log.d("TAG","UPLOAD ALLOWED");
                        Thread.sleep(100);

                        sendFiles(DocumentFile.fromTreeUri(folderSaveList.this, fileSource), output , isChecked);

                        System.out.println("Files uploaded, sending upload end");
                        new custompacket(method.UPLOAD_END, user, "").send(output);

                    } else if (response.PacketMethod.equals(method.UNKNOWN_METHOD.getMethod())) {
                        System.out.println("UNKNOWN METHOD: Update client");
                    } else if (response.PacketMethod.equals(method.UPLOAD_CANCEL.getMethod())) {
                        System.out.println("UPLOAD CANCELED: ABORTING UPLOAD");
                    }

                } catch (Exception e) {
                    // Manejar errores aquí
                }
            }
        });

        thread.start();
    }

    private String addDateToUser(String user) {
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String fechaFormateada = fechaActual.format(formatter);
        System.out.println(fechaFormateada);

        StringBuilder buser = new StringBuilder();
        buser.append(fechaFormateada).append(" ").append(user);
        return  buser.toString();
    }

    private void sendFiles(DocumentFile rootFile, BufferedWriter out , boolean isChecked) throws IOException, Exception {
        Log.d("TAG","uploading file: " + rootFile.getName());
        // Si es un directorio, llamada recursiva
        if (rootFile.isDirectory()) {
            for (DocumentFile file : rootFile.listFiles()) {
                sendFiles(file, out, isChecked);
            }

        } else {

            try {
                // leemos la data del archivo
                byte[] data = readFile(rootFile);
                //System.out.println("data = " + data);

                // Encode path
                String Filepath = "";
                Filepath = simplifyRoute(fileSource, rootFile.getUri());
                String EncodedPath = Base64.getEncoder().encodeToString(Filepath.getBytes());

                // encode data
                String Encodeddata = Base64.getEncoder().encodeToString(data);
                custompacket sended = new custompacket(method.UPLOAD_FILE.getMethod(), user, EncodedPath, Encodeddata);

                sended.send(out);
                // TODO:Tratar este upload ack
                custompacket ack =  new custompacket(input);
                while(!ack.PacketMethod.equals(method.UPLOAD_FILE_ACK.getMethod())){
                    Thread.sleep(100);
                    Log.d("TAG","Waiting for upload ack");
                    ack =  new custompacket(input);
                }

                if(ack.PacketMethod.equals(method.UPLOAD_FILE_ACK.getMethod())){
                    Log.d("TAG","Got an upload ack");
                    adapter.setTrue(rootFile.getName());
                    cont ++;
                    if(cont == 10){
                        folderList.setAdapter(adapter);
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });


                        cont = 0;
                    }

                    //Thread.sleep(100);


                }




            }catch (IOException e){
                Log.d("TAG","Error sending file, aborting sending more msg");
                throw new IOException();
            }catch (Exception e){
                Log.d("TAG" ,"Warning log: " + e.getCause()  + "   "  + e.getMessage());
                Log.d("TAG" ,e.getCause()  + e.getMessage());
            }

        }
    }

    byte[] readFile(DocumentFile rootFile) {

        try {
            try (FileInputStream stream = new FileInputStream(folderSaveList.this.getContentResolver().openFileDescriptor(rootFile.getUri(), "r").getFileDescriptor());) {
                byte[] bytes = new byte[(int) rootFile.length()];
                stream.read(bytes);
                return bytes;
            } catch (IOException e) {
                Log.d("TAG","Error al leer archivo como bytes: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }catch (Exception e){}


    return null;
    }

    public static String simplifyRoute(Uri ruta, Uri file) {
        String rootPath = ruta.getPath();
        rootPath = rootPath.replace(':','\\');

        String filePath = file.getPath();
        filePath = filePath.replace(':','\\');

        String[] arrayRuta = rootPath.split("\\\\");
        String[] arrayFile = filePath.split("\\\\");
        StringBuilder toret = new StringBuilder();

        for (int i = arrayRuta.length - 1; i < arrayFile.length; i++) {
            toret.append("\\").append(arrayFile[i]);
        }

        return toret.toString();
    }



}