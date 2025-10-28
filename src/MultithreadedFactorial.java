import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultithreadedFactorial {
    public static void main(String[] args) {
        ExecutorService es = Executors.newFixedThreadPool(4);
        //goal is to calculate 20
        List<Future<Long>> results = new LinkedList<>();
        results.add(es.submit(new CIM(1, 5)));
        results.add(es.submit(new CIM(6, 10)));
        results.add(es.submit(new CIM(11, 15)));
        results.add(es.submit(new CIM(16, 20)));
        long product = 1;
        for (Future<Long> f : results){
            try{
                product = product * f.get();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        es.shutdown();
        System.out.println(product);
        //hard code
        //    es.submit(new CIM(1,5));
        //    es.submit(new CIM(6, 10));
        //    es.submit(new CIM(11, 15));
        //    es.submit(new CIM(16, 20));

    }
}
