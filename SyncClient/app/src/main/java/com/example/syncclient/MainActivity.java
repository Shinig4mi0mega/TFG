package com.example.syncclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    String user;
    String ip;
    int port;

    EditText user_input;
    EditText ip_input;
    EditText port_input;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_input = findViewById(R.id.user_input);
        ip_input = findViewById(R.id.ip_input);
        port_input = findViewById(R.id.port_input);
        CheckBox checkBox = findViewById(R.id.test_result);

        prefs = getSharedPreferences("conection", Context.MODE_PRIVATE);

        loadPreferences();

        Button save_button = findViewById(R.id.save_button);
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            savePreferences();
            }
        });

        //TODO:hacer que funcione el boton test
        Button test_button = findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        final boolean res = testServer();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.d("TAG", "Estado de conexión: " + res);
                                // Usar la variable final adicional para actualizar la interfaz de usuario
                                checkBox.setChecked(res);
                            }
                        });
                    }
                }).start();
            }
        });




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
            port_input.setText("" + port);
        }
    }

    private void savePreferences(){
         user = user_input.getText().toString();

         ip = ip_input.getText().toString();

        String valor = port_input.getText().toString();
        port = Integer.parseInt(valor);

        //Log.d("TAG", "Valor de user: " + user);
        //Log.d("TAG", "Valor de ip: " + ip);
        //Log.d("TAG", "Valor de port: " + port);


        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("user", user);
        editor.putString("ip", ip);
        editor.putString("port", "" + port);
        editor.commit();

        this.startActivity( new Intent( this, folderSaveList.class ) );
    }



    private boolean testServer(){
        user = user_input.getText().toString();
        ip = ip_input.getText().toString();
        String valor = port_input.getText().toString();
        port = Integer.parseInt(valor);

        try (Socket socket = new Socket(ip, port)) {
            socket.setSoTimeout(200);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));


            custompacket request = new custompacket(method.TEST.getMethod(), user, "Esto es un test");
            request.send(output);

            custompacket response = new custompacket(input);
            socket.close();

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}