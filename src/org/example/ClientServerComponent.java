// Імпорт необхідних бібліотек
import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

// Клас основного клієнт-серверного компонента
public class ClientServerComponent {
    private static final int PORT = 5678;
    private static Map<String, String> database = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new Thread(ClientServerComponent::startServer).start();
        new Thread(ClientServerComponent::startClient).start();
    }

    // Метод запуску сервера
    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущено на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод запуску клієнта
    private static void startClient() {
        try (Socket socket = new Socket("localhost", PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Клієнт підключений до сервера");

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("Відповідь сервера: " + in.readLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Обробник клієнтів
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String request;
                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(" ", 2);
                    String command = parts[0];

                    switch (command) {
                        case "PUT":
                            String[] putParts = parts[1].split(" ", 2);
                            String putKey = putParts[0];
                            String putValue = putParts[1];
                            database.put(putKey, putValue);
                            out.println("OK");
                            break;
                        case "GET":
                            String getKey = parts[1];
                            String getValue = database.get(getKey);
                            out.println(getValue != null ? getValue : "NOT_FOUND");
                            break;
                        case "DELETE":
                            String deleteKey = parts[1];
                            database.remove(deleteKey);
                            out.println("OK");
                            break;
                        default:
                            out.println("UNKNOWN_COMMAND");
                            break;
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
