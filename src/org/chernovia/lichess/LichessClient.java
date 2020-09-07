package org.chernovia.lichess;

import java.net.URI;
import java.util.Collection;
import java.util.Vector;
import org.chernovia.lib.net.zugclient.WebSock;
import org.chernovia.lib.net.zugclient.WebSockListener;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class LichessClient {
    public static final int TIMEOUT = 30000;
	private static final Logger LOG = Log.getLogger(LichessClient.class);
    private WebSocketClient client;
    private Vector<SockThread> threads;
    
    public class SockThread extends Thread {
        private boolean RUNNING = false;
    	private String sock_id;
        private WebSock sock;
    	public SockThread(WebSock s, String id) { 
    		sock = s; 
    		sock_id = new String(id); 
    		setName("Sock Thread: " + sock_id);
    	}
    	public void run() {
    		mainLoop(sock);
        	sock.end();
        	LOG.info("Exiting thread...");
        	threads.remove(this);
    	}
    	public WebSock getSock() { return sock; }
    	
    	public void end() {
    		interrupt(); RUNNING = false; 
    	}
    	
        private void mainLoop(WebSock sock) {
        	//LOG.info("MAIN LOOP CALLED");
        	RUNNING = true;
        	sock.last_msg = System.currentTimeMillis();
        	while (RUNNING && (sock.isConnecting() || sock.isConnected())) {
        		if (sock.isConnected()) { 
        			//LOG.info("MAIN LOOP ENTERED");
    				sock.send("{\"t\":\"p\",\"v\":9999999}"); //HUGE number for socket version
        		}
   				try { Thread.sleep(2000); } catch (InterruptedException e) { return; }
    			if ((System.currentTimeMillis() - sock.last_msg) > TIMEOUT) {
    				LOG.warn(sock.toString() + " -> timeout"); return;
    			}
       		}
        	RUNNING = false;
        }
    }
    
    public LichessClient() {
    	SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(true); //magic!
        client = new WebSocketClient(sslContextFactory);
        threads = new Vector<SockThread>();
    }
    
    public WebSock newGame(String gid, WebSockListener w) {
    	URI uri = URI.create("wss://socket.lichess.org/watch/" + gid + "/black/v5?sri=zug" + (int)(Math.random() * 999));
    	return newSock(uri,gid,w);
    }
    
    public WebSock newLobby(WebSockListener w) {
    	URI uri = URI.create("wss://socket.lichess.org/lobby/socket/v4?sri=zug" + (int)(Math.random() * 999));
    	return newSock(uri,"lobby",w);
    }
    
    public WebSock newFollow(String player, WebSockListener w) {
    	//wss://socket2.lichess.org/watch/No6ODjDD/black/v5?sri=cnQaC6VFNuyR&userTv=zaven_chessmood&v=0
    	return null;
    }
    
    private WebSock newSock(URI uri, String id, WebSockListener w) {
    	WebSock sock = new WebSock(id); sock.addListener(w);
    	ClientUpgradeRequest request = new ClientUpgradeRequest();
		try { client.start(); client.connect(sock,uri,request); }
		catch (Exception augh) { augh.printStackTrace(); return null; }
		LOG.info("Creating new socket: " + id);
		SockThread new_thread = new SockThread(sock,id);
		threads.add(new_thread);
		new_thread.start();
		return new_thread.getSock();
    }
    
    public void endAllThreads() {
    	for (SockThread thread : getThreads()) thread.end();
    }
    
    public Collection<SockThread> getThreads() { return threads; }
    public Collection<SockThread> getThread(String id) {
    	Vector<SockThread> v = new Vector<SockThread>();
    	for (SockThread thread : threads) if (thread.sock_id.equals(id)) v.add(thread);
    	return v; 
    }
}
