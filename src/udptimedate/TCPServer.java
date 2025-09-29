package udptimedate;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;



public class TCPServer {
    public static void main(String[] args) throws Exception{
        int port = 3000;

        ServerSocketChannel listenSocket = ServerSocketChannel.open();

        listenSocket.bind(new InetSocketAddress(3000)); //binding the socket to the port


        while(true){
            //accepts the client connection request and establish a dedicated channel with new client
            //dedicated channel is represented by serveChannel
            SocketChannel serverChannel = listenSocket.accept();;
            ByteBuffer commandBuffer = ByteBuffer.allocate(2);
            int bytesRead = serverChannel.read(commandBuffer);
            commandBuffer.flip();
            char command = commandBuffer.getChar();
            switch(command){
                case 'L':

                    
                    break;

                case '-': //delete?


                    break;
                case 'R':


                    break;

                case 'D': //download

                    break;

                case 'U':
                   // ByteBuffer buffer = ByteBuffer.allocate(4);
                   // ByteBuffer byteBuffer = buffer.putInt(fileProject3.size());
                   // buffer.flip()

                    break;
                default:
                    System.out.println("Invalid Command Received.");

            }
        }
    }
}

