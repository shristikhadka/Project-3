import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Scanner;

public class Client{
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java Client <serverIP> <port>");
            return;
        }

        String serverIP = args[0];
        int port = Integer.parseInt(args[1]);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Connected to server .");

        while (true) {
            System.out.print("Enter command: ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ", 2);
            String cmd = parts[0].toUpperCase();

            // connect per command
            try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(serverIP, port))) {
                // --- send command ---
                ByteBuffer commandBuffer = ByteBuffer.wrap(input.getBytes());
                channel.write(commandBuffer);
                channel.shutdownOutput();

                // --- receive response or file ---
                ByteBuffer replyBuffer = ByteBuffer.allocate(4096);

                switch (cmd) {
                    case "LIST", "DELETE", "RENAME" -> {
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] data = new byte[bytesRead];
                        replyBuffer.get(data);
                        System.out.println("Server: " + new String(data));
                    }

                    case "UPLOAD" -> {
                        if (parts.length < 2) continue;
                        File file = new File(parts[1]);
                        if (!file.exists()) {
                            System.out.println("Local file not found.");
                            continue;
                        }
                        byte[] fileBytes = new byte[(int) file.length()];
                        try (FileInputStream fis = new FileInputStream(file)) {
                            fis.read(fileBytes);
                        }
                        ByteBuffer fileBuffer = ByteBuffer.wrap(fileBytes);
                        channel.write(fileBuffer);
                        // read back
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] data = new byte[bytesRead];
                        replyBuffer.get(data);
                        System.out.println("Server: " + new String(data));
                    }

                    case "DOWNLOAD" -> {
                        if (parts.length < 2) continue;
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] fileData = new byte[replyBuffer.remaining()];
                        replyBuffer.get(fileData);
                        try (FileOutputStream fos = new FileOutputStream(parts[1])) {
                            fos.write(fileData);
                        }
                        System.out.println("Download complete.");
                    }

                    case "QUIT" -> {
                        return;
                    }

                    default -> System.out.println("Unknown command.");
                }
            }
        }
    }
}


