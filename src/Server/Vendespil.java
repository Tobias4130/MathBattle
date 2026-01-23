package Server;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Vendespil extends Connection {
    //Random generator
    Random random = new Random();
    //Map der holder styr på matchende regnestykker
    HashMap<String, String> calculationsMap;
    //Spilleboardet som har 16 kort
    String[] board = new String[16];
    String lastMove = "6767";
    //Tæller antal træk, antal par fundet og tiden starter på 0
    int movesMade = 0;
    int pairsFound = 0;
    double startTime = 0;

    //Forbindelse til klient
    public  Vendespil(Socket gameSocket){
        super(gameSocket);
        createHashMapAndBoard();
    }

    //Opretter spillebræt ud fra hashmap, hvor der genereres 8 regnestykker og hver regnestykke placeres tilfældigt.
    void createHashMapAndBoard(){
        calculationsMap = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            int number1 = random.nextInt(1,11);
            int number2 = random.nextInt(1,11);
            int operator = random.nextInt(3);
            //Regnestykkerne, først vælges +-* og efterfølgende tal
            String temp1 = switch (operator) {
                case 0 -> (number1) + "+" + (number2);
                case 1 -> (number1) + "-" + (number2);
                case 2 -> (number1) + "*" + (number2);
                default -> "";
            };
            String temp2 = switch (operator) {
                case 0 -> String.valueOf(number1 + number2);
                case 1 -> String.valueOf(number1 - number2);
                case 2 -> String.valueOf(number1 * number2);
                default -> "";
            };
            int r1 = random.nextInt(16);
            while (board[r1] != null && !board[r1].isEmpty()) {
                r1 = random.nextInt(16);
            }
            board[r1] = temp1;

            int r2 = random.nextInt(16);
            while ((r2 == r1) || (board[r2] != null && !board[r2].isEmpty())) {
                r2 = random.nextInt(16);
            }
            board[r2] = temp2;

            calculationsMap.put(temp1, temp2);
        }
    }
    //Modtager kommandoer fra klienten, validerer træk og sender svar tilbage
    @Override
    public void run() {
        System.out.println("Vendespil er startet");
        //Serveren informere klienten om den er klar til at starte
        writer.println("Ready to start");
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            System.out.println("Received data: " + data);
            //Hvis start knap = spillet starter
            if (Objects.equals(data, "START")) {
                startTime = System.nanoTime();
                writer.println("Game has started");
                System.out.println("Game has started");
            }

            String[] dataSplit = data.split(":");
            //Klienten vælger kort
            if (dataSplit[0].equals("MOVE")) {
                movesMade++;
                int currentMoveInt = Integer.parseInt(dataSplit[1]);
                String currentMoveString = board[currentMoveInt];
                writer.println("Move received:" + currentMoveString);
                if (Objects.equals(lastMove, "6767")) {
                    lastMove = currentMoveString;
                }
                //Tjekker om kortene matcher
                else {
                    if (Objects.equals(calculationsMap.get(lastMove), currentMoveString)) {
                        writer.println("Correct");
                        pairsFound++;
                        if (pairsFound == 8) {
                            double endTime = (System.nanoTime() - startTime) / 1000000000.0;
                            double roundedEndTime = Math.round(endTime * 100.0) / 100.0;
                            writer.println("Game has ended:" + endTime + " seconds");
                            String username = reader.nextLine();
                            double score = calculateScore(roundedEndTime,movesMade);
                            new Leaderboard().addResult("Vendespil",username,score);
                            break;
                        }
                    } else if (Objects.equals(calculationsMap.get(currentMoveString), lastMove)) {
                        writer.println("Correct");
                        pairsFound++;
                        if (pairsFound == 8) {
                            double endTime = (System.nanoTime() - startTime) / 1000000000.0;
                            double roundedEndTime = Math.round(endTime * 100.0) / 100.0;
                            writer.println("Game has ended:" + roundedEndTime);
                            String username = reader.nextLine();
                            double score = calculateScore(roundedEndTime,movesMade);
                            new Leaderboard().addResult("Vendespil",username,score);
                            break;
                        }
                    } else {
                        writer.println("Wrong");
                    }
                    lastMove = "6767";
                }
            }
        }
    }
    //Beregner spillerens score i forhold til tid og antal træk.
    private double calculateScore(double timeSeconds, int movesMade) {
        double multiplier;

        if (movesMade <= 16) {
            multiplier = 1.0;
        } else if (movesMade <= 20) {
            multiplier = 1.25;
        } else if (movesMade <= 25) {
            multiplier = 1.5;
        } else if (movesMade <= 30) {
            multiplier = 1.75;
        } else if (movesMade <= 35) {
            multiplier = 2.0;
        } else {
            multiplier = 3.0;
        }
        return timeSeconds * multiplier;
    }
}

