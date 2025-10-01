import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.File;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);

        // server socket channel/ bind to port
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));

        while (true) {
            try (SocketChannel clientChannel = serverSocket.accept()) {
                ByteBuffer buffer = ByteBuffer.allocate(4096);
                int bytesRead = clientChannel.read(buffer);
                if (bytesRead == -1) continue;

                buffer.flip();
                byte[] data = new byte[bytesRead];
                buffer.get(data);
                String input = new String(data);

                // split command
                String[] parts = input.split(" ", 2);
                String cmd = parts[0].toUpperCase();

                System.out.println("Client command: " + input);

                switch (cmd) {
                    case "LIST" -> {
                        File dir = new File(".");
                        String[] files = dir.list();
                        String reply = (files != null) ? String.join("\n", files) : "No files found.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                    }

                    case "DELETE" -> {
                        if (parts.length < 2) break;
                        File file = new File(parts[1]);
                        String reply = (file.exists() && file.delete()) //Looked up this part
                                ? "File deleted."
                                : "File not found.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                    }

                    case "RENAME" -> {
                        if (parts.length < 2) break;
                        String[] argsSplit = parts[1].split(" ");
                        String reply = "Invalid arguments.";
                        if (argsSplit.length == 2) {
                            File oldFile = new File(argsSplit[0]);
                            File newFile = new File(argsSplit[1]);
                            if (oldFile.exists() && oldFile.renameTo(newFile)) {
                                reply = "File renamed.";
                            } else {
                                reply = "Rename failed.";
                            }
                        }
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                    }

                    case "UPLOAD" -> {

                    }

                    case "DOWNLOAD" -> {

                    }

                    case "QUIT" -> {
                        System.out.println("Server quit.");
                    }

                    default -> {
                        String reply = "Invalid command.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                    }
                }
            }
        }
    }
}
