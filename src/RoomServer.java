import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;


public class RoomServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(Helper.RoomServerPort);
        System.out.println("RoomServer is running...");

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();
            // Create a BufferedReader to read from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Read the request line
            String requestLine = in.readLine();
            if(requestLine.contains("favicon.ico")){
                continue;
            }
            Map<String, String> parameters = new HashMap<>();
            Map<String, Map<String, String>> requests = new HashMap<>();
            String[] tokens = requestLine.split("\\?");
            String getAndMethod = tokens[0];
            //tokenize method by ' /' char and set method to last token
            String[] getAndMethodTokenized = getAndMethod.split(" /");
            String method = getAndMethodTokenized[getAndMethodTokenized.length - 1];
            String get = getAndMethodTokenized[0];

            //region Get all requests and store in requests
            // Split the query string into name-value pairs
            //if there are parameters
            if (tokens.length > 1) {
                tokens[1] = tokens[1].substring(0, tokens[1].indexOf(" "));
                //initialize a string array with name pairs with one element with tokens[1]
                String[] pairs = {tokens[1]};
                if (tokens[1].contains("&")) {
                    pairs = tokens[1].split("&");
                }

                for (String pair : pairs) {
                    int equalsIndex = pair.indexOf("=");
                    String name = pair.substring(0, equalsIndex);
                    String value = pair.substring(equalsIndex + 1);
                    parameters.put(name, value);
                    requests.put(method, parameters);
                }
            }
            //Get the method if there are no parameters
            else if (tokens.length == 1){
                method = method.substring(0, method.indexOf(" "));
                requests.put(method, parameters);
            }
            else {
                break;
            }

            //endregion

            String[] parts = requestLine.split(" ");
            // Check if the request is a GET request
            if (parts[0].equals("GET")) {
                //region Iterate for each request
                for (Map.Entry<String, Map<String, String>> entry : requests.entrySet()) {
                    //get parameters map
                    Map<String, String> parametersMap = entry.getValue();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                    //check if method is add
                    switch (method) {
                        //region case:Add
                        case "add":
                            //check if inputs are valid
                            if (parametersMap.containsKey("name")) {
                                //if room exists, send HTTP 403 Forbidden message indicating that the room already exists
                                if (roomExists(parametersMap.get("name"))) {
                                    // Send the response
                                    Helper.printHtmlMessage("403", "The room already exists", out);
                                } else {
                                    //get name and day from parametersMap
                                    String name = parametersMap.get("name");
                                    //create new room and add to Rooms
                                    addRoom(name);
                                    // Send the response
                                    Helper.printHtmlMessage("200", "Room " + name + " added succesfully", out);
                                }
                            }
                            else {
                                //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //region case:Remove
                        case "remove":
                            //check if inputs are valid
                            if (parametersMap.containsKey("name")) {
                                //if room exists, remove it
                                if (roomExists(parametersMap.get("name"))) {
                                    String name = parametersMap.get("name");
                                    removeRoom(name);
                                    Helper.printHtmlMessage("200", "The room removed successfully", out);
                                }
                                //if room doesn't exist, send HTTP 403 Forbidden message indicating that the room doesn't exist
                                else {
                                    Helper.printHtmlMessage("403", "The room does not exist", out);
                                }
                            }
                            else {
                                //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //region case:Reserve
                        case "reserve":
                            //check if inputs are valid
                            if (parametersMap.containsKey("name")
                                    && parametersMap.containsKey("day")
                                    && parametersMap.containsKey("hour")
                                    && parametersMap.containsKey("duration")) {

                                //region Invalid Input Error Messages
                                //if day hour and duration is not numeric
                                if (!Helper.isNumeric(parametersMap.get("day"))
                                        || !Helper.isNumeric(parametersMap.get("hour"))
                                        || !Helper.isNumeric(parametersMap.get("duration"))) {
                                    Helper.printHtmlMessage("400", "Error: Day, hour and duration must be numbers", out);
                                    break;
                                }
                                //if day is not between 1 and 7
                                else if (Integer.parseInt(parametersMap.get("day")) < 1
                                        || Integer.parseInt(parametersMap.get("day")) > 7) {
                                    Helper.printHtmlMessage("400", "Error: Day must be between 1 and 7", out);
                                    break;
                                }
                                //if hour is not between 9 and 17
                                else if (Integer.parseInt(parametersMap.get("hour")) < 9
                                        || Integer.parseInt(parametersMap.get("hour")) > 17) {
                                    Helper.printHtmlMessage("400", "Error: Hour must be between 9 and 17", out);
                                    break;
                                }
                                //if hour+duration is greater than 17
                                else if (Integer.parseInt(parametersMap.get("hour")) + Integer.parseInt(parametersMap.get("duration")) > 17) {
                                    Helper.printHtmlMessage("400", "Error: You can't reserve a room after 17", out);
                                    break;
                                }
                                //endregion

                                //get parameters
                                String name = parametersMap.get("name");
                                int day = Integer.parseInt(parametersMap.get("day"));
                                int hour = Integer.parseInt(parametersMap.get("hour"));
                                int duration = Integer.parseInt(parametersMap.get("duration"));

                                //if room doesn't exist, send HTTP 404 Not Found message indicating that the room doesn't exist
                                if(!roomExists(name)) {
                                    Helper.printHtmlMessage("404", "The room doesn't exist", out);
                                }
                                //if room exists, check if it's available
                                else if(!isAvailable(name, day, hour, duration)) {
                                    Helper.printHtmlMessage("403", "The room is already reserved for specified hours", out);
                                }
                                //if room is available, reserve it
                                else {
                                    int id = reserveRoom(name, day, hour, duration);
                                    Helper.printHtmlMessage("200", "The room " + name + " reserved successfully on " +
                                             Helper.convertDay(day) + " at " + Helper.convertDuration(hour, duration) + ", with Id:" + id + " ", out);
                                }
                            }
                            //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                            else {
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //region case:CheckAvailability
                        case "checkavailability":
                            //check if inputs are valid
                            if (parametersMap.containsKey("name") && parametersMap.containsKey("day")) {
                                //if day is not numeric, send HTTP 400 Bad Request message indicating that the day is not numeric
                                if (!Helper.isNumeric(parametersMap.get("day"))) {
                                    Helper.printHtmlMessage("400", "Error: Day must be a number", out);
                                    break;
                                }
                                //if day is not between 1 and 7
                                else if (Integer.parseInt(parametersMap.get("day")) < 1
                                        || Integer.parseInt(parametersMap.get("day")) > 7) {
                                    Helper.printHtmlMessage("400", "Error: Day must be between 1 and 7", out);
                                    break;
                                }
                                //check if room exists
                                if (roomExists(parametersMap.get("name"))) {
                                    String name = parametersMap.get("name");
                                    int day = Integer.parseInt(parametersMap.get("day"));
                                    ArrayList<Integer> availableHours = checkAvailability(name, day);
                                    String availableHoursString = "";
                                    //generate available hours string
                                    for (int i = 0; i < availableHours.size(); i++) {
                                        availableHoursString += availableHours.get(i);
                                        if (i != availableHours.size() - 1) {
                                            availableHoursString += " ";
                                        }
                                    }
                                    // Send the response
                                    Helper.printHtmlMessage("200", "Available hours on " + Helper.convertDay(day) + " for " + name + ": " + availableHoursString, out);
                                }
                                //if room doesn't exist, send HTTP 404 Not Found message indicating that the room doesn't exist
                                else {
                                    Helper.printHtmlMessage("404", "The room does not exist", out);
                                }

                            }
                            //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                            else {
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;

                        default:
                            Helper.printHtmlMessage("400", "You entered invalid method name", out);
                            break;
                            //endregion
                    }
                }
                //endregion
            }
            clientSocket.close();
        }
    }

    //reserve room with name and day and hour and duration
    public static int reserveRoom(String roomName, int day, int hour, int duration) {

        int lineCount = 0;
        //region Read src/db/Reservations.txt and count lines
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Helper.ReservationsPath));
            while (reader.readLine() != null) lineCount++;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion
        int id = lineCount + 1;
        String reservationString = id + " " + roomName + " " + day + " " + hour + " " + duration;
        //region Append to src/db/Reservations.txt
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(Helper.ReservationsPath, true));
            writer.write(reservationString);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion
        return id;
    }
    //checkAvailability method with roomname and day, returns back all available hours for specified day in the body of html message. If no such room exists it sends back an HTTP 404 Not Found message,
    //or if x is not a valid input then it sends back an HTTP 400 Bad Request message.

    //This method reads reservations.txt, gets the reservations with the specified room name and day,
    // and returns back all available hours for specified day
    public static ArrayList<Integer> checkAvailability(String name, int day) {
        Hashtable<Integer, ArrayList<Integer>> DaysAndAvailableHours = new Hashtable<Integer, ArrayList<Integer>>();
        for(int i = 1; i <= 7; i++) {
            DaysAndAvailableHours.put(i, new ArrayList<Integer>(Arrays.asList(9, 10, 11, 12, 13, 14, 15, 16, 17)));
        }
        //read src/db/Reservations.txt, read each line and tokenize by space
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Helper.ReservationsPath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                //if room name is equal to name, add day and hour to DaysAndAvailableHours
                if (tokens[1].equals(name)) {
                    int dayFromFile = Integer.parseInt(tokens[2]);
                    int hourFromFile = Integer.parseInt(tokens[3]);
                    int durationFromFile = Integer.parseInt(tokens[4]);
                    for (int i = hourFromFile; i < hourFromFile + durationFromFile; i++) {
                        DaysAndAvailableHours.get(dayFromFile).remove(Integer.valueOf(i));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
        return DaysAndAvailableHours.get(day);
    }
    //A method which takes room, day, hour and duration and returns true if room is not available for specified time and durations
    public static boolean isAvailable(String roomName, int day, int hour, int duration) {
        ArrayList<Integer> availableHours = checkAvailability(roomName, day);
        //return true if room has available hours for the specified day by checking if availableHours any hour between contains hour and hour + duration
        for (int i = hour; i < hour + duration; i++) {
            if (!availableHours.contains(i)) {
                return false;
            }
        }
        return true;
    }
    public static boolean roomExists(String name) {
        for (String activity : getRooms()) {
            if (activity.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getRooms() {
        ArrayList<String> rooms = new ArrayList<>();
        try {
            File file = new File(Helper.RoomsPath);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                rooms.add(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return rooms;
    }


    public static void removeRoom(String name) throws IOException {
        //remove room by name from rooms.txt
        File file = new File(Helper.RoomsPath);
        File temp = new File("./_temp_");
        PrintWriter out = new PrintWriter(new FileWriter(temp));
        Files.lines(file.toPath())
                .filter(line -> !line.contains(name))
                .forEach(out::println);
        out.flush();
        out.close();
        //delete original file
        file.delete();
        //rename temp file to original file
        temp.renameTo(file);
    }

    public static void addRoom(String name) {
        //add activity name to src/db/activities.txt
        try {
            FileWriter fileWriter = new FileWriter(Helper.RoomsPath, true);
            fileWriter.write(name + "\n");
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}