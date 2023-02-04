import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/*
 * Method can have multiple values that can be:
 * TEST:The server will answer with the data of the packet send
 * TEST_RESPONSE:Server response to TEST method client package
 * 
 * UPLOAD_SYN: Informs the server that the client wants to upload files
 * UPLOAD_ACK: Confirms the upload and that the server is ready to recive the files
 * UPLOAD_END:Informs the server there arent more files to send
 * UPLOAD_CANCEL:Server negates the upload to the client
 */

 //TODO:param for login user
public class custompacket {
    String method;
    String data;

    public custompacket(String method, String data) {
        this.method = method;
        this.data = data;
    }

    public custompacket(method method, String data) {
        this.method = method.getMethod();
        this.data = data;
    }

    public custompacket(BufferedReader reader) {
        try {
            method = reader.readLine();
            data = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        StringBuilder toret = new StringBuilder();
        toret.append(method).append("\n");
        toret.append(data).append("\n");

        return toret.toString();
    }

    public void send(BufferedWriter writer) {
        try {
            writer.write(method + "\n");
            writer.write(data + "\n");
            writer.flush();
        } catch (Exception e) {e.printStackTrace();}

    }

}
