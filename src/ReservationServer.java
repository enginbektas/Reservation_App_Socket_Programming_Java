import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


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

                                //region Room Server Settings
                                //Send to room server
                                Socket roomSocket = new Socket("localhost", Helper.RoomServerPort);
                                //get from room server
                                BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
                                PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
                                //endregion

                                //region Initialize parameters
                                String name = parametersMap.get("room");
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
                                //get status code
                                String status = roomResponse.split(" ")[1];
                                Helper.printHtmlMessage(status, roomMessage, out);
                                //if 200, add activity to that reservation
                                if(status.equals("200")){
                                    //TODO add activity to reservation
                                    //get substring of roomMessage from index of "id:" to next index of " "
                                    String id = roomMessage.substring(roomMessage.indexOf("id:") + 3, roomMessage.indexOf(" ", roomMessage.indexOf("id:")));
                                    //add activity to reservation
                                    addActivityToReservationById(id, activityName);

                                }
                                break;
                                //endregion

                            }
                            //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                            else {
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                        //endregion
                        //region case:ListAvailability
                        case "listavailability":
                            //region With parameter day
                            //check if inputs are valid
                            if (parametersMap.containsKey("room")
                                    && parametersMap.containsKey("day")) {

                                //region Room Server Settings
                                //Send to room server
                                Socket roomSocket = new Socket("localhost", Helper.RoomServerPort);
                                //get from room server
                                BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
                                PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
                                //endregion
                                //region Initialize parameters
                                String name = parametersMap.get("room");
                                int day = Integer.parseInt(parametersMap.get("day"));
                                //endregion
                                //region Contact Room Server to checkavailibility with parameters room and day
                                roomOut.println("GET /checkavailability?name=" + name + "&day=" + day + " HTTP/1.1");
                                //Send to activity server
                                String roomResponse = roomIn.readLine();
                                roomIn.readLine(); roomIn.readLine();
                                String roomMessage = roomIn.readLine();
                                roomIn.read();
                                String roomMessageSubstring = roomMessage.substring(roomMessage.indexOf("<p>") + 3, roomMessage.indexOf("</p>"));
                                String status = roomResponse.split(" ")[1];
                                Helper.printHtmlMessage(status, roomMessageSubstring, out);
                                break;
                                //endregion

                            }
                            //endregion
                            //region Without parameter day
                            else if (parametersMap.containsKey("room")) {

                                //region Initialize parameters
                                String name = parametersMap.get("room");

                                //endregion
                                ArrayList<String> availableHoursForEachDay = new ArrayList<>();
                                //region Contact Room Server to checkavailibility with parameters room and day
                                for (int day = 1; day <= 7; day++) {

                                    //region Room Server Settings
                                    //Send to room server
                                    Socket roomSocket = new Socket("localhost", Helper.RoomServerPort);
                                    //get from room server
                                    BufferedReader roomIn = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
                                    PrintWriter roomOut = new PrintWriter(roomSocket.getOutputStream(), true);
                                    //endregion

                                    roomOut.println("GET /checkavailability?name=" + name + "&day=" + day + " HTTP/1.1");

                                    String roomResponse = roomIn.readLine();
                                    roomIn.readLine();
                                    roomIn.readLine();
                                    String roomMessage = roomIn.readLine();

                                    //get substring of roomMessage between <p> and </p>
                                    String roomMessageSubstring = roomMessage.substring(roomMessage.indexOf("<p>") + 3, roomMessage.indexOf("</p>"));
                                    //tokenize roomMessageSubstring by ":" and set roomMessageSubstring to the second token without the first character
                                    roomMessageSubstring = roomMessageSubstring.split(":")[1].substring(1);
                                    //generate string and add to list
                                    String availableHoursString = "Available hours for room " + name + " on day " + day + ": " + roomMessageSubstring;

                                    availableHoursForEachDay.add(availableHoursString);
                                    String status = roomResponse.split(" ")[1];

                                    //region Print error message if status is not 200
                                    if (!status.equals("200")) {
                                        Helper.printHtmlMessage(status, roomMessageSubstring, out);
                                        break;
                                    }
                                    //endregion
                                    //endregion
                                }
                                //region Print all available hours for each day
                                String Response = "";
                                for (String s : availableHoursForEachDay) {
                                    Response += s + "<br>";
                                }
                                Helper.printHtmlMessage("200", Response, out);
                                break;
                                //endregion
                            }
                            //if any of these inputs are not valid, it sends back an HTTP 400 Bad Request message.
                            else {
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
                            }
                            break;
                            //endregion
                        //endregion
                        //region case:Display
                        case "display":
                            //check if inputs are valid
                            if (parametersMap.containsKey("id")) {

                                //region Initialize parameters
                                String id = parametersMap.get("id");
                                //endregion
                                //region Find reservation with id and display it
                                Reservation reservation = getReservationById(id);
                                if(reservation == null){
                                    Helper.printHtmlMessage("404", "Reservation with id " + id + " doesn't exist", out);
                                    break;
                                }
                                else {
                                    Helper.printHtmlMessage("200", reservation.ReservationString, out);
                                }
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
    //Method to append activity to the reservation by ReservationServer
    public static void addActivityToReservationById(String id, String activityName) {
        String reservationString = "";
        boolean found = false;
        try {
            File file = new File("src/db/Reservations.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //tokenize line by space
                String[] tokens = line.split(" ");
                if (tokens[0].equals(id)) {
                    //add activityName to the end of line
                    line += " " + activityName;
                    found = true;
                }
                reservationString += line + "\n";
            }
            scanner.close();

            if (!found) {
                System.out.println("Reservation not found with id: " + id);
                return;
            }

            // write modified string to file
            PrintWriter writer = new PrintWriter(file);
            writer.print(reservationString);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static Reservation getReservationById(String id) {
        boolean found = false;
        Reservation reservation = new Reservation();
        try {
            File file = new File("src/db/Reservations.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //tokenize line by space
                String[] tokens = line.split(" ");
                if (tokens[0].equals(id)) {
                    found = true;
                    reservation.Id = tokens[0];
                    reservation.RoomName = tokens[1];
                    reservation.Day = Integer.parseInt(tokens[2]);
                    reservation.Hour = Integer.parseInt(tokens[3]);
                    reservation.Duration = Integer.parseInt(tokens[4]);
                    reservation.ActivityName = tokens[5];
                    //redeclare reservation then return it
                    reservation = new Reservation(reservation.Id, reservation.RoomName, reservation.ActivityName,
                            reservation.Day, reservation.Hour, reservation.Duration);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!found)
            return null;
        return reservation;
    }
}
//Helper.printHtmlMessage("404", "The room does not exist", out);