package flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static LinkedHashMap<String, String> myMap = new LinkedHashMap<>();
    private static boolean wantToChoose = true;
    private static ArrayList<String> logArrList = new ArrayList<>();
    private static HashMap<String, Integer> mistakesMap = new HashMap<String, Integer>();
    private static Map<String, String> cmdMap = new HashMap<>();
    private static String pathToFile = null;
    private static String pathToSave = null;
    private static String globalAction = null;




    public static void main(String[] args) throws FileNotFoundException {

        saveMainArrgs(args);

        importBeforeAction();

        while (wantToChoose) {
            chooseAction();
        }

    }

    private static void importBeforeAction() {
        if (cmdMap.containsKey("-import")) {
            pathToFile = cmdMap.get("-import");
            importCards();
        }
    }

    private static void saveMainArrgs (String[] args) {
        String key = null;
        String value = null;
        for (int i = 0; i < args.length - 1; i+=2) {
            key = args[i];
            value = args[i + 1];
            cmdMap.put(key, value);
        }
    }

    private static void chooseAction() {


        String actionString = "Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):";
        printLog(actionString);
        String action = userInput(scanner.nextLine().trim());
        globalAction = action;

        if ("add".equals(action)) {
            add();
        } else if ("remove".equals(action)) {
            remove();
        } else if ("import".equals(action)) {
            importCards();
        } else if ("export".equals(action)) {
            exportCards();
        } else if ("ask".equals(action)){
            ask();
        } else if ("log".equals(action)) {
            log();
        } else if ("hardest card".equals(action)) {
            hardestCard();
        } else if ("reset stats".equals(action)) {
            resetCard();
        } else if ("exit".equals(action)){
            printLog("Bye bye!");
            saveBeforeExitIfArrgs();
            wantToChoose = false;
        } else {
            printLog("Typo");
        }
        System.out.println();
    }

    public static void saveBeforeExitIfArrgs() {
        if (cmdMap.containsKey("-export")) {
            pathToSave = cmdMap.get("-export");
            if (myMap.size() > 0) {
                exportCards();
            }
        }
    }

    private static void add() {
        printLog("The card:");
        String nameOfCards = userInput(scanner.nextLine());
        boolean isKey = true;
        if (myMap.containsKey(nameOfCards)) {
            printLog("The card \"" + nameOfCards + "\" already exists.");
            isKey = false;
        }
        boolean isValue = true;
        if (isKey) {
            printLog("The definition of the card:");
            String valueOfCards = userInput(scanner.nextLine());

            if (myMap.containsValue(valueOfCards)) {
                printLog("The definition \"" + valueOfCards + "\" already exists.");
                isValue = false;
            }
            if (isValue) {
                myMap.put(nameOfCards, valueOfCards);
                printLog("The pair (\"" + nameOfCards + "\"" + ":" + "\"" + valueOfCards + "\") has been added");
            }

        }
    }


    private static void remove() {
        printLog("The card:");
        String removeCard = userInput(scanner.nextLine());
        if (myMap.containsKey(removeCard)) {
            myMap.remove(removeCard);
            printLog("The card has been removed.");
            mistakesMap.remove(removeCard);
        } else {
            printLog("Can't remove \"" + removeCard + "\": there is no such card.");
        }
    }

    private static void importCards() {
        if (pathToFile == null) {
            printLog("File name:");
            pathToFile = userInput(scanner.nextLine());
        }
        File file = new File(pathToFile);
        int countOfImportCard = 0;

        try (Scanner sc = new Scanner(file)){
            while (sc.hasNextLine()) {
                String[] stringFromImport = sc.nextLine().split(":");
                myMap.put(stringFromImport[0].trim(), stringFromImport[1].trim());
                if (stringFromImport.length > 2) {
                    mistakesMap.put(stringFromImport[0].trim(), Integer.parseInt(stringFromImport[2].trim()));
                }

                countOfImportCard++;
            }
            printLog(countOfImportCard + " cards have been loaded.");
        } catch (FileNotFoundException ex) {
            printLog("File not found.");
        }

    }


    private static void exportCards() {
        if ("exit".equals(globalAction)) {
            pathToSave = cmdMap.get("-export");
        } else {
            printLog("File name:");
            pathToSave = userInput(scanner.nextLine());
        }
        File file = new File(pathToSave);
        pathToSave = null;
        int countOfExport = 0;
        try (PrintWriter printWriter = new PrintWriter(file)){
            for (Map.Entry<String, String> map : myMap.entrySet()) {

                if (mistakesMap.containsKey(map.getKey())) {
                    printWriter.printf("%s : %s : %s\n", map.getKey(), map.getValue(), mistakesMap.get(map.getKey()));
                } else {
                    printWriter.printf("%s : %s\n", map.getKey(), map.getValue());
                }
                countOfExport++;
            }

            printLog(countOfExport +  " cards have been saved.");
            myMap.clear();
        } catch (IOException es) {
            printLog("Errors occur");
        }
    }

    public static void ask() {
        printLog("How many time to ask?");
        int howManyTimeToAsk = Integer.parseInt(userInput(scanner.nextLine()));

        Object[] arraysOfKeys = myMap.keySet().toArray();
        int arraySize = arraysOfKeys.length;

        String askAnswer;
        Random random = new Random();
        for (int i = 0; i < howManyTimeToAsk ; i++) {
            int randomNumberInArray = random.nextInt(arraySize);
            String randomKey = (String) arraysOfKeys[randomNumberInArray];

            printLog("Print the definition of \"" + randomKey + "\":");
            askAnswer = userInput(scanner.nextLine());


            if (askAnswer.equals(myMap.get(randomKey))) {
                printLog("Correct answer.");
            } else {
                if (myMap.containsValue(askAnswer)) {
                    printLog("Wrong answer. The correct one is \"" + myMap.get(randomKey)
                            + "\", you've just written the definition of \"" + getKeyFromValue(myMap, askAnswer) + "\"");

                } else {
                    printLog("Wrong answer. The correct one is \"" + myMap.get(randomKey) + "\"");
                }

                if (mistakesMap.containsKey(randomKey)) {
                    mistakesMap.put(randomKey, mistakesMap.get(randomKey) + 1);
                } else {
                    mistakesMap.put(randomKey, 1);
                }

            }
        }
    }


    public static void log(){
        printLog("File name:");
        String nameOFLogFile = userInput(scanner.nextLine());
        File file = new File(nameOFLogFile);
        try (PrintWriter thePrintWriter = new PrintWriter(file)){
            for (String str : logArrList) {
                thePrintWriter.println(str);
            }
            printLog("The log has been saved.");
        } catch (IOException ex) {
            printLog("Errors occur");
        }
    }

    public static void hardestCard() {
        if (!mistakesMap.isEmpty()) {

            Integer max = Collections.max(mistakesMap.values());

            ArrayList<String> arrayOfHardestCards = new ArrayList<>();
            String output = "The hardest ";
            StringBuilder sb = new StringBuilder(output);

            if (mistakesMap.size() == 1) {
                startString(max, sb);
            } else if (mistakesMap.size() > 1) {
                for (Map.Entry<String, Integer> entry : mistakesMap.entrySet()) {
                    if (entry.getValue().equals(max)) {
                        arrayOfHardestCards.add(entry.getKey());
                    }
                }
                if (arrayOfHardestCards.size() > 1) {
                    sb.append("cards are: ");
                    for (int i = 0; i < arrayOfHardestCards.size(); i++) {
                        sb.append("\"");
                        sb.append(arrayOfHardestCards.get(i));
                        sb.append("\"").append(", ");
                    }
                    String stringWithLastCommaReplace = sb.toString().replaceAll(", $", ".");
                    printLog(stringWithLastCommaReplace + " You have " + mistakesMap.get(getKeyFromValueInt(mistakesMap, max))
                                    + " to errors answering them.");

                } else {
                    startString(max, sb);
                }
            }
        } else {
            printLog("There are no cards with errors.");
        }

    }

    public static void resetCard() {
        mistakesMap.clear();
        printLog("Card statistics has been reset.");
    }
    private static void startString(Integer max, StringBuilder sb) {
        sb.append("card is ");
        sb.append("\"").append(getKeyFromValueInt(mistakesMap, max));
        sb.append("\". You have ").append(mistakesMap.get(getKeyFromValueInt(mistakesMap, max)));
        sb.append(" error answering it.");
        printLog(sb.toString());
    }

    public static void printLog(String str) {
        System.out.println(str);
        logArrList.add(str);
    }

    public static String userInput(String str) {
        logArrList.add(str + "\n");
        return str;
    }

    // method to get the key String from the value string
    public static String getKeyFromValue(Map<String, String> map, String value){
        for (String key : map.keySet()) {
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        logArrList.add("unknown");
        return "unknown";
    }
    // method to get the key String from the value Integer
    public static String getKeyFromValueInt(Map<String, Integer> map, Integer value){
        for (String key : map.keySet()) {
            if (map.get(key).equals(value)) {
                return key;
            }
        }
        logArrList.add("unknown");
        return "unknown";
    }
}
