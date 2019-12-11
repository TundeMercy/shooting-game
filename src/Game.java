import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;


/**
 * @author MERCY
 *
 */
public class Game extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	JFrame board;
	int x1=250,y1=450, x2=270,y2=440,bulletThatKills,enemyKilled;
	boolean  fired = false;
	static volatile boolean  pause=false;
	static Enemy enemy;
	static volatile int score;
	volatile int xDirection;
	static List<Bullet> bulletsList= new  ArrayList<Bullet>(10);
	static List<Enemy> enemyList = new ArrayList<Enemy>(10);
	AudioClip bgSound,collisionSound;
	JMenuBar menu;
	protected int life;
	protected volatile boolean killMe;
	protected boolean gameOver;

	//CONSTRUCTOR
	public Game(){
		super();
		life= 5;
		menu = new JMenuBar();
		bgSound = Applet.newAudioClip(Game.class.getResource("U too dey bless me.wav"));
		collisionSound = Applet.newAudioClip(Game.class.getResource("I don die.wav"));
		board = new JFrame("Tunde Game");
		board.setSize(500,500);
		setSize(500,500);
		setBackground(Color.BLACK);
		board.setLayout(new BorderLayout()	);
		board.add(this,BorderLayout.CENTER);
		board.add(menu, BorderLayout.NORTH);
		board.setVisible(true);
		board.addKeyListener(new KL());
		board.setLocationRelativeTo(null);
		board.setResizable(false);
		board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		playSound();
		//	bgSound.loop();
	}

	public void paintComponent(Graphics g){
		super.paintComponent(g);
		//render();
		g.setColor(Color.green);
		g.fillRect(x1, y1, 50, 12);
		g.setColor(Color.pink);
		g.fillRect(x2, y2, 11, 5);

		if ((!getBulletsList().isEmpty())&&(!gameOver)){
			g.setColor(Color.GREEN);
			for(int i=0; i<getBulletsList().size();i++){
				g.fillOval(getBulletsList().get(i).getBulletx(),
						getBulletsList().get(i).getBullety(), 11, 10);
			}
		}

		if ((!getEnemyList().isEmpty())&&(!gameOver)){
			for(int i=0;i<getEnemyList().size();i++){
				g.setColor(getEnemyList().get(i).getColor());
				g.fillRoundRect(getEnemyList().get(i).getX(),getEnemyList().get(i).getY(),
						getEnemyList().get(i).getSize(), 15,4,4);
			}

		}
		if((pause)&&(!gameOver)){
			g.setColor(Color.blue);
			g.setFont(new Font("arial",Font.BOLD,20));
			g.drawString("PAUSED!", 220, 100);
		}
		if(!gameOver){
			g.setColor(Color.white);
			g.setFont(new Font("sanserif",Font.BOLD|Font.ITALIC,18));
			g.drawString("Your score: "+score,getInsets().left+5, 
					getInsets().top+g.getFontMetrics().getHeight());
		}

		if(killMe){
			g.setColor(Color.red);
			g.drawString("LIFE REMAINING: "+ life, 40, 50);
		}

		if(gameOver){
			g.setFont(new Font("sanserif",Font.BOLD|Font.ITALIC,18));
			g.setColor(Color.red);
			g.drawString("GAME OVER!!!", 170, 100);
			g.setColor(Color.green);
			g.drawString("YOUR SCORE IS: "+ score, 150, 170);
		}
	}

	public void playSound(){
		collisionSound.play();
	}

	private synchronized List<Bullet> getBulletsList(){
		return bulletsList;
	}

	private synchronized static List<Enemy> getEnemyList(){
		return enemyList;
	}

	public synchronized void animate(){
		if(!pause){

			x1+=xDirection;
			x2+=xDirection;
			for(int i=0; i<getBulletsList().size();i++){
				if(getBulletsList().get(i)!=null){
					if ((getBulletsList().get(i)).getBullety()>8)
						(getBulletsList().get(i)).setBullety(
								getBulletsList().get(i).getBullety()-5);
					else //canFire=true;
						getBulletsList().remove(i);
				}
			}

			repaint();
		} 
	}

	//InputStream is = new FileInputStream(new File("sr\\U too dey bless me.wav"));

	public void detectCollision(){
		if(!pause){
			int[] theseCollides;
			theseCollides=collides();
			if(theseCollides!=null){
				bulletThatKills =theseCollides[1];
				enemyKilled=theseCollides[0];
				destroyThem();
				score++;
			}
		}
	}

	public static void controlEnemy(){
		for(int i=0; i<getEnemyList().size();i++){
			if(getEnemyList().get(i).getY()<475)
				getEnemyList().get(i).approach();
			else{
				getEnemyList().remove(i);
				if(score>0)
					score--;
			}
		}

	}

	static Thread contEnemy = new CtrlEnemy();



	private Thread againstMe = new Thread(){
		public void run(){
			while (true){
				if(!pause){
					try {
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					if (!getEnemyList().isEmpty()){
						for(int a = 0; a < getEnemyList().size(); a++){
							if(new Rectangle(getEnemyList().get(a).getX(),
									getEnemyList().get(a).getY(), 
									getEnemyList().get(a).getSize(),
									15).intersects(new Rectangle(x1,y2,50,10))){
								killMe = true;
								getEnemyList().remove(a);
								if (--life<1){
									pause = true;
									gameOver=true;
									killMe= false;
									repaint();
								}
								try {
									Thread.sleep(2000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							else
								killMe = false;
						}
					}
				}
			}
		}
	};

	public synchronized void destroyThem(){

		//playSound();
		getEnemyList().remove(enemyKilled);
		getBulletsList().remove(bulletThatKills);//fired=false;
		//canFire=true;
	}

	public synchronized int[] collides(){
		if(getEnemyList().size()>0)
			//if(!canFire){
			for(int i = 0;i<getBulletsList().size();i++){
				for(int e = 0;e<getEnemyList().size();e++){
					if(
							new Rectangle(getBulletsList().get(i).getBulletx(),
									getBulletsList().get(i).getBullety(), 
									11, 10).intersects(
											new Rectangle(getEnemyList().get(e).getX(),
													getEnemyList().get(e).getY(),
													getEnemyList().get(e).getSize(), 15))){

						int[] thisCollides ={e,i};
						return thisCollides;
					}
				}
			}
		else
			return null;
		return null;

	}



	public static void main(String[] args){
		startGame();
	}

	public static void startGame(){
		Game game = new Game();
		Thread t1 = new Thread(game);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t1.start();
		enemyFactory.start();
		contEnemy.start();
		game.againstMe.start();
		game.bulletFactory.start();
		//	count.start();
	}

	private static final class EnemyFactory extends Thread {
		public synchronized void run(){
			while(true){
				try{
					Thread.sleep(1000);
				}
				catch(Exception e){

				}
				if(!pause){
					enemy= new Enemy();
					getEnemyList().add(enemy);
				}
			}
		}
	}

	Thread bulletFactory = new Thread(){
		public void run(){
			while (true){
				if(!pause){
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Bullet bullet = new Bullet();
					bullet.setBulletx(x2);
					bullet.setBullety(y2);
					getBulletsList().add(bullet);
				}
			}
		}
	};

	private static final class CtrlEnemy extends Thread {
		public void run(){
			while(true){
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!pause)	
					controlEnemy();
			}

		}
	}


	class KL extends KeyAdapter{
		public void	keyPressed(KeyEvent e){
			int kp = e.getKeyCode();
			if(!pause){
				if(kp==KeyEvent.VK_LEFT){
					if(x1>getInsets().left+4)
						xDirection=-4;
					else
						xDirection=0;
				}
				if (kp==KeyEvent.VK_RIGHT){
					if(x1+50<500)
						xDirection=4;
					else
						xDirection=0;
				}
				if(kp==KeyEvent.VK_SPACE){
					//if (canFire){
					Bullet bullet= new Bullet();
					bullet.setBulletx(x2);
					bullet.setBullety(y2);
					getBulletsList().add(bullet);
					fired=true;
				}
			}
			if (kp == KeyEvent.VK_ENTER){
				if (!gameOver){
					pause=!pause;
					repaint();
				}
				else {
					life = 5;
					score = 0;
					gameOver = false;
				}
			}
		}
		public void keyReleased(KeyEvent e){
			xDirection=0;
		}
	}


	public void run() {
		while(true){
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			animate();
			detectCollision();
		}
	}

	/**static Thread count = new Thread(){
		public void run(){
			while(true){
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("No of enemy: "+enemyList.size());
				System.out.println("No of bullet: "+bulletsList.size());
			}
		}
	};
	 */

	static Thread enemyFactory = new EnemyFactory();
}

class Enemy{
	int x, y,size;
	boolean isAlive;
	private Color color;

	public Enemy(){
		Random rand =new Random();//Game.ran;
		x=10+ rand.nextInt(450);
		y=0;
		size=10+rand.nextInt(30);
		int R,G,B;
		R= 1+ rand.nextInt(255);
		G= 1+ rand.nextInt(255);
		B= 1+ rand.nextInt(255);
		color = new Color(R,G,B);
	}

	public void approach() {
		y+=3;

	}

	public boolean isAlive(){
		return isAlive;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}


	public int getSize() {
		return size;
	}

	public void die(){
		isAlive=false;
		setY(0);
	}
	public Color getColor(){
		return color;
	}
}

class Bullet{
	private int bulletx,bullety;
	public Bullet(){

	}

	public int getBulletx() {
		return bulletx;
	}

	public void setBulletx(int bulletx) {
		this.bulletx = bulletx;
	}

	public int getBullety() {
		return bullety;
	}

	public void setBullety(int bullety) {
		this.bullety = bullety;
	}
}


