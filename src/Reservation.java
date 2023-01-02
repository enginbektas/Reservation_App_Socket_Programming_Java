public class Reservation {
    public String Id;
    public String RoomName;
    public String ActivityName;
    public String Day;
    public String Hour;
    public String Duration;
    //default constructor
    public Reservation() {
    }

    public Reservation(String id, String roomName, String activityName, String day, String hour, String duration) {
        Id = id;
        RoomName = roomName;
        ActivityName = activityName;
        Day = day;
        Hour = hour;
        Duration = duration;
    }
}
