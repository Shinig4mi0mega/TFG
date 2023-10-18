import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

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

            //System.out.println(response.toString());

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
        } else if (packet.PacketMethod.equals(method.LAST_UPLOADS_SYN.getMethod())) {
            return lastUploadsHandler();
        } else if (packet.PacketMethod.equals(method.FIND_SERVER.getMethod())) {
            return FindServerHandler();
        } else {
            return new custompacket(method.UNKNOWN_METHOD, "Server", "");
        }
    }

    private custompacket FindServerHandler() {

        System.out.println("PING RECIVED");
        return new custompacket(method.FIND_SERVER_ACK,"server","");
        
    }

    private custompacket lastUploadsHandler() {
        //System.out.println(lastUploads());
        System.out.println("Enviando ultimos archivos subidos por cada usuario");
        return new custompacket(method.LAST_UPLOADS_ACK, "server", lastUploads());
    }

    private custompacket SynHandler(custompacket packet) {
        if (true) {

            try {
                output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                //e.printStackTrace();
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

        custompacket packetFile = null;
        try {
            packetFile = new custompacket(input);
        } catch (Exception e) {
            System.out.println("failed to read packet");
        }
       

        int i = 0;
        while (!packetFile.PacketMethod.equals(method.UPLOAD_END.getMethod())) {
            try {
                //Thread.currentThread().sleep(100);
                if (packetFile.PacketMethod == null || !packetFile.PacketMethod.equals(method.UPLOAD_FILE.getMethod())){
                    System.out.println("Null packet found");
                    continue;
                }

                savefile(packetFile);
                new custompacket(method.UPLOAD_FILE_ACK.getMethod(), "Server", "").send(output);

                
            } catch (Exception e) {
                //System.out.println(e.getMessage());
                System.out.println(packetFile.file + " failed to send");
                i++;
                if(i == 20){
                    System.out.println("Too many errors reciving, closing thread");
                    break;
                }
                    
                continue;
            }
            try {
                packetFile = new custompacket(input);
            } catch (Exception e) {
                System.out.println("failed to read packet");
                i++;
                if(i == 50){
                    System.out.println("Too many errors, closing thread");
                    break;
                }
            }
        }

        System.out.println("All files recived");
        return new custompacket(method.UPLOAD_END_ACK.getMethod(), "Server", "");
    }

    private void savefile(custompacket packetFile) {
        byte[] decodedBytesPath = Base64.getDecoder().decode(packetFile.file);
        String decodedPath = new String(decodedBytesPath);
        decodedPath = decodedPath.replace(":", "");
        decodedPath = decodedPath.replace("\\", separador);
        decodedPath = decodedPath.replace("/", separador);

        System.out.println( packetFile.user  + " subio: " + decodedPath );

        // ruta al archivo
        String localRoute = fileSystemRootFile + separador + packetFile.user + decodedPath;

        File currentFile = new File(localRoute);
        File parentDir = currentFile.getParentFile();
        // Si no existen los directorios padre, se crean
        if (!parentDir.exists())
            parentDir.mkdirs();

        // creamos y escribimos en el archivo la data
        try {
            currentFile.createNewFile();
            byte[] decodedBytesData = Base64.getDecoder().decode(packetFile.data.getBytes());

            FileOutputStream stream = new FileOutputStream(localRoute);
            stream.write(decodedBytesData);
            stream.close();
        } catch (IOException e) {}

    }

    private custompacket TestHandler(custompacket packet) {
        System.out.println("Test packet recive from: " + packet.user);
        return new custompacket(method.TEST_RESPONSE.getMethod(), "Server", packet.data);
 
    }

        private String lastUploads() {

            File savingFolder = new File(fileSystemRootFile);
            StringBuilder toret = new StringBuilder();

            for (File f : savingFolder.listFiles()) {

                Date date = new Date();
                date.setTime(f.lastModified());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = formatter.format(date);

                toret.append(f.getName()).append("=").append(formattedDate).append(";");
            }

            return toret.toString();
        }
}
