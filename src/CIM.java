import java.util.concurrent.Callable;

public class CIM implements Callable<Long> {
    private long m;
    private long n;
    //constructer
    public CIM (long lower, long upper){
        m = lower;
        n = upper;
    }
    public Long call(){
        //any code in this call method will be executed by a separate thread
        long product = 1;
        for(long i=m; i<= n; i++){
            product = product * i;
        }
        return product;
    }
}
