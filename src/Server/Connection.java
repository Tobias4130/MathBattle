package Server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public abstract class Connection implements Runnable {
    PrintWriter writer;
    Scanner reader;
    Socket socket;
    boolean connectionSuccessful = false;

    public Connection(Socket gameSocket) {
        socket = gameSocket;

        try {
            PrintWriter writer = new PrintWriter(gameSocket.getOutputStream(), true);
            Scanner reader = new Scanner(gameSocket.getInputStream());
            this.writer = writer;
            this.reader = reader;
            connectionSuccessful = true;
        } catch (Exception e) {
            System.out.println("Connection failed to " + socket.getInetAddress() + ":" + socket.getPort());
        }
    }

    public Connection() {

    }
}