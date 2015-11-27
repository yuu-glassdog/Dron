import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random; //add

import javax.swing.JApplet;

public class Dron extends JApplet implements Runnable, KeyListener {
	private Color state[][];
	private int xSize, ySize;
	private int block;
	private int xL, yL, xR, yR;
	private int xA,yA;
	private int dxA, dyA;
	private int dxL, dyL, dxR, dyR;
	private boolean liveL, liveR;
	//private int countL, countR;
	private Thread thread;
	private String message;
	private Font font;
	//private int turn_a = 1;
	//private int turn_b = 1;
	private int num[] = {-1,1,0};

	private Image img;     // �I�t�X�N���[���C���[�W
	private Graphics offg; // �I�t�X�N���[���p�̃O���t�B�b�N�X
	private int width, height;

	private void initialize() {
		int i,j;

		for(j=0; j<ySize; j++) {
			state[0][j] = state[xSize-1][j] = Color.BLACK;
		}
		for (i=1;i<xSize-1;i++) {
			state[i][0] = state[i][ySize-1] = Color.BLACK;
			for (j=1;j<ySize-1;j++) {
				state[i][j] = Color.WHITE;
			}
		}
		xL = yL = 2;
		xR = xSize-3; yR = ySize-3;
		xA = yA = 40;
		dxL = dxR = 0;
		dyL = 1; dyR = -1;
		liveL = liveR = true;
	}

	@Override
	public void init() {
		xSize = ySize = 80;
		block = 4;
		state = new Color[xSize][ySize];
		message = "Game started!";
		font = new Font("Monospaced", Font.PLAIN, 12);
		setFocusable(true);
		addKeyListener(this);
		Dimension size = getSize();
		width = size.width; height = size.height;
		img  = createImage(width, height);
		offg = img.getGraphics();
	}

	@Override
	public void start() {
		if (thread==null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void stop() {
		if (thread != null) {
			thread = null;
		}
	}

	@Override
	public void paint(Graphics g) {
		// �S�̂��w�i�F�œh���Ԃ��B
		offg.clearRect(0, 0, width, height); 
		
		 // ���U�A�ʂ̉摜�i�I�t�X�N���[���j�ɏ�������
		int i, j;
		for (i=0; i<xSize; i++) {
			for (j=0; j<ySize; j++) {
				offg.setColor(state[i][j]);
				offg.fillRect(i*block, j*block, block, block);
			}
		}
		offg.setFont(font);
		offg.setColor(Color.GREEN.darker());
		offg.drawString(message, 2*block, block*(ySize+3));
		offg.setColor(Color.RED.darker());
		offg.drawString("Left:  A(L), S(D), D(U), F(R)", 2*block, block*(ySize+6));
		offg.setColor(Color.BLUE.darker());
		offg.drawString("Right: H(L), J(D), K(U), L(R)", 2*block, block*(ySize+9));
	
		g.drawImage(img, 0, 0, this);  // ���C�ɉ��ʂɃR�s�[
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread==thread) {
			initialize();
			requestFocus();
			while (liveL&&liveR) {
			
				xA += dxA; yA += dyA;
				
				if (state[xA][yA]!=Color.WHITE) {
					xA -= dxA; yA -= dyA;
				}
				state[xA][yA] = Color.BLACK;
				xL += dxL; yL += dyL;
				if (state[xL][yL]!=Color.WHITE) {
					liveL = false;
				} else {
					state[xL][yL] = Color.RED;
				}
				xR += dxR; yR += dyR;
				if (state[xR][yR]!=Color.WHITE) {
					liveR = false;
					if(xR==xL && yR==yL) {
						liveL = false;
						state[xL][yL] = Color.MAGENTA.darker();
					}
				} else {
					state[xR][yR] = Color.BLUE;
				}
				if (!liveL) {
					if (!liveR) {
						message = "Draw!";
					} else {
						//countR++;
						message = "R won!";
					}
				} else if (!liveR) {
					//countL++;
					message = "L won!";
				}
				repaint();
				try{
					Thread.sleep(250);
				} catch(InterruptedException e) {}
			}
			try{
				Thread.sleep(1750);
			} catch(InterruptedException e) {}
		}
	}
	
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		//add
		Random rnd = new Random(); //rand変数を作成
		dxA = num[rnd.nextInt(3)]; //中心から始まるAIは、ユーザがキーをタイプする毎に動く。
        if(dxA == 0){
        	dyA = num[rnd.nextInt(2)];
        }
        else{
        	dyA = 0;
        }
		while (state[xA+dxA][yA+dyA]!=Color.WHITE) {
			dxA = num[rnd.nextInt(3)];
        	if(dxA == 0){
        		dyA = num[rnd.nextInt(2)];
        	}
        	else{
        		dyA = 0;
        	}
		}
		switch (key) {
		case 'A':  dxL =-1; dyL = 0; break;
		case 'S':  dxL = 0; dyL = 1; break;
		case 'D':  dxL = 0; dyL =-1; break;
		case 'F':  dxL = 1; dyL = 0; break;
		case 'H':  dxR =-1; dyR = 0; break;
		case 'J':  dxR = 0; dyR = 1; break;
		case 'K':  dxR = 0; dyR =-1; break;
		case 'L':  dxR = 1; dyR = 0; break;

		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}