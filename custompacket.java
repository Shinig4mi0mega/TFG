import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/*
 * Method:One of the many methods of the method class
 * User:Identifies who is sending the packet and is the ame of the folder where the file will be saved
 * file:Identifies the name of the file and the absolute route from the user folder
 * data:Contains the data of the file
 */

 //TODO:param for login user
public class custompacket {
    String PacketMethod;
    String user;
    String file;
    String data;

    public custompacket(String method, String data) {
        this.PacketMethod = method;
        this.data = data;
    }

    public custompacket(method method, String data) {
        this.PacketMethod = method.getMethod();
        this.data = data;
    }

    

    public custompacket(String method, String user, String file, String data) {
        this.PacketMethod = method;
        this.user = user;
        this.file = file;
        this.data = data;
    }

    public custompacket(BufferedReader reader) {
        try {
            PacketMethod = reader.readLine();
            user = reader.readLine();
            if(this.PacketMethod == method.UPLOAD_FILE.getMethod()){
                file = reader.readLine();
                data = reader.readLine();
            }
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        StringBuilder toret = new StringBuilder();
        toret.append(PacketMethod).append("\n");
        toret.append(user).append("\n");
        if(this.PacketMethod == method.UPLOAD_FILE.getMethod()){
            toret.append(file).append("\n");
            toret.append(data).append("\n");
        }

        return toret.toString();
    }

    public void send(BufferedWriter writer) {
        try {
            writer.write(PacketMethod + "\n");
            writer.write(user + "\n");
            if(this.PacketMethod == method.UPLOAD_FILE.getMethod()){
                writer.write(file + "\n");
                writer.write(data + "\n");
            }
            writer.flush();
        } catch (Exception e) {e.printStackTrace();}

    }

}
