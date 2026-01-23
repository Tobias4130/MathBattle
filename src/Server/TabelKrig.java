package Server;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TabelKrig extends Connection {
    private List<Integer> tableList;
    double startTime;
    int numberCorrect = 0;
    int mistakes = 0;
    public TabelKrig(Socket gameSocket){
        super(gameSocket);
    }


    @Override
    public void run() {
        System.out.println("TabelKrig er startet");
//Læs input fra klient
        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            System.out.println("Received line: " + line);
            String[] splitLine = line.split(":");
//Start nyt spil
            if (Objects.equals(splitLine[0], "START")) {
                int selectedTable = Integer.parseInt(splitLine[1]);
                tableList = buildTableList(selectedTable);

                startTime = System.nanoTime();
            }
            //Gæt forventes fra klienten
            else {
                int guess = Integer.parseInt(line);
                int expected = tableList.get(numberCorrect);
                //Rigtig svar
                if (guess == expected) {
                    numberCorrect++;
                    //Hvis alle 10 rigtige
                    if (numberCorrect == tableList.size()){
                        double endTime = System.nanoTime();
                        double roundedEndTime = Math.round(endTime * 100.0) / 100.0;
                        double seconds = (roundedEndTime - startTime) / 1_000_000_000.0;

                        //Score = sekunder + 5 sekunder straf pr fejl
                        double score = seconds + (mistakes * 5.0);
                        //send sluttid
                        writer.println("DONE:" + String.format(java.util.Locale.US, "%.2f", seconds));
                        String username = reader.nextLine();
                        new Leaderboard().addResult("TabelKrig",username,score);
                        break;
                    } else {
                        writer.println("OK");
                    }

                } else {
                    mistakes++;
                    writer.println("WRONG");
                }
            }
        }
    }
    //Liste til at vise korrekte svar
    private List<Integer> buildTableList(int table) {
        List<Integer> res = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            res.add(table * i);
        }
        return res;
    }
}
