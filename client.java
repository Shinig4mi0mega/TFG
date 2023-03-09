import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class client {
  private String fileSource;

  public void start() {
    fileSource = System.getProperty("user.dir") + "\\PRUEBAS" + "\\SOURCE";

    try (Socket socket = new Socket("localhost", 9000)) {
      BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
      BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      System.out.println("TEST REQUEST----------------------------------------------------------------");
      System.out.println("client test request:");
      custompacket request = new custompacket(method.TEST.getMethod(), "Esto es un test");
      request.send(output);

      custompacket response = new custompacket(input);
      System.out.println(response.toString());
      System.out.println("END TEST REQUEST----------------------------------------------------------------");

      System.out.println(fileSource);

      socket.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

    File source = new File(fileSource);
    System.out.println("Absolute path:" + source.getAbsolutePath());

    try (Socket socket = new Socket("localhost", 9000)) {
      BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
      BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      custompacket uploadSYN = new custompacket("UPLOAD_SYN", "");
      uploadSYN.send(output);
      custompacket response = new custompacket(input);
      if (response.method.equals(method.UPLOAD_ACK.getMethod())) {
        System.out.println("UPLOAD ALLOWED");
      } else if (response.method.equals(method.UNKNOWN_METHOD.getMethod())) {
        System.out.println("UNKNOWN METHOD: Update client");
      } else if (response.method.equals(method.UPLOAD_CANCEL.getMethod())) {
        System.out.println("UPLOAD CANCELED: ABORTING UPLOAD");
      }

      
    } catch (Exception e) {}

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
