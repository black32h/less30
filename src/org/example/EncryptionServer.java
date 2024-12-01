package org.example;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class EncryptionServer {
    private static final int PORT = 7890;
    private static final String SECRET_KEY = "1234567890123456"; // Приклад ключа (16 байт)

    public static void main(String[] args) {
        new Thread(EncryptionServer::startServer).start();
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер шифрування запущено на порту: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new EncryptionHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Обробник шифрування.
    private static class EncryptionHandler implements Runnable {
        private Socket clientSocket;

        public EncryptionHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String encryptedMessage = in.readLine();
                String decryptedMessage = decrypt(encryptedMessage, SECRET_KEY);
                out.println(decryptedMessage);
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }

        private String decrypt(String encryptedMessage, String secret) throws GeneralSecurityException {
            byte[] decodedKey = secret.getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);
        }
    }
}

