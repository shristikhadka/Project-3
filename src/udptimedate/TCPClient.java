package udptimedate;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) throws Exception{
        if(args.length != 2) {
            System.out.println("Need <serverIP> and <serverPort");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        Scanner keyboard = new Scanner(System.in);
        char command;
        do {
            System.out.println("Enter a command ( L, -, R, D, U, Q)");
            String input = keyboard.nextLine();
            command = input.toUpperCase().charAt(0);
            switch (command){
                case 'L':




                    break;

                case '-': //delete?





                    break;

                case 'R':





                    break;

                case 'D': //download?

                    break;
                case 'U':

                    break;

                case 'Q':
                    break;

                default:
                    System.out.println("Invalid command, please try again!");
            }

        }while (command != 'Q');


    };
}
