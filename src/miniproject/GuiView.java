package miniproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * 
 * 그림판 Gui 클래스
 * 
 * @author 이선주
 * @author 이태행
 *
 */
public abstract class GuiView extends JFrame
		implements ActionListener, MouseListener, MouseMotionListener, WindowListener, KeyListener {

	private Graphics g;
	protected Graphics2D graphic;

	protected JPanel paintPanel;
	protected JPanel infoPanel;

	protected JButton pencilBtn1;
	protected JButton pencilBtn2;
	protected JButton pencilBtn3;

	protected JButton eraserBtn1;
	protected JButton eraserBtn2;
	protected JButton eraserBtn3;

	protected JButton clearBtn;

	protected JButton colorBtn_red;
	protected JButton colorBtn_orange;
	protected JButton colorBtn_yellow;
	protected JButton colorBtn_green;
	protected JButton colorBtn_blue;
	protected JButton colorBtn_black;

	protected int thickness = 5;
	protected int startX;
	protected int startY;
	protected int endX;
	protected int endY;

	protected Color currentColor = Color.BLACK;
	protected Color currentColorMemory;

	JLabel[] nameLabelArr = new JLabel[4];
	JLabel[] scoreLabelArr = new JLabel[4];
	JTextArea[] msgTaArr = new JTextArea[4];

	JLabel turnLabel;
	JLabel statusLabel;
	JLabel timerLabel;

	JTextField messageTf = new JTextField();

	private ImageIcon chatBgIcon = new ImageIcon("chatBG.png");
	private ImageIcon statusBarBgIcon = new ImageIcon("statusBarBG.png");
	private Font bigFont = new Font("1훈솜사탕 Regular", Font.PLAIN, 24);
	private Font smallFont = new Font("1훈솜사탕 Regular", Font.PLAIN, 16);

	/**
	 * 채팅 메서드
	 * 
	 * @param idx 플레이어 인덱스
	 * @return 플레이어 닉네임, 스코어, 채팅 내용을 반환
	 */
	public JPanel getChatPanel(int idx) {

		JPanel panel = new JPanel() {

			/**
			 * 이미지 삽입
			 */
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(chatBgIcon.getImage(), 0, 0, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};

		JPanel taPanel = new JPanel();

		panel.setLayout(null);

		nameLabelArr[idx] = new JLabel("Player" + (idx + 1));
		nameLabelArr[idx].setFont(bigFont);
		nameLabelArr[idx].setBounds(30, 20, 100, 30);

		scoreLabelArr[idx] = new JLabel("점수 : 0");
		scoreLabelArr[idx].setFont(smallFont);
		scoreLabelArr[idx].setBounds(30, 50, 100, 20);

		taPanel.setOpaque(false);
		taPanel.setBounds(140, 20, 150, 100);

		msgTaArr[idx] = new JTextArea(5, 10);
		msgTaArr[idx].setEditable(false);
		msgTaArr[idx].setLineWrap(true);

		panel.add(nameLabelArr[idx]);
		panel.add(scoreLabelArr[idx]);
		taPanel.add(msgTaArr[idx]);
		panel.add(taPanel);

		return panel;
	}

	/**
	 * 채팅 전송 버튼 창
	 * 
	 * @return 전송 버튼 클릭시 채팅 내용 전송
	 */
	public JPanel getChatInsertPanel() {
		JPanel panel = new JPanel();

		messageTf = new JTextField();
		JButton sendBtn = new JButton("전송");

		sendBtn.addActionListener(this);
		messageTf.addKeyListener(this);

		panel.setLayout(new BorderLayout());
		panel.add(messageTf, BorderLayout.CENTER);
		panel.add(sendBtn, BorderLayout.EAST);

		return panel;
	}

	/**
	 * 상태 표시창
	 * 
	 * @return 턴, 게임진행 상태, 타이머
	 * 
	 */
	public JPanel getStatusBarPanel() {
		JPanel panel = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				g.drawImage(statusBarBgIcon.getImage(), 0, 0, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};

		turnLabel = new JLabel("-/10 턴");
		statusLabel = new JLabel("환영합니다.");
		timerLabel = new JLabel("10 초");

		panel.setLayout(null);

		turnLabel.setFont(bigFont);
		turnLabel.setBounds(20, 25, 100, 20);

		statusLabel.setFont(smallFont);
		statusLabel.setHorizontalAlignment(JLabel.CENTER);
		statusLabel.setBounds(150, 25, 300, 20);

		timerLabel.setFont(bigFont);
		timerLabel.setHorizontalAlignment(JLabel.RIGHT);
		timerLabel.setBounds(480, 25, 100, 20);

		panel.add(turnLabel);
		panel.add(statusLabel);
		panel.add(timerLabel);

		return panel;
	}

	/**
	 * 그림판 메서드
	 * 
	 * 연필의 버튼 : 대, 중, 소 굵기 구별 지우개 : 대, 중, 소 굵기 구별 전체 삭제 : 그림판 내용 초기화 색상 버튼 : 6가지 색상
	 * 버튼 구별
	 * 
	 * @return 그림판 도구 패널
	 */
	public JPanel getInfoPanel() {

		JPanel leftPanel1 = new JPanel();
		leftPanel1.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "연필"));
		leftPanel1.setLayout(new GridLayout(1, 3));

		pencilBtn1 = new JButton("대");
		pencilBtn2 = new JButton("중");
		pencilBtn3 = new JButton("소");

		pencilBtn1.addActionListener(this);
		pencilBtn2.addActionListener(this);
		pencilBtn3.addActionListener(this);

		leftPanel1.add(pencilBtn1);
		leftPanel1.add(pencilBtn2);
		leftPanel1.add(pencilBtn3);

		JPanel leftPanel2 = new JPanel();
		leftPanel2.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "지우개"));
		leftPanel2.setLayout(new GridLayout(1, 3));

		eraserBtn1 = new JButton("대");
		eraserBtn2 = new JButton("중");
		eraserBtn3 = new JButton("소");

		eraserBtn1.addActionListener(this);
		eraserBtn2.addActionListener(this);
		eraserBtn3.addActionListener(this);

		leftPanel2.add(eraserBtn1);
		leftPanel2.add(eraserBtn2);
		leftPanel2.add(eraserBtn3);

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "전체삭제"));
		clearBtn = new JButton("삭제");
		clearBtn.addActionListener(this);
		centerPanel.add(clearBtn);

		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "색상선택"));
		rightPanel.setLayout(new GridLayout(2, 3));

		colorBtn_red = new JButton();
		colorBtn_orange = new JButton();
		colorBtn_yellow = new JButton();
		colorBtn_green = new JButton();
		colorBtn_blue = new JButton();
		colorBtn_black = new JButton();

		colorBtn_red.setBackground(Color.RED);
		colorBtn_orange.setBackground(Color.ORANGE);
		colorBtn_yellow.setBackground(Color.YELLOW);
		colorBtn_green.setBackground(Color.GREEN);
		colorBtn_blue.setBackground(Color.BLUE);
		colorBtn_black.setBackground(Color.BLACK);

		colorBtn_red.addActionListener(this);
		colorBtn_orange.addActionListener(this);
		colorBtn_yellow.addActionListener(this);
		colorBtn_green.addActionListener(this);
		colorBtn_blue.addActionListener(this);
		colorBtn_black.addActionListener(this);

		rightPanel.add(colorBtn_red);
		rightPanel.add(colorBtn_orange);
		rightPanel.add(colorBtn_yellow);
		rightPanel.add(colorBtn_green);
		rightPanel.add(colorBtn_blue);
		rightPanel.add(colorBtn_black);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(2, 1));
		leftPanel.add(leftPanel1);
		leftPanel.add(leftPanel2);

		infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(1, 3));
		infoPanel.add(leftPanel);
		infoPanel.add(centerPanel);
		infoPanel.add(rightPanel);

		return infoPanel;
	}

	/**
	 * 흰 배경에 실질적으로 그림이 그려지는 PaintPanel
	 * 
	 * @return
	 */
	public JPanel getPaintPanel() {
		paintPanel = new JPanel();
		paintPanel.setBackground(Color.WHITE);
		paintPanel.setPreferredSize(new Dimension(400, 600));
		paintPanel.addMouseListener(this);
		paintPanel.addMouseMotionListener(this);

		return paintPanel;
	}

	public GuiView() {

		JPanel p1 = getChatPanel(0);
		p1.setBounds(0, 0, 300, 167);

		JPanel p2 = getChatPanel(1);
		p2.setBounds(0, 167, 300, 167);

		JPanel p3 = getChatPanel(2);
		p3.setBounds(0, 334, 300, 167);

		JPanel p4 = getChatPanel(3);
		p4.setBounds(0, 501, 300, 167);

		JPanel p5 = getChatInsertPanel();
		p5.setBounds(0, 668, 300, 32);

		JPanel p6 = getStatusBarPanel();
		p6.setBounds(300, 0, 600, 70);

		paintPanel = getPaintPanel();
		JPanel infoPanel = getInfoPanel();
		paintPanel.setBounds(320, 90, 560, 440);
		infoPanel.setBounds(320, 530, 560, 150);

		setTitle("캐치마인드");
		setSize(new Dimension(912, 735));
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		getContentPane().setBackground(new Color(164, 249, 130));

		setLayout(null);
		add(p1);
		add(p2);
		add(p3);
		add(p4);
		add(p5);
		add(p6);
		add(paintPanel);
		add(infoPanel);

		g = paintPanel.getGraphics();
		graphic = (Graphics2D) g;

	}

}