package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ClockServer {
    private static final int PORT = 8901;
    private static final Map<String, ZoneId> TIME_ZONES = Map.of(
            "UTC", ZoneId.of("UTC"),
            "EET", ZoneId.of("Europe/Kiev"),
            "EST", ZoneId.of("America/New_York")
    );

    public static void main(String[] args) {
        new Thread(ClockServer::startServer).start();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер годинника запущено на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClockHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Обробник годинника.
    private static class ClockHandler implements Runnable {
        private Socket clientSocket;

        public ClockHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String timeZone = in.readLine();
                ZoneId zoneId = TIME_ZONES.getOrDefault(timeZone, ZoneId.of("UTC"));
                LocalDateTime currentTime = LocalDateTime.now(zoneId);
                out.println(currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}