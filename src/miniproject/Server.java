package miniproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * 서버 클래스
 * 
 * @author 이선주
 * @author 이태행
 *
 */
public class Server implements Constants {

	private ServerSocket serverSocket;
	private Socket socket;

	private Vector<ClientInfo> vcClient;

	private String[] nicknames = new String[PLAYER_COUNT];

	private String words[] = { "사자", "비둘기", "김경호", "고양이", "강아지", "얼룩말", "카레이서", "기린", "물고기", "사과", "파인애플", "카카오나무",
			"상추", "나무", "가격표", "라면", "전화", "탁구", "마이크", "공중전화", "샴푸", "빵", "태양", "수영", "열쇠", "바지", "부메랑", "컴퓨터", "소프라노",
			"정장", "원숭이", "공부", "선생님", "다이아몬드", "그물", "창조물", "호랑이", "사람", "축구", "파리", "소방관", "가수", "악기", "노래", "귓속말",
			"동아리", "코끼리", "게릴라", "완두콩", "산책" };

	private int[] wordsIdx = new int[TURN_COUNT];

	private int count = 0;
	private int turnCount = 0;
	private int currentPlayer;
	private int ansPlayer;
	private String answer;

	public Server() {

		connectSocket();
		makeWordsIdx();

	}

	/**
	 * 제시어 출제 매서드 50개의 단어를 랜덤하게(중복 불가) 인덱스 턴 수 만큼 뽑는다.
	 */
	public void makeWordsIdx() {

		for (int i = 0; i < TURN_COUNT; ++i) {
			wordsIdx[i] = (int) (Math.random() * 50);

			for (int j = 0; j < i; ++j) {
				if (wordsIdx[i] == wordsIdx[j]) {
					i--;
					break;
				}
			}
		}
	}

	/**
	 * 클라이언트와 연결 포트를 50000만으로 설정. 연결 상태를 출력
	 */
	public void connectSocket() {
		try {

			serverSocket = new ServerSocket(50000);
			vcClient = new Vector<>();

			while (vcClient.size() < PLAYER_COUNT) {
				System.out.println("서버 준비 완료! 클라이언트를 기다립니다...");

				socket = serverSocket.accept();

				ClientInfo ci = new ClientInfo(socket);
				ci.start();
				vcClient.add(ci);

				System.out.println("클라이언트" + (vcClient.size()) + "(과)와 연결되었습니다.");
			}

			System.out.println("모든 클라이언트 연결 완료");

		} catch (Exception e) {
			System.out.println("연결안됨");
		}
	}

	/**
	 * 클라이언트로부터 정보를 수신하는 메서드
	 */
	class ClientInfo extends Thread {
		BufferedReader br;
		PrintWriter writer;
		Socket socket;

		public ClientInfo(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				writer = new PrintWriter(socket.getOutputStream(), true);
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				String msg = null;
				String[] parsMsg;

				sendId();
				allConnected();

				while ((msg = br.readLine()) != null) {
					parsMsg = msg.split("&");

					switch (parsMsg[0]) {
					case NICKNAME:
						receivedNickname(parsMsg);
						break;
					case START_READY:
						receivedStartReady(parsMsg);
						break;
					case TURN_READY:
						receivedTurnReady(parsMsg);
						break;
					case TURN_END:
						receivedTurnEnd(parsMsg);
						break;
					case TIMEOVER:
						for (int i = 0; i < vcClient.size(); i++) {
							if (vcClient.get(i) == this) {
								count++;
							}
						}

						if (count == PLAYER_COUNT) {
							int randNum = currentPlayer;
							while (randNum == currentPlayer) {
								randNum = (int) (Math.random() * PLAYER_COUNT);
							}

							currentPlayer = randNum;

							sendString("CurP&" + currentPlayer);

							count = 0;
							turnCount++;
						}

						break;
					case RESULT:
						receivedResult(parsMsg);
						break;
					case EXIT:
						serverSocket.close();
						System.exit(0);
						break;

					case DRAW:
						sendStringWithoutSelf(msg);
						break;
					case COLOR:
						sendStringWithoutSelf(msg);
						break;
					case THICKNESS:
						sendStringWithoutSelf(msg);
						break;
					case CHAT:
						sendStringWithoutSelf(msg);
						findAnswer(parsMsg);
						break;
					default:
					}
				}
			} catch (Exception e) {
				System.out.println("메세지 통신 실패.");
				e.printStackTrace();
			}
		}

