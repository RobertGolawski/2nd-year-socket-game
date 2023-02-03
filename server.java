import java.io.*;
import java.net.*;
import java.util.*;

public class server {
    private final static int port = 9001;
    static ArrayList<Socket> socket_list = new ArrayList<Socket>();
    static ArrayList<player> player_list = new ArrayList<player>();
    static boolean firstPlayer;

    public static void main(String[] args) throws IOException {

        startServer();

    }

    private static void startServer() throws IOException {
        ServerSocket ss;

        int idCount = 1;
        try {
            ss = new ServerSocket(port);
            System.out.println("Awaiting connections");
            new Thread(new checkConnection()).start();
            new Thread(new announcement()).start();
            while (true) {
                Socket s = ss.accept();
                System.out.println("Connected with ID: " + idCount);
                new Thread(new commandConnection(s, idCount)).start();
                socket_list.add(s);
                player p = new player(idCount, s);
                if (firstPlayer) {
                    p.getBall();
                    System.out.println("Player " + p.getID() + " gets the ball from the server");
                    firstPlayer = false;
                } else {
                    announceNewPlayer(idCount);
                }
                player_list.add(p);
                idCount += 1;
                ArrayList<Integer> temp = new ArrayList<>();
                for (player i : player_list) {
                    temp.add(i.getID());
                }
                System.out.println("The full player list is: " + temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void announceNewPlayer(int i) {
        for (player p : player_list) {
            try {
                PrintWriter writer = new PrintWriter(p.getSocket().getOutputStream(), true);
                writer.println("Player " + i + " has joined the game");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Socket> getSocket_list() {
        return socket_list;
    }

    public static ArrayList<player> getPlayer_list() {
        return player_list;
    }

    public static void setPlayer_list(ArrayList<player> player_list) {
        server.player_list = player_list;
    }

    public static void setSocket_list(ArrayList<Socket> socket_list) {
        server.socket_list = socket_list;
    }
}

class commandConnection implements Runnable {
    private int ID;
    private final Socket socket;

    public commandConnection(Socket socket, int ID) {
        this.socket = socket;
        this.ID = ID;
    }

    public static void announcePass(int a, int b) throws IOException {
        ArrayList<player> players = server.getPlayer_list();

        for (player p : players) {
            if (!p.getSocket().isClosed()) {
                try {
                    PrintWriter writer = new PrintWriter(p.getSocket().getOutputStream(), true);
                    writer.println("Player " + a + " passes to player " + b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Player " + a + " passes to player " + b);
    }

    @Override
    public void run() {
        try (Scanner reader = new Scanner(socket.getInputStream())) {
            while (true) {
                try {
                    String line = reader.nextLine();
                } catch (NoSuchElementException e) {
                    break;
                }
                String line = reader.nextLine();
                try {
                    int idToCheck = Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    if (!socket.isClosed()) {
                        PrintWriter reply = new PrintWriter(socket.getOutputStream(), true);
                        reply.println("Unknown command, please try again");
                        continue;
                    }
                }
                int idToCheck = Integer.parseInt(line);
                if (player.getPlayer(socket).HasBall()) {
                    if (player.playerWithID(idToCheck) != null) {
                        if (!player.playerWithID(idToCheck).getSocket().isClosed()) {
                            player p = player.getPlayer(socket);
                            player r = player.playerWithID(idToCheck);
                            p.passBall();
                            r.getBall();
                            announcePass(p.getID(), r.getID());
                        } else {
                            announcePass(player.getPlayer(socket).getID(), player.getPlayer(socket).getID());
                        }
                    } else {
                        PrintWriter reply = new PrintWriter(socket.getOutputStream(), true);
                        reply.println("Unknown player");
                        System.out.println("unknown player");
                    }
                } else {
                    PrintWriter reply = new PrintWriter(socket.getOutputStream(), true);
                    reply.println("You don't have the ball");
                    System.out.println("no ball");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public int getID() {
        return ID;
    }
}

class checkConnection implements Runnable {
    public checkConnection() {
    }

    @Override
    public void run() {
        while (true) {
            ArrayList<Socket> socket_list = server.getSocket_list();
            if (socket_list.size() == 0) {
                server.firstPlayer = true;
            }
            for (Socket s : socket_list) {
                if (!s.isClosed()) {
                } else {
                    System.out.println("Removing socket: " + s + " Player ID: " + player.getPlayer(s).getID());
                    ArrayList<player> players = server.getPlayer_list();
                    for (player b : players) {
                        if (b.getSocket() == s) {
                            if (b.HasBall()) {
                                int i = players.indexOf(b);
                                if (players.size() != 1) {
                                    player z = players.get((i + 1) % players.size());
                                    b.passBall();
                                    z.getBall();
                                    System.out.println("Ball passed automatically to player with ID: " + z.getID());
                                    try {
                                        commandConnection.announcePass(b.getID(), z.getID());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    System.out.println(
                                            "No players in player queue, system will pass the ball to the first player who joins.");
                                    server.firstPlayer = true;
                                }
                            }
                        }
                    }
                    players.removeIf(p -> p.getSocket().equals(s));
                    server.setPlayer_list(players);
                    socket_list.remove(s);
                    server.setSocket_list(socket_list);
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class player {
    private int ID;
    private Socket socket;
    private boolean hasBall = false;

    public player(int ID, Socket socket) {
        this.socket = socket;
        this.ID = ID;
    }

    public void getBall() {
        hasBall = true;
    }

    public void passBall() {
        hasBall = false;
    }

    public int getID() {
        return ID;
    }

    public static player getPlayer(Socket s) {
        for (player p : server.getPlayer_list()) {
            if (p.getSocket().equals(s)) {
                return p;
            }
        }
        return null;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean HasBall() {
        return hasBall;
    }

    public static player playerWithID(int ID) {
        for (player p : server.getPlayer_list()) {
            if (p.getID() == ID) {
                return p;
            }
        }
        return null;
    }

    public player whoHas() {
        for (player p : server.getPlayer_list()) {
            if (p.HasBall()) {
                return p;
            }
        }
        return null;
    }
}

class announcement implements Runnable {
    int playerWithBall;

    public announcement() {
    }

    @Override
    public void run() {

        while (true) {
            ArrayList<Integer> IDs = new ArrayList<>();
            ArrayList<player> players = server.getPlayer_list();
            for (player p : players) {
                IDs.add(p.getID());
                Socket s = p.getSocket();
                if (!s.isClosed()) {
                    try {
                        PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
                        writer.println("Connected with ID: " + p.getID());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (p.HasBall()) {
                    playerWithBall = p.getID();

                }
            }
            for (player p : players) {
                if (!p.getSocket().isClosed()) {
                    try {
                        PrintWriter writer = new PrintWriter(p.getSocket().getOutputStream(), true);
                        writer.println("List of currently connected player IDs: " + IDs
                                + " Player currently holding the ball is: " + playerWithBall);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            for (player p : players) {
                if (!p.getSocket().isClosed()) {
                    if (p.HasBall()) {
                        try {
                            PrintWriter writer = new PrintWriter(p.getSocket().getOutputStream(), true);
                            writer.println("Enter the ID of the player you wish to pass to");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}