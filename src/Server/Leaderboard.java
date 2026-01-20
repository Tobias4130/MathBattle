package Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;


public class Leaderboard extends Connection{

    File vendespilData;
    File tabelKrigData;
    File simpleRegnData;
    File selectedFile;
    File monkeyRaceData;

    public Leaderboard(Socket connection){
        super(connection);
        try {
            vendespilData = new File("src/Server/Database/Vendespil.csv");
        } catch (Exception e) {
            System.out.println("Could not find file Vendespil.csv");
        }
        try {
            tabelKrigData = new File("src/Server/Database/TabelKrig.csv");
        } catch (Exception e) {
            System.out.println("Could not find file TabelKrig.csv");
        }
        try {
            simpleRegnData = new File("src/Server/Database/simpleRegn.csv");
        } catch (Exception e) {
            System.out.println("Could not find file simpleRegn.csv");
        }
        try {
            monkeyRaceData = new File("src/Server/Database/monkeyRace.csv");
        } catch (Exception e) {
            System.out.println("Could not find file simpleRegn.csv");
        }
    }

    public Leaderboard(){
        super();
        try {
            vendespilData = new File("src/Server/Database/Vendespil.csv");
        } catch (Exception e) {
            System.out.println("Could not find file Vendespil.csv");
        }
        try {
            tabelKrigData = new File("src/Server/Database/TabelKrig.csv");
        } catch (Exception e) {
            System.out.println("Could not find file TabelKrig.csv");
        }
        try {
            simpleRegnData = new File("src/Server/Database/simpleRegn.csv");
        } catch (Exception e) {
            System.out.println("Could not find file simpleRegn.csv");
        }
        try {
            monkeyRaceData = new File("src/Server/Database/monkeyRace.csv");
        } catch (Exception e) {
            System.out.println("Could not find file simpleRegn.csv");
        }
    }

    public void addResult(String gameName, String user, double score) {
        setSelectedFile(gameName);
        HashMap<String, Double> map = new HashMap<>();

        if (selectedFile.exists()) {
            try (Scanner reader = new Scanner(selectedFile)) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    String[] lineArray = line.split(",");
                    String tempUser = lineArray[0];
                    String tempScore = lineArray[1];
                    map.put(tempUser, Double.valueOf(tempScore));
                }
            } catch (FileNotFoundException ex) {
                System.out.println("Now we cry"+ex.getMessage());
            }
        }

        map.merge(user, score, Math::min);

        map = sortMap(map);

        try (PrintWriter writer = new PrintWriter(selectedFile)) {
            for (String key : map.keySet()) {
                writer.println(key + "," + map.get(key));
            }
            writer.flush();
        } catch (FileNotFoundException ex) {
            System.out.println("Now we cry"+ex.getMessage());
        }
    }

    public void setSelectedFile(String gameName){
        switch (gameName){
            case "Vendespil":
                selectedFile = vendespilData;
                break;

            case "TabelKrig":
                selectedFile = tabelKrigData;
                break;

            case "simpleRegnestyk":
                selectedFile = simpleRegnData;
                break;

            case "MonkeyRace":
                selectedFile = monkeyRaceData;
                break;
        }
    }

    private HashMap<String, Double> loadDataToMap(Scanner dataReader){
        HashMap<String, Double> map = new HashMap<>();

        while (dataReader.hasNextLine()) {
            String line = dataReader.nextLine();
            String[] lineArray = line.split(",");
            String tempUser = lineArray[0];
            String tempScore = lineArray[1];
            map.put(tempUser, Double.valueOf(tempScore));
        }
        return map;
    }

    public String getGlobalTop10Leaderboard(){
        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, Double> totalScores = new HashMap<>();

        try (Scanner vendeReader = new Scanner(vendespilData); Scanner tabelReader = new Scanner(tabelKrigData);Scanner regnReader = new Scanner(simpleRegnData);Scanner monkeyReader = new Scanner(monkeyRaceData);) {

            HashMap<String, Double> vendespilScores = loadDataToMap(vendeReader);
            HashMap<String, Double> tabelKrigScores = loadDataToMap(tabelReader);
            HashMap<String, Double> regnstykScores = loadDataToMap(regnReader);
            HashMap<String, Double> monkeyScores = loadDataToMap(monkeyReader);

            for (String key : vendespilScores.keySet()) {
                Double score1 = vendespilScores.get(key);
                Double score2 = tabelKrigScores.get(key);
                Double score3 = regnstykScores.get(key);
                Double score4 = monkeyScores.get(key);
                System.out.println(key + " " + score1 + " " + score2 + " " + score3 + " " + score4);

                if (score1 != null && score2 != null && score3 != null && score4 != null) {
                    double roundScore = (double) Math.round((score1 + score2 + score3 + score4) * 100) / 100;
                    totalScores.put(key, roundScore);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Now we cry"+ex.getMessage());
        }
        totalScores = sortMap(totalScores);
        for (String key : totalScores.keySet()) {
            stringBuilder.append(";").append(key).append(",").append(totalScores.get(key));
        }
        return stringBuilder.toString();
    }

    public String getSpecificTop10Leaderboard(String gameName){
        setSelectedFile(gameName);
        StringBuilder result = new StringBuilder();
        try (Scanner fileReader = new Scanner(selectedFile)) {
            for (int i = 0; i < 10; i++) {
                if (fileReader.hasNextLine()) {
                    result.append(";").append(fileReader.nextLine());
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Now we cry"+ex.getMessage());
        }
        return result.toString();
    }

    HashMap<String, Double> sortMap(HashMap<String, Double> map) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(map.entrySet());

        list.sort(Map.Entry.comparingByValue());

        HashMap<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    @Override
    public void run() {
        while(reader.hasNextLine()) {
            String line = reader.nextLine();
            System.out.println(line);
            String gameName = line.split(":")[0];
            if (Objects.equals(gameName, "Vendespil") || Objects.equals(gameName, "TabelKrig")  || Objects.equals(gameName, "simpleRegnestyk") || Objects.equals(gameName, "MonkeyRace")) {
                writer.println(gameName + getSpecificTop10Leaderboard(gameName));
            } else if (Objects.equals(gameName, "Total")) {
                writer.println("Total"+getGlobalTop10Leaderboard());
            }
        }
    }
}
