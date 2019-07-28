package view;

import java.awt.Cursor;
import java.awt.Insets;

import javax.swing.JButton;

public class GButton extends JButton{

	private static final long serialVersionUID=7394409353841819383L;

	int w,h;

	public GButton(boolean background,String text){
		super(text);

		setFocusPainted(false);
		setMargin(new Insets(0,0,0,0));
		setBorderPainted(false);

		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		if(!background){
			setContentAreaFilled(false);
			setOpaque(false);
		}
		w=getWidth();
		h=getHeight();
	}

	public void setBounds(int x,int y,int width,int height){
		super.setBounds(x,y,width,height);
		w=width;
		h=height;
	}

	public int gGetX(){
		return getX()+w/2;
	}

	public int gGetY(){
		return getY()+h/2;
	}

	public void gSetXY(int x,int y){
		setBounds(x-w/2,y-h/2,w,h);
	}

	public void gSetX(int x){
		setBounds(x-w/2,getY(),w,h);
	}

	public void gSetY(int y){
		setBounds(getX(),y-h/2,w,h);
	}

	public void gSetWH(int width,int height){
		setBounds(getX(),getY(),width,height);
	}

	public void gSetW(int width){
		setBounds(getX(),getY(),width,h);
	}

	public void gSetH(int height){
		setBounds(getX(),getY(),w,height);
	}

	public void gSetCWH(int width,int height){
		setBounds(getX()+(w-width)/2,getY()+(h-height)/2,width,height);
	}

	public void gSetCW(int width){
		setBounds(getX()+(w-width)/2,getY(),width,h);
	}

	public void gSetCH(int height){
		setBounds(getX(),getY()+(h-height)/2,w,height);
	}
}
