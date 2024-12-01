package org.example;// Імпорт необхідних бібліотек
import java.io.*;
import java.net.*;

// Файловий менеджер сервер.
public class FileManagerServer {
    private static final int PORT = 6789;
    private static final String DIRECTORY_PATH = "files";

    public static void main(String[] args) {
        new Thread(FileManagerServer::startServer).start();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Файловий сервер запущено на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new FileHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Обробник файлів
    private static class FileHandler implements Runnable {
        private Socket clientSocket;

        public FileHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request;
                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(" ", 2);
                    String command = parts[0];

                    switch (command) {
                        case "LIST":
                            listFiles(out);
                            break;
                        case "UPLOAD":
                            uploadFile(parts[1], in);
                            out.println("Файл завантажено");
                            break;
                        case "DOWNLOAD":
                            downloadFile(parts[1], out);
                            break;
                        default:
                            out.println("UNKNOWN_COMMAND");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void listFiles(PrintWriter out) {
            File directory = new File(DIRECTORY_PATH);
            String[] files = directory.list();
            if (files != null) {
                out.println(String.join(",", files));
            } else {
                out.println("Папка порожня або не існує");
            }
        }

        private void uploadFile(String fileName, BufferedReader in) throws IOException {
            File directory = new File(DIRECTORY_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File file = new File(directory, fileName);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                String line;
                while ((line = in.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        }

        private void downloadFile(String fileName, PrintWriter out) throws IOException {
            File file = new File(DIRECTORY_PATH, fileName);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.println(line);
                    }
                }
            } else {
                out.println("Файл не знайдено");
            }
        }
    }
}




