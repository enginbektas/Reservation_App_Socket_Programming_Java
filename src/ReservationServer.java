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
        System.out.println("Reservation Server is running");

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
                    outerloop:
                    switch (method) {
                        //region case:Reserve
                        case "reserve":

                            //check if inputs are valid
                            if (parametersMap.containsKey("room")
                                    && parametersMap.containsKey("activity")
                                    && parametersMap.containsKey("day")
                                    && parametersMap.containsKey("hour")
                                    && parametersMap.containsKey("duration")) {

                                //region Invalid Input Error Messages
                                //if day or hour or duration is not a number
                                if (!Helper.isNumeric(parametersMap.get("day"))
                                        || !Helper.isNumeric(parametersMap.get("hour"))
                                        || !Helper.isNumeric(parametersMap.get("duration"))) {
                                    Helper.printHtmlMessage("400", "Error: Day, Hour and Duration must be numbers", out);
                                    break outerloop;
                                }

                                //if day is not between 1 and 7
                                if (Integer.parseInt(parametersMap.get("day")) < 1
                                        || Integer.parseInt(parametersMap.get("day")) > 7) {
                                    Helper.printHtmlMessage("400", "Error: Day must be between 1 and 7", out);
                                    break outerloop;
                                }
                                //if hour is not between 9 and 17
                                if (Integer.parseInt(parametersMap.get("hour")) < 9
                                        || Integer.parseInt(parametersMap.get("hour")) > 17) {
                                    Helper.printHtmlMessage("400", "Error: Hour must be between 9 and 17", out);
                                    break outerloop;
                                }
                                //if hour+duration is bigger than 17
                                if (Integer.parseInt(parametersMap.get("hour")) + Integer.parseInt(parametersMap.get("duration")) > 17) {
                                    Helper.printHtmlMessage("400", "Error: You can not reserve a room after 17:00", out);
                                    break outerloop;
                                }
                                //endregion
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
                                String outMessage = "";
                                //get substring of roomMessage between <p> and </p> to get the message
                                if(roomMessage.contains("<p>") && roomMessage.contains("</p>")){
                                    outMessage = roomMessage.substring(roomMessage.indexOf("<p>")+3, roomMessage.indexOf("</p>"));
                                }
                                //if 200, add activity to that reservation
                                if(status.equals("200")){
                                    //Append activity to the reservation string
                                    outMessage = outMessage + " for " + activityName;
                                    //get substring of roomMessage from index of "id:" to next index of " "
                                    String id = roomMessage.substring(roomMessage.indexOf("Id:") + 3, roomMessage.indexOf(" ", roomMessage.indexOf("Id:")));
                                    //add activity to reservation
                                    addActivityToReservationById(id, activityName);
                                    Helper.printHtmlMessage(status, outMessage, out);
                                    break;
                                }
                                Helper.printHtmlMessage(status, outMessage, out);
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
                                //region Invalid Input Error Messages
                                //if day is not a number
                                if (!Helper.isNumeric(parametersMap.get("day"))) {
                                    Helper.printHtmlMessage("400", "Error: Day must be a number", out);
                                    break outerloop;
                                }
                                //if day is not between 1 and 7
                                if (Integer.parseInt(parametersMap.get("day")) < 1 || Integer.parseInt(parametersMap.get("day")) > 7) {
                                    Helper.printHtmlMessage("400", "Error: Day must be between 1 and 7", out);
                                    break outerloop;
                                }
                                //endregion
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
                                //region Contact Room Server to checkavailibility with parameters room and day
                                String availableHoursString = "Availability of room: " + name + "<br>";
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
                                    String status = roomResponse.split(" ")[1];
                                    //region Print error message if status is not 200
                                    if (!status.equals("200")) {
                                        Helper.printHtmlMessage(status, roomMessageSubstring, out);
                                        break outerloop;
                                    }
                                    //endregion
                                    //tokenize roomMessageSubstring by ":" and set roomMessageSubstring to the second token without the first character
                                    roomMessageSubstring = roomMessageSubstring.split(":")[1].substring(1);
                                    //generate string and add to list
                                    availableHoursString += Helper.convertDay(day) + ": " + roomMessageSubstring + "<br>";

                                    //endregion
                                }
                                Helper.printHtmlMessage("200", availableHoursString, out);
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
                                //region Invalid Input Error Messages
                                //if id is not a number
                                if (!Helper.isNumeric(parametersMap.get("id"))) {
                                    Helper.printHtmlMessage("400", "Error: ID must be a number", out);
                                    break outerloop;
                                }
                                //endregion

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
                        //region case:ResetAll
                        case "resetall":
                            boolean status = Helper.resetDatabase();
                            if (status)
                                Helper.printHtmlMessage("200", "Reset database successful", out);
                            break;
                            //endregion
                        //region case:ResetRooms
                        case "resetrooms":
                            boolean status2 = Helper.resetRooms();
                            if (status2)
                                Helper.printHtmlMessage("200", "Reset rooms successful", out);
                            break;
                        //endregion
                        //region case:ResetActivities
                        case "resetactivities":
                            boolean status3 = Helper.resetActivities();
                            if (status3)
                                Helper.printHtmlMessage("200", "Reset activities successful", out);
                            break;
                        //endregion
                        //region case:ResetReservations
                        case "resetreservations":
                            boolean status4 = Helper.resetReservations();
                            if (status4)
                                Helper.printHtmlMessage("200", "Reset reservations successful", out);
                            break;
                        //endregion
                        //region case:Default
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


    //Method to append activity to the reservation by ReservationServer
    //We use this method because RoomServer does not implicitly adds activity to the reservation
    public static void addActivityToReservationById(String id, String activityName) {
        String reservationString = "";
        boolean found = false;
        try {
            File file = new File(Helper.ReservationsPath);
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
    //For display reservation
    public static Reservation getReservationById(String id) {
        boolean found = false;
        Reservation reservation = new Reservation();
        try {
            File file = new File(Helper.ReservationsPath);
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