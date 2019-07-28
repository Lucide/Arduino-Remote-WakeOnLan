package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandler extends Runner{
	private int id;
	private Socket socket;
	private BufferedReader in;

	public ClientHandler(FromThread fromThread, int id, Socket clientSocket){
		super(fromThread);
		this.threadName="client-"+id;
		this.id=id;
		this.socket=clientSocket;
		launch(false);
	}

	private void dieCallback(){
		try{
			in.close();
		}catch(IOException ex){
			fromThread.threadReceived(0,new String[]{id+"] buffered reader closing failed: "+ex.getMessage()},null,null);
		}
		try{
			socket.close();
		}catch(IOException ex){
			fromThread.threadReceived(0,new String[]{id+"]  socket closing failed: "+ex.getMessage()},null,null);
		}
		fromThread.threadReceived(1,null,null,new ArrayList<ClientHandler>(Arrays.asList(this)));
	}

	@Override
	public void run(){
		String inputLine="";
		try{
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(IOException ex){
			fromThread.threadReceived(0,new String[]{id+"] failed creating bufferedReader: "+ex.getMessage()},null,null);
			die();
		}

		while(isEnable()){
			try{
				socket.setSoTimeout(2*1000);
				inputLine=in.readLine();
			}catch(IOException ex){
				die();
			}
			if("lalilulelo".equals(inputLine)){
				fromThread.threadReceived(2,null,null,null);
			}
		}
		dieCallback();
	}
}