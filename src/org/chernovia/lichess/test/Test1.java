package org.chernovia.lichess.test;

import org.chernovia.lichess.LichessClient;
import org.chernovia.lichess.util.LichessUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.chernovia.lib.misc.net.NetUtils;
import org.chernovia.lib.net.zugclient.*;


public class Test1 implements WebSockListener {
	
	LichessClient client;
		
	public Test1() {
		
		NetUtils.trustAllCerts(); 
		JsonNode node = LichessUtils.getUser("ZugAddict"); 
		System.out.println(node.toString());
	
		String gid = LichessUtils.getTVData("blitz");
		System.out.println("gid: " + gid);
		client = new LichessClient();
		client.newGame(gid, this);
	}

	public static void main(String[] args) {
		new Test1();
	}

	@Override
	public void sock_msg(WebSock sock, String message) {
		System.out.println(message);
	}

	@Override
	public void sock_fin(WebSock sock) {
		System.out.println("Game over: " + sock.last_msg);
	}

}
