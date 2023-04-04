import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.Scanner;

public class client {
  private String fileSource;
  BufferedReader input;
  BufferedWriter output;
  String user = "user";

  public void start() {
    fileSource = System.getProperty("user.dir") + "\\PRUEBAS" + "\\SOURCE";

    File source = new File(fileSource);
    System.out.println("Absolute path:" + source.getAbsolutePath());

    try (Socket socket = new Socket("localhost", 9001)) {
      input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
      output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      custompacket uploadSYN = new custompacket("UPLOAD_SYN", user, "");
      uploadSYN.send(output);
      custompacket response = new custompacket(input);
      System.out.println(response.toString());

      if (response.PacketMethod.equals(method.UPLOAD_ACK.getMethod())) {
        System.out.println("UPLOAD ALLOWED");
        Thread.sleep(100);

        sendFiles(new File(fileSource), output);

        System.out.println("Files uploaded, sending upload end");
        new custompacket(method.UPLOAD_END, user, "").send(output);

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

      // leemos la data del archivo
      byte[] data = readFile(rootFile);
      //System.out.println("data = " + data);

      // Encode path
      String Filepath = rootFile.getAbsolutePath();
      Filepath = simplifyRoute(fileSource, Filepath);
      System.out.println("simplified route = " + Filepath);
      String EncodedPath = Base64.getEncoder().encodeToString(Filepath.getBytes());

      // encode data
      String Encodeddata = Base64.getEncoder().encodeToString(data);
      custompacket sended = new custompacket(method.UPLOAD_FILE.getMethod(), user, EncodedPath, Encodeddata);

      sended.send(out);
      // TODO:Tratar este upload ack
      new custompacket(input);
    }
  }

  byte[] readFile(File rootFile) {

    if (rootFile.getName().contains(".png")||rootFile.getName().contains(".pdf")) {
      System.out.println("tratando imagen");
      return readimg(rootFile);
    }

    Scanner reader;
    StringBuilder data = new StringBuilder();
    try {
      reader = new Scanner(rootFile, "UTF-8");

      // leer archivo
      while (reader.hasNextLine()) {
        String line = reader.nextLine();
        System.out.println(line);
        data.append(line);

      }
      reader.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return data.toString().getBytes();
  }

  private byte[] readimg(File rootFile) {
    try (FileInputStream stream = new FileInputStream(rootFile)) {
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
