import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class client {
    public static void main(String[] args) {
        File source = new File("source");
    int port = 9000;

    try {
        new client().sendFiles(port, source);
    } catch (IOException e) { e.printStackTrace();}
  }

  private void sendFiles(int port, File source) throws IOException {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("Server started on port " + port);

      while (true) {
        try (Socket clientSocket = serverSocket.accept();
             OutputStream out = clientSocket.getOutputStream()) {
          System.out.println("Accepted connection from " + clientSocket.getInetAddress());

          copyDirectory(source, out);
        }
      }
    }
  }

  private void copyDirectory(File source, OutputStream out) throws IOException {
    if (source.isDirectory()) {
      for (File file : source.listFiles()) {
        copyDirectory(file, out);
      }
    } else {
      try (FileInputStream in = new FileInputStream(source)) {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
          out.write(buffer, 0, read);
        }
      }
    }
  }
}


