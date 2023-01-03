public class Reservation {
    public String Id;
    public String RoomName;
    public String ActivityName;
    public int Day;
    public int Hour;
    public int Duration;
    public String ReservationString;
    //default constructor
    public Reservation() {
    }

    public Reservation(String id, String roomName, String activityName, int day, int hour, int duration) {
        Id = id;
        RoomName = roomName;
        ActivityName = activityName;
        Day = day;
        Hour = hour;
        Duration = duration;
        ReservationString = "Id:" + id + "<br>" + "Room:" + roomName + "<br>" + "Activity:" + activityName + "<br>" +
                "Day:" + Helper.convertDay(day) + "<br>" + "Time:" + Helper.convertDuration(hour, duration) + "<br>";

    }
}
