package com.example.syncclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class conectionParams extends AppCompatActivity {
    String user;
    String ip;
    int port;

    EditText user_input;
    EditText ip_input;
    EditText port_input;
    SharedPreferences prefs;
    lastUploadAdapter adapter;

    boolean test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conection_params);

        user_input = findViewById(R.id.user_input);
        ip_input = findViewById(R.id.ip_input);
        port_input = findViewById(R.id.port_input);
        ListView users = findViewById(R.id.lastUploads);
        CheckBox checkBox = findViewById(R.id.test_result);

        test = false;
        prefs = getSharedPreferences("conection", Context.MODE_PRIVATE);

        loadPreferences();

        Button save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            savePreferences();
            }
        });

        Button test_button = findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        final boolean resTest = testServer();
                        test = resTest;
                        String lastUploads = getUploads();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d("TAG", "Estado de conexión: " + resTest);
                                // Usar la variable final adicional para actualizar la interfaz de usuario
                                checkBox.setChecked(resTest);
                                if(resTest){
                                    adapter = new lastUploadAdapter(conectionParams.this,lastUploads);
                                    users.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }


                            }
                        });
                    }
                }).start();
            }
        });


        Button auto_button = findViewById(R.id.auto_button);
        auto_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String valor = port_input.getText().toString();
                try {
                    port = Integer.parseInt(valor);
                }catch (Exception e){
                    port = 9001;
                    Toast.makeText(getApplicationContext(), "Usando numero de puerto predeterminado", Toast.LENGTH_SHORT).show();
                    port_input.setText("9001");
                    try {
                        Thread.sleep(1000);
                    }catch (Exception x){

                    }
                }

                if(port <0 || port> 65535){
                    Toast.makeText(getApplicationContext(), "Usando numero de puerto predeterminado", Toast.LENGTH_SHORT).show();
                    port = 9001;
                    port_input.setText("9001");
                    try {
                        Thread.sleep(1000);
                    }catch (Exception e){

                    }

                }
                Toast.makeText(getApplicationContext(), "Buscando servidor, espera unos segundos", Toast.LENGTH_SHORT).show();

                new Thread(new Runnable() {
                    public void run() {
                        String IP = findServer(getheadIP());
                        Log.d("TAG", "IP DEL SERVER: " + IP);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                if(IP.equals("ERROR"))
                                Toast.makeText(getApplicationContext(), "No se encontro el servidor", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(getApplicationContext(), "Servidor encontrado IP, añadida al campo IP", Toast.LENGTH_SHORT).show();
                                ip_input.setText(IP);

                            }
                        });
                    }
                }).start();
            }
        });






    }

    private String getheadIP(){
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int IipAddress = wifiInfo.getIpAddress();
        String SipAddress = String.format("%d.%d.%d",
                (IipAddress & 0xff), (IipAddress >> 8 & 0xff),
                (IipAddress >> 16 & 0xff));
        StringBuilder ipAddress = new StringBuilder(SipAddress);
        ipAddress.append(".");
        Log.d("TAG", "IP DEL TLF: " + ipAddress.toString());
        return ipAddress.toString();
    }

    private String findServer(String headIP){
        for(int i= 1; i< 256; i++){
            Log.d("TAG", "TESTING IP: " + headIP + i + ":" + port);

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(headIP + i, port), 50);
                //socket.setSoTimeout();
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


                custompacket request = new custompacket(method.FIND_SERVER.getMethod(), user, "");
                request.send(output);

                Thread.sleep(100);

                custompacket response = new custompacket(input);

                // Publica un Runnable en el UI thread

                if(response.PacketMethod.equals(method.FIND_SERVER_ACK.getMethod())){
                    testServer();
                    return headIP + i;

                }
            }catch (Exception e){}
        }
        return "ERROR";
    }

    private String getUploads() {
        try {
            user = user_input.getText().toString();
            ip = ip_input.getText().toString();
            String valor = port_input.getText().toString();
            port = Integer.parseInt(valor);
        }catch (Exception e){
            return "";
        }


        try (Socket socket = new Socket(ip, port)) {
            socket.setSoTimeout(200);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


            custompacket request = new custompacket(method.LAST_UPLOADS_SYN.getMethod(), user, "");
            request.send(output);

            custompacket response = new custompacket(input);
            socket.close();

            Log.d("TAG", response.data);

            return response.data;

        } catch (Exception e) {
            return "";
        }
    }

    private void loadPreferences() {
        Log.d("TAG", "Leyendo preferencias");
        //:Leer los archivos de la app para ver si se guardo algo anteriormente y cargarlos
        user = prefs.getString("user", "");
        ip = prefs.getString("ip", "");
        port = Integer.parseInt(prefs.getString("port", "9001"));

        //Log.d("TAG", "Valor de user: " + user);
        //Log.d("TAG", "Valor de ip: " + ip);
        //Log.d("TAG", "Valor de port: " + port);
        if (!user.equals("")) {
            user_input.setText(user);
            ip_input.setText(ip);

        }

        port_input.setText("" + port);
    }

    private void savePreferences(){
         user = user_input.getText().toString();

         ip = ip_input.getText().toString();

        String valor = port_input.getText().toString();
        port = Integer.parseInt(valor);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user", user);
        editor.putString("ip", ip);
        editor.putString("port", "" + port);
        editor.commit();

        if(test)
            this.startActivity( new Intent( this, folderSaveList.class ) );
        else
            Toast.makeText(getApplicationContext(), "Verifica la conexión", Toast.LENGTH_SHORT).show();
    }



    private boolean testServer(){
        try {
            user = user_input.getText().toString();
            ip = ip_input.getText().toString();
            String valor = port_input.getText().toString();
            port = Integer.parseInt(valor);
        } catch (Exception e) {
            return false;
        }


        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 100);

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


            custompacket request = new custompacket(method.TEST.getMethod(), user, "Esto es un test");
            request.send(output);

            custompacket response = new custompacket(input);
            socket.close();

            Log.d("TAG", "the test result: " +  (response.PacketMethod.equals(method.TEST_RESPONSE.getMethod())));

            if((response.PacketMethod.equals(method.TEST_RESPONSE.getMethod())))
                return true;
            else
                return false;

        } catch (SocketTimeoutException e) {
            Log.d("TAG", "the test result: False");
            return false;
        }catch (Exception e){
            return false;
        }
    }
}