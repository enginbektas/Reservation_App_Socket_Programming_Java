import java.io.*;
import java.net.*;

public class ReservationServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(8082);

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();

            // Create a BufferedReader to read from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Read the request line
            String requestLine = in.readLine();

            // Split the request line into its parts
            String[] parts = requestLine.split(" ");

            // Check if the request is a GET request
            if (parts[0].equals("GET")) {
                // The request is a GET request
                // You can now parse the rest of the request to get the requested resource and other request parameters

                // For example, you can get the requested resource by using parts[1]
                String resource = parts[1];

                // You can then send a response back to the client
                // For example, you can send a simple HTML page as the response
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("");
                out.println("<html>");
                out.println("<body>");
                out.println("<h1>Hello, World!</h1>");
                out.println("</body>");
                out.println("</html>");
            }

            // Close the client socket
            clientSocket.close();
        }
    }
}