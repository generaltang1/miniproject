package miniproject;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JOptionPane;
/**
 * Client 클래스
 * 
 * @author 이선주
 * @author 이태행
 *
 */
public class Client extends GuiView implements Runnable, Constants {
   
   private Socket socket;
   private BufferedReader br;
   private PrintWriter writer;
   
   private String sendDraw = null;
   private String sendColor = null;
   private String sendThickness = null;
   private String sendPoint = null;
   private String sendMsg = null;
   

   private String myNickname;  
   private int myId;  
   private String[] nicknames = new String[PLAYER_COUNT];  
   private int[] scores = new int[PLAYER_COUNT]; 
   private int currentPlayer;  
   private boolean canDraw = true;
   
   private int turnCount = 0;

   private SimpleDateFormat sdf = new SimpleDateFormat("(YYYY-MM-dd HH:mm:ss)");
   
   
   public Client() {
      
      addWindowListener(this);
      
      connectSocket();
   }
   
   TimerThread timer;
   
   /**
    * TimerThread
    * 제한 시간 표시 
    *
    */
   class TimerThread extends Thread {
      
      Client client;
      boolean run = true;
      
      TimerThread(Client client) {
         this.client = client;
      }
      
      public void stopTimer() {
         run = false;
      }
      
   @Override
   /**
    * Timer의 동작 메서드 
    * 제한 시간에서 1초씩 감소 
    * 타임 오버시에 시간초과 안내 JOp
    */
   public void run() {
      try {
         int i = SEC;
         while (true) {
         
            if (run == true) {
               timerLabel.setText(i + " 초");
               Thread.sleep(1000);
            
               i--;
            
               if (i < 0) {
                  int timeover = JOptionPane.showConfirmDialog(client, "타임오버!", "시간초과 안내", JOptionPane.DEFAULT_OPTION);
                  
                  if (timeover == JOptionPane.OK_OPTION) {
                     writer.println("TIMEOVER&");
                     turnCount++;
                     
                     
                     if (turnCount == TURN_COUNT) {
                        writer.println("RESULT&");
                        
                     }
                  }
                  
                  
                  
                  break;
               }
            }
         }
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
      
   }
   
   /**
    *  소켓 통신 연결 메서드
    *  서버에게 정보를 보내는 OutputStream 연결
    * 		  정보를 받는 InputStream 연결 
    * 
    *  서버와 연결 시 IP, 현재 시간 출력 
    *  연결 실패 시, "연결 실패 출력 "
    */
   public void connectSocket() {
      try {
        socket = new Socket("127.0.0.1", 50000); 

         writer = new PrintWriter(socket.getOutputStream(), true); 
         br = new BufferedReader(new InputStreamReader(socket.getInputStream())); 

         Thread thread = new Thread(this); 
         thread.start();

         System.out.println(
               "서버와 연결되었습니다.\n" + "IP : " + socket.getInetAddress() + sdf.format(System.currentTimeMillis()));

      } catch (IOException e) {
         System.out.println("서버와 연결 실패");
         e.printStackTrace();
      }
   }

   /**
    * 정보 수신 동작 메서드
    * 그림내용, 닉네임, 채팅내용 등을 수신 
    */
   @Override
   public void run() {
      try {
       
         String msg;
         String[] parsMsg;

        
         int startX = 0;
         int startY = 0;
         int endX = 0;
         int endY = 0;
         Color sendedColor = Color.BLACK;
         Color sendedColorMemory = Color.BLACK;
         
         
                  while ((msg = br.readLine()) != null) {
            
           
            
            parsMsg = msg.split(DELIMETER);
            
            switch (parsMsg[0]) {
            case ID:
               myId = Integer.parseInt(parsMsg[1]);
               break;
            
            case ALL_CONNECTED:
               myNickname = JOptionPane.showInputDialog("닉네임을 입력하세요.");
               nameLabelArr[myId].setText(myNickname + "(나)");  
               writer.println("NICKNAME&" + myNickname);  
               break;
            
            case NICKNAME:
               receiveNickname(parsMsg);
               break;
            
            case CurP:
               receiveCurP(parsMsg);
               break;
              
            case WORD:
               receiveWord(parsMsg);
               break;
            
            case CORRECT:
               receiveCorrect(parsMsg);
               break;
           
            case RESULT:
               receiveResult(parsMsg);
               break;
            
            case DRAW:
              
               if ("delete".equals(parsMsg[1])) {
                  paintPanel.repaint();
                  break;
               }
               
               String draw[] = parsMsg[1].split(SUB_DELIMETER);
              
               graphic.setColor(sendedColor);
               
               if ("start".equals(draw[0])) {
                  startX = Integer.parseInt(draw[1]);
                  startY = Integer.parseInt(draw[2]);
               }

               if ("end".equals(draw[0])) {
                  endX = Integer.parseInt(draw[1]);
                  endY = Integer.parseInt(draw[2]);

                  graphic.drawLine(startX, startY, endX, endY); 

                  startX = endX;
                  startY = endY;
               }

               break;
            case COLOR:
               if ("red".equals(parsMsg[1])) {
                  graphic.setColor(Color.RED);
                  sendedColor = Color.RED;
               } else if ("orange".equals(parsMsg[1])) {
                  graphic.setColor(Color.ORANGE);
                  sendedColor = Color.ORANGE;
               } else if ("yellow".equals(parsMsg[1])) {
                  graphic.setColor(Color.YELLOW);
                  sendedColor = Color.YELLOW;
               } else if ("green".equals(parsMsg[1])) {
                  graphic.setColor(Color.GREEN);
                  sendedColor = Color.GREEN;
               } else if ("blue".equals(parsMsg[1])) {
                  graphic.setColor(Color.BLUE);
                  sendedColor = Color.BLUE;
               } else if ("black".equals(parsMsg[1])) {
                  graphic.setColor(Color.BLACK);
                  sendedColor = Color.BLACK;
               } else if ("white".equals(parsMsg[1])) {
                  graphic.setColor(Color.WHITE);
                  sendedColorMemory = sendedColor;
                  sendedColor = Color.WHITE;
               }
               break;
            case THICKNESS:
               graphic.setStroke(new BasicStroke(Integer.parseInt(parsMsg[1]), BasicStroke.CAP_ROUND, 0)); 
               break;
            case CHAT:
               String chat[] = parsMsg[1].split(SUB_DELIMETER);
               
               if ("0".equals(chat[0])) {
                  msgTaArr[0].setText(chat[1]);
               } else if ("1".equals(chat[0])) {
                  msgTaArr[1].setText(chat[1]);
               } else if ("2".equals(chat[0])) {
                  msgTaArr[2].setText(chat[1]);
               } else if ("3".equals(chat[0])) {
                  msgTaArr[3].setText(chat[1]);
               }
               break;
            default:
            }
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   
   /**
    * 닉네임 수신 메서드
    * 플레이어가 닉네임을 수신한 뒤에 게임 시작 안내 
    * @param 닉네임 문자열
    */
   public void receiveNickname(String[] parsMsg) {
      nicknames = parsMsg[1].split(SUB_DELIMETER);
       
       
       for (int i=0; i<PLAYER_COUNT; ++i) {
          nameLabelArr[i].setText(nicknames[i]);
       }
       nameLabelArr[myId].setText(myNickname + "(나)"); 
       
       
       int startReady = JOptionPane.showConfirmDialog(this, "게임을 시작합니다.", "게임 시작 안내",
             JOptionPane.DEFAULT_OPTION);

       if (startReady == JOptionPane.OK_OPTION) {
          writer.println("START_READY&");
       }
      
       
   }
   /**
    * 현재 출제자를 표시하는 메서드
    * 
    * 턴과 현재 상태를 표시한다.
    * 턴이 넘어가면 타이머를 리셋시킨다. 
    * 서버로 부턴 제시어를 받는다.
    * 
    * @param 현재 출제자 문자열 
    */
   public void receiveCurP(String[] parsMsg) {
      turnLabel.setText((turnCount+1) + "/10 턴");  
       statusLabel.setText("-");  
       paintPanel.repaint();  
       timerLabel.setText(SEC + " 초");  
       
       currentPlayer = Integer.parseInt(parsMsg[1]);
       
       int turnReady = JOptionPane.showConfirmDialog(this, nicknames[currentPlayer] + "님 차례입니다.", "출제자 안내",
             JOptionPane.DEFAULT_OPTION);
       
       if (turnReady == JOptionPane.OK_OPTION) {
          writer.println("TURN_READY&");
       }
      
   }

   /**
    *  서버로 부터 제시어를 받는 메서드 
    *  출제자가 아닌 경우에는 상태표시란에 제시어가 아닌, 차례를 나타낸다.
    *  제시어를 받는 순간 타이머 작동 
    * @param 수신할 문자열 
    */
   public void receiveWord(String[] parsMsg) {
      
       if (myId != currentPlayer) {
          
          msgTaArr[currentPlayer].setText("[출제 중입니다]");
          
        
          messageTf.setEditable(true);
          msgTaArr[myId].setText("");
          
          
           statusLabel.setText(nicknames[currentPlayer] + "님 차례입니다.");
          
          
           canDraw = false;
       }
       
       
       if (myId == currentPlayer) {
          
          messageTf.setEditable(false);
          for (int i=0; i<PLAYER_COUNT; ++i) msgTaArr[i].setText("");  
          msgTaArr[myId].setText("[출제 중입니다]");
          
          canDraw = true;
          
        
          statusLabel.setText("제시어 : " + parsMsg[1]);
          
       }
    
       timer = new TimerThread(this);
       timer.start();
       
   }
   	
   /**
    * 정답 확인 메서드 
    * 정답을 맞출 경우 타이머 정지
    * 모든 클라이언트로 부터 TURN_END를 받으면 서버가 다음 출제자를 알려주면서 다음턴이 시작된다. 
    * 
    * 정답을 맞추면 정답자의 score가 2점, 출제자는 1점 가산
    * 
    * @param parsMsg 수신할 문자열 
    */
   public void receiveCorrect(String[] parsMsg) {
      turnCount++;  
       timer.stopTimer();  
       timer = null;
       System.gc();
       String correct[] = parsMsg[1].split(SUB_DELIMETER);
       String correctNickname = nicknames[Integer.parseInt(correct[0])];
       
              int correctJop = JOptionPane.showConfirmDialog(this, "정답은 " + correct[1] + "입니다!\n" + correctNickname + "님 정답!", "정답 안내",
             JOptionPane.DEFAULT_OPTION);
       
       if (correctJop == JOptionPane.OK_OPTION) {
          if (turnCount != TURN_COUNT)
             writer.println("TURN_END&");  
       }
       
       scores[Integer.parseInt(correct[0])] += 2;
       scores[currentPlayer] += 1;
       
       scoreLabelArr[Integer.parseInt(correct[0])].setText("점수 : " + scores[Integer.parseInt(correct[0])]);
       scoreLabelArr[currentPlayer].setText("점수 : " + scores[currentPlayer]);
       
       
       if (turnCount == TURN_COUNT) {
          writer.println("RESULT&");
         
       }
   }
/**
 *  결과창 수신 메서드 
 * @param parsMsg
 */
   public void receiveResult(String[] parsMsg) {
       String resultMsg = "==결과 발표==\n";
      
         int[] rank = idxOfSorted(scores);
      
         for (int i=0; i<rank.length; ++i) {
            resultMsg += (i+1) + "위 : " + nicknames[rank[i]] + " ---- " + scores[rank[i]] + "점\n";
         }
      
         int result = JOptionPane.showConfirmDialog(this, resultMsg, "결과 안내", JOptionPane.DEFAULT_OPTION);
         
          if (result == JOptionPane.OK_OPTION) {
             setVisible(false);
             writer.println("EXIT&");
             System.exit(0);
          }
   }
   
   
   
   /**
    * 결과 창 순위를 나태내기 위한 정렬 메서드 
    * 내림 차순 정렬 
    * @param scores 점수 
    * @return 정렬된 순위를 반환 
    */
   public int[] idxOfSorted(int[] scores) {
      
      Integer[] castedScores = Arrays.stream(scores).boxed().toArray(Integer[]::new);  
      Arrays.sort(castedScores, Collections.reverseOrder());  
      int[] sortedScores = Arrays.stream(castedScores).mapToInt(i -> i).toArray();  
      
      
      int[] idxOfSorted = new int[scores.length];
      
      
      for (int i=0; i<sortedScores.length; ++i) {
         for (int j=0; j<sortedScores.length; ++j) {
            if (sortedScores[i] == scores[j]) {
               idxOfSorted[i] = j;
            }
         }   
      }
      
      return idxOfSorted;
   }

  

   @Override
   /**
    * 엔터 키 입력시 채팅 내역 전송 
    */
   public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        
         msgTaArr[myId].setText(messageTf.getText());

      
         sendMsg = "CHAT&" + myId + "," + messageTf.getText();
         writer.println(sendMsg);

       
         messageTf.setText(null);
      }
   }
/**
 * 페인트 페널에서  마우스 클릭 시
 *  x, y좌표값으로 초기화 
 */
   @Override
   public void mousePressed(MouseEvent e) {
      
      
      if (canDraw == true) {
              graphic.setColor(currentColor);
              
            startX = e.getX(); 
            startY = e.getY(); 

            // 서버로 전달
            sendPoint = "DRAW&" + "start," + startX + "," + startY;
            writer.println(sendPoint);
            if (true) {
               sendThickness = "THICKNESS&" + thickness;
               writer.println(sendThickness);
            }
      }
   }
  
   /**
    * 마우스 드래그 
    * 드래그 되는 시점에서 끝날 때 까지의 X,Y좌표를 연결해주어 선이 그려진다. 
    * 
    * sendPoint로 서버에 전달 
    */

   @Override
   public void mouseDragged(MouseEvent e) {
      
      
      if (canDraw == true) {
              endX = e.getX();
           

            endY = e.getY();
            
            sendPoint = "DRAW&" + "end," + endX + "," + endY;
            writer.println(sendPoint);

            graphic.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, 0)); 
            graphic.drawLine(startX, startY, endX, endY); 

            startX = endX; 
            startY = endY; 
      }
   }
/**
 * 전송 버튼을 클릭했을 때 서버에게 텍스트 필드를 전송 
 */
   @Override
   public void actionPerformed(ActionEvent e) {
     
      JButton jButton = (JButton) e.getSource();
      if ("전송".equals(jButton.getText())) {
         
         msgTaArr[myId].setText(messageTf.getText());

        
         sendMsg = "CHAT&" + myId + "," + messageTf.getText();
         writer.println(sendMsg);

         

         messageTf.setText(null);
      }

     
      if (e.getSource() == pencilBtn1) {
        currentColor = currentColorMemory;
         graphic.setColor(currentColor);
         thickness = 10; 
         sendThickness = "THICKNESS&" + thickness; 
         writer.println(sendThickness);

      }
      if (e.getSource() == pencilBtn2) {
        currentColor = currentColorMemory;
         graphic.setColor(currentColor);
         thickness = 5; 
         sendThickness = "THICKNESS&" + thickness; 
         writer.println(sendThickness);

      }
      if (e.getSource() == pencilBtn3) {
        currentColor = currentColorMemory;
         graphic.setColor(currentColor);
         thickness = 1; 
         sendThickness = "THICKNESS&" + thickness; 
         writer.println(sendThickness);
      }
     
      if (e.getSource() == eraserBtn1) {
        currentColorMemory = currentColor;
         currentColor = Color.WHITE;
         graphic.setColor(currentColor);
         thickness = 10; 
         sendThickness = "THICKNESS&" + thickness; 
         sendColor = "COLOR&white"; 
         writer.println(sendThickness);
         writer.println(sendColor);
      }
      if (e.getSource() == eraserBtn2) {
        currentColorMemory = currentColor;
         currentColor = Color.WHITE;
         graphic.setColor(currentColor);
         thickness = 5; 
         sendThickness = "THICKNESS&" + thickness; 
         sendColor = "COLOR&white"; 
         writer.println(sendThickness);
         writer.println(sendColor);
      }
      if (e.getSource() == eraserBtn3) {
        currentColorMemory = currentColor;
         currentColor = Color.WHITE;
         graphic.setColor(currentColor);
         thickness = 1; 
         sendThickness = "THICKNESS&" + thickness; 
         sendColor = "COLOR&white"; 
         writer.println(sendThickness);
         writer.println(sendColor);
      }
      
      if (e.getSource() == clearBtn) {
         paintPanel.repaint(); 
         sendDraw = "DRAW&delete";
         writer.println(sendDraw);
      }
      
      if (e.getSource() == colorBtn_red) {
         currentColor = Color.RED;
         graphic.setColor(currentColor);

         sendColor = "COLOR&red";
         writer.println(sendColor);
      }
      if (e.getSource() == colorBtn_orange) {
         currentColor = Color.ORANGE;
         graphic.setColor(currentColor);

         sendColor = "COLOR&orange";
         writer.println(sendColor);
      }
      if (e.getSource() == colorBtn_yellow) {
         currentColor = Color.YELLOW;
         graphic.setColor(currentColor);

         sendColor = "COLOR&yellow";
         writer.println(sendColor);
      }
      if (e.getSource() == colorBtn_green) {
         currentColor = Color.GREEN;
         graphic.setColor(currentColor);

         sendColor = "COLOR&green";
         writer.println(sendColor);
      }
      if (e.getSource() == colorBtn_blue) {
         currentColor = Color.BLUE;
         graphic.setColor(currentColor);

         sendColor = "COLOR&blue";
         writer.println(sendColor);
      }
      if (e.getSource() == colorBtn_black) {
         currentColor = Color.BLACK;
         graphic.setColor(currentColor);

         sendColor = "COLOR&black";
         writer.println(sendColor);
      }
   }
/**
 * 윈도우 창 종료 시, 소켓 종료 
 */
   @Override
   public void windowClosed(WindowEvent e) {
      try {
         socket.close();
         System.out.println("소켓 닫힘");
      } catch (IOException e1) {
         e1.printStackTrace();
      }
   }

   @Override
   public void keyTyped(KeyEvent e) {}
   @Override
   public void keyReleased(KeyEvent e) {}
   @Override
   public void mouseClicked(MouseEvent e) {}
   @Override
   public void mouseReleased(MouseEvent e) {}
   @Override
   public void mouseEntered(MouseEvent e) {}
   @Override
   public void mouseExited(MouseEvent e) {}
   @Override
   public void mouseMoved(MouseEvent e) {}
   @Override
   public void windowOpened(WindowEvent e) {}
   @Override
   public void windowClosing(WindowEvent e) {}
   @Override
   public void windowIconified(WindowEvent e) {}
   @Override
   public void windowDeiconified(WindowEvent e) {}
   @Override
   public void windowActivated(WindowEvent e) {}
   @Override
   public void windowDeactivated(WindowEvent e) {}

   public static void main(String[] args) {
      new Client();
   }
}
