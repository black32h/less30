// Імпорт необхідних бібліотек
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// Клас сервера для системи сповіщень
public class NotificationServer {
    private static final int PORT = 1234;
    private static Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Сервер запущено на порту: " + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    // Обробник клієнтів
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                String clientName = in.readLine();
                clients.put(clientName, out);

                String message;
                while ((message = in.readLine()) != null) {
                    for (Map.Entry<String, PrintWriter> client : clients.entrySet()) {
                        if (!client.getKey().equals(clientName)) {
                            client.getValue().println(clientName + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

// Клас сервера для бази даних
