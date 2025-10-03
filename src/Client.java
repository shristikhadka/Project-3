import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.FileChannel;
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

        System.out.println("Connected to server.");

        while (true) {
            System.out.print("Enter command (LIST/DELETE <file>/RENAME <old> <new>/UPLOAD <file>/DOWNLOAD <file>/QUIT): ");
            String input = scanner.nextLine();
            String[] parts = input.split(" ", 2);
            String cmd = parts[0].toUpperCase();

            if (cmd.equals("QUIT")) {
                System.out.println("Exiting...");
                return;
            }

            // connect per command
            try (SocketChannel channel = SocketChannel.open(new InetSocketAddress(serverIP, port))) {
                ByteBuffer replyBuffer = ByteBuffer.allocate(4096);

                switch (cmd) {
                    case "LIST" -> {
                        // Send command
                        ByteBuffer commandBuffer = ByteBuffer.allocate(2);
                        commandBuffer.putChar('L');
                        commandBuffer.flip();
                        channel.write(commandBuffer);
                        channel.shutdownOutput();

                        // Receive response
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] data = new byte[bytesRead];
                        replyBuffer.get(data);
                        System.out.println("Files on server:\n" + new String(data));
                    }

                    case "DELETE" -> {
                        if (parts.length < 2) {
                            System.out.println("Usage: DELETE <filename>");
                            continue;
                        }
                        String fileName = parts[1];

                        // Send command
                        ByteBuffer commandBuffer = ByteBuffer.allocate(2);
                        commandBuffer.putChar('D');
                        commandBuffer.flip();
                        channel.write(commandBuffer);

                        // Send filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        lengthBuffer.putInt(fileName.length());
                        lengthBuffer.flip();
                        channel.write(lengthBuffer);

                        // Send filename
                        ByteBuffer nameBuffer = ByteBuffer.wrap(fileName.getBytes());
                        channel.write(nameBuffer);
                        channel.shutdownOutput();

                        // Receive response
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] data = new byte[bytesRead];
                        replyBuffer.get(data);
                        System.out.println("Server: " + new String(data));
                    }

                    case "RENAME" -> {
                        if (parts.length < 2) {
                            System.out.println("Usage: RENAME <oldname> <newname>");
                            continue;
                        }
                        String[] names = parts[1].split(" ", 2);
                        if (names.length < 2) {
                            System.out.println("Usage: RENAME <oldname> <newname>");
                            continue;
                        }

                        // Send command
                        ByteBuffer commandBuffer = ByteBuffer.allocate(2);
                        commandBuffer.putChar('R');
                        commandBuffer.flip();
                        channel.write(commandBuffer);

                        // Send old filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        lengthBuffer.putInt(names[0].length());
                        lengthBuffer.flip();
                        channel.write(lengthBuffer);

                        // Send old filename
                        ByteBuffer oldNameBuffer = ByteBuffer.wrap(names[0].getBytes());
                        channel.write(oldNameBuffer);

                        // Send new filename length
                        lengthBuffer.clear();
                        lengthBuffer.putInt(names[1].length());
                        lengthBuffer.flip();
                        channel.write(lengthBuffer);

                        // Send new filename
                        ByteBuffer newNameBuffer = ByteBuffer.wrap(names[1].getBytes());
                        channel.write(newNameBuffer);
                        channel.shutdownOutput();

                        // Receive response
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] data = new byte[bytesRead];
                        replyBuffer.get(data);
                        System.out.println("Server: " + new String(data));
                    }

                    case "UPLOAD" -> {
                        if (parts.length < 2) {
                            System.out.println("Usage: UPLOAD <filename>");
                            continue;
                        }
                        String fileName = parts[1];
                        File file = new File("ClientFiles", fileName);

                        if (!file.exists()) {
                            System.out.println("Local file not found: " + file.getPath());
                            continue;
                        }

                        // Send command
                        ByteBuffer commandBuffer = ByteBuffer.allocate(2);
                        commandBuffer.putChar('U');
                        commandBuffer.flip();
                        channel.write(commandBuffer);

                        // Send filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        lengthBuffer.putInt(fileName.length());
                        lengthBuffer.flip();
                        channel.write(lengthBuffer);

                        // Send filename
                        ByteBuffer nameBuffer = ByteBuffer.wrap(fileName.getBytes());
                        channel.write(nameBuffer);

                        // Send file content in chunks
                        FileInputStream fis = new FileInputStream(file);
                        FileChannel fc = fis.getChannel();
                        ByteBuffer contentBuffer = ByteBuffer.allocate(1024);

                        while(fc.read(contentBuffer) != -1) {
                            contentBuffer.flip();
                            channel.write(contentBuffer);
                            contentBuffer.clear();
                        }

                        channel.shutdownOutput();
                        fis.close();

                        // Receive server response
                        int bytesRead = channel.read(replyBuffer);
                        replyBuffer.flip();
                        byte[] data = new byte[bytesRead];
                        replyBuffer.get(data);
                        System.out.println("Server: " + new String(data));
                    }

                    case "DOWNLOAD" -> {
                        if (parts.length < 2) {
                            System.out.println("Usage: DOWNLOAD <filename>");
                            continue;
                        }
                        String fileName = parts[1];

                        // Send command
                        ByteBuffer commandBuffer = ByteBuffer.allocate(2);
                        commandBuffer.putChar('W');
                        commandBuffer.flip();
                        channel.write(commandBuffer);

                        // Send filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        lengthBuffer.putInt(fileName.length());
                        lengthBuffer.flip();
                        channel.write(lengthBuffer);

                        // Send filename
                        ByteBuffer nameBuffer = ByteBuffer.wrap(fileName.getBytes());
                        channel.write(nameBuffer);
                        channel.shutdownOutput();

                        // Receive status code first (1 byte: 'S' for success, 'F' for failure)
                        ByteBuffer statusBuffer = ByteBuffer.allocate(1);
                        channel.read(statusBuffer);
                        statusBuffer.flip();
                        char status = (char) statusBuffer.get();

                        if (status == 'F') {
                            System.out.println("Server: File not found or error.");
                            continue;
                        }

                        // Receive file content
                        File downloadFile = new File("ClientFiles", fileName);
                        downloadFile.getParentFile().mkdirs();

                        FileOutputStream fos = new FileOutputStream(downloadFile);
                        ByteBuffer fileBuffer = ByteBuffer.allocate(1024);

                        while (channel.read(fileBuffer) > 0) {
                            fileBuffer.flip();
                            while (fileBuffer.hasRemaining()) {
                                fos.write(fileBuffer.get());
                            }
                            fileBuffer.clear();
                        }

                        fos.close();
                        System.out.println("Download complete: " + downloadFile.getPath());
                    }

                    default -> System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}




