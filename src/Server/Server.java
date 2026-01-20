package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    public Server(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket connection = serverSocket.accept();

                try  {
                    Scanner input = new Scanner(connection.getInputStream());
                    String gameName = input.nextLine();
                    switch (gameName) {
                        case "Vendespil" -> {
                            Vendespil game = new Vendespil(connection);
                            if (game.connectionSuccessful) {
                                new Thread(game).start();
                            }
                        }
                        case "Tabeltræning" -> {
                            TabelKrig game = new TabelKrig(connection);
                            if (game.connectionSuccessful) {
                                new Thread(game).start();
                            }
                        }
                        case "Regnestykker" -> {
                            simpleRegnestyk game = new simpleRegnestyk(connection);
                            if (game.connectionSuccessful) {
                                new Thread(game).start();
                            }
                        }
                        case "Monkey Race" -> {
                            MonkeyRace game = new MonkeyRace(connection);
                            if (game.connectionSuccessful) {
                                new Thread(game).start();
                            }
                        }
                        case "Leaderboard" -> {
                            System.out.println("Leaderboard");
                            Leaderboard game = new Leaderboard(connection);
                            if (game.connectionSuccessful) {
                                new Thread(game).start();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Can't get setup input from client, Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error when creating the server: "+ e.getMessage());
        }
    }
}