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
import java.util.Base64;
import java.util.Scanner;

public class client {
  private String fileSource;
  BufferedReader input;
  BufferedWriter output;

  public void start() {
    fileSource = System.getProperty("user.dir") + "\\PRUEBAS" + "\\SOURCE";
    //fileSource = "C:\\Users\\Manolo\\Desktop\\haizea" ;
    //fileSource = "C:\\Users\\Manolo\\Desktop\\Sockets" ;

    File source = new File(fileSource);
    System.out.println("Absolute path:" + source.getAbsolutePath());

    try (Socket socket = new Socket("localhost", 9000)) {
      input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
      output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      custompacket uploadSYN = new custompacket("UPLOAD_SYN", "clien", "");
      uploadSYN.send(output);
      custompacket response = new custompacket(input);
      System.out.println(response.toString());
      if (response.PacketMethod.equals(method.UPLOAD_ACK.getMethod())) {
        System.out.println("UPLOAD ALLOWED");
        //Thread.sleep(100);
        sendFiles(new File(fileSource), output);
        System.out.println("Files uploaded, sending upload end");
        new custompacket(method.UPLOAD_END, "client", "").send(output);
        new custompacket(input);

      } else if (response.PacketMethod.equals(method.UNKNOWN_METHOD.getMethod())) {
        System.out.println("UNKNOWN METHOD: Update client");
      } else if (response.PacketMethod.equals(method.UPLOAD_CANCEL.getMethod())) {
        System.out.println("UPLOAD CANCELED: ABORTING UPLOAD");
      }

    } catch (Exception e) {
    }

  }

  private void sendFiles(File rootFile, BufferedWriter out) throws IOException {
    System.out.println("uploading file: " + rootFile.getName());
    // Si es un directorio, llamada recursiva
    if (rootFile.isDirectory()) {
      for (File file : rootFile.listFiles()) {
        sendFiles(file, out);
      }

    } else {
      Scanner reader = new Scanner(rootFile);
      String data = "";
      // leer archivo
      while (reader.hasNextLine()) {
        data += reader.nextLine();
      }

      String Filepath = rootFile.getAbsolutePath();
      
      String EncodedPath = Base64.getEncoder().encodeToString(Filepath.getBytes());

      data = Base64.getEncoder().encodeToString(data.getBytes());
      custompacket sended = new custompacket(method.UPLOAD_FILE.getMethod(), "client", EncodedPath, data);

      sended.send(out);
      new custompacket(input);
    }
  }

  

  public void test() {
    try (Socket socket = new Socket("localhost", 9000)) {
      BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
      BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      System.out.println("TEST REQUEST----------------------------------------------------------------");
      System.out.println("client test request:");
      custompacket request = new custompacket(method.TEST.getMethod(), "clien", "Esto es un test");
      System.out.println(request.toString());
      request.send(output);

      custompacket response = new custompacket(input);
      System.out.println(response.toString());
      System.out.println("END TEST REQUEST----------------------------------------------------------------");

      System.out.println(fileSource);

      socket.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
