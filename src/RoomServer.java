import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoomServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(8081);
        ArrayList<Room> Rooms = new ArrayList<>();

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();

            // Create a BufferedReader to read from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Read the request line
            String requestLine = in.readLine();
            Map<String, String> parameters = new HashMap<>();
            Map<String, Map<String, String>> requests = new HashMap<>();

            //region Get all requests and store in requests
            // Split the query string into name-value pairs
            String[] pairs = requestLine.split("&");
            for (String pair : pairs) {
                //tokenize pair by '?' char
                String[] tokens = pair.split("\\?");
                String method = tokens[0];
                //tokenize method by '/' char and set method to last token
                String[] methodTokens = method.split("/");
                method = methodTokens[methodTokens.length - 1];
                String parametersString = tokens[1];

                int equalsIndex = parametersString.indexOf("=");
                String name = parametersString.substring(0, equalsIndex);

                String value = parametersString.substring(equalsIndex + 1);
                //remove last word from value
                value = value.substring(0, value.indexOf(" "));

                parameters.put(name, value);
                requests.put(method, parameters);
            }
            //endregion

            String[] parts = requestLine.split(" ");
            // Check if the request is a GET request
            if (parts[0].equals("GET")) {
                //region Iterate for each request
                for (Map.Entry<String, Map<String, String>> entry : requests.entrySet()) {
                    String method = entry.getKey();
                    Map<String, String> parametersMap = entry.getValue();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                    //check if method is add
                    switch (method) {
                        //region case:Add
                        case "add":
                            if (parametersMap.containsKey("name") || parametersMap.containsKey("day")) {
                                //if room exists, send HTTP 403 Forbidden message indicating that the room already exists
                                if (roomExists(parametersMap.get("name"), Rooms)) {
                                    // Send the response
                                    printHtmlMessage("403", "The room already exists", out);
                                } else {
                                    //get name and day from parametersMap
                                    String name = parametersMap.get("name");
                                    //create new room with name and day
                                    addRoom(name, Rooms);
                                    // Send the response
                                    printHtmlMessage("200", "The room added succesfully", out);
                                }
                            }
                            else {
                                //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                                printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //region case:Remove
                        case "remove":
                            if (parametersMap.containsKey("name")) {
                                if (roomExists(parametersMap.get("name"), Rooms)) {
                                    String name = parametersMap.get("name");
                                    removeRoom(name, Rooms);
                                    printHtmlMessage("200", "The room removed successfully", out);
                                }
                                else {
                                    printHtmlMessage("403", "The room does not exist", out);
                                }
                            }
                            else {
                                //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                                printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //region case:ReserveRoom
                        case "reserveRoom":
                            if (parametersMap.containsKey("name") || parametersMap.containsKey("day")) {
                                String name = parametersMap.get("name");
                                int day = Integer.parseInt(parametersMap.get("day"));
                                reserveRoom(name, day, 0, 0, Rooms);

                            }
                            else {
                                //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                                printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //region case:CheckAvailability
                        case "checkAvailability":
                            if (parametersMap.containsKey("name") || parametersMap.containsKey("day")) {
                                String name = parametersMap.get("name");
                                int day = Integer.parseInt(parametersMap.get("day"));
                                checkAvailability(name, day, Rooms, out);
                            }
                            else {
                                //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                                printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                    }

                }
                //endregion
            }

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
        for (Room room : Rooms) {
            if (room.Name.equals(name)) {
                if (room.Day == day) {
                    //return all available hours for specified day in the body of html message

                }
            }
            else {
                printHtmlMessage("200", "The room does not exist", out);
            }
        }
        return null;
    }

    public static void printHtmlMessage(String status, String message, PrintWriter out) {
        if (status == "200") {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>200 OK</title></head><body><h1>200 OK</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
        else if (status == "400") {
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>400 Bad Request</title></head><body><h1>400 Bad Request</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
        if (status == "403") {
            out.println("HTTP/1.1 403 Forbidden");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>403 Forbidden</title></head><body><h1>403 Forbidden</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
    }
}
//