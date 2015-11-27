import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.JApplet;

// bgm
import java.applet.AudioClip;

public class Dron extends JApplet implements Runnable, KeyListener {
	private Color state[][];
	private int xSize, ySize;
	private int block;
	private int xL, yL, xR, yR;
	private int dxL, dyL, dxR, dyR;
	private boolean liveL, liveR, liveA;
	private Thread thread;
	private String message;
	private Font font;
	private int bKeyL = 'D', bKeyR = 'I';	// １つ前に押したキー(最初の進行方向で初期化)

	// rand wall
	private int xA,yA;
	private int dxA, dyA;
	private int num[] = {-1,1,0};

	private Image img;     // オフスクリーンイメージ
	private Graphics offg; // オフスクリーン用のグラフィックス
	private Image img2;		// キーマップイメージ
	private int width, height;

	// スタート画面表示
	private String title;
	private int g_state;  // ゲームの状態 0:ゲーム開始前 1:ゲーム中 2:引き分け 3:R勝利 4:L勝利


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
		liveL = liveR = liveA = true;
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
		img2 = getImage(getCodeBase(), "keymap.png");
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
		} else if ( g_state == 1 ) {
			// 一旦、別の画像（オフスクリーン）に書き込む
			int i, j;
			for (i=0; i<xSize; i++) {
				for (j=0; j<ySize; j++) {
					offg.setColor(state[i][j]);
					offg.fillRect(i*block, j*block, block, block);
				}
			}
		} else {
			// 結果の表示
			switch ( g_state ) {
			case 2: message = "Draw!"; offg.setColor(Color.YELLOW.darker()); break;    // 引分
			case 3: message = "R won!"; offg.setColor(Color.BLUE.darker()); break;     // R勝利
			case 4: message = "L won!"; offg.setColor(Color.RED.darker()); break;      // L勝利
			}
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
			message = "No : Close the Window";
			offg.setColor(Color.BLUE.darker());
			offg.drawString(message, block*(xSize/3), block*(ySize/5*4));
		}
		offg.drawImage(img2, 0, block*(ySize+4), this);    // keymapの表示
		g.drawImage(img, 0, 0, this);    // 一気に画面にコピー
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		// SE
		AudioClip hit = getAudioClip(getDocumentBase(), "18am10.wav");
		// BGM
		AudioClip bgm = getAudioClip(getDocumentBase(), "game_maoudamashii_7_event41.mid");
		while (thisThread==thread) {
			initialize();
			requestFocus();
			if ( g_state == 1 ) {
				bgm.loop();    // BGM開始
				while (liveL&&liveR) {
					// rand wall
					if ( liveA == true ) {
						xA += dxA; yA += dyA;
						if (state[xA][yA]!=Color.WHITE) {
							xA -= dxA; yA -= dyA;
						}
						state[xA][yA] = Color.BLACK;
					}
					//
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
							g_state = 2;    // 引分
							hit.play();
							bgm.stop();
						} else {
							g_state = 3;    // R勝利
							hit.play();
							bgm.stop();
						}
					} else if (!liveR) {
						g_state = 4;        // L勝利
						hit.play();
						bgm.stop();
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
		if ( g_state != 1 ) {
			if ( key == KeyEvent.VK_ENTER ) {
				g_state = 1;
			}
		} else {
			Random rnd = new Random();
			// 中心から始まるAIは、ユーザがキーをタイプする毎に動く。
			dxA = num[rnd.nextInt(3)];
			if(dxA == 0){
				dyA = num[rnd.nextInt(2)];
			}
			else{
				dyA = 0;
			}
			while (state[xA+dxA][yA+dyA]!=Color.WHITE) {
				// 四方が白でないときAI停止
				if ( state[xA+1][yA]!=Color.WHITE && state[xA][yA+1]!=Color.WHITE && state[xA-1][yA]!=Color.WHITE && state[xA][yA-1]!=Color.WHITE) {
					liveA = false;
					break;
				}
				dxA = num[rnd.nextInt(3)];
				if(dxA == 0){
					dyA = num[rnd.nextInt(2)];
				}
				else{
					dyA = 0;
				}
			}
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
