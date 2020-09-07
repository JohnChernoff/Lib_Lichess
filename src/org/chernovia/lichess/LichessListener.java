package org.chernovia.lichess;

import com.fasterxml.jackson.databind.JsonNode;

public interface LichessListener {
	public void startWatching(String gid);
	public void tvSelect(String channel, String gid);
	public void newMove(LichessMove move);
	public void endGame(JsonNode data);
}
