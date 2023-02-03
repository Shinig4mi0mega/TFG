import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    static String host;
    static int port;
    static File target;
    private Thread serverThread;
    private int nthreads;

    public void start() {

        //defaultSetUp
        //if(args.length == 0){
            host = "localhost";
            port = 9000;
            target = new File("target");
            nthreads = 100;
        //}

        this.serverThread = new Thread() {
			@Override
			public void run() {
				try (final ServerSocket serverSocket = new ServerSocket(port)) {
					ExecutorService threadPool = Executors.newFixedThreadPool(nthreads);
					while (true) {

						Socket socket = serverSocket.accept();
						//if (stop)
							//break;
						ServiceThread st = new ServiceThread(socket);
						threadPool.execute(st);

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		//this.stop = false;
		this.serverThread.start();


        try {
            new server().saveFiles(host, port, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFiles(String host, int port, File target) throws IOException {
        try (Socket socket = new Socket(host, port);
                InputStream in = socket.getInputStream();
                FileOutputStream out = new FileOutputStream(target)) {
            System.out.println("Connected to " + host + " on port " + port);

            copyDirectory(in, target);
        }
    }

    private void copyDirectory(InputStream in, File target) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        FileOutputStream out = new FileOutputStream(target);
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.close();
    }
}
