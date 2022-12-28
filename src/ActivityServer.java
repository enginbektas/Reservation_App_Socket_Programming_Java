import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(8082);
        ArrayList<Activity> Activities = new ArrayList<>();

        while (true) {
            // Accept a client connection
            Socket clientSocket = serverSocket.accept();

            // Create a BufferedReader to read from the client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // Read the request line
            String requestLine = in.readLine();
            Map<String, String> parameters = new HashMap<>();
            Map<String, Map<String, String>> requests = new HashMap<>();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
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

                    //check if method is add
                    switch (method) {
                        //region case:Add
                        case "add":
                            //Adds a new activity. If the activity is already added, this server
                            //should return an HTTP 403 Forbidden message indicating that the activity already exists in the
                            //database. Otherwise, it adds the activity to the database, and sends back a 200 OK message
                            //including an HTML file in the body that informs that the activity is added using printHtmlMessage method.
                            if (parametersMap.containsKey("name")) {
                                //if activity exists, send HTTP 403 Forbidden message indicating that the activity already exists
                                if (activityExists(Activities, parametersMap.get("name"))) {
                                    printHtmlMessage("403", "Activity already exists", out);
                                } else {
                                    //add activity to Activities
                                    Activities.add(new Activity(parametersMap.get("name")));
                                    //send HTTP 200 OK message
                                    printHtmlMessage("200", "Activity added successfully", out);
                                }
                            }
                            break;
                        //endregion
                        //region case:Remove
                        case "remove":
                            //Removes the activity with name=activityname. If the activity
                            //exists, it removes the activity from the database and sends back HTTP 200 OK message
                            //(including relevant info in the body as an HTML object). If the activity doesn’t exist, then it
                            //sends back an HTTP 403 Forbidden message.
                            if (parametersMap.containsKey("name")) {
                                //if activity exists, remove activity from Activities
                                if (activityExists(Activities, parametersMap.get("name"))) {
                                    Activities.removeIf(activity -> activity.name.equals(parametersMap.get("name")));
                                    //send HTTP 200 OK message
                                    printHtmlMessage("200", "Activity removed successfully", out);
                                } else {
                                    //send HTTP 403 Forbidden message indicating that the activity doesn't exist
                                    printHtmlMessage("403", "Activity doesn't exist", out);
                                }
                            }

                            break;
                        //endregion
                        //region case:Check
                        case "check":
                            //Checks whether there exists an activity with name=activityname.
                            //If the activity exists, it sends back an HTTP 200 OK message, otherwise it sends HTTP 404 Not
                            //Found message.
                            if (parametersMap.containsKey("name")) {
                                //if activity exists, send HTTP 200 OK message
                                if (activityExists(Activities, parametersMap.get("name"))) {
                                    printHtmlMessage("200", "Activity exists", out);
                                } else {
                                    //send HTTP 404 Not Found message
                                    printHtmlMessage("404", "Activity doesn't exist", out);
                                }
                            }
                            break;
                        //endregion
                    }
                }
                //endregion
            }
        }
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
    public static boolean activityExists(ArrayList<Activity> activities, String name) {
        for (Activity activity : activities) {
            if (activity.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
//