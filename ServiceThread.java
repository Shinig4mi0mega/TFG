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

            socket.close();
           
        }catch (Exception e) {e.printStackTrace();}
    }


    private custompacket methodHandler(custompacket packet) {
        if(packet.method.equals(method.TEST.getMethod())){
            return TestHandler(packet);
        }else if(packet.method.equals(method.UPLOAD_SYN.getMethod())){
            return SynHandler(packet);
        }else{
            return new custompacket(method.UNKNOWN_METHOD,"");
        }
    }


    //TODO:Validate via a token in custom pakage that corresponds to the password hash

    private custompacket SynHandler(custompacket packet) {
        if(true){
            return new custompacket(method.UPLOAD_ACK.getMethod(),"");
        }else{
            return new custompacket(method.UPLOAD_CANCEL.getMethod(),"");
        }
    }


    private custompacket TestHandler(custompacket packet) {
        System.out.println("Server response to test:");
        return new custompacket(method.TEST_RESPONSE.getMethod(),packet.data);
        
    }
    
}


