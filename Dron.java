import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import javax.swing.JApplet;

// 音再生
import java.io.File;
import javax.sound.sampled.*;

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

	private Image img;      // オフスクリーンイメージ
	private Graphics offg;  // オフスクリーン用のグラフィックス
	private Image img2;		// キーマップイメージ
	private int width, height;

	// 画面表示
	private boolean threadSuspended = true;
	private String title;
	private int g_state;  // ゲームの状態 0:開始前 1:ゲーム中 2:引き分け 3:R勝利 4:L勝利 5:ポーズ中

	// 音再生
	private String sound;
	private AudioInputStream bgm_in, hit_in;
	private Clip bgm = null, hit = null;
	private int soundVolume = 5;    // 音量 0～10

	// 難易度
	private String difficulty;
	private boolean normal = true;    // 難易度 true:Normal false:Hard

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
		bKeyL = 'D'; bKeyR = 'I';
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
		// タイトル画面
		if ( g_state == 0 ) {
			// タイトル表示
			title = "どろん";
			font = new Font("Monospaced", Font.BOLD, 35);
			offg.setFont(font);
			offg.setColor(Color.BLACK);
			offg.drawString(title, block*(xSize/3), block*(ySize/4));
			// 難易度表示
			message = "↑";
			font = new Font("Monospaced", Font.BOLD, 10);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(message, block*(xSize/3+12), block*(ySize/2));
			if ( normal == true ) { difficulty = "Normal"; } else { difficulty = " Hard "; }
			font = new Font("Monospaced", Font.BOLD, 15);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(difficulty, block*(xSize/3+7), block*(ySize/2+5));
			message = "↓";
			font = new Font("Monospaced", Font.BOLD, 10);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(message, block*(xSize/3+12), block*(ySize/2+10));
			//
			message = "Press the Enter";
			font = new Font("Monospaced", Font.BOLD, 20);
			offg.setFont(font);
			offg.setColor(Color.RED);
			offg.drawString(message, block*(xSize/40*9), block*(ySize/4*3));
		// ゲーム中画面
		} else if ( g_state == 1 ) {
			// 一旦、別の画像（オフスクリーン）に書き込む
			int i, j;
			for (i=0; i<xSize; i++) {
				for (j=0; j<ySize; j++) {
					offg.setColor(state[i][j]);
					offg.fillRect(i*block, j*block, block, block);
				}
			}
		// ポーズ画面
		} else if ( g_state == 5 ) {
			int i, j;
			for (i=0; i<xSize; i++) {
				for (j=0; j<ySize; j++) {
					lowerAlpha(state[i][j]);    // 描画色を薄く
					offg.fillRect(i*block, j*block, block, block);
				}
			}
			message = "Pause";
			font = new Font("Monospaced", Font.BOLD, 25);
			offg.setFont(font);
			offg.setColor(Color.GREEN.darker());
			offg.drawString(message, block*(xSize/3+5), block*(ySize/2));
		// リザルト画面
		} else {
			// 結果の表示
			switch ( g_state ) {
			case 2: message = "Draw!"; offg.setColor(Color.YELLOW.darker()); break;    // 引分
			case 3: message = "R won!"; offg.setColor(Color.BLUE.darker()); break;     // R勝利
			case 4: message = "L won!"; offg.setColor(Color.RED.darker()); break;      // L勝利
			}
			font = new Font("Monospaced", Font.BOLD, 35);
			offg.setFont(font);
			offg.drawString(message, block*(xSize/3+2), block*(ySize/4));

			// 難易度表示
			message = "↑";
			font = new Font("Monospaced", Font.BOLD, 10);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(message, block*(xSize/3+12), block*(ySize/2));
			if ( normal == true ) { difficulty = "Normal"; } else { difficulty = " Hard "; }
			font = new Font("Monospaced", Font.BOLD, 15);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(difficulty, block*(xSize/3+7), block*(ySize/2+5));
			message = "↓";
			font = new Font("Monospaced", Font.BOLD, 10);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(message, block*(xSize/3+12), block*(ySize/2+10));
			//
			message = "Replay the game ?";
			font = new Font("Monospaced", Font.BOLD, 17);
			offg.setFont(font);
			offg.setColor(Color.GREEN.darker());
			offg.drawString(message, block*(xSize/40*9+2), block*(ySize/4*3));

			message = "Yes: Press the Enter";
			font = new Font("Monospaced", Font.BOLD, 12);
			offg.setFont(font);
			offg.setColor(Color.RED.darker());
			offg.drawString(message, block*(xSize/3), block*(ySize/4*3+6));
			message = "No : Close the Window";
			offg.setColor(Color.BLUE.darker());
			offg.drawString(message, block*(xSize/3), block*(ySize/4*3+10));
		}
		// 音量調整
		sound = "Volume " + " ← " + String.format("%1$02d", soundVolume) + " → ";
		font = new Font("Monospaced", Font.BOLD, 15);
		offg.setFont(font);
		offg.setColor(Color.GREEN.darker());
		offg.drawString(sound, block*(xSize/3-1), block*(ySize+4));
		//
		offg.drawImage(img2, 0, block*(ySize+5), this);    // keymapの表示
		g.drawImage(img, 0, 0, this);                      // 一気に画面にコピー
	}

	// 色のアルファ値を下げる
	public void lowerAlpha(Color c){
		if ( c == Color.BLACK ) {
			offg.setColor(new Color(0, 0, 0, 60));
		} else if ( c == Color.RED ) {
			offg.setColor(new Color(255, 0, 0, 60));
		} else if ( c == Color.BLUE ) {
			offg.setColor(new Color(0, 0, 255, 60));
		} else if ( c == Color.WHITE ) {
			offg.setColor(c);
		}
	}

	// 音楽ファイルの設定
	public void setSound() {
		try {
			bgm = AudioSystem.getClip();
			hit = AudioSystem.getClip();
			bgm_in = AudioSystem.getAudioInputStream(new File("game_maoudamashii_7_event41.mid"));
			hit_in = AudioSystem.getAudioInputStream(new File("18am10.wav"));
			bgm.open(bgm_in);
			hit.open(hit_in);
			changeSoundVolume(soundVolume);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void controlByLinearScalar(FloatControl control, double linearScalar) {
		control.setValue((float)Math.log10(linearScalar) * 20);
	}

	// 音量の変更
	public void changeSoundVolume(int volume) {
		double set_v = (double)volume / 10;
		FloatControl bgm_control = (FloatControl)bgm.getControl(FloatControl.Type.MASTER_GAIN);
		FloatControl hit_control = (FloatControl)hit.getControl(FloatControl.Type.MASTER_GAIN);
		controlByLinearScalar(bgm_control, set_v);
		controlByLinearScalar(hit_control, set_v);
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (thisThread==thread) {
			setSound();
			initialize();
			requestFocus();
			// ゲーム中でない
			if ( g_state != 1 ) {
				// ポーズ画面の場合bgmを停止
				if ( g_state == 5 ) {
					bgm.stop();
				}
				while ( threadSuspended ) {
					synchronized(this) {
						try {
							wait();
						} catch (InterruptedException e) {}
					}
				}
			// ゲーム中
			} else {
				bgm.loop(-1);
				while (liveL&&liveR) {
					while ( threadSuspended ) {
						synchronized(this) {
							try {
								wait();
							} catch (InterruptedException e) {}
						}
					}
					// 難易度がHard
					if ( normal == false ) {
						// rand wall
						if ( liveA == true ) {
							xA += dxA; yA += dyA;
							if (state[xA][yA]!=Color.WHITE) {
								xA -= dxA; yA -= dyA;
							}
							state[xA][yA] = Color.BLACK;
						}
						//
					}
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
							threadSuspended = true;
							hit.start();
							bgm.stop();
						} else {
							g_state = 3;    // R勝利
							threadSuspended = true;
							hit.start();
							bgm.stop();
						}
					} else if (!liveR) {
						g_state = 4;        // L勝利
						threadSuspended = true;
						hit.start();
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
			}
		}
	}

	public synchronized void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		// 左右キーで音量調整
		switch ( key ) {
		case KeyEvent.VK_LEFT: if ( soundVolume != 0 ) { changeSoundVolume(--soundVolume); repaint(); } break;
		case KeyEvent.VK_RIGHT: if ( soundVolume != 10 ) { changeSoundVolume(++soundVolume); repaint(); } break;
		}
		// タイトル画面、リザルト画面でエンターキーを押すとゲーム開始、上下キーで難易度変更
		if ( g_state != 1 && g_state != 5 ) {
			// 難易度変更
			if ( key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN ) {			
				if ( normal == true ) { 
					normal = false;
					repaint();
				} else {
					normal = true;
					repaint();
				}
			}
			// ゲーム開始
			if ( key == KeyEvent.VK_ENTER ) {
				threadSuspended = false;
				g_state = 1;
				notify();
			}
		// ゲーム中
		} else if ( g_state == 1 ) {
			// スペースキーを押すとポーズ
			if ( key == KeyEvent.VK_SPACE ) {
				g_state = 5;
				bgm.stop();
				repaint();
				threadSuspended = true;
				notify();
			} else if ( key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT ) { 

			} else {
				// 難易度がHard
				if ( normal == false ) {
					// rand wall
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
					//
				}
			}
			// プレイヤーのキー入力
			switch (key) {
			case 'S':  if ( bKeyL == 'F' ) { break; }			// 逆向き入力の即死回避
			else { dxL =-1; dyL = 0; bKeyL = key; break;	} 	// 1P左
			case 'D':  if ( bKeyL == 'E' ) { break; }  
			else { dxL = 0; dyL = 1; bKeyL = key; break; }	    // 1P下
			case 'E':  if ( bKeyL == 'D' ) { break; }
			else { dxL = 0; dyL =-1; bKeyL = key; break;	}	// 1P上
			case 'F':  if ( bKeyL == 'S' ) { break; }
			else { dxL = 1; dyL = 0; bKeyL = key; break;	}	// 1P右
			case 'J':  if ( bKeyR == 'L' ) { break; }
			else { dxR =-1; dyR = 0; bKeyR = key; break; }      // 2P左
			case 'K':  if ( bKeyR == 'I' ) { break; }
			else { dxR = 0; dyR = 1; bKeyR = key; break; }	    // 2P下
			case 'I':  if ( bKeyR == 'K' ) { break; }
			else { dxR = 0; dyR =-1; bKeyR = key; break; }	    // 2P上
			case 'L':  if ( bKeyR == 'J' ) { break; }
			else { dxR = 1; dyR = 0; bKeyR = key; break; }	    // 2P右
			}
		// ポーズ中にスペースキーを押すとゲーム再開
		} else {
			if ( key == KeyEvent.VK_SPACE ) {
				g_state = 1;
				bgm.loop(-1);
				threadSuspended = false;
				notify();
			}
		}
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
