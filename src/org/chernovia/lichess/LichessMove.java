package org.chernovia.lichess;

import org.chernovia.lib.chess.ChessGame;
import org.chernovia.lib.chess.ChessVector;
import com.fasterxml.jackson.databind.JsonNode;

public class LichessMove extends ChessGame.ChessMove {
	
	public enum Piece { NONE, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING }; 
	
	Piece piece;
	int ply;
	boolean black;
	
	public LichessMove(JsonNode move) {
		super(null,null);
		piece = parsePiece(move.get("san").asText().substring(0,1));
		String uci = move.get("uci").asText();
		from = new ChessVector(uci.charAt(0) - 97, 7 - (uci.charAt(1) - 49));
		to = new ChessVector(uci.charAt(2) - 97, 7 - (uci.charAt(3) - 49));
		ply = move.get("ply").asInt();
		black = (ply % 2 == 0);
	}
	
	public Piece parsePiece(String p) {
		switch(p) {
			case "N": return Piece.KNIGHT;
			case "B": return Piece.BISHOP;
			case "R": return Piece.ROOK;
			case "Q": return Piece.QUEEN;
			case "K": return Piece.KING;
			default: return Piece.PAWN;
		}
	}
	
	@Override
	public String toString() {
		return ply + ". " + (black ? piece.toString().toLowerCase() : piece.toString().toUpperCase()) + 
		": " + from.getFile() + "," + from.getRank() + " - " + to.getFile() + "," + to.getRank();
	}
}
