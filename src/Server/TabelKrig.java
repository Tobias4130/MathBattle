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

        while (reader.hasNextLine()) {
            String line = reader.nextLine();
            System.out.println("Received line: " + line);
            String[] splitLine = line.split(":");

            if (Objects.equals(splitLine[0], "START")) {
                int selectedTable = Integer.parseInt(splitLine[1]);
                tableList = buildTableList(selectedTable);

                numberCorrect = 0;
                mistakes = 0;
                startTime = System.nanoTime();
            }
            else {
                int guess = Integer.parseInt(line);
                int expected = tableList.get(numberCorrect);
                if (guess == expected) {
                    numberCorrect++;
                    if (numberCorrect == tableList.size()){
                        double endTime = System.nanoTime();
                        double seconds = (endTime - startTime) / 1_000_000_000.0;
                        double score = seconds + (mistakes * 5.0);

                        writer.println("DONE:" + String.format(java.util.Locale.US, "%.2f", seconds));
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

    private List<Integer> buildTableList(int table) {
        List<Integer> res = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            res.add(table * i);
        }
        return res;
    }
}
