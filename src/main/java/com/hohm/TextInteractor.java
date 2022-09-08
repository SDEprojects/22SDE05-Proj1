package com.hohm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hohm.models.MemeRoom;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.hohm.GameInit.player;

public class TextInteractor {


    public static void go(String input, MemeRoom currentRoom) {

        String[] rooms = GameInit.rooms.keySet().toArray(new String[0]);
        String room = String.join(" ",rooms);

        List<String> roomArr = Arrays.stream(room.split(" ")).map(String::toLowerCase).collect(Collectors.toList());
        Set<String> set = Arrays.stream(input.split(" ")).map(String::toLowerCase).collect(Collectors.toCollection(LinkedHashSet::new));

        set.retainAll(roomArr);
        set.retainAll(Arrays.asList(currentRoom.getExit()));

        if(set.iterator().hasNext()){
            player.setRoom(set.iterator().next());
            printSeparator();
        }else{
            printSeparator();
            System.out.println("INVALID DIRECTION: Try typing 'WHERE AM I' for a list of valid exits\n");
        }
    }

    public static void look(String input, MemeRoom currentRoom) throws IOException {
        String[] currentItems = player.getItems();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        JsonNode dialogue = Generator.parse(classLoader.getResourceAsStream("dialogue.json"));

        try{
            String[] currentRoomItems = currentRoom.getItems().keySet().toArray(new String[0]);
            for(String item: currentRoomItems){
                if(input.contains(item)){
                    printSeparator();
                    System.out.println(currentRoom.getItems().get(item).get("description"));
                }
            }
        }catch (NullPointerException e){
            printSeparator();
            //Do Nothing if there is a null pointer exception
        }
        try {
            if (input.contains("doge")) {
                if (currentRoom.getTitle().equals("kitchen")) {
                    ArrayNode returnDialogue = (ArrayNode) dialogue.get("doge").get("dialogue");
                }
            } else if (input.contains("kermit")) {
                if (currentRoom.getTitle().equals("diningroom")) {
                    ArrayNode returnDialogue = (ArrayNode) dialogue.get("kermit").get("dialogue");
                }
            } else if (input.contains("cat")) {
                if (currentRoom.getTitle().equals("living room")) {
                    ArrayNode returnDialogue = (ArrayNode) dialogue.get("grumpycat").get("dialogue");
                }
            } else {
                System.out.println("There's no one of that name here.");
            }
        } catch (NullPointerException e) {
            System.out.println("You can't look that person...");
        }

        if (input.contains("room")) {
            if (player.getRoom().equals("hallway") || player.getRoom().equals("basement")) {
                //Do nothing, description is handled in the start of the game loop
            } else if (currentRoom.getComplete()) {
                printSeparator();
                System.out.println(currentRoom.getDescription().get("memeComplete"));
            } else if (currentRoom.getObjectives().get("check complete").get("complete").equals("false")) {
                printSeparator();
                System.out.println(currentRoom.getObjectives().get("check complete").get("incomplete"));
            } else if (currentRoom.getObjectives().get("itemFound").get("complete").equals("false")
                    && currentRoom.getObjectives().get("clueFound").get("complete").equals("false")) {
                printSeparator();
                System.out.printf("%s%n%s%n",
                        currentRoom.getObjectives().get("clueFound").get("incomplete"),
                        currentRoom.getObjectives().get("itemFound").get("incomplete"));
            } else if (currentRoom.getObjectives().get("itemFound").get("complete").equals("true")
                    && currentRoom.getObjectives().get("clueFound").get("complete").equals("false")) {
                printSeparator();
                System.out.println(currentRoom.getObjectives().get("clueFound").get("incomplete"));
            } else {
                printSeparator();
                System.out.println(currentRoom.getObjectives().get("itemFound").get("incomplete"));
            }

        }
    }

    public static void get(String input, MemeRoom currentRoom) {

        String[] key = input.split(" ", 2);
        try {
            if (currentRoom.getItems().containsKey(key[1])) {
                String objective = currentRoom.getItems().get(key[1]).get("prereq");
                boolean objComplete = Boolean.parseBoolean(currentRoom.getObjectives().get(objective).get("complete"));
                String objType = currentRoom.getItems().get(key[1]).get("type");

                if (objComplete && objType.equals("misc")) {
                    String[] items = {key[1]};
                    player.setItems(items);
                    GameInit.rooms.get(currentRoom.getTitle()).getObjectives().get("itemFound").put("complete", "true");
                    checkComplete(currentRoom);
                    printSeparator();
                    System.out.println(currentRoom.getItems().get(key[1]).get("prereqMet"));
                    System.out.println("You now have: " + Arrays.toString(player.getItems()).replaceAll("[\\[\\](){}\"]", ""));
                }
                else if (objComplete && objType.equals("clue")) {
                    if(GameInit.rooms.get(currentRoom.getTitle()).getObjectives().get("clueFound").get("complete").equals("false")){
                        player.incrementClues();
                        GameInit.rooms.get(currentRoom.getTitle()).getObjectives().get("clueFound").put("complete", "true");
                    }
                    checkComplete(currentRoom);
                    printSeparator();
                    System.out.println(currentRoom.getItems().get(key[1]).get("prereqMet"));
                } else {
                    printSeparator();
                    System.out.println(currentRoom.getItems().get(key[1]).get("prereqNotMet"));
                    player.setRoom("dead");
                }
            } else {
                printSeparator();
                System.out.printf("You are unable to get the %s... whatever that is%n%n", key[1]);
            }
        } catch (NullPointerException e) {
            printSeparator();
            System.out.printf("You are unable to get the %s... whatever that is%n%n", key[1]);
        }

    }

