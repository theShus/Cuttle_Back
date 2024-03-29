package rs.raf.app.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.raf.app.model.Room;
import rs.raf.app.model.actions.GameAction;
import rs.raf.app.model.actions.GameResponse;
import rs.raf.app.model.actions.StartGameResponse;
import rs.raf.app.responses.JoinRoomResponse;
import rs.raf.app.responses.ResponseDto;
import rs.raf.app.utils.RoomKeyGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class RoomService {

    private static final int MAX_NUMBER_OF_PLAYERS = 4;
    private final Map<String, Room> activeRooms = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();

    Thread oldRoomRemover = new OldRoomRemover(activeRooms);

    @Autowired
    public RoomService() {
        oldRoomRemover.setDaemon(true);
        oldRoomRemover.start();
    }

    public String createRoom(String owner_username) {
        Room room = new Room();
        String roomKey = RoomKeyGenerator.generateKey();
        room.setIdKey(roomKey);
        room.setRoomOwner(owner_username);
        room.getPlayers().add(owner_username);
        room.setRoomLastUpdated(System.currentTimeMillis());
        activeRooms.put(roomKey, room);
        return roomKey;
    }

    public ResponseDto joinRoom(String roomKey, String username) {
        if (activeRooms.containsKey(roomKey)) {
            Room room = activeRooms.get(roomKey);
            if (room.isGameIsRunning()) return new ResponseDto("Game already in progress", 401);
            if (room.getPlayerHands().size() == MAX_NUMBER_OF_PLAYERS)
                return new ResponseDto("Maximum room capacity reached", 401);
            if (room.getPlayers().contains(username)) return new ResponseDto("Already in room", 200);//todo test
            room.getPlayers().add(username);

            String userJsonBody;
            JoinRoomResponse joinRoomResponse = new JoinRoomResponse(username, room.getPlayers().size() - 1, room.getPlayers());

            try {
                userJsonBody = mapper.writeValueAsString(joinRoomResponse);
            } catch (JsonProcessingException e) {
                return new ResponseDto("Error while joining room", 500);
            }
            return new ResponseDto(userJsonBody, 200);
        } else return new ResponseDto("Requested room doesn't exist", 404);
    }


    public StartGameResponse startGame(String roomKey, String commandIssuingUser) {
        GameResponse gameResponse;
        if (activeRooms.containsKey(roomKey)) {
            Room room = activeRooms.get(roomKey);
            if (room.isGameIsRunning())
                return new StartGameResponse(new ResponseDto("Game already in progress", 401), null);
            if (!room.getRoomOwner().equals(commandIssuingUser))
                return new StartGameResponse(new ResponseDto("You are not the room owner", 403), null);
            gameResponse = room.startGame();
            room.setRoomLastUpdated(System.currentTimeMillis());
            return new StartGameResponse(new ResponseDto(gameResponse.toString(), 200), gameResponse);
        } else return new StartGameResponse(new ResponseDto("Requested room doesn't exist", 404), null);
    }

    public ResponseDto stopGame(String roomKey, String commandIssuingUser) {
        if (activeRooms.containsKey(roomKey)) {
            Room room = activeRooms.get(roomKey);
            if (!room.isGameIsRunning()) return new ResponseDto("Game is already finished", 401);
            if (!room.getRoomOwner().equals(commandIssuingUser))
                return new ResponseDto("You are not the room owner", 403);
            room.stopGame();
            return new ResponseDto("Game stopped", 200);
        } else return new ResponseDto("Requested room doesn't exist", 404);
    }

    public StartGameResponse restartRoom(String roomKey, String commandIssuingUser) {
        GameResponse gameResponse;
        if (activeRooms.containsKey(roomKey)) {
            Room room = activeRooms.get(roomKey);
            if (!room.getRoomOwner().equals(commandIssuingUser))
                return new StartGameResponse(new ResponseDto("You are not the room owner", 403), null);
            gameResponse = room.restartGame();
            room.setRoomLastUpdated(System.currentTimeMillis());
            return new StartGameResponse(new ResponseDto(gameResponse.toString(), 200), gameResponse);
        } else return new StartGameResponse(new ResponseDto("Requested room doesn't exist", 404), null);
    }


    public GameResponse playCard(GameAction gameAction) {
        GameResponse gameResponse = null;
        if (activeRooms.containsKey(gameAction.getRoomKey())) {
            Room room = activeRooms.get(gameAction.getRoomKey());
            gameResponse = room.playTurn(gameAction);
            room.setRoomLastUpdated(System.currentTimeMillis());
        } else {
            System.err.println("Room not found");
            System.err.println(activeRooms);
        }

        return gameResponse;
    }


    public GameResponse drawCard(String roomKey) {
        GameResponse gameResponse;

        if (activeRooms.containsKey(roomKey)) {
            Room room = activeRooms.get(roomKey);
            gameResponse = room.drawCard();
            room.setRoomLastUpdated(System.currentTimeMillis());
            return gameResponse;
        } else {
            System.err.println("Room not found");
            return null;
        }
    }

    public ArrayList<String> getRoomPlayers(String roomKey) {
        if (activeRooms.containsKey(roomKey)) {
            return activeRooms.get(roomKey).getPlayers();
        } else {
            System.err.println("Room not found");
            return null;
        }
    }

}
