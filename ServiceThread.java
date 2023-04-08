import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Base64;

public class ServiceThread implements Runnable {
    private Socket socket;
    BufferedReader input;
    BufferedWriter output;
    private String fileSystemRootFile;
    private String separador;
    private String os;

    public ServiceThread(Socket socket, String fileSystemRootFile, String os) {
        this.socket = socket;
        this.fileSystemRootFile = fileSystemRootFile;
        this.os = os;
    }

    @Override
    public void run() {

        if (os.contains("win")) {
            // El programa se está ejecutando en Windows
            separador = "\\";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            // El programa se está ejecutando en Linux o Unix
            separador = "/";
        }
        fileSystemRootFile = fileSystemRootFile.replace("\\", separador);

        try (Socket socket = this.socket) {

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            custompacket packet = new custompacket(input);
            custompacket response = null;

            response = methodHandler(packet);

            response.send(output);

            socket.close();

        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println("conexión cerrada");
        }
    }

    private custompacket methodHandler(custompacket packet) {
        if (packet.PacketMethod.equals(method.TEST.getMethod())) {

            return TestHandler(packet);
        } else if (packet.PacketMethod.equals(method.UPLOAD_SYN.getMethod())) {

            return SynHandler(packet);
        } else {
            return new custompacket(method.UNKNOWN_METHOD, "Server", "");
        }
    }

    private custompacket SynHandler(custompacket packet) {
        if (true) {

            try {
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            new custompacket(method.UPLOAD_ACK.getMethod(), "Server", "").send(output);

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
            // e.printStackTrace();
            System.out.println("fallo con un input que se puso a null");
        }

        custompacket packetFile = new custompacket(input);

        while (!packetFile.PacketMethod.equals(method.UPLOAD_END.getMethod())) {
            if (!packetFile.PacketMethod.equals(method.UPLOAD_FILE.getMethod()))
                continue;

            savefile(packetFile);
            new custompacket(method.UPLOAD_ACK.getMethod(), "Server", "").send(output);

            packetFile = new custompacket(input);
        }

        return new custompacket(method.UPLOAD_END_ACK.getMethod(), "Server", "");
    }

    private void savefile(custompacket packetFile) {
        byte[] decodedBytesPath = Base64.getDecoder().decode(packetFile.file);
        String decodedPath = new String(decodedBytesPath);
        decodedPath = decodedPath.replace(":", "");
        decodedPath = decodedPath.replace("\\", separador);
        decodedPath = decodedPath.replace("/", separador);

        System.out.println("guardando archivo: " + decodedPath);

        // ruta al archivo
        String localRoute = fileSystemRootFile + separador + packetFile.user + decodedPath;

        File currentFile = new File(localRoute);
        File parentDir = currentFile.getParentFile();
        // Si no existen los directorios padre, se crean
        if (!parentDir.exists())
            parentDir.mkdirs();

        // creamos y escribimos en el archivola data
        try {
            currentFile.createNewFile();
            byte[] decodedBytesData = Base64.getDecoder().decode(packetFile.data.getBytes());

            FileOutputStream stream = new FileOutputStream(localRoute);
            stream.write(decodedBytesData);
            stream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private custompacket TestHandler(custompacket packet) {
        System.out.println("Test packet recive from: " + packet.user);
        return new custompacket(method.TEST_RESPONSE.getMethod(), "Server", packet.data);

    }

}
