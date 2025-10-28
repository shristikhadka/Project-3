public class PrintingTask implements Runnable {
    private String message;
    //constructor
    public PrintingTask (String message){
        this.message = message;
    }
    public void run(){
        System.out.println(message);
    }
}
