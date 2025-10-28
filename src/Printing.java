import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Printing {

    public static void main(String[] args){
        //create the executor (i.e., a thread pool)
        ExecutorService es =
                Executors.newFixedThreadPool(4);
        //submit tasks to the executor
        es.submit(new PrintingTask("Hello"));
        es.submit(new PrintingTask("World"));
        //shutdown the executor
        es.shutdown();
    }

}