    public static void use(String input, MemeRoom currentRoom) {
        String[] key = input.split(" ", 2);
        try {
            String chkObj = currentRoom.getObjectives().get("check complete").get("useItem");
            if (Arrays.asList(player.getItems()).contains(key[1]) && chkObj.equals(player.getItems()[0])) {
                currentRoom.getObjectives().get("check complete").put("complete", String.valueOf(true));
                String[] temp = {"[]"};
                player.setItems(temp);
                printSeparator();
                System.out.println(currentRoom.getObjectives().get("check complete").get("completed"));

            } else if (Arrays.asList(player.getItems()).contains(key[1])) {
                printSeparator();
                System.out.printf("That's a nice thought to use the %s... won't do anything..%n", key[1]);
            } else {
                printSeparator();
                System.out.printf("Might be nice to use the %s, but... you don't even have that!%n", key[1]);
            }
        } catch (NullPointerException e) {
            printSeparator();
            System.out.println("You can't use that here...");
        }
    }

    public static void talk(String input, MemeRoom currentRoom) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        JsonNode dialogue = Generator.parse(classLoader.getResourceAsStream("dialogue.json"));
        int random = (int) (Math.random() * 3);
        try {
            if (input.contains("doge")) {
                if (currentRoom.getTitle().equals("kitchen")) {
                    ArrayNode returnDialogue = (ArrayNode) dialogue.get("doge").get("dialogue");
                    System.out.println(returnDialogue.get(random));
                }
            } else if (input.contains("kermit")) {
                if (currentRoom.getTitle().equals("diningroom")) {
                    ArrayNode returnDialogue = (ArrayNode) dialogue.get("kermit").get("dialogue");
                    System.out.println(returnDialogue.get(random));
                }
            } else if (input.contains("cat")) {
                if (currentRoom.getTitle().equals("living room")) {
                    ArrayNode returnDialogue = (ArrayNode) dialogue.get("grumpycat").get("dialogue");
                    System.out.println(returnDialogue.get(random));
                }
            } else {
                System.out.println("There's no one to talk to here.");
            }
        } catch (NullPointerException e) {
            System.out.println("You can't talk to that person...");
        }
    }
    public static void description(MemeRoom currentRoom){
        if (currentRoom.getTitle().equals("hallway")) {
            String[] currentItem = player.getItems();
            if (Objects.equals(currentItem[0], "[]")) {
                System.out.println(currentRoom.getDescription().get("nullHallway"));
            } else {
                System.out.println(currentRoom.getDescription().get(currentItem[0]));
            }
        }
        else {
                if(currentRoom.getComplete()){
                    System.out.println(currentRoom.getDescription().get("memeComplete"));
                }
                else if (currentRoom.getObjectives().get("check complete").get("complete").equals("true")) {
                    System.out.println(currentRoom.getObjectives().get("clueFound").get("incomplete"));
                } else {
                    System.out.println(currentRoom.getDescription().get("memeIncomplete"));
                }
        }
    }
    public static void checkComplete(MemeRoom currentRoom) {
        Map<String, Map<String, String>> objectives = GameInit.rooms.get(currentRoom.getTitle()).getObjectives();
        if (objectives.get("itemFound").get("complete").equals("true") && objectives.get("clueFound").get("complete").equals("true")) {
            GameInit.rooms.get(currentRoom.getTitle()).setComplete(true);
        }
    }

    public static void printSeparator() {
        String printRoom = player.getRoom();

        if(player.getRoom().equals("living") || player.getRoom().equals("dining")){
            printRoom = player.getRoom() + " room";
        }

        ClearScreen.ClearConsole();
        String dash = "- - ".repeat(29);
        String printSeparator = String.format("Current Room: %s %20sInventory: %s %20sClues Found: %s"
                , printRoom.toUpperCase()
                , "", player.getItems()[0].toUpperCase()
                , "", player.getClues());
        System.out.println(dash);
        System.out.println(printSeparator);
        System.out.println(dash);
    }
}
