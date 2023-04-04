import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandle;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import javax.xml.transform.sax.TemplatesHandler;

public class ServiceThread implements Runnable {
    private Socket socket;
    BufferedReader input;
    BufferedWriter output;
    private String fileSystemRootFile;
    private String separador;

    public ServiceThread(Socket socket, String fileSystemRootFile) {
        this.socket = socket;
        this.fileSystemRootFile = fileSystemRootFile;
    }

    @Override
    public void run() {
        String sistemaOperativo = System.getProperty("os.name").toLowerCase();
        if (sistemaOperativo.contains("win")) {
            // El programa se está ejecutando en Windows
            separador = "\\" ;
        }else if (sistemaOperativo.contains("nix") || sistemaOperativo.contains("nux") || sistemaOperativo.contains("aix")) {
            // El programa se está ejecutando en Linux o Unix
            separador = "/" ;
        }
        fileSystemRootFile = fileSystemRootFile.replace("\\", separador);
        System.out.println("fileSystemRootFile = " + fileSystemRootFile);




        try (Socket socket = this.socket) {

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            System.out.println("reading packet");
            custompacket packet = new custompacket(input);
            custompacket response = null;

            response = methodHandler(packet);
            System.out.println(response);
            response.send(output);

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private custompacket methodHandler(custompacket packet) {
        if (packet.PacketMethod.equals(method.TEST.getMethod())) {
            System.out.println("method:TEST");
            return TestHandler(packet);
        } else if (packet.PacketMethod.equals(method.UPLOAD_SYN.getMethod())) {
            System.out.println("method:SYN UPLOAD");
            return SynHandler(packet);
        } else {
            return new custompacket(method.UNKNOWN_METHOD, "Server", "");
        }
    }

    private custompacket SynHandler(custompacket packet) {
        if (true) {
            System.out.println("Valid packet: sending ACK to begin upload");
            try {
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            new custompacket(method.UPLOAD_ACK.getMethod(), "Server", "").send(output);
            System.out.println("llamando save files");
            return savefiles();
            // return new custompacket(method.UPLOAD_END_ACK, "Server", "");
        } else {
            return new custompacket(method.UPLOAD_CANCEL.getMethod(), "Server", "");
        }
    }

    private custompacket savefiles() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Ready to save files");
        custompacket packetFile = new custompacket(input);

        while (!packetFile.PacketMethod.equals(method.UPLOAD_END.getMethod()) ) {
            if (!packetFile.PacketMethod.equals(method.UPLOAD_FILE.getMethod()))
                continue;

            // packetFile.toString();
           
            savefile(packetFile);
            new custompacket(method.UPLOAD_ACK.getMethod(), "Server", "").send(output);
            System.out.println(packetFile.PacketMethod);
            packetFile = new custompacket(input);
        }

        System.out.println("No more files, sending upload end ack");
        return new custompacket(method.UPLOAD_END_ACK.getMethod(), "Server", "");
    }

    private void savefile(custompacket packetFile) {
        byte[] decodedBytesPath = Base64.getDecoder().decode(packetFile.file);
        String decodedPath = new String(decodedBytesPath);
        decodedPath = decodedPath.replace(":", "");
        decodedPath = decodedPath.replace("\\", separador);



        // ruta al archivo
        String localRoute = fileSystemRootFile + separador + packetFile.user + decodedPath;

        System.out.println("decodedPath: " + decodedPath);
        System.out.println("saving on user: " + packetFile.user);
        System.out.println("Saving in: " + localRoute);

        File currentFile = new File(localRoute);
        File parentDir = currentFile.getParentFile();
        // Si no existen los directorios padre, se crean
        if (!parentDir.exists())
            parentDir.mkdirs();

        if(!currentFile.exists())
            try {
                currentFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        // creamos y escribimos en el archivola data
        try {
            currentFile.createNewFile();
            byte[] decodedBytesData = Base64.getDecoder().decode(packetFile.data.getBytes());
            String decodedData = new String(decodedBytesData);
            System.out.println(decodedData);

            FileWriter myWriter = new FileWriter(localRoute);
            myWriter.write(decodedData);
            myWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private custompacket TestHandler(custompacket packet) {
        System.out.println("Server response to test:");
        return new custompacket(method.TEST_RESPONSE.getMethod(), "Server", packet.data);

    }

}