		/**
		 * 인덱스 발신 매서드
		 */
		private void sendId() {
			for (int i = 0; i < vcClient.size(); i++) {
				vcClient.get(i).writer.println("ID&" + i);
			}
		}

		/**
		 * 클라이언트 모두 접속 시에ALL_CONNECTED 발신
		 */
		private void allConnected() {
			if (vcClient.size() == PLAYER_COUNT) {
				for (int i = 0; i < vcClient.size(); i++) {
					vcClient.get(i).writer.println("ALL_CONNECTED&");
				}
			}
			System.out.println("ALL_CONNECTED 보내짐");

		}

		/**
		 * 닉네임을 수신하는 매서드
		 * 
		 * @param 닉네임
		 */
		private void receivedNickname(String[] parsMsg) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					count++;
					nicknames[i] = parsMsg[1];
				}
			}

			if (count == PLAYER_COUNT) {
				String nicks = "NICKNAME&";
				for (int i = 0; i < vcClient.size(); i++) {
					nicks += nicknames[i];
					if (i == vcClient.size() - 1)
						break;
					nicks += ",";
				}

				sendString(nicks);

				count = 0;
			}
		}

		/**
		 * 준비 상태 수신 매서드 플레이어로부터 준비상태를 수신 한 뒤 모두 준비완료 되면 출제자를 전송
		 */
		private void receivedStartReady(String[] parsMsg) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					count++;
				}
			}
			System.out.println("START_READY 도착함");

			if (count == PLAYER_COUNT) {
				currentPlayer = (int) (Math.random() * PLAYER_COUNT);
				sendString("CurP&" + currentPlayer);

				System.out.println("curP 보내짐");
				count = 0;
			}
		}

		/**
		 * 다음 턴으로의 준비 수신 메서드 다 받으면 제시어를 전송
		 */
		private void receivedTurnReady(String[] parsMsg) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					count++;
				}
			}
			System.out.println("TURN_READY 도착함");

			if (count == PLAYER_COUNT) {

				answer = words[wordsIdx[turnCount]];
				sendString("WORD&" + answer);
				System.out.println("word 보내짐");
				System.out.println("turnCount: " + turnCount);
				count = 0;
			}
		}

		/**
		 * 턴 종료 수신 메서드
		 */
		private void receivedTurnEnd(String[] parsMsg) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					count++;
				}
			}

			if (count == PLAYER_COUNT) {
				currentPlayer = ansPlayer;
				sendString("CurP&" + currentPlayer);

				count = 0;
			}
		}

		/**
		 * 결과 수신 메서드
		 * 
		 * @param 결과 값
		 */
		private void receivedResult(String[] parsMsg) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) == this) {
					count++;
				}
			}
			System.out.println("RESULT 도착함");

			if (count == PLAYER_COUNT) {
				sendString("RESULT&");
				count = 0;

				System.out.println("RESULT 보내짐");
			}
		}

		private void sendString(String msg) {
			for (int i = 0; i < vcClient.size(); i++) {
				vcClient.get(i).writer.println(msg);
			}
		}

		private void sendStringWithoutSelf(String msg) {
			for (int i = 0; i < vcClient.size(); i++) {
				if (vcClient.get(i) != this) {
					vcClient.get(i).writer.println(msg);
				}
			}
		}

		/**
		 * 정답 찾기 메서드 채팅 내에서 정답이 나올 시 다음 턴으로
		 * 
		 * @param parsMsg 채팅 내용
		 */
		private void findAnswer(String[] parsMsg) {
			String[] chat = parsMsg[1].split(SUB_DELIMETER);
			ansPlayer = Integer.parseInt(chat[0]);

			if (chat[1].equals(answer)) {

				sendString("CORRECT&" + ansPlayer + SUB_DELIMETER + chat[1]);

				turnCount++;
			}
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
