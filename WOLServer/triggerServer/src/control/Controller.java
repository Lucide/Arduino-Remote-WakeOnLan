package control;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import model.FromThread;
import model.Server;
import view.Gui;

public class Controller implements WindowListener, FromThread{
	Server server;
	Gui gui;
	boolean close;

	public Controller(){
		server=new Server(this);
		gui=new Gui();
		gui.setVisible(true);
		server.launch(14096);
		close=false;
		gui.addWindowListener(this);
	}

	@Override
	public void windowClosing(WindowEvent e){
		if(close){
			System.exit(0);
		}
		else{
			server.die();
			gui.removeWindowListener(this);
		}
	}

	@Override
	public void threadReceived(int id, String[] s, int[] v, List<?> l){
		switch(id){
		case 0:// log
			synchronized(gui.taLog){
				gui.taLog.append(s[0]+"\n");
			}
			break;
		case 1:// client number refresh
			synchronized(gui.lbDescription){
				gui.lbDescription.setText(v[0]+" clients connected");
			}
			break;
		case 2:// WOL sent
			gui.lbLight.setForeground(Color.GREEN);
			break;
		case 3:// all clients dead
			close=true;
			gui.addWindowListener(this);
			break;
		}
	}

	@Override
	public void windowActivated(WindowEvent e){
	}

	@Override
	public void windowClosed(WindowEvent e){
	}

	@Override
	public void windowDeactivated(WindowEvent e){
	}

	@Override
	public void windowDeiconified(WindowEvent e){
	}

	@Override
	public void windowIconified(WindowEvent e){
	}

	@Override
	public void windowOpened(WindowEvent e){
	}

}
