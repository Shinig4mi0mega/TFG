import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    static String host;
    static int port;
    static String  fileSystemRootFile;
    private Thread serverThread;
    private int nthreads;
    private HashMap<String, String> users;

    public void start() {

        // defaultSetUp
        // if(args.length == 0){
        host = "localhost";
        port = 9000;
        fileSystemRootFile = System.getProperty("user.dir") + "\\DondeGuardoLosArchivosDeFormaTemporal";
        File savingFile = new File(fileSystemRootFile);
        if(!savingFile.exists())
            savingFile.mkdirs();
        nthreads = 100;
        // }

        //TODO:register and login of users
        //users = new HashMap<>();
        //mapUsers();

        this.serverThread = new Thread() {
            @Override
            public void run() {
                try (final ServerSocket serverSocket = new ServerSocket(port)) {
                    ExecutorService threadPool = Executors.newFixedThreadPool(nthreads);
                    while (true) {

                        Socket socket = serverSocket.accept();
                        // if (stop)
                        // break;
                        ServiceThread st = new ServiceThread(socket,fileSystemRootFile);
                        threadPool.execute(st);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // this.stop = false;
        this.serverThread.start();

        try {
            //new server().saveFiles(host, port, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean mapUsers() {
        System.out.println("Loading users...");
        File userFile = new File("users");
        if (!userFile.exists()) {
            System.out.println("No user file found, building file");
            try {
                userFile.createNewFile();
            } catch (IOException e) {}
            return false;
        }

        Scanner reader = null;
        try {
            reader = new Scanner(userFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            users.put(data.split(":")[0], data.split(":")[1]);
            System.out.println("user: " + data);
        }

        reader.close();

        return true;
    }

    private void saveFiles(String host, int port, File target) throws IOException {
        try (Socket socket = new Socket(host, port);
                InputStream in = socket.getInputStream();
                FileOutputStream out = new FileOutputStream(target)) {
            System.out.println("Connected to " + host + " on port " + port);

            copyDirectory(in, target);
        }
    }

    private void copyDirectory(InputStream in, File target) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        FileOutputStream out = new FileOutputStream(target);
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.close();
    }
}
