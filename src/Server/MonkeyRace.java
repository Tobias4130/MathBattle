package Server;

import java.net.Socket;
import java.util.Random;

//serveren håndtere spil logik
public class MonkeyRace extends Connection {
    private final Random random = new Random(); //tilfældige tal generares
    private int expectedAnswer; //Svaret vi forventer (rigtige)
    private int leftUntilWin = 8; //hvor mange rigtige svar spilleren mangler
    private double startTime; // tidspunktet hvor spillet startede.

    public MonkeyRace(Socket gameSocket) {
        super(gameSocket);
    }
    //Modtager beskeder fra klient og reagerer
    @Override
    public void run() {
        String line;
        while(reader.hasNextLine()){
            line = reader.nextLine();
            System.out.println("Received: " + line);
            //Starter spil
            if (line.equals("Start")) {
                nextQuestion();
                startTime = System.nanoTime();
            } else if (line.startsWith("A:")) {
                checkAnswer(line.split(":")[1]);
            }
        }
    }

    //Generer nyt regnestykker og sender til klient
    private void nextQuestion() {
        int a = random.nextInt(91) + 10;
        int b = random.nextInt(91) + 10;
        char op;
        if (random.nextBoolean()) {
            op = '+';
            expectedAnswer = a + b;
        } else {
            op = '-';
            expectedAnswer = a - b;
        }
        //sender til klient
        writer.println("Q:"+a+op+b);
    }
    // tjekker om spillerens svar er korrekt
    private void checkAnswer(String answer) {
        try {
            int answerInt = Integer.parseInt(answer);
            //Korrekt svar
            if (answerInt == expectedAnswer){
                leftUntilWin--;
                if (leftUntilWin <= 0){
                    //Spilleren når i mål
                    writer.println("W");
                    String username = reader.nextLine();
                    System.out.println("We got the username: "+username);
                    double endTime = (System.nanoTime() - startTime) / 1_000_000_000.0;
                    double roundedEndTime = Math.round(endTime * 100.0) / 100.0;
                    new Leaderboard().addResult("MonkeyRace", username, roundedEndTime);

                } else {
                    //Spiller rykker videre til næste palmetræ
                    writer.println("C");
                    nextQuestion();
                }
                //Forkert svar
            } else {
                leftUntilWin++;
                writer.println("I");
                nextQuestion();
            }
            //Hvis input ikke er et tal
        } catch (NumberFormatException _) {
            leftUntilWin++;
            writer.println("I");
            nextQuestion();
        }
    }
}