import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    
    static int port;
    static String fileSystemRootFile;
    private Thread serverThread;
    private int nthreads;
    private HashMap<String, String> users;
    private HashMap<String, String> configMap;
    private String os;

    public void start() {

        configMap = new HashMap<>();
        if (!loadConfig()) {
            port = 9000;
            nthreads = 100;
            fileSystemRootFile = System.getProperty("user.dir") + "\\DondeGuardoLosArchivosDeFormaTemporal";
        }else{
             port = Integer.parseInt(configMap.get("port")); 
             nthreads = Integer.parseInt(configMap.get("threads"));
             fileSystemRootFile = configMap.get("saveRoute");
        }

        
        File savingFile = new File(fileSystemRootFile);
        if (!savingFile.exists())
            savingFile.mkdirs();
        
            printBanner();
        
        System.out.println("Config loaded");
        System.out.println("Detected os: " + os);
        System.out.println("saving files in: " + configMap.get("saveRoute"));
        System.out.println("threadpool of : " + configMap.get("threads") + " threads");
        System.out.println("Listening on port: " + configMap.get("port"));
        System.out.println("----------------------------------------------");

        this.serverThread = new Thread() {
            @Override
            public void run() {
                try (final ServerSocket serverSocket = new ServerSocket(port)) {
                    ExecutorService threadPool = Executors.newFixedThreadPool(nthreads);
                    while (true) {

                        Socket socket = serverSocket.accept();
                        ServiceThread st = new ServiceThread(socket, fileSystemRootFile,os);
                        threadPool.execute(st);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        this.serverThread.start();

    }

    /*
     * setable configuration:
     * port
     * threads
     * saving pwd(MUST BE ABSOLUTE PATH)
     * 
     */

    private void printBanner() {
        System.out.println("==============================================================================================================");
        System.out.println(" ####    ##    ####  #    #  ####      ####  #   # #    #  ####      ####  ###### #####  #    # ###### #####  ");
        System.out.println("#       #  #  #    # #    # #    #    #       # #  ##   # #    #    #      #      #    # #    # #      #    # ");
        System.out.println("#      #    # #      ###### #    #     ####    #   # #  # #          ####  #####  #    # #    # #####  #    # ");
        System.out.println("#      ###### #      #    # #    #         #   #   #  # # #              # #      #####  #    # #      #####  ");
        System.out.println("#      #    # #    # #    # #    #    #    #   #   #   ## #    #    #    # #      #   #   #  #  #      #   #  ");
        System.out.println(" ####  #    #  ####  #    #  ####      ####    #   #    #  ####      ####  ###### #    #   ##   ###### #    # ");
        System.out.println("==============================================================================================================");
    }

    private boolean loadConfig() {
        File file = new File("config");
        if (!file.exists()) {
            System.out.println("Config file doesnt exist, please restore it");
            System.out.println("Using default config");
        }

        try (BufferedReader br = new BufferedReader(new FileReader("config"))) {
            String line;
            String lineArray[];

            while ((line = br.readLine()) != null) {
                if (!line.contains("="))
                    continue;

                lineArray = line.split("=");
                configMap.put(lineArray[0], lineArray[1]);
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }

        os = System.getProperty("os.name").toLowerCase();

        return true;
    }

}
