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
    
    @Override
    public void run() {
        System.out.println("simpleRegnestyk er startet");
        problems = new ArrayList<>();
        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();
            if (line.isEmpty()) continue;

            if (line.equals("START")) {
                problems = generateProblem (10);
                index =0;
                mistakes = 0;
                startTime = System.nanoTime();

                sendQuestion ();
                continue;
            }
            if (line.startsWith("ANSWER")) {
                if (problems == null || problems.isEmpty()) {
                    writer.println("ERR:NOT_STARTED");
                    continue;
                }
                String [] parts = line.split(":");
                int guess = Integer.parseInt(parts[1]);

                int expectedAnswer = problems.get(index).answer;
                System.out.println("Guess: " + guess + " Expected: " + expectedAnswer);
                if (guess == expectedAnswer) {
                    writer.println("OK:" + (index + 1));
                    index++;

                    if (index >= problems.size()) {
                        double seconds = (System.nanoTime() - startTime) / 1_000_000_000.0;
                        double score = seconds + (mistakes * 10);
                        writer.println("DONE:" + String.format(java.util.Locale.US, "%.2f", seconds));
                        break;
                    }
                    else {
                        sendQuestion();
                    }
                } else {
                    mistakes++;
                    writer.println("WRONG:" + (index+1));
                }
            }
        }
    }
    private void sendQuestion() {
        int questionNumber = index + 1;
        Problem p = problems.get(index);
        writer.println("Q:" + questionNumber + ":" + p.expr);
    }
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