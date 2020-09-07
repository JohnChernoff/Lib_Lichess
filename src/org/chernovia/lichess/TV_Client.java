package org.chernovia.lichess;

import org.chernovia.lichess.util.LichessUtils;
import com.fasterxml.jackson.databind.JsonNode;

public class TV_Client extends Thread implements LichessListener {
	
	public LichessClient client;
	LichessAdapter adapter;  
	LichessListener listener;
	String game_type;
	String current_gid = null;

	public TV_Client(String type, LichessListener l) {
		listener = l;
		adapter = new LichessAdapter(this);
		client = new LichessClient();
		game_type = type;
	}
	
	@Override
	public void run() {
		newGame(game_type);
	}
	
	public void newGame(String type) {
		game_type = type;
		client.endAllThreads(); //System.out.println("Old Game: " + current_gid);
		String gid;
		System.out.println("Looking for new " + game_type + " game: ");
		do {
			gid = LichessUtils.getTVData(game_type);
			if (gid.equals(current_gid)) {
				try { sleep(2500); } catch (InterruptedException ignore) {};
			}
		} while (gid.equals(current_gid));
		current_gid = gid; 
		System.out.println("New Game: " + current_gid);
		client.newGame(current_gid, adapter);
	}

	@Override
	public void startWatching(String gid) { listener.startWatching(gid); }
	
	@Override
	public void tvSelect(String channel, String gid) { listener.tvSelect(channel, gid); }
	
	@Override
	public void newMove(LichessMove move) { listener.newMove(move); }
	
	@Override
	public void endGame(JsonNode data) {
		listener.endGame(data);
		newGame(game_type);
	}
	
}
