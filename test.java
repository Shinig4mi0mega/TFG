import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class test {

    public static void main(String[] args) {

        copiarContenido("C:\\Users\\Usuario\\Documents\\GitHub\\TFG\\PRUEBAS\\SOURCE\\image.png");

        
    }

    public static void copiarContenido(String origen) {
        try{
        BufferedReader lector = new BufferedReader(new FileReader(origen));
        
        String linea;
    
        while ((linea = lector.readLine()) != null) {
            System.out.println(linea);
        }
    
        lector.close();
    }catch(Exception e){
        System.out.println(e.getMessage());
    }
    }
}