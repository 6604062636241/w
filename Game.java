package oop3;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Game extends JPanel implements ActionListener,KeyListener,MouseListener {
    private int playerX=200,playerY=520,playerHealth=200,ammoCount=20,killcount=0;
    private boolean isJumping=false,isGameOver=false,isReloading=false,gameStarted=false;
    private ArrayList<Monster>monsters=new ArrayList<>();
    private ArrayList<Bullet>bullets=new ArrayList<>();
    private Random random=new Random();
    private boolean facingRight=true;
    private JSlider healthBar;
    private JLabel reloadLabel;
    private JButton startButton;
    private Timer gameTimer;
    private Timer healthTimer;
    private Image playerImage;
    private Image monsterImage;
    private Image background;
    private Image gameOver;
    private Image winbg;

    public Game() {
    	playerImage = new ImageIcon(getClass().getResource("MainChar.png")).getImage();
        monsterImage = new ImageIcon(getClass().getResource("Monster.png")).getImage();
        background = new ImageIcon(getClass().getResource("BG.jpg")).getImage();
        gameOver = new ImageIcon(getClass().getResource("gameOver.png")).getImage();
        winbg = new ImageIcon(getClass().getResource("win.png")).getImage();
        setFocusable(true);
        setPreferredSize(new Dimension(800, 600));
        setLayout(null);
        addKeyListener(this);
        addMouseListener(this);
        
        healthBar=new JSlider(0,200,playerHealth);
        healthBar.setPreferredSize(new Dimension(200,50));
        healthBar.setBorder(BorderFactory.createLineBorder(Color.black));
        healthBar.setBounds(10, 10, 200, 30);
        healthBar.setForeground(Color.RED);
        healthBar.setVisible(false);
        healthBar.setEnabled(false);
        add(healthBar);
        
        reloadLabel=new JLabel("Reloading",JLabel.CENTER);
        reloadLabel.setFont(new Font(null,Font.PLAIN,30));
        reloadLabel.setForeground(Color.white);
        reloadLabel.setVisible(false);
        reloadLabel.setBounds(0,0,1000,1000);
        add(reloadLabel);
        
        startButton = new JButton("Start Game");
        startButton.setBounds(350, 300, 100, 50);
        startButton.addActionListener(e->startGame());
        add(startButton);
        
        gameTimer=new Timer(30,this);
        healthTimer=new Timer(500,this);
        
        spawnMonstersRandomly();
    }
    private void startGame() {
        gameStarted=true;
        startButton.setVisible(false);
        gameTimer.start();
        healthTimer.start();
        healthBar.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted&&!isGameOver) {
            movePlayer();
            moveMonsters();
            moveBullets();
            checkCollisions();
            repaint();
        }
    }

    boolean check=true;
    int temp;
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        startButton.setLocation(getWidth()/2-50,getHeight()/2);
        if (playerY<getHeight()/2+100&&check) {
        	g.drawImage(background,0,0,getWidth(),playerY+100,this);
        	temp=playerY+100;
        	check=false;
        } else {
        	if(check) {
        		temp=getHeight();
        	}
        	g.drawImage(background,0,0,getWidth(),temp,this);
        }
        if(playerX>getWidth()-20) {
        	playerX=getWidth()-20;
        }
        if (!gameStarted) {
            g.setColor(Color.white);
            g.setFont(new Font(null,Font.PLAIN,30));
            g.drawString("Monster Hunter",getWidth()/2-100,getHeight()/2-100);
            return;
        }
        if (facingRight){
        	g.drawImage(playerImage,playerX,playerY,50,50,this);
        } else {
        	g.drawImage(playerImage,playerX+20,playerY,-50,50,this);
        }
        
        for (Monster monster:monsters) {
        	g.drawImage(monsterImage,monster.x,monster.y,40,50,this);
        }
        
        g.setColor(Color.BLACK);
        
        for (Bullet bullet:bullets) {
            g.fillOval(bullet.x,bullet.y,10,10);
        }
        
        if (isGameOver) {
        	healthBar.setVisible(false);
        	if (killcount>=10) {
            	g.setColor(Color.blue);
            	g.setFont(new Font(null,Font.PLAIN,getHeight()/25));
            	g.drawImage(winbg,0,0,getWidth(),getHeight(),this);
                return;
        	}
            g.setColor(Color.RED);
            g.setFont(new Font(null,Font.PLAIN,getHeight()/25));
            g.drawImage(gameOver,0,0,getWidth(),getHeight(),this);
        }
        
        healthBar.setUI(new BasicSliderUI(healthBar) {
        	@Override
        	public void paintThumb(Graphics g) {}
        	@Override
            public void paintTrack(Graphics g) {
                g.setColor(slider.getForeground());
                g.fillRect(0,healthBar.getHeight()/2-5,5*playerHealth,healthBar.getHeight());
            }
        });
    }

    private void movePlayer() {
        if (isJumping) {
            playerY-=10;
            if (playerY<=450) {
            	isJumping=false;
            }
        } else if (playerY<520) {
            playerY+=10;
        }
    }

    private void moveMonsters() {
        for (Monster monster:monsters) {
            if (monster.x<playerX) {
            	monster.x+=monster.speed;
            } else {
            	monster.x-=monster.speed;
            }
            
            if (Math.abs(monster.x-playerX)<30&&Math.abs(monster.y-playerY)<30) {
                playerHealth-=1;
                if (playerHealth<=0) {
                	isGameOver=true;
                }
            }
        }
    }

    private void moveBullets() {
        for (int i=0;i<bullets.size();i++) {
            Bullet bullet=bullets.get(i);
            if (bullet.direction==-1) {
            	bullet.x-=20;
            }
            bullet.x+=bullet.direction*bullet.speed;
            if (bullet.x<playerX-300||bullet.x>playerX+300) {
                bullets.remove(i);
                i--;
            }
        }
    }

    private void checkCollisions() {
        for (int i=0;i<monsters.size();i++) {
            Monster monster=monsters.get(i);
            for (int j=0;j<bullets.size();j++) {
                Bullet bullet=bullets.get(j);
                if (bullet.x>=monster.x-20&&bullet.y>=monster.y-20) {
                    monster.health-=10;
                    bullets.remove(j);
                    if (monster.health<=0) {
                    	killcount++;
                    	monsters.remove(monster);
                    	break;
                    }
                }
            }
        }
        if (monsters.isEmpty()) {
        	if(killcount>=10) {
        		isGameOver=true;
        	}
            spawnMonstersRandomly();
        }
    }

    private void spawnMonstersRandomly() {
        int count=random.nextInt(3)+1;
        for (int i=0;i<count;i++) {
            int x=Math.max(getWidth()==0?1000:getWidth(),getWidth()+(random.nextInt(100)+random.nextInt(100)));
            int y=520;
            monsters.add(new Monster(x,y,2,100));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    	if (gameStarted) {
	        if (e.getKeyCode()==KeyEvent.VK_W&&!isJumping) {
	            isJumping=true;
	        } else if (e.getKeyCode()==KeyEvent.VK_D) {
	            playerX+=10;
	            facingRight=true;
	        } else if (e.getKeyCode()==KeyEvent.VK_A) {
	            playerX-=10;
	            facingRight=false;
	            if (playerX<=0) {
	            	playerX=0;
	            }
	        } else if (e.getKeyCode()==KeyEvent.VK_R) {
	            reload();
	        }
    	}
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)&&!isReloading&&gameStarted) {
            shoot();
        }
    }

    private void shoot() {
        if (ammoCount>0) {
            int direction=facingRight ? 1:-1;
            bullets.add(new Bullet(playerX+25,playerY+25,direction,40));
            ammoCount--;
        } else {
            reloadLabel.setVisible(true);
            isReloading=true;
            Thread thread=new Thread(()->{
                ammoCount=20;
                reloadLabel.setVisible(false);
                isReloading=false;
            });
            thread.start();
        }
    }

    private void reload() {
        if (ammoCount<20) {
            reloadLabel.setVisible(true);
            isReloading=true;
            Timer reloadTimer=new Timer(2000,e->{
            	reloadLabel.setLocation(playerX-520,playerY-520);
                ammoCount=20;
                reloadLabel.setVisible(false);
                isReloading=false;
            });
            reloadTimer.setRepeats(false);
            reloadTimer.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    
    public static void main(String[] args) {
        JFrame frame=new JFrame("Monster Hunter Game");
        frame.add(new Game());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Monster {
    int x,y,speed,health;
    Monster(int x,int y,int speed,int health) {
        this.x=x;
        this.y=y;
        this.speed=speed;
        this.health=health;
    }
}

class Bullet {
    int x,y,direction,speed;
    Bullet(int x,int y,int direction,int speed) {
        this.x=x;
        this.y=y;
        this.direction=direction;
        this.speed=speed;
    }
}
