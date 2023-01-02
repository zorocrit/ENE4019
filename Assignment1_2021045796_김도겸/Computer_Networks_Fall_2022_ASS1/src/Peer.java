import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;


public class Peer {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        InetAddress inetAddress = null;
        BufferedReader br = null;
        UDPMultiReceive udpMultiReceive = null;
        UDPMultiSend udpMultiSend = null;
        int portNumber = 0;
        String strId = "";

        try {
            // 1. IP 주소, port 번호 입력 사항 확인
            br = new BufferedReader(new InputStreamReader(System.in));
            String port = args[0];
            portNumber = Integer.parseInt(port);
            System.out.print("running - port number : " + portNumber + "\n");
            String chatName;
            String[] chatNameArr;
            while(true) {
                chatName = br.readLine();
                chatNameArr = chatName.split(" ");
                if(chatName.equals("#EXIT")) {
                    System.out.println("종료합니다.");
                    System.exit(0);
                }
                else if(chatNameArr.length == 3 && chatNameArr[0].equals("#JOIN")) {
                    break;
                }
                else {
                    continue;
                }
            }
            String address = chatNameArr[1];
            SHA256 sha256 = new SHA256();
            strId = sha256.encrypt(address);
            byte[] id = strId.getBytes();
            String finalAddress = "225." + id[61] + "." + id[62] + "." + id[63];
            inetAddress = InetAddress.getByName(finalAddress);
            // 2. 이름 입력
            br = new BufferedReader(new InputStreamReader(System.in));
            strId = chatNameArr[2];
            System.out.println("*********** " + strId + "님 접속 ***********");
            // 3. 데이터 받기
            udpMultiReceive = new UDPMultiReceive(inetAddress, portNumber);
            udpMultiReceive.start();
            // 4. 데이터 보내기
            udpMultiSend = new UDPMultiSend(inetAddress, portNumber, strId);
            udpMultiSend.start();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 