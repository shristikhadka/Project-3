import java.nio.channels.SocketChannel;
import java.util.Scanner;
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

                //submit AcceptClients (es.listenChannel);
                //wait for Q for shutdown from keyboard (scanner)
                //shutdown(es);
                //close listenchannel

                Scanner keyboard = new Scanner(System.in);
                System.out.println("Q for Quit?");
                if (keyboard.equals("Q")){
                    es.shutdown();
                    listenChannel.close();
                }


            } catch (Exception e) {
                System.out.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            }

        }

    }
}
