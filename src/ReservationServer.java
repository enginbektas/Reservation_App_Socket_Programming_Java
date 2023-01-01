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
            String[] pairs = tokens[1].split("&");

            for (String pair : pairs) {


                int equalsIndex = pair.indexOf("=");
                String name = pair.substring(0, equalsIndex);
                String value = pair.substring(equalsIndex + 1);
                //remove last word from value
                if(value.contains(" ")){
                    value = value.substring(0, value.indexOf(" "));
                }

                parameters.put(name, value);
                requests.put(method, parameters);
            }
            //endregion

            String[] parts = requestLine.split(" ");
            // Check if the request is a GET request
            if (parts[0].equals("GET")) {
                //region Iterate for each request
                for (Map.Entry<String, Map<String, String>> entry : requests.entrySet()) {

                    Map<String, String> parametersMap = entry.getValue();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
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

                                String name = parametersMap.get("name");

                                Room room = Helper.findRoomByName(name, Rooms);
                                String ActivityName = parametersMap.get("activity");
                                int day = Integer.parseInt(parametersMap.get("day"));
                                int hour = Integer.parseInt(parametersMap.get("hour"));
                                int duration = Integer.parseInt(parametersMap.get("duration"));

                                //checks whether there exists an activity with name
                                //activityname by contacting the Activity Server. If no such activity exists it sends back an HTTP
                                //404 Not Found message.
                                //check if activity exists by contacting ActivityServer

                                //establish a connection with ActivityServer that has port 8081 then send message to it
                                //Server:reservationServer, method:reserve



                                //Send to activity server
                                Socket activitySocket = new Socket("localhost", Helper.ActivityServerPort);

                                //get from activity server
                                BufferedReader activityIn = new BufferedReader(new InputStreamReader(activitySocket.getInputStream()));
                                PrintWriter activityOut = new PrintWriter(activitySocket.getOutputStream(), true);
                                activityOut.println("GET /check?name=" + "HelloActivity" + " HTTP/1.1");
                                String response = activityIn.readLine();
                                System.out.println(response);



                                //if room doesn't exist, send HTTP 404 Not Found message indicating that the room doesn't exist
                                if(!roomExists(name, Rooms)) {
                                    Helper.printHtmlMessage("404", "The room doesn't exist", out);
                                }
                                //if room exists, check if it's available
                                else if(Helper.isAvailable(room, day, hour, duration)) {
                                    Helper.printHtmlMessage("403", "The room is already reserved", out);
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