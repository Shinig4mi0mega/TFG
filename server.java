import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class server {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9000;
        File target = new File("target");

        try {
            new server().saveFiles(host, port, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
