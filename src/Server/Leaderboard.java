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

        map.merge(user, score, Math::max);

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
        }
    }

    public String getGlobalTop10Leaderboard(){
        return "";
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
        System.out.println("Thread Started");
        while(reader.hasNextLine()) {
            System.out.println("We got here");
            String line = reader.nextLine();
            System.out.println(line);
            String gameName = line.split(":")[0];
            if (Objects.equals(gameName, "Vendespil") || Objects.equals(gameName, "TabelKrig")  || Objects.equals(gameName, "simpleRegnestyk")) {
                writer.println(gameName + getSpecificTop10Leaderboard(gameName));
            } else if (Objects.equals(gameName, "Total")) {
                writer.println("Total"+getGlobalTop10Leaderboard());
            }
        }
    }
}
