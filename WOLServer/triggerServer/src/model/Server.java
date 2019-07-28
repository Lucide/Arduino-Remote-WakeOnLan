package model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Runner implements FromThread{
	private ServerSocket serverSocket;
	private ArrayList<ClientHandler> clients;

	public Server(FromThread fromThread){
		super(fromThread);
		this.threadName="server";
		clients=new ArrayList<>();
	}

	public void launch(int port){
		fromThread.threadReceived(0,new String[]{"server starting on port "+port},null,null);
		clients.clear();
		while(true){
			try{
				serverSocket=new ServerSocket(port);
				fromThread.threadReceived(0,new String[]{"server started"},null,null);
				break;
			}catch(IOException ex){
				fromThread.threadReceived(0,new String[]{"server socket creation failed, retrying: "+ex.getMessage()},null,null);
			}
		}
		super.launch(false);
	}

	@Override
	public void die(){
		fromThread.threadReceived(0,new String[]{"server stopping"},null,null);
		super.die();
		try{
			serverSocket.close();
		}catch(IOException ex){
			fromThread.threadReceived(0,new String[]{"server socket closing failed: "+ex.getMessage()},null,null);
		}
	}

	private void dieCallback(){
		synchronized(clients){
			if(clients.size()==0){
				clientsDead();
			}
			for(int i=0; i<clients.size(); i++){
				clients.get(i).die();
			}
		}
	}

	private void clientsDead(){
		fromThread.threadReceived(0,new String[]{"server stopped"},null,null);
		fromThread.threadReceived(3,null,null,null);
	}

	@Override
	public void threadReceived(int id, String[] s, int[] v, List<?> l){
		switch(id){
		case 1:// client dead
			synchronized(clients){
				clients.remove(l.get(0));
				fromThread.threadReceived(1,null,new int[]{clients.size()},null);
				if(!isEnable()&&clients.size()==0){
					clientsDead();
				}
			}
			break;
		default:// anything else
			fromThread.threadReceived(id,s,v,l);
			break;
		}
	}

	@Override
	public void run(){
		Socket clientSocket;
		while(isEnable()){
			try{
				clientSocket=serverSocket.accept();
			}catch(IOException ex){
				clientSocket=null;
				fromThread.threadReceived(0,new String[]{"failed accepting connection: "+ex.getMessage()},null,null);
			}
			if(clientSocket!=null){
				synchronized(clients){
					clients.add(new ClientHandler(this,clients.size(),clientSocket));
					fromThread.threadReceived(1,null,new int[]{clients.size()},null);
				}
			}
		}
		dieCallback();
	}
}
