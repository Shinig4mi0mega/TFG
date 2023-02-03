import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.invoke.MethodHandle;
import java.net.Socket;

import javax.xml.transform.sax.TemplatesHandler;

public class ServiceThread implements Runnable{
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
           
        }catch (Exception e) {e.printStackTrace();}
    }


    private custompacket methodHandler(custompacket packet) {
        if(packet.method.equals("TEST")){
            return TestHandler(packet);
        }

        return null;
    }


    private custompacket TestHandler(custompacket packet) {
        System.out.println("Server response to test:");
        return new custompacket("TEST_RESPONSE",packet.data);
    }
    
}


