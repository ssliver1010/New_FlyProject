import java.awt.Color;
import java.awt.event.*;
import javax.swing.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import java.io.*;
import java.util.Arrays;

// 확인용 주석 !

@SuppressWarnings("serial")
public class NewCatchGame extends JFrame 
{
	JMenuBar menuBar;
	JMenu menu;
	JMenuItem item1,item2;
	String result = "@";	
	JLabel[] fly = new JLabel[20];	// 파리 수를 늘리기 위해 배열 생성 
	FlyThread flyThread = null;
	TimeThread timeThread = null;
	int time,i,flycnt=0;	// 잡은 파리 수 카운트 할 변수 flycnt 생성 
	
	public NewCatchGame()
	{
		super("파리잡기 게임");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(400,400);
		
		//
		File file = new File("./Morning-Stroll.wav");
		
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(file);
			Clip clip = AudioSystem.getClip();
			clip.open(stream);
			clip.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
		//
		menuBar = new JMenuBar();
		menu = new JMenu("게임");
		item1 = new JMenuItem("게임설정");
		item2 = new JMenuItem("게임하기");
		
		menuBar.add(menu);
		menu.add(item1); menu.add(item2);
		setJMenuBar(menuBar);

		
		item1.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e)
			{
				// 입력받은 메세지 문자열을 리턴받아 변수 result에 저장
				result = JOptionPane.showInputDialog("게임에 사용할 문자를 입력하세요");
				
			}
		});	
		item2.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e)
			{				
				// 참조 변수가 객체를 가리키고 있으면 인터럽트 발생
				if(flyThread != null)
				{
					timeThread.interrupt();
					flyThread.interrupt();
				}
				
				timeThread = new TimeThread();	
				flyThread = new FlyThread();
				
				time = 0;	// 시간 초기화
				GamePanel gamePanel = new GamePanel();
				setContentPane(gamePanel);
				setVisible(true);	
				gamePanel.startGame();
				
				timeThread.start();
				flyThread.start();	
			}
		});
		
		setVisible(true);
		setLocationRelativeTo(getParent());
	}
	
	class GamePanel extends JPanel
	{
		public GamePanel()
		{
			setLayout(null);
			
			// 각각의 배열에 result 삽입 및 사이즈 부여, 객체 추가하기..(?)
			for(i=0; i<fly.length; i++)
			{
				fly[i] = new JLabel(result);
				if(i==0 || i==3 || i==11)
					fly[i].setForeground(Color.DARK_GRAY);	// 폭탄색 다크그레이로 수정 
				fly[i].setSize(30,30);
				add(fly[i]);
			}				
		}
		
		public void startGame()
		{
			int x,y;	// 여러 파리의 랜덤한 위치를 담을 변수 생성 
			
			for(i=0; i<fly.length; i++)
			{
				// 파리의 위치를 프레임 내의 (30,30)에서 (270,270) 영역 내 랜던함 위치로 제한
				x = (int)(Math.random() * 240) + 30;
				y = (int)(Math.random() * 240) + 30;
				fly[i].setLocation(x,y);
				fly[i].addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						// fly[i]로 하면 for문을 거친 마지막 i값(20)을 인덱스로 사용한다.
						// 그래서 e.getSuorce()로 어떤 JLabel이 클릭되었는지 확인하고,
						// 그것의 인덱스를 구해서 삭제한다.
						int idx = Arrays.asList(fly).indexOf(e.getSource());
						
						GamePanel.this.remove(fly[idx]);
						GamePanel.this.repaint();	// 이 코드가 없으면 파리가 계속 JPanel에 남아있다.
						
						if(idx==0 || idx==3 || idx==11) 
							flycnt=0; // 잡은 파리 수를 0으로 초기화 (폭탄 역할)
						else
							flycnt++;	// 파리를 잡을 시, 잡은 파리 수가 1씩 증가
					}
				}); 
			}
		}
	}
	
	class FlyThread extends Thread
	{
		final int FLY_MOVE = 15;	// 한 번에 움직이는 거리
			
		public void run()
		{
			while(true)
			{
				for(i=0; i<fly.length; i++)
				{
					int x = fly[i].getX(), y = fly[i].getY(), num;	// 현재 좌표 반환
					
					num = (int)(Math.random()*4);
					
					if(x>getWidth()) 
						fly[i].setLocation(0, y);
					
					else if(y>getHeight())
						fly[i].setLocation(x, 0);
					
					else if(x<0) 
						fly[i].setLocation(getWidth(), y);
					
					else if(y<0)
						fly[i].setLocation(x, getHeight());
				
					else 
					{
						switch (num)
						{
						case 0 :
							fly[i].setLocation(x+FLY_MOVE, y);
							break;
						case 1 :
							fly[i].setLocation(x-FLY_MOVE, y);
							break;
						case 2 :
							fly[i].setLocation(x, y+FLY_MOVE);
							break;
						case 3 :
							fly[i].setLocation(x, y-FLY_MOVE);
							break;
						}	
					}	
					try {
						sleep(10); 	
					}catch (InterruptedException e)  { return; } 
				}
			}
		}
	}
	
	class TimeThread extends Thread
	{
		public void run()
		{
			while(true)
			{	
				time++;
				if(time>60)
				{
					JOptionPane.showMessageDialog(null, String.format("지금까지 잡은 파리 수는 %d마리 입니다.", flycnt));	// 알림창 수정 
					timeThread.interrupt();
					flyThread.interrupt();
				}
				try {
					sleep(1000); 	
				}catch (InterruptedException e) {return;}			
			}
		}
	}
	
	public static void main(String[] args) { new NewCatchGame(); } 	
}