package org.example;

import java.io.*;
import java.net.*;

public class DistributedComputeServer {
    private static final int PORT = 5678;

    public static void main(String[] args) {
        new Thread(DistributedComputeServer::startServer).start();
        new Thread(DistributedComputeServer::startClient).start();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер обчислень запущено на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new TaskHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient() {
        try (Socket socket = new Socket("localhost", PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Клієнт підключений до сервера");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Результат обчислення: " + in.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class TaskHandler implements Runnable {
        private Socket clientSocket;

        public TaskHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String task;
                while ((task = in.readLine()) != null) {
                    String[] parts = task.split(" ");
                    String command = parts[0];
                    String result = "UNKNOWN_COMMAND";

                    switch (command) {
                        case "SUM":
                            int sum = 0;
                            for (int i = 1; i < parts.length; i++) {
                                sum += Integer.parseInt(parts[i]);
                            }
                            result = "Сума: " + sum;
                            break;
                        case "FACTORIAL":
                            int n = Integer.parseInt(parts[1]);
                            result = "Факторіал: " + factorial(n);
                            break;
                    }
                    out.println(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private int factorial(int n) {
            if (n == 0) return 1;
            int result = 1;
            for (int i = 1; i <= n; i++) {
                result *= i;
            }
            return result;
        }
    }
}
