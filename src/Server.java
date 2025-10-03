import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Server {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Server <port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        // Create ServerFiles directory if it doesn't exist
        File serverDir = new File("ServerFiles");
        if (!serverDir.exists()) {
            serverDir.mkdir();
        }

        // server socket channel/ bind to port
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));
        System.out.println("Server started on port " + port);

        while (true) {
            try (SocketChannel clientChannel = serverSocket.accept()) {
                System.out.println("Client connected.");

                // Read command character (2 bytes for char)
                ByteBuffer commandBuffer = ByteBuffer.allocate(2);
                int bytesRead = clientChannel.read(commandBuffer);

                if (bytesRead < 2) {
                    System.out.println("Client disconnected without sending command.");
                    continue;
                }

                commandBuffer.flip();
                char command = commandBuffer.getChar();

                System.out.println("Received command: " + command);

                switch (command) {
                    case 'L' -> { // LIST
                        File dir = new File("ServerFiles");
                        String[] files = dir.list();
                        String reply = (files != null && files.length > 0)
                            ? String.join("\n", files)
                            : "No files found.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                        System.out.println("Sent file list.");
                    }

                    case 'D' -> { // DELETE
                        // Read filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        clientChannel.read(lengthBuffer);
                        lengthBuffer.flip();
                        int fileNameLength = lengthBuffer.getInt();

                        // Read filename
                        ByteBuffer nameBuffer = ByteBuffer.allocate(fileNameLength);
                        clientChannel.read(nameBuffer);
                        nameBuffer.flip();
                        byte[] nameBytes = new byte[fileNameLength];
                        nameBuffer.get(nameBytes);
                        String fileName = new String(nameBytes);

                        // Delete file
                        File file = new File("ServerFiles", fileName);
                        String reply = (file.exists() && file.delete())
                            ? "SUCCESS: File deleted."
                            : "FAILURE: File not found.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                        System.out.println("DELETE " + fileName + ": " + reply);
                    }

                    case 'R' -> { // RENAME
                        // Read old filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        clientChannel.read(lengthBuffer);
                        lengthBuffer.flip();
                        int oldNameLength = lengthBuffer.getInt();

                        // Read old filename
                        ByteBuffer oldNameBuffer = ByteBuffer.allocate(oldNameLength);
                        clientChannel.read(oldNameBuffer);
                        oldNameBuffer.flip();
                        byte[] oldNameBytes = new byte[oldNameLength];
                        oldNameBuffer.get(oldNameBytes);
                        String oldName = new String(oldNameBytes);

                        // Read new filename length
                        lengthBuffer.clear();
                        clientChannel.read(lengthBuffer);
                        lengthBuffer.flip();
                        int newNameLength = lengthBuffer.getInt();

                        // Read new filename
                        ByteBuffer newNameBuffer = ByteBuffer.allocate(newNameLength);
                        clientChannel.read(newNameBuffer);
                        newNameBuffer.flip();
                        byte[] newNameBytes = new byte[newNameLength];
                        newNameBuffer.get(newNameBytes);
                        String newName = new String(newNameBytes);

                        // Rename file
                        File oldFile = new File("ServerFiles", oldName);
                        File newFile = new File("ServerFiles", newName);
                        String reply = (oldFile.exists() && oldFile.renameTo(newFile))
                            ? "SUCCESS: File renamed."
                            : "FAILURE: Rename failed.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                        System.out.println("RENAME " + oldName + " to " + newName + ": " + reply);
                    }

                    case 'U' -> { // UPLOAD
                        // Read filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        clientChannel.read(lengthBuffer);
                        lengthBuffer.flip();
                        int fileNameLength = lengthBuffer.getInt();

                        // Read filename
                        ByteBuffer nameBuffer = ByteBuffer.allocate(fileNameLength);
                        clientChannel.read(nameBuffer);
                        nameBuffer.flip();
                        byte[] nameBytes = new byte[fileNameLength];
                        nameBuffer.get(nameBytes);
                        String fileName = new String(nameBytes);

                        // Receive file content and save to ServerFiles directory
                        File uploadedFile = new File("ServerFiles", fileName);
                        FileOutputStream fos = new FileOutputStream(uploadedFile);
                        ByteBuffer contentBuffer = ByteBuffer.allocate(1024);

                        int uploadBytesRead;
                        while ((uploadBytesRead = clientChannel.read(contentBuffer)) > 0) {
                            contentBuffer.flip();
                            while (contentBuffer.hasRemaining()) {
                                fos.write(contentBuffer.get());
                            }
                            contentBuffer.clear();
                        }

                        fos.close();

                        // Send success response
                        String reply = "SUCCESS: File uploaded.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                        System.out.println("UPLOAD " + fileName + ": " + reply);
                    }

                    case 'W' -> { // DOWNLOAD
                        // Read filename length
                        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
                        clientChannel.read(lengthBuffer);
                        lengthBuffer.flip();
                        int fileNameLength = lengthBuffer.getInt();

                        // Read filename
                        ByteBuffer nameBuffer = ByteBuffer.allocate(fileNameLength);
                        clientChannel.read(nameBuffer);
                        nameBuffer.flip();
                        byte[] nameBytes = new byte[fileNameLength];
                        nameBuffer.get(nameBytes);
                        String fileName = new String(nameBytes);

                        // Check if file exists
                        File downloadFile = new File("ServerFiles", fileName);
                        if (!downloadFile.exists()) {
                            // Send failure status
                            ByteBuffer statusBuffer = ByteBuffer.allocate(1);
                            statusBuffer.put((byte) 'F');
                            statusBuffer.flip();
                            clientChannel.write(statusBuffer);
                            System.out.println("DOWNLOAD " + fileName + ": File not found.");
                        } else {
                            // Send success status
                            ByteBuffer statusBuffer = ByteBuffer.allocate(1);
                            statusBuffer.put((byte) 'S');
                            statusBuffer.flip();
                            clientChannel.write(statusBuffer);

                            // Send file content
                            FileInputStream fis = new FileInputStream(downloadFile);
                            ByteBuffer contentBuffer = ByteBuffer.allocate(1024);
                            byte[] chunk = new byte[1024];
                            int downloadBytesRead;

                            while ((downloadBytesRead = fis.read(chunk)) > 0) {
                                contentBuffer.clear();
                                contentBuffer.put(chunk, 0, downloadBytesRead);
                                contentBuffer.flip();
                                clientChannel.write(contentBuffer);
                            }

                            fis.close();
                            System.out.println("DOWNLOAD " + fileName + ": File sent.");
                        }
                    }

                    default -> {
                        String reply = "ERROR: Invalid command.";
                        clientChannel.write(ByteBuffer.wrap(reply.getBytes()));
                        System.out.println("Invalid command received: " + command);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error handling client: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

