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
import java.applet.Applet;

// start
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.BorderLayout;


public class Dron extends JApplet implements Runnable, KeyListener, ActionListener {
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
	private int g_state;
	private JPanel p_st;
	private JButton bt_st;

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
		System.out.println("a");
		xSize = ySize = 80;
		block = 4;
		state = new Color[xSize][ySize];
		g_state = 0;
		message = "Game started!";
		font = new Font("Monospaced", Font.PLAIN, 12);
		
		System.out.println("aa");
		p_st = new JPanel();
		p_st.setLayout(null);
		bt_st = new JButton("Start");
		bt_st.setBounds(50, 50, 100, 50);
		bt_st.addActionListener(this);
		p_st.add(bt_st);
		Container contentPane = getContentPane();
		contentPane.add(p_st, BorderLayout.CENTER);

		setFocusable(true);
		addKeyListener(this);
		Dimension size = getSize();
		width = size.width; height = size.height;
		img  = createImage(width, height);
		offg = img.getGraphics();
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("b");
		if ( e.getSource() == bt_st ) {
			g_state = 1;
			remove(p_st);
			start();
			//			thread = new Thread(this);
			//			thread.start();
		}
	}

	@Override
	public void start() {
		if (thread==null && g_state == 1) {
			thread = new Thread(this);
			thread.start();
		}
	}

	@Override
	public void stop() {
		System.out.println("stop");
		if (thread != null) {
			thread = null;
		}
	}

	@Override
	public void paint(Graphics g) {
		System.out.println("c");
		if ( g_state == 0 ) {
			
		} else if ( g_state == 1 ) {
			// 全体を背景色で塗りつぶす。
			offg.clearRect(0, 0, width, height);
			// 		if ( g_state == 0 ) {
			//			offg.setFont(font);
			//			offg.setColor(Color.RED.darker());
			//			offg.drawString(message, 25*block, 25*block);
			//		} else {
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
			//		}
			g.drawImage(img, 0, 0, this);  // 一気に画面にコピー
		} else {
			// 全体を背景色で塗りつぶす。
			offg.clearRect(0, 0, width, height);
			// 		if ( g_state == 0 ) {
			//			offg.setFont(font);
			//			offg.setColor(Color.RED.darker());
			//			offg.drawString(message, 25*block, 25*block);
			//		} else {
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
			//		}
			g.drawImage(img, 0, 0, this);  // 一気に画面にコピー
		}


	}

	public void run() {
		System.out.println("d");
		Thread thisThread = Thread.currentThread();
		AudioClip bgm = getAudioClip(getDocumentBase(), "menuettm.mid");
		bgm.loop();
		while (thisThread==thread) {
			initialize();
			requestFocus();
			//			if ( g_state == 0 ) {
			//				bt_st = new JButton("Start");
			//				add(bt_st);
			//				System.out.printf("スタート画面\n");
			//				try{
			//					Thread.sleep(10000);
			//				} catch(InterruptedException e) {}
			//				g_state = 1;
			//				bgm.loop();
			//			} else {
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
						bgm.stop();
						message = "Draw!";
						System.out.printf("終了画面\n");
					} else {
						countR++;
						g_state = 2;
						bgm.stop();
						message = "R won!";
						System.out.printf("終了画面\n");
						stop();
					}
				} else if (!liveR) {
					countL++;
					g_state = 2;
					bgm.stop();
					message = "L won!";
					System.out.printf("終了画面\n");
				}
				repaint();
				try{
					Thread.sleep(250);
				} catch(InterruptedException e) {}
			}
			try{
				Thread.sleep(1750);
			} catch(InterruptedException e) {}
			//			}

		}

	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
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

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
