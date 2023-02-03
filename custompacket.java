import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class custompacket {
    String method;
    String data;

    public custompacket(String method, String data) {
        this.method = method;
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
