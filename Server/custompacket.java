import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/*
 *Se espera que en el paquet vengan todos los campos de forma campo:valor\n 
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

    public custompacket(String method, String user, String data) {
        this.PacketMethod = method;
        this.user = user;
        this.file = "emty";
        this.data = data;

    }

    public custompacket(method method, String user, String data) {
        this.PacketMethod = method.getMethod();
        this.user = user;
        this.data = data;
        this.file = "emty";
    }

    public custompacket(String method, String user, String file, String data) {
        this.PacketMethod = method;
        this.user = user;
        this.file = file;
        this.data = data;
    }

    public custompacket(BufferedReader reader) throws Exception {

            PacketMethod = reader.readLine().split(":")[1];
            user = reader.readLine().split(":")[1];

            String[] fileArray = reader.readLine().split(":");
            if (fileArray.length > 1)
                file = fileArray[1];
            else
                file = "emty";

            String[] dataArray = reader.readLine().split(":");
            if (dataArray.length > 1)
                data = dataArray[1];
            else
                data = "emty";

            // System.out.println(this.toString());

        

    }

    @Override
    public String toString() {
        StringBuilder toret = new StringBuilder();
        toret.append(PacketMethod).append("\n");
        toret.append(user).append("\n");
        toret.append(file).append("\n");
        toret.append(data).append("\n");

        return toret.toString();
    }

    public void send(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.write("method:" + PacketMethod + "\n");
                writer.write("user:" + user + "\n");
                writer.write("file:" + file + "\n");
                writer.write("data:" + data + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
