package Server;

import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class Vendespil extends Connection {
    Random random = new Random();
    HashMap<String, String> calculationsMap;
    String[] board = new String[16];
    String lastMove = "6767";
    int movesMade = 0;
    int pairsFound = 0;
    double startTime = 0;

    public  Vendespil(Socket gameSocket){
        super(gameSocket);
        createHashMapAndBoard();
        System.out.println(Arrays.deepToString(board));
        System.out.println(calculationsMap);
    }

    void createHashMapAndBoard(){
        calculationsMap = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            int number1 = random.nextInt(1,11);
            int number2 = random.nextInt(1,11);
            int operator = random.nextInt(3);
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

    @Override
    public void run() {
        System.out.println("Vendespil er startet");
        writer.println("Ready to start");
        while (reader.hasNextLine()) {
            String data = reader.nextLine();
            System.out.println("Received data: " + data);
            if (Objects.equals(data, "START")) {
                startTime = System.nanoTime();
                writer.println("Game has started");
                System.out.println("Game has started");
            }

            String[] dataSplit = data.split(":");
            if (dataSplit[0].equals("MOVE")) {
                movesMade++;
                int currentMoveInt = Integer.parseInt(dataSplit[1]);
                String currentMoveString = board[currentMoveInt];
                writer.println("Move received:" + currentMoveString);
                if (Objects.equals(lastMove, "6767")) {
                    lastMove = currentMoveString;
                }
                else {
                    if (Objects.equals(calculationsMap.get(lastMove), currentMoveString)) {
                        writer.println("Correct");
                        pairsFound++;
                        if (pairsFound == 8) {
                            double endTime = (System.nanoTime() - startTime) / 1_000_000_000.0;
                            writer.println("Game has ended:" + endTime + " seconds");
                            break;
                        }
                    } else if (Objects.equals(calculationsMap.get(currentMoveString), lastMove)) {
                        writer.println("Correct");
                        pairsFound++;
                        if (pairsFound == 8) {
                            double endTime = System.nanoTime() - startTime;
                            writer.println("Game has ended:" + endTime);
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

