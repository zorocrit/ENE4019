import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPMultiSend extends Thread {
    private InetAddress inetAddress = null;
    private int portNumber = 0;
    private String strId = "";
    private DatagramSocket datagramSocket = null;
    private DatagramPacket datagramPacket = null;
    private BufferedReader br = null;

    public UDPMultiSend(InetAddress tmpInetAddress, int tmpportNumber, String tmpstrId) {
        inetAddress = tmpInetAddress;
        portNumber = tmpportNumber;
        strId = tmpstrId;
    }

    public void exit() {
        datagramSocket.close();
        System.exit(0);
    }

    public void run() {
        // 1. Data를 보낼 버퍼 생성
        byte[] buffer = new byte[512];
        try {
            // 2. Socket 열기
            datagramSocket = new DatagramSocket();
            // 3. Server로 메시지 전송을 위한 입력 스트림 생성
            br = new BufferedReader(new InputStreamReader(System.in));
            // 메시지 계속 전송
            while(true) {
                // 4. 메시지 입력
                String temp = br.readLine();
                String msg = strId + ": " + temp;
                byte [] b = temp.getBytes();
                buffer = msg.getBytes();
                if(temp.equals("#EXIT")) {
                    throw new CustomException("종료합니다.");
                }
                if(b[0] == '#') {
                    throw new CustomException2("명령어가 아닙니다.");
                }
                // 6. DatagramPacket에 각 정보를 담고 전송
                datagramPacket = new DatagramPacket(buffer, buffer.length, inetAddress, portNumber);
                datagramSocket.send(datagramPacket);
            }
        } catch(IOException ie) {
            System.out.println("send 오류");
        } catch(CustomException ce) {
            System.out.println(ce.getMessage());
            exit();
        } catch (CustomException2 c2) {
            System.out.println(c2.getMessage());
            run();
        }
        finally {
            datagramSocket.close();
        }
    }
}