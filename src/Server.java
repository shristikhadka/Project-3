import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }


        int port = Integer.parseInt(args[0]);

        // Create ServerFiles directory if it doesn't exist
        File serverDir = new File("ServerFiles");
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }

        // server socket channel/ bind to port
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("Server started on port " + port);
        ExecutorService es = Executors.newFixedThreadPool(4);

        //submit AcceptClients (es.listenChannel);
        //wait for Q for shutdown from keyboard (scanner)
        //shutdown(es);
        //close listenchannel
        Scanner keyboard = new Scanner(System.in);
        if (keyboard.equals("Q")) {
            es.shutdown();
            keyboard.close();

        }
        es.shutdown();
    }
}