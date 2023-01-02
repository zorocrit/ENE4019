import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class UDPMultiReceive extends Thread {
    private InetAddress inetAddress = null;
    private int portNumber = 0;
    private MulticastSocket multicastSocket = null;
    private DatagramPacket datagramPacket = null;

    public UDPMultiReceive(InetAddress tmpInetAddress, int tmpportNumber) {
        inetAddress = tmpInetAddress;
        portNumber = tmpportNumber;
    }
    
    public void run() {
        // 1. Data를 받을 버퍼 생성
        byte[] buffer = new byte[512];
        try {
            // 2. DatagramPacket을 받기 위한 Socket 생성
            multicastSocket = new MulticastSocket(portNumber);
            // 3. 그룹 등록 - 통신 가능하게 함
            multicastSocket.joinGroup(inetAddress);
            // 메시지 계속 받음
            while(true) {
                // 4. Data를 받을 Packet 생성
                datagramPacket = new DatagramPacket(buffer, buffer.length);
                // 5. 멀티캐스트에 존재하는 메시지 받음
                multicastSocket.receive(datagramPacket);
                // 6. 수신된 메시지 출력
                String msg = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                System.out.println(msg);
            }
        } catch (IOException e) {
            System.err.println(e);
            System.exit(0);
        } finally {
            multicastSocket.close();
        }
    }
}