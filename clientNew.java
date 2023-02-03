import java.io.*;
import java.net.*;
import java.util.*;

public class clientNew {
    public static void main(String[] args) throws IOException {
        new setUp2();
    }

}

class setUp2 {
    private final static int port = 9001;
    private final static String host = "localhost";
    private static Socket s;

    public setUp2() throws IOException {
        s = new Socket(host, port);
        new Thread(new reply()).start();
        new Thread(new listen()).start();

    }

    public static Socket getS() {
        return s;
    }
}

class reply implements Runnable {
    public reply() {
    }

    @Override
    public void run() {
        try (Scanner commands = new Scanner(System.in)) {
            try (PrintWriter writer = new PrintWriter(setUp2.getS().getOutputStream(), true)) {
                while (true) {
                    writer.println(commands.nextLine());

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class listen implements Runnable {
    public listen() {

    }

    @Override
    public void run() {
        try {
            try (Scanner reader = new Scanner(setUp2.getS().getInputStream())) {
                while (true) {
                    System.out.println(reader.nextLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}