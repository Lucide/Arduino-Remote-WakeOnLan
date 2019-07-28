package model;

public abstract class Runner implements Runnable{

	private Thread thread;
	protected FromThread fromThread;
	protected String threadName;
	private boolean pause;
	private boolean enable;

	public Runner(FromThread fromThread){
		this.fromThread=fromThread;
		threadName="thread";
		enable=false;
		pause=false;
	}

	public boolean isPause(){
		return pause;
	}

	public boolean isEnable(){
		return enable;
	}

	public void launch(boolean paused){
		if(paused){
			pause();
		}
		enable=true;
		thread=new Thread(this,threadName);
		thread.start();
	}

	public void die(){
		enable=false;
		resume();
		thread=null;
	}

	public void pause(){
		pause=true;
	}

	public void resume(){
		pause=false;
		if(thread!=null){
			synchronized(thread){
				thread.notify();
			}
		}
	}

	protected boolean check(boolean stopTo){
		if(stopTo==pause){
			fromThread.threadReceived(0,new String[]{"Runner ("+threadName+"): waiting"},null,null);
			try{
				synchronized(thread){
					thread.wait();
				}
			}catch(InterruptedException ex){
				fromThread.threadReceived(0,new String[]{"Runner ("+threadName+"): error going into wait state"},null,null);
				ex.printStackTrace();
			}
		}
		return true;
	}

}
