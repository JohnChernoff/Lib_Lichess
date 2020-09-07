package org.chernovia.lichess;

import java.io.IOException;

import org.chernovia.lib.net.zugclient.WebSock;
import org.chernovia.lib.net.zugclient.WebSockListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

final public class LichessAdapter implements WebSockListener {

	LichessListener listener;
	ObjectMapper mapper = new ObjectMapper();
	
	public LichessAdapter(LichessListener l) {
		listener = l;
	}
	
	@Override
	public void sock_msg(WebSock sock, String message) {
		//System.out.println(message);
		try {
			JsonNode msg = mapper.readTree(message);
			JsonNode type = msg.get("t");
			if (type == null) return;
			JsonNode data = msg.get("d");
			switch (type.asText()) {
				case "move":
					listener.newMove(new LichessMove(data)); break;
				case "endData":
					listener.endGame(data); break;
				case "tvSelect":
					listener.tvSelect(data.get("channel").asText(),data.get("id").asText());
				case "startWatching":
					listener.startWatching(data.asText());
			}
		} 
		catch (JsonProcessingException e) {	e.printStackTrace(); } 
		catch (IOException e) { e.printStackTrace(); }
		
	}

	@Override
	public void sock_fin(WebSock sock) {}

}
