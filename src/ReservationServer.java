import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ReservationServer {

    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(Helper.ReservationServerPort);
        ArrayList<Room> Rooms = new ArrayList<>();
        System.out.println("Reservation Server is running");

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
            //endregion
            //region Server Settings
            //Send to activity server
            Socket activitySocket = new Socket("localhost", Helper.ActivityServerPort);
            //get from activity server
            BufferedReader activityIn = new BufferedReader(new InputStreamReader(activitySocket.getInputStream()));
            PrintWriter activityOut = new PrintWriter(activitySocket.getOutputStream(), true);

            //Send to activity server
            Socket roomSocket = new Socket("localhost", Helper.RoomServerPort);
            //get from activity server
            BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
            PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
            //endregion

            String[] parts = requestLine.split(" ");
            // Check if the request is a GET request
            if (parts[0].equals("GET")) {
                //region Iterate for each request
                for (Map.Entry<String, Map<String, String>> entry : requests.entrySet()) {

                    Map<String, String> parametersMap = entry.getValue();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    //check if method is add
                    switch (method) {

                        //region case:Reserve
                        case "reserve":
                            //check if inputs are valid
                            if (parametersMap.containsKey("room")
                                    && parametersMap.containsKey("activity")
                                    && parametersMap.containsKey("day")
                                    && parametersMap.containsKey("hour")
                                    && parametersMap.containsKey("duration")) {

                                //region Initialize parameters
                                String name = parametersMap.get("room");
                                Room room = Helper.findRoomByName(name, Rooms);
                                String activityName = parametersMap.get("activity");
                                int day = Integer.parseInt(parametersMap.get("day"));
                                int hour = Integer.parseInt(parametersMap.get("hour"));
                                int duration = Integer.parseInt(parametersMap.get("duration"));
                                //endregion

                                //region Contact Activity Server to check if activity exists
                                activityOut.println("GET /check?name=" + activityName + " HTTP/1.1");
                                String activityResponse = activityIn.readLine();

                                if(activityResponse.equals("HTTP/1.1 404 Not Found")){
                                    Helper.printHtmlMessage("404", "Activity doesn't exist", out);
                                    break;
                                }
                                //endregion

                                //region Contact Room Server to try to reserve the room, redirect the response to outstream
                                roomOut.println("GET /reserve?name=" + name + "&day=" + day + "&hour=" + hour + "&duration=" + duration + " HTTP/1.1");
                                String roomResponse = roomIn.readLine();
                                roomIn.readLine(); roomIn.readLine();
                                String roomMessage = roomIn.readLine();
                                String status = roomResponse.split(" ")[1];
                                Helper.printHtmlMessage(status, roomMessage, out);
                                break;
                                //endregion

                            }
                            //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                            else {
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                        //endregion

                        //region case:ListAvailability /listavailability?room=roomname&day=x:
                        case "listavailability":
                            //check if inputs are valid
                            if (parametersMap.containsKey("room")
                                    && parametersMap.containsKey("day")) {

                                //region Initialize parameters
                                String name = parametersMap.get("room");
                                Room room = Helper.findRoomByName(name, Rooms);
                                int day = Integer.parseInt(parametersMap.get("day"));
                                //endregion

                                //region Contact Room Server to checkavailibility with parameters room and day
                                roomOut.println("GET /checkavailability?room=" + name + "&day=" + day + " HTTP/1.1");
                                String roomResponse = roomIn.readLine();
                                roomIn.readLine(); roomIn.readLine();
                                String roomMessage = roomIn.readLine();
                                //get substring of roomMessage between <p> and </p>
                                String roomMessageSubstring = roomMessage.substring(roomMessage.indexOf("<p>") + 3, roomMessage.indexOf("</p>"));

                                String status = roomResponse.split(" ")[1];
                                Helper.printHtmlMessage(status, roomMessageSubstring, out);
                                break;
                                //endregion

                            }
                            else if (parametersMap.containsKey("room")) {

                                //region Initialize parameters
                                String name = parametersMap.get("room");
                                Room room = Helper.findRoomByName(name, Rooms);
                                String availableHoursForEachDay = "";
                                //endregion

                                //region Contact Room Server to checkavailibility with parameters room and day
                                //do this for each day in week
                                for (int i = 1; i <= 7; i++) {
                                    roomOut.println("GET /checkavailability?room=" + name + "&day=" + i + " HTTP/1.1");
                                    String roomResponse = roomIn.readLine();
                                    roomIn.readLine(); roomIn.readLine();
                                    String roomMessage = roomIn.readLine();
                                    //get substring of roomMessage between <p> and </p>
                                    String roomMessageSubstring = roomMessage.substring(roomMessage.indexOf("<p>") + 3, roomMessage.indexOf("</p>"));

                                    String status = roomResponse.split(" ")[1];
                                    if(status.equals("200")){
                                        //for each day store available hours in availableHoursForEachDay with days
                                        availableHoursForEachDay += "Day " + i + ": " + roomMessageSubstring + "\n";
                                    }

                                }
                                Helper.printHtmlMessage("200", availableHoursForEachDay, out);
                                break;
                                //endregion
                            }
                            //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                            else {
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                        //endregion


                        //region case:Display
                        case "display":
                            //check if inputs are valid
                            if (parametersMap.containsKey("id")) {

                                //region Initialize parameters
                                String id = parametersMap.get("id");
                                //endregion

                                //region Find reservation with id and display it
                                Reservation reservation = new Reservation();

                                //endregion
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
    public static ArrayList<Integer> checkAvailability(String name, int day, ArrayList<Room> Rooms, PrintWriter out) {
        ArrayList<Integer> availableHours = new ArrayList<>();
        for (Room room : Rooms) {
            if (room.Name.equals(name)) {
                if (room.Day == day) {
                    //return all available hours for specified day in the body of html message

                }

            }
            else {
                //room doesn't exist

            }
        }
        //return a tuple which consists of availablehours and roomExists
        return availableHours;

    }

}
//Helper.printHtmlMessage("404", "The room does not exist", out);