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

    public ServiceThread(Socket socket, String fileSystemRootFile) {
        this.socket = socket;
        this.fileSystemRootFile = fileSystemRootFile;
    }

    @Override
    public void run() {
        try (Socket socket = this.socket) {

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            BufferedWriter output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

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
            return TestHandler(packet);
        } else if (packet.PacketMethod.equals(method.UPLOAD_SYN.getMethod())) {
            return SynHandler(packet);
        } else {
            return new custompacket(method.UNKNOWN_METHOD, "");
        }
    }

    private custompacket SynHandler(custompacket packet) {
        if (true) {
            new custompacket(method.UPLOAD_ACK.getMethod(), "").send(output);
            // Este metodo va a devolver un paquete de ACK de que se acabarón los archivos a subir

            //return savefiles();
            return new custompacket(method.UPLOAD_END_ACK,"");
        } else {
            return new custompacket(method.UPLOAD_CANCEL.getMethod(), "");
        }
    }

    private custompacket savefiles() {
        custompacket packetFile = new custompacket(input);
        while (packetFile.PacketMethod != method.UPLOAD_END.getMethod()) {
            if (packetFile.PacketMethod != method.UPLOAD_FILE.getMethod())
                continue;

            savefile(packetFile);

        }

        return new custompacket(method.UPLOAD_END_ACK.getMethod(), "");
    }

    private void savefile(custompacket packetFile) {
        packetFile.file.replace(":", "");
        // ruta al archivo
        String localRoute = fileSystemRootFile + "\\" + packetFile.user + "\\" + packetFile.file;

        File currentFile = new File(localRoute);
        File parentDir = currentFile.getParentFile();
        // Si no existen los directorios padre, se crean
        if (!parentDir.exists())
            parentDir.mkdirs();

        // creamos y escribimos en el archivola data
        try {
            currentFile.createNewFile();
            String decodedData = new String(Base64.getDecoder().decode(packetFile.data.getBytes()));

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
        return new custompacket(method.TEST_RESPONSE.getMethod(), packet.data);

    }

}
