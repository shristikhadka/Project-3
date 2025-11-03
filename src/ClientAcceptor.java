import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

public class ClientAcceptor implements Runnable
{
    private ServerSocketChannel listenChannel;
    private ExecutorService es;

    public ClientAcceptor(ServerSocketChannel listenChannel, ExecutorService es) {
        this.listenChannel = listenChannel;
        this.es = es;
    }

    @Override
    public void run() {
        while(true){
            try{
                SocketChannel serveChannel=listenChannel.accept();
                es.submit(new ClientHandler(serveChannel));
            }catch(AsynchronousCloseException e){
                //expected due to listenchannel cose'this means the server has been shut down so get out of the loop to complete
                break;
            } catch (Exception e) {
                //any other exceprion are unexpected
                //show the exception details
                e.printStackTrace();
            }
        }
    }
}
