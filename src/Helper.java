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

    public static boolean isNumeric(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean resetDatabase() {
        //remove all lines in the files activites.txt, reservations.txt and rooms.txt
        try {
            PrintWriter out = new PrintWriter("src/db/activities.txt");
            out.print("");
            out.close();
            out = new PrintWriter("src/db/reservations.txt");
            out.print("");
            out.close();
            out = new PrintWriter("src/db/rooms.txt");
            out.print("");
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean resetRooms() {
        //remove all lines in the file rooms.txt
        try {
            PrintWriter out = new PrintWriter("src/db/rooms.txt");
            out.print("");
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean resetActivities() {
        //remove all lines in the file activities.txt
        try {
            PrintWriter out = new PrintWriter("src/db/activities.txt");
            out.print("");
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean resetReservations() {
        //remove all lines in the file reservations.txt
        try {
            PrintWriter out = new PrintWriter("src/db/reservations.txt");
            out.print("");
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    //method that converts numbers betwen 1 and 7 to days of the week
    public static String convertDay(int day) {
        switch (day) {
            case 1:
                return "Monday";
            case 2:
                return "Tuesday";
            case 3:
                return "Wednesday";
            case 4:
                return "Thursday";
            case 5:
                return "Friday";
            case 6:
                return "Saturday";
            case 7:
                return "Sunday";
            default:
                return "Invalid day";
        }
    }

    public static String convertDuration(int hour, int duration) {
        int endHour = hour + duration;
        return hour + ":00-" + endHour + ":00";
    }
}
