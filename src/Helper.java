import java.io.PrintWriter;
import java.util.ArrayList;

//Helper class to implement common methods for all classes
public class Helper {
    public final static int ActivityServerPort = 8082;
    public final static int RoomServerPort = 8081;
    public final static int ReservationServerPort = 8080;
    //A method which takes status code and message and handles the response
    public static void printHtmlMessage(String status, String message, PrintWriter out) {
        if (status.equals("200")) {
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>200 OK</title></head><body><h1>200 OK</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
        else if (status.equals("400")) {
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>400 Bad Request</title></head><body><h1>400 Bad Request</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
        if (status.equals("403")) {
            out.println("HTTP/1.1 403 Forbidden");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>403 Forbidden</title></head><body><h1>403 Forbidden</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
        else if (status.equals("404")) {
            out.println("HTTP/1.1 404 Not Found");
            out.println("Content-Type: text/html");
            out.println();
            out.println("<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p>" + message + "</p></body></html>");
            out.flush();
        }
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
