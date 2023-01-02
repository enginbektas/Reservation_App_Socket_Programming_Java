import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityServer {
    public static void main(String[] args) throws IOException {
        // Create a ServerSocket to listen for client connections
        ServerSocket serverSocket = new ServerSocket(Helper.ActivityServerPort);
        ArrayList<Activity> Activities = new ArrayList<>();
        System.out.println("Activity Server is running");

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

            String[] parts = requestLine.split(" ");
            // Check if the request is a GET request
            if (parts[0].equals("GET")) {
                //region Iterate for each request
                for (Map.Entry<String, Map<String, String>> entry : requests.entrySet()) {
                    //get parameters map
                    Map<String, String> parametersMap = entry.getValue();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
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
                                    Helper.printHtmlMessage("403", "Activity already exists", out);
                                } else {
                                    //add activity to Activities
                                    Activities.add(new Activity(parametersMap.get("name")));
                                    //send HTTP 200 OK message
                                    Helper.printHtmlMessage("200", "Activity added successfully", out);
                                }
                            }
                            else {
                                //send HTTP 400 Bad Request indicating that input is invalid
                                Helper.printHtmlMessage("400", "Inputs are invalid", out);
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
                                    Helper.printHtmlMessage("200", "Activity removed successfully", out);
                                } else {
                                    //send HTTP 403 Forbidden message indicating that the activity doesn't exist
                                    Helper.printHtmlMessage("403", "Activity doesn't exist", out);
                                }
                            }
                            else {
                                //send HTTP 400 Bad Request indicating that input is invalid
                                Helper.printHtmlMessage("400", "Input is invalid", out);
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
                                    Helper.printHtmlMessage("200", "Activity exists", out);
                                } else {
                                    //send HTTP 404 Not Found message
                                    Helper.printHtmlMessage("404", "Activity exists", out);

                                }


                                    //Helper.printHtmlMessage("404", "Activity doesn't exist", out);
                                //}
                            }
                            else {
                                //send HTTP 400 Not Found message
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