package org.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class DelayedEchoServer {
    private static final int PORT = 4567;
    private static final int DELAY_SECONDS = 5;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Ехо-сервер із затримкою запущено на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Отримано повідомлення: " + message);
                    TimeUnit.SECONDS.sleep(DELAY_SECONDS); // Затримка
                    out.println("Відповідь із затримкою: " + message);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}



