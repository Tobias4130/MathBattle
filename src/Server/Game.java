package Server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public abstract class Game implements Runnable {
    PrintWriter writer;
    Scanner reader;
    Socket socket;
    boolean connectionSuccessful = false;

    public Game(Socket gameSocket) {
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
}

class Vendespil extends Game {
    Random random = new Random();
    HashMap<String, String> calculationsMap;


    public  Vendespil(Socket gameSocket){
        super(gameSocket);
        createHashMap();


    }

    void createHashMap(){
        calculationsMap = new HashMap<>();
        for (int i = 0; i < 16; i++) {
            int number1 = random.nextInt(1,10);
            int number2 = random.nextInt(1,10);
            int operator = random.nextInt(3);
            switch (operator) {
                case 0:
                    calculationsMap.put((number1)+"+"+(number2), String.valueOf(number1+number2));
                    break;
                case 1:
                    calculationsMap.put((number1)+"-"+(number2), String.valueOf(number1-number2));
                    break;
                case 2:
                    calculationsMap.put((number1)+"*"+(number2), String.valueOf(number1*number2));
                    break;
            }
        }
    }

    @Override
    public void run() {
        System.out.println("Vendespil er startet");
        writer.println("Ready to start");
        String lastMove = "6767";
        int movesMade = 0;
        int pairsFound = 0;
        double startTime = 0;
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            System.out.println("Received data: " + data);
            if (Objects.equals(data, "START")) {
                startTime = System.nanoTime();
                writer.println("COUNTDOWN");
                System.out.println("Game has started");
            }

            String[] dataSplit = data.split(" ");
            if (dataSplit[0].equals("MOVE")) {
                movesMade = movesMade + 1;
                String currentMove = dataSplit[1];

                if (!Objects.equals(lastMove, "6767")) {
                    if (lastMove.length() > currentMove.length()) {
                        if (Objects.equals(calculationsMap.get(lastMove), currentMove)) {
                            writer.println("Correct");
                            pairsFound++;
                            if (pairsFound == 8) {

                                break;
                            }
                        } else {
                            writer.println("Wrong");
                        }
                    } else if (lastMove.length() < currentMove.length()) {
                        if (Objects.equals(calculationsMap.get(currentMove), lastMove)) {
                            writer.println("Correct");
                            pairsFound++;
                            if (pairsFound == 8) {
                                double endTime = startTime - System.nanoTime();
                                break;
                            }
                        } else {
                            writer.println("Wrong");
                        }
                    }
                }
                lastMove = currentMove;
            }
        }
    }
}


class TabelKrig extends Game {
    private List<Integer> tableList;
    int numberCorrect = 0;
    public TabelKrig(Socket gameSocket){
        super(gameSocket);

    }

    @Override
    public void run() {
        System.out.println("TabelKrig er startet");

        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            System.out.println("Received line: " + line);
            String[] splitLine = line.split(":");

            if (Objects.equals(splitLine[0], "START")) {
                int selectedTable = Integer.parseInt(splitLine[1]);
                tableList = buildTableList(selectedTable);
            }
            else {
                int guess = Integer.parseInt(line);
                int expected = tableList.get(numberCorrect);
                if (guess == expected) {
                    writer.println("OK");
                    numberCorrect++;

                } else writer.println("WRONG");

                if (numberCorrect == tableList.size()){
                    writer.println("DONE");
                    break;
                }
            }
        }
    }

    private List<Integer> buildTableList(int table) {
        List<Integer> res = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            res.add(table * i);
        }
        return res;
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