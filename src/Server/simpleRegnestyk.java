package Server;

import java.net.Socket;
import java.util.*;

public class simpleRegnestyk extends Connection {
    private static class Problem {
        String expr;
        int answer;
        Problem(String expr, int answer) { this.expr = expr; this.answer = answer; }
    }

    private final Random random = new Random();
    private List<Problem> problems;
    private int index = 0;
    private double startTime = 0;
    private int mistakes = 0;

    public  simpleRegnestyk(Socket gameSocket) {
        super(gameSocket);
    }
    //Lytter på beskeder fra klienten og reagerer efter det
    @Override
    public void run() {
        System.out.println("simpleRegnestyk er startet");
        problems = new ArrayList<>();
        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();
            if (line.isEmpty()) continue;
            //Ny spille runde
            if (line.equals("START")) {
                problems = generateProblem (10);
                index =0;
                mistakes = 0;
                startTime = System.nanoTime();

                sendQuestion ();
                continue;
            }
            //modtager spillerens svar
            if (line.startsWith("ANSWER")) {
                if (problems == null || problems.isEmpty()) {
                    writer.println("ERR:NOT_STARTED");
                    continue;
                }
                String [] parts = line.split(":");
                int guess = Integer.parseInt(parts[1]);

                int expectedAnswer = problems.get(index).answer;
                System.out.println("Guess: " + guess + " Expected: " + expectedAnswer);
                //korrekt svar
                if (guess == expectedAnswer) {
                    writer.println("OK:" + (index + 1));
                    index++;
                    //Hvis alle 10 spørgsmål done og tilføjes straf
                    if (index >= problems.size()) {
                        double endTime = (System.nanoTime() - startTime);
                        double roundedEndTime = Math.round(endTime * 100.0) / 100.0;
                        double seconds = roundedEndTime / 1_000_000_000.0;
                        double score = seconds + (mistakes * 10);
                        writer.println("DONE:" + String.format(java.util.Locale.US, "%.2f", seconds));
                        String username = reader.nextLine();
                        new Leaderboard().addResult("simpleRegnestyk",username,score);
                        break;
                    }
                    else {
                        sendQuestion();
                    }
                } else {
                    //Forkert svar tælles og tillægges højere oppe
                    mistakes++;
                    writer.println("WRONG:" + (index+1));
                }
            }
        }
    }
    //næste regnestykke sendes
    private void sendQuestion() {
        int questionNumber = index + 1;
        Problem p = problems.get(index);
        writer.println("Q:" + questionNumber + ":" + p.expr);
    }
    //Generer tilfældige regnestykker
    private List<Problem> generateProblem (int count) {
        List<Problem> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int a = random.nextInt(91) + 10;
            int b = random.nextInt(91) + 10;
            boolean plus = random.nextBoolean();

            String expr;
            int ans;

            if (plus) {
                expr = a + "+" + b;
                ans = a + b;
            } else {
                expr = a + "-" + b;
                ans = a - b;
            }
            list.add(new Problem(expr, ans));
        }
        return list;
    }
}