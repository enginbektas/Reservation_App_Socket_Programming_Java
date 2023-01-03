This program can either be run by running 3 of the servers in the src folder in seperate cmd's with
the command: java ServerName

java RoomServer
java ActivityServer
java ReservationServer

or by using an IDE.

How to use the program:

You can invoke curl commands to the server to make requests. The server will respond with a status message.
Or you can use a web browser to make requests to the server.

How to reserve a room?
-> First you need a room, if you do not any, you will need to create one by contacting the Room Server.
-> Then you will need an activity that will be associated with the reservation. 
Again if you do not have any, you will need to create one by contacting the Activity Server.
-> Now you can make reservations by contacting the Reservation Server! But be careful. You will get errors
when you enter invalid inputs.
-> Remember to use reset methods if things go worse.

You can invoke servers methods by using the following commands:

Room Server (8081):
/add?name=roomname: Adds a new room.
/remove?name=roomname: Removes the room with name=roomname.
/reserve?name=roomname&day=x&hour=y&duration=z: Reserves the room for specified time.
/checkavailability?name=roomname&day=x: lists available hours on the specified day.

Activity Server (8082):
/add?name=activityname: Adds a new activity.
/remove?name=activityname: Removes the activity with name=activityname.
/check?name=activityname: Checks whether there exists an activity with name=activityname.

Reservation Server (8080):
/reserve?room=roomname&activity=activityname&day=x&hour=y&duration=z: Adds a new reservation.
/listavailability?room=roomname&day=x: Lists all the available hours for the specified day
/listavailability?room=roomname: Lists all the available hours for all days of the week
/display?id=reservation_id: Returns the details of the reservation with the specified id.
/resetall: Resets the whole database.
/resetrooms: Resets the rooms database.
/resetactivities: Resets the activities database.
/resetreservations: Resets the reservations database.


Example web browser requests sequence:
http://localhost:8081/add?name=M2Z08
http://localhost:8081/remove?name=M1Z01
http://localhost:8082/add?name=CSE4197
http://localhost:8080/reserve?room=M2Z08&activity=CSE4197&day=5&hour=10&duration=2
http://localhost:8080/listavailability?room=M2Z08&day=5
http://localhost:8080/listavailability?room=M2Z08
http://localhost:8080/display?id=1

Thanks for evaluating this project.
