import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RoomServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(Helper.RoomServerPort);
        ArrayList<Room> Rooms = new ArrayList<>();
        System.out.println("RoomServer is running...");

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();
            // Create a BufferedReader to read from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Read the request line
            String requestLine = in.readLine();
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
            tokens[1] = tokens[1].substring(0, tokens[1].indexOf(" "));
            //initialize a string array with name pairs with one element with tokens[1]
            String[] pairs = { tokens[1] };
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
                            if (parametersMap.containsKey("name") || parametersMap.containsKey("day")) {
                                //if room exists, send HTTP 403 Forbidden message indicating that the room already exists
                                if (roomExists(parametersMap.get("name"), Rooms)) {
                                    // Send the response
                                    Helper.printHtmlMessage("403", "The room already exists", out);
                                } else {
                                    //get name and day from parametersMap
                                    String name = parametersMap.get("name");
                                    //create new room and add to Rooms
                                    addRoom(name, Rooms);
                                    // Send the response
                                    Helper.printHtmlMessage("200", "The room added succesfully", out);
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
                                if (roomExists(parametersMap.get("name"), Rooms)) {
                                    String name = parametersMap.get("name");
                                    removeRoom(name, Rooms);
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

                                String name = parametersMap.get("name");
                                Room room = Helper.findRoomByName(name, Rooms);

                                int day = Integer.parseInt(parametersMap.get("day"));
                                int hour = Integer.parseInt(parametersMap.get("hour"));
                                int duration = Integer.parseInt(parametersMap.get("duration"));

                                //if room doesn't exist, send HTTP 404 Not Found message indicating that the room doesn't exist
                                if(!roomExists(name, Rooms)) {
                                    Helper.printHtmlMessage("404", "The room doesn't exist", out);
                                }
                                //if room exists, check if it's available
                                else if(!isAvailable(room, day, hour, duration)) {
                                    Helper.printHtmlMessage("403", "The room is already reserved for specified hours", out);
                                }
                                //if room is available, reserve it
                                else {
                                    reserveRoom(name, day, hour, duration, Rooms);
                                    Helper.printHtmlMessage("200", "The room reserved successfully", out);
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
                            if (parametersMap.containsKey("name") || parametersMap.containsKey("day")) {
                                //check if room exists
                                if (roomExists(parametersMap.get("name"), Rooms)) {
                                    String name = parametersMap.get("name");
                                    //initialize room using findroombyname
                                    Room room = Helper.findRoomByName(name, Rooms);

                                    int day = Integer.parseInt(parametersMap.get("day"));
                                    ArrayList<Integer> availableHours = checkAvailability(room, day);
                                    //convert availableHours to string seperated by " "
                                    String availableHoursString = "";
                                    for (int i = 0; i < availableHours.size(); i++) {
                                        availableHoursString += availableHours.get(i);
                                        if (i != availableHours.size() - 1) {
                                            availableHoursString += " ";
                                        }
                                    }
                                    // Send the response
                                    Helper.printHtmlMessage("200", availableHoursString, out);
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
                            //endregion
                    }
                }
                //endregion
            }
            clientSocket.close();
        }
    }

    private static boolean roomExists(String name, ArrayList<Room> rooms) {
        for (Room room : rooms) {
            if (room.Name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void addRoom(String name, ArrayList<Room> Rooms) {
        Room room = new Room(name, 0);
        Rooms.add(room);
    }
    public static void removeRoom(String name, ArrayList<Room> Rooms) {
        for (Room room : Rooms) {
            if (room.Name.equals(name)) {
                Rooms.remove(room);
            }
        }
    }


    //reserve room with name and day and hour and duration
    public static void reserveRoom(String name, int day, int hour, int duration, ArrayList<Room> Rooms) {
        for (Room room : Rooms) {
            if (room.Name.equals(name)) {
                room.Day = day;
            }
        }
    }
    //checkAvailability method with roomname and day, returns back all available hours for specified day in the body of html message. If no such room exists it sends back an HTTP 404 Not Found message,
    //or if x is not a valid input then it sends back an HTTP 400 Bad Request message.

    //A method which takes room and day and returns all available hours for specified day
    public static ArrayList<Integer> checkAvailability(Room room, int day) {
        ArrayList<Integer> availableHours = new ArrayList<>();
        //find availableHours for room

        //return a tuple which consists of availablehours and roomExists
        return availableHours;

    }
    //A method which takes room, day, hour and duration and returns true if room is not available for specified time and durations
    public static boolean isAvailable(Room room, int day, int hour, int duration) {
        ArrayList<Integer> availableHours = checkAvailability(room, day);
        //return true if room has available hours for the specified day by checking if availableHours any hour between contains hour and hour + duration
        for (int i = hour; i < hour + duration; i++) {
            if (!availableHours.contains(i)) {
                return false;
            }
        }
        return true;
    }



}
//Helper.printHtmlMessage("404", "The room does not exist", out);