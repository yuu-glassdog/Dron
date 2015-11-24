import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JApplet;

// bgm
import java.applet.AudioClip;

public class Dron extends JApplet implements Runnable, KeyListener {
	private Color state[][];
	private int xSize, ySize;
	private int block;
	private int xL, yL, xR, yR;
	private int dxL, dyL, dxR, dyR;
	private boolean liveL, liveR;
	private int countL, countR;
	private Thread thread;
	private String message;
	private Font font;
	private int bKeyL = 'D', bKeyR = 'I';	// １つ前に押したキー(最初の進行方向で初期化)

	private Image img;     // オフスクリーンイメージ
	private Graphics offg; // オフスクリーン用のグラフィックス
	private int width, height;

	// スタート画面表示
	private String title;
	private int g_state;  // ゲームの状態 0:ゲーム開始前 1:ゲーム中 2:ゲーム終了


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
		dxL = dxR = 0;
		dyL = 1; dyR = -1;
		liveL = liveR = true;
	}

	@Override
	public void init() {
		xSize = ySize = 80;
		block = 4;
		state = new Color[xSize][ySize];
		g_state = 0;
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
		// 全体を背景色で塗りつぶす。
		offg.clearRect(0, 0, width, height);
		if ( g_state == 0 ) {
			// タイトル表示
			title = "どろん";
			font = new Font("Monospaced", Font.BOLD, 30);
			offg.setFont(font);
			offg.setColor(Color.BLACK);
			offg.drawString(title, block*(xSize/3), block*(ySize/3));
			
			message = "Press the Enter";
			font = new Font("Monospaced", Font.BOLD, 20);
			offg.setFont(font);
			offg.setColor(Color.RED);
			offg.drawString(message, block*(xSize/40*9), block*(ySize/4*3));
			
			font = new Font("Monospaced", Font.PLAIN, 12);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString("Left:  S(←), D(↓), E(↑), F(→)", 2*block, block*(ySize+6));
			offg.setColor(Color.BLUE.darker());
			offg.drawString("Right: J(←), K(↓), I(↑), L(→)", 2*block, block*(ySize+9));
		} else if ( g_state == 1 ) {
			message = "Game started!";
			font = new Font("Monospaced", Font.PLAIN, 12);
			// 一旦、別の画像（オフスクリーン）に書き込む
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
			offg.drawString("Left:  S(←), D(↓), E(↑), F(→)", 2*block, block*(ySize+6));
			offg.setColor(Color.BLUE.darker());
			offg.drawString("Right: J(←), K(↓), I(↑), L(→)", 2*block, block*(ySize+9));
		} else {
			// 結果の表示
			font = new Font("Monospaced", Font.BOLD, 30);
			offg.setFont(font);
			offg.drawString(message, block*(xSize/3), block*(ySize/3));

			message = "Replay the game ?";
			font = new Font("Monospaced", Font.BOLD, 17);
			offg.setFont(font);
			offg.setColor(Color.GREEN.darker());
			offg.drawString(message, block*(xSize/40*9), block*(ySize/3*2));

			message = "Yes: Press the Enter";
			font = new Font("Monospaced", Font.BOLD, 12);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(message, block*(xSize/3), block*(ySize/4*3));
			message = " No: Close the Window";
			offg.setColor(Color.BLUE.darker());
			offg.drawString(message, block*(xSize/3), block*(ySize/5*4));

			font = new Font("Monospaced", Font.PLAIN, 12);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString("Left:  S(←), D(↓), E(↑), F(→)", 2*block, block*(ySize+6));
			offg.setColor(Color.BLUE.darker());
			offg.drawString("Right: J(←), K(↓), I(↑), L(→)", 2*block, block*(ySize+9));
		}
		g.drawImage(img, 0, 0, this);  // 一気に画面にコピー
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		AudioClip hit = getAudioClip(getDocumentBase(), "18am10.wav");
		AudioClip bgm = getAudioClip(getDocumentBase(), "game_maoudamashii_7_event41.mid");
		while (thisThread==thread) {
			initialize();
			requestFocus();
			if ( g_state == 1 ) {
				bgm.loop();
				while (liveL&&liveR) {
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
							g_state = 2;
							hit.play();
							bgm.stop();
							message = "Draw!";
							offg.setColor(Color.YELLOW.darker());
						} else {
							countR++;
							g_state = 2;
							hit.play();
							bgm.stop();
							message = "R won!";
							offg.setColor(Color.BLUE.darker());
						}
					} else if (!liveR) {
						countL++;
						g_state = 2;
						hit.play();
						bgm.stop();
						message = "L won!";
						offg.setColor(Color.RED.darker());
					}
					repaint();
					try{
						Thread.sleep(250);
					} catch(InterruptedException e) {}
				}
				try{
					Thread.sleep(1750);
				} catch(InterruptedException e) {}
			} else {

			}
		}
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if ( g_state == 0 || g_state == 2 ) {
			if ( key == KeyEvent.VK_ENTER ) {
				g_state = 1;
			}
		} else if ( g_state == 1 ){
			switch (key) {
			case 'S':  if ( bKeyL == 'F' ) { break; }					// 逆向き入力の即死回避
			else { dxL =-1; dyL = 0; bKeyL = key; break;	} 	// 1P左
			case 'D':  if ( bKeyL == 'E' ) { break; }  
			else { dxL = 0; dyL = 1; bKeyL = key; break; }	// 1P下
			case 'E':  if ( bKeyL == 'D' ) { break; }
			else { dxL = 0; dyL =-1; bKeyL = key; break;	}	// 1P上
			case 'F':  if ( bKeyL == 'S' ) { break; }
			else { dxL = 1; dyL = 0; bKeyL = key; break;	}	// 1P右
			case 'J':  if ( bKeyR == 'L' ) { break; }
			else { dxR =-1; dyR = 0; bKeyR = key; break; }	// 2P左
			case 'K':  if ( bKeyR == 'I' ) { break; }
			else { dxR = 0; dyR = 1; bKeyR = key; break; }	// 2P下
			case 'I':  if ( bKeyR == 'K' ) { break; }
			else { dxR = 0; dyR =-1; bKeyR = key; break; }	// 2P上
			case 'L':  if ( bKeyR == 'J' ) { break; }
			else { dxR = 1; dyR = 0; bKeyR = key; break; }	// 2P右
			}
		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
