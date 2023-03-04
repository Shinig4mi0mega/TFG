import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandle;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.ws.Response;

public class ServiceThread implements Runnable {
    private Socket socket;

    public ServiceThread(Socket socket) {
        this.socket = socket;
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
        } else if (packet.PacketMethod.equals(method.UPLOAD_FILE.getMethod())) {
            return fileUploadHandler(packet);
        } else {
            return new custompacket(method.UNKNOWN_METHOD, "");
        }
    }

    // TODO:Validate via a token in custom pakage that corresponds to the password
    // hash

    private custompacket fileUploadHandler(custompacket packet) {

        if (packet.file == null || packet.user == null) {
            custompacket response = new custompacket(method.FILE_SAVE_FAILED, "");
            return response;
        }

        File userFolder = new File(System.getProperty("user.dir") + "\\UserFiles" + "\\" + packet.user);
        String filePath = System.getProperty("user.dir") + "\\UserFiles" + "\\" + packet.user + "\\" + packet.file;
        File uploadedFile = new File(filePath);
        // If user folder doesnt exist, is created
        if (!userFolder.exists()) {
            userFolder.mkdir();
        }
        try {
            uploadedFile.createNewFile();
            FileWriter Writer = new FileWriter(uploadedFile);
            byte[] decoded = Base64.getDecoder().decode(packet.data);
            String decodedStr = new String(decoded, StandardCharsets.UTF_8);
            Writer.write(decodedStr);
            Writer.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        custompacket response = new custompacket(method.FILE_SAVED.getMethod(), packet.user, packet.file, null);
        return response;
    }

    private custompacket SynHandler(custompacket packet) {
        if (true) {
            return new custompacket(method.UPLOAD_ACK.getMethod(), "");
        } else {
            return new custompacket(method.UPLOAD_CANCEL.getMethod(), "");
        }
    }

    private custompacket TestHandler(custompacket packet) {
        System.out.println("Server response to test:");
        return new custompacket(method.TEST_RESPONSE.getMethod(), packet.data);

    }

}
