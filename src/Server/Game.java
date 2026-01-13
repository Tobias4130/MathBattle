package Server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public abstract class Game implements Runnable {

    PrintWriter writer;
    Scanner reader;
    Socket socket;
    boolean connectionSuccessful = false;

    public Game(Socket gameSocket) {
        socket = gameSocket;

        try (PrintWriter writer = new PrintWriter(gameSocket.getOutputStream()); Scanner reader = new Scanner(gameSocket.getInputStream());) {
            this.writer = writer;
            this.reader = reader;
            connectionSuccessful = true;
        } catch (Exception e) {
            System.out.println("Connection failed to " + socket.getInetAddress() + ":" + socket.getPort());
        }
    }
}

class Vendespil extends Game {

    public  Vendespil(Socket gameSocket){
        super(gameSocket);

    }

    @Override
    public void run() {
        System.out.println("Vendespil er startet");
    }
}

class TabelKrig extends Game {

    public TabelKrig(Socket gameSocket){
        super(gameSocket);

    }

    @Override
    public void run() {
        System.out.println("TabelKrig er startet");
    }
}

class FaldendeRegnestykker extends Game {

    public  FaldendeRegnestykker(Socket gameSocket) {
        super(gameSocket);
    }


    @Override
    public void run() {
        System.out.println("FaldendeRegnestykker er startet");
    }
}