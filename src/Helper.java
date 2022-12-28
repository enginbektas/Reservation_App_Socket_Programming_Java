import java.io.PrintWriter;

public class Helper {
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
}
