package org.example.IPC;

import java.io.*;
import java.net.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainProcess {
    public static void main(String[] args) {
        String host = "127.0.0.1";
        int port = 65432;

        try (Socket socket = new Socket(host, port)) {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    int randomNumber = new Random().nextInt(1000);
                    System.out.println("Java: Згенеровано число " + randomNumber);

                    System.out.println("Java: Відправляємо число");
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(randomNumber);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    System.out.println("Java: Отримано відповідь від Python: " + response);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка з'єднання: " + e.getMessage());
        }
    }

}
