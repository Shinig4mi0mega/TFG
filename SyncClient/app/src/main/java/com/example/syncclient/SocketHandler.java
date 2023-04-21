package com.example.syncclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Base64;

public class SocketHandler {
    private String ip;
    private int port;
    private String user;
    private BufferedReader input;
    private BufferedWriter output;

    public void send(Uri fileSource, Context context) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                String fileSource = strings[0];

                SharedPreferences connectPrefs = context.getSharedPreferences("conection", Context.MODE_PRIVATE);
                user = connectPrefs.getString("user", "");
                ip = connectPrefs.getString("ip", "");
                port = Integer.parseInt(connectPrefs.getString("port", "9001"));
                Log.d("TAG", "loaded ip: " + ip + " port. " + port + " user: " + user);

                try (Socket socket = new Socket(ip, port)) {
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    custompacket uploadSYN = new custompacket("UPLOAD_SYN", user, "");
                    uploadSYN.send(output);
                    custompacket response = new custompacket(input);

                    if (response.PacketMethod.equals(method.UPLOAD_ACK.getMethod())) {
                        Log.d("TAG", "UPLOAD ALLOWED");
                        DocumentFile rootFile = DocumentFile.fromTreeUri(context, Uri.parse(fileSource));
                        sendFiles(rootFile, output, fileSource);

                        Log.d("TAG", "Files uploaded, sending upload end");
                        new custompacket(method.UPLOAD_END, user, "").send(output);

                    } else if (response.PacketMethod.equals(method.UNKNOWN_METHOD.getMethod())) {
                        System.out.println("UNKNOWN METHOD: Update client");
                    } else if (response.PacketMethod.equals(method.UPLOAD_CANCEL.getMethod())) {
                        System.out.println("UPLOAD CANCELED: ABORTING UPLOAD");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    private void sendFiles(DocumentFile rootFile, BufferedWriter out, String fileSource) throws IOException {
        Log.d("TAG","uploading file: " + rootFile.getName());
        // Si es un directorio, llamada recursiva
        if (rootFile.isDirectory()) {
            for (DocumentFile file : rootFile.listFiles()) {
                sendFiles(file, out, fileSource);
            }

        } else {

            // leemos la data del archivo
            byte[] data = readFile(rootFile);
            //System.out.println("data = " + data);

            // Encode path
            String Filepath = rootFile.getUri().getPath();
            //Filepath = simplifyRoute(fileSource, Filepath);
            Log.d("TAG", "ORIGINAL PATH: " + Filepath);
            Log.d("TAG", "SIMPLIFIEDpATH: " + Filepath);
            String EncodedPath = Base64.getEncoder().encodeToString(Filepath.getBytes());

            // encode data
            String Encodeddata = Base64.getEncoder().encodeToString(data);
            custompacket sended = new custompacket(method.UPLOAD_FILE.getMethod(), user, EncodedPath, Encodeddata);

            sended.send(out);
            // TODO:Tratar este upload ack
            new custompacket(input);
        }
    }

    byte[] readFile(DocumentFile rootFile) {

        try (FileInputStream stream = new FileInputStream(rootFile.getUri().getPath())) {
            byte[] bytes = new byte[(int) rootFile.length()];
            stream.read(bytes);
            return bytes;
        } catch (IOException e) {
            System.err.println("Error al leer archivo como bytes: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String simplifyRoute(String ruta, String file) {
        String[] arrayRuta = ruta.split("\\\\");
        String[] arrayFile = file.split("\\\\");
        StringBuilder toret = new StringBuilder();

        for (int i = arrayRuta.length - 1; i < arrayFile.length; i++) {
            toret.append("\\").append(arrayFile[i]);
        }

        return toret.toString();
    }
}