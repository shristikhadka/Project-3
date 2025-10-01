import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.File;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 3000;

        // server socket channel/ bind to port
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));

        File sharedDir = new File("ServerFiles");
        if (!sharedDir.exists()) {
            sharedDir.mkdir();
        }


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
                        sharedDir = new File("ServerFiles");
                        if (!sharedDir.exists()) {
                            sharedDir.mkdir();
                        }

                        String[] files = sharedDir.list();
                        String reply = (files != null) ? String.join("\n", files) : "No files found.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                    }

                    case "DELETE" -> {
                        if (parts.length < 2) break;
                        File file = new File(parts[1]);
                        String reply;
                        if (file.exists() && file.delete()) {
                            reply = "File deleted.";
                        } else {
                            reply = "File not found.";
                        }
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
                        if (parts.length < 2) break;
                        File file = new File(sharedDir, parts[1]);

                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            ByteBuffer fileBuffer = ByteBuffer.allocate(4096);
                            int bytes;
                            while ((bytes = clientChannel.read(fileBuffer)) > 0) {
                                fileBuffer.flip();
                                byte[] chunk = new byte[bytes];
                                fileBuffer.get(chunk);
                                fileBuffer.clear();
                            }
                            clientChannel.write(ByteBuffer.wrap("Upload complete.".getBytes()));
                        } catch (Exception e) {
                            clientChannel.write(ByteBuffer.wrap(("Upload failed: " + e.getMessage()).getBytes()));
                        }
                    }

                    case "DOWNLOAD" -> {
                        if (parts.length < 2) break;
                        File file = new File(sharedDir, parts[1]);
                        if (!file.exists()) {
                            clientChannel.write(ByteBuffer.wrap("File not found.".getBytes()));
                            break;
                        }

                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] buf = new byte[4096];
                            int count;
                            while ((count = fis.read(buf)) != -1) {
                                clientChannel.write(ByteBuffer.wrap(buf, 0, count));
                            }
                        }
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
