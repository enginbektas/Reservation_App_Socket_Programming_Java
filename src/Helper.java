import java.io.PrintWriter;
import java.util.ArrayList;

//Helper class to implement common methods for all classes
public class Helper {
    //A method which takes status code and message and handles the response
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
        else {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
    }

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
    //A method which takes room name then returns the room object
    public static Room findRoomByName(String name, ArrayList<Room> Rooms) {
        for (Room room : Rooms) {
            if (room.Name.equals(name)) {
                return room;
            }
        }
        return null;
    }
}
