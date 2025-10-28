import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcceptClients implements Runnable{
    private SocketChannel listenChannel;
    //constructor
    public AcceptClients (SocketChannel clientChannel){
        this.listenChannel = clientChannel;



    }
    public void run(){
        ExecutorService es = Executors.newFixedThreadPool(4);
        es.submit((Runnable) new AcceptClients(listenChannel));


        while (true){
            try (SocketChannel clientChannel = listenChannel) {

            } catch (Exception e) {
                System.out.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            }

        }

    }
}
