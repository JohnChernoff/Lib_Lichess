package org.chernovia.lichess.util;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.chernovia.lib.chess.ChessGame;
import org.chernovia.lib.chess.ChessUtils;
import org.chernovia.lib.chess.ChessVector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LichessUtils {
	
	private LichessUtils() { throw new IllegalStateException("Utility class"); }
	
	static final ObjectMapper mapper = new ObjectMapper();
	static long waitTime = 5000;
	static long lastQuery = 0;
	static boolean noCheck = true;
	
	public static JsonNode getUser(String user) {
		queueChk();
		try { return mapper.readTree(Jsoup.connect(
				"https://lichess.org/api/user/" + user).
				ignoreContentType(true).
				header("Accept","application/json").
				validateTLSCertificates(false).get().body().text());  
		}
		catch (Exception e) { e.printStackTrace(); return null; }
	}
	
	public static String getUserGID(String user) {
		queueChk();
		JsonNode usr = getUser(user);
		try { 
			JsonNode gidURL = usr != null ? usr.get("playing") : null;
			if (gidURL != null) return gidURL.asText().split("/")[3];
		} 
		catch (Exception e) { e.printStackTrace(); }
		return null;
	}
	
	public static Iterator<JsonNode> getGames(String oauth) {
		queueChk();
		try { 
			return mapper.readTree(Jsoup.connect(
				"https://lichess.org/api/account/playing").
				ignoreContentType(true).
				header("Accept","application/json").
				header("Authorization","Bearer " + oauth).
				validateTLSCertificates(false).get().body().text()).get("nowPlaying").elements();  
		}
		catch (Exception e) { e.printStackTrace(); return null; }
	}
	
	//TODO: update API
	public static JsonNode getGameByGID(String gid) {
		try {
			return mapper.readTree(Jsoup.connect(
			"https://en.lichess.org/"+gid).ignoreContentType(true).
			header("Content-Type","application/x-www-form-urlencoded").
			header("Accept","application/vnd.lichess.v2+json").
			validateTLSCertificates(false).
			get().body().text()); 
		} catch (IOException e) { e.printStackTrace(); return null; }	
	}
	
	public static String[] getTVDataList(String game_type) {
		queueChk();
		Document doc;
		try {
			doc = Jsoup.connect(
			"https://en.lichess.org/games/" + game_type).ignoreContentType(false).header(
			"Content-Type","application/xhtml+xml").validateTLSCertificates(false).get();
			Elements gameIDs = doc.select("[data-live]");
			String[] data = new String[gameIDs.size()]; int i=0;
			for (Element e: gameIDs) {
				data[i++] = e.attr("data-live");
			}
			return data;
		} catch (IOException e) { e.printStackTrace(); return null; }
	}
	
	public static String getTVData(String gameType) {
		queueChk();
		try { return mapper.readTree(new URL("https://lichess.org/tv/channels")).get(gameType).get("gameId").asText();	} 
		catch (Exception e) { e.printStackTrace(); } 
		return null;
	}
	
	public static String makeMove(String gid, String move, String oauth) {
		queueChk();
		try { return Jsoup.connect(
				"https://lichess.org/api/board/game/" + gid + "/move/" + move).
				ignoreContentType(true).
				header("Accept","application/json").
				header("Authorization","Bearer " + oauth).
				validateTLSCertificates(false).post().body().text();  
		}
		catch (Exception e) { return e.getMessage(); }
	}
	
	public static String makeMove(int piece, ChessVector to, String oauth) {
		JsonNode gameNode = getGames(oauth).next();
		if (gameNode == null) return "Game not found";
		piece = Math.abs(piece) * (gameNode.get("color").asText().equals("white") ? 1 : -1);
		String fen = gameNode.get("fen").asText();
		ChessGame game = new ChessGame(fen);
		ChessVector from = game.findPiece(piece,to);
		if (from != null) {
			String move = ChessUtils.coord2alg(from) + ChessUtils.coord2alg(to);
			String gid = gameNode.get("gameId").asText();
			return makeMove(gid, move, oauth);
		}
		else return "Bad move: " + piece + " -> " + to;
	}
	
	private static void queueChk() {
		if (noCheck) return;
		long lag = System.currentTimeMillis() - lastQuery;
		if (lag < waitTime) {
			System.out.println("Waiting " + lag + " milliseconds...");
			try { Thread.sleep(lag); } catch (InterruptedException ignore) {} 
		}
		lastQuery = System.currentTimeMillis();
	}
	
}

