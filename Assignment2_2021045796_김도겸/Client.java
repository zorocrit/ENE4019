


/*콘솔 멀티채팅 클라이언트 프로그램*/
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

//import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;
 

public class Client {
	
	
	static boolean chatmode = false;
	static int chatState = 0;
	//0 : 로그온 안된상태, 1 : 로그온된상태, 2: 방입장완료 (대화가능),
	//5 : req_fileSend (상대방이 현재사용자에게 파일전송을 수락요청한 상태)
	static int roomInfo = 0;
	//0 : 초기값
	//1 : 방 생성하기
	//2 : 방 입장하기
	static String inputIP;

    public static void main(String[] args) throws UnknownHostException, IOException {
       inputIP = args[0];
	   String firstPort = args[1];
	   String secondPort = args[2];
       
        try{
            //String ServerIP = "localhost";
            //Socket socket = new Socket(ServerIP, 9999); //소켓 객체 생성
			Socket socket = new Socket(inputIP, Integer.parseInt(firstPort)); //소켓 객체 생성
            System.out.println("[##] 서버와 연결이 되었습니다......");
            //사용자로부터 얻은 문자열을 서버로 전송해주는 역할을 하는 쓰레드.
            
            
            /////////////////////////////////////////////////////////////
            
            Thread sender = new Sender(socket);  
            Thread receiver = new Receiver(socket);        
       
            sender.start(); //스레드 시동
            receiver.start(); //스레드 시동
           
        }catch(Exception e){
            System.out.println("예외[Client class]:"+e);
        }
        
    }//main()-------
}//End class Client





/////////////////////////////////////////////////////////////////////
 
//서버로부터 메시지를 읽는 클래스
class Receiver extends Thread{
   
    Socket socket;
    DataInputStream in;
   
    //Socket을 매개변수로 받는 생성자.
    public Receiver(Socket socket){
        this.socket = socket;
        
        try{
            in = new DataInputStream(this.socket.getInputStream());
        }catch(Exception e){
            System.out.println("예외:"+e);
        }
    }//생성자 --------------------
   
    
    /**메시지 파서*/     
   public String[] getMsgParse(String msg){
    	//System.out.println("msgParse()=>msg?"+ msg);
    	
    	String[] tmpArr = msg.split("[|]");
    	
    	return tmpArr;
    }
    
    
    
    @Override
    public void run(){ //run()메소드 재정의
    	
        while(in!=null){ //입력스트림이 null이 아니면..반복
            try{     	
            
            	
            	String msg = in.readUTF(); //입력스트림을 통해 읽어온 문자열을 msg에 할당.
            	
                String[] msgArr = getMsgParse(msg.substring(msg.indexOf("|")+1));
                
                //메세지 처리 ----------------------------------------------
                if(msg.startsWith("logon#yes")){ //로그온 시도 (대화명)
                	Client.chatState = 1; //채팅 상태를 로그온 된 상태로 변경.
                	//logon#yes|그룹리스트              
                	System.out.println(msgArr[0]);
                	//System.out.println("▶방 생성하기/방 입장하기:");
                	
                }else  if(msg.startsWith("logon#no")){ //로그온 실패 (대화명)
                	
                	Client.chatState = 0;
                	System.out.println("[##] 중복된 이름이 존재합니다");
                	//1. 이름이 중복될경우(서버전체 or 그룹) logon#no 패킷이 서버로부터 전달됨.
                	//2. 이름이 중복될경우 서버에서 자체적으로 name(1), name(2) 이런식으로 중복되지 않게 변경하는 방법.
                	                	
                }else if(msg.startsWith("enterRoom#yes")){ //그룹입장  
                	
                	//enterRoom#yes|지역
                    System.out.println("[##] 채팅방 ("+msgArr[0]+") 에 입장하였습니다.");
                    Client.chatState = 2; //챗 상태 변경 ( 채팅방입장 완료로 대화가능상태)
                	 
                }else if(msg.startsWith("enterRoom#no")){
                	//enterRoom#no|지역
                	 System.out.println("[##] 입력하신 ["+msgArr[0]+ "]는 존재하지않는 채팅방입니다.");
                	 System.out.println("▶채팅방을 다시 입력해 주세요:");
					Client.chatState = 0;

				}else if(msg.startsWith("show")){ //서버에서전달하고자하는 메시지
                
                	//show|메시지내용     
                	System.out.println(msgArr[0]);
                	
                }else if(msg.startsWith("say")){ //대화내용
                	//say|아이디|대화내용            		
            		System.out.println("["+msgArr[0]+"] "+msgArr[1] );	
            		
                }else if(msg.startsWith("req_fileSend")){ //상대방이 현재 사용자에게 파일전송 수락 요청
                	//req_fileSend|출력내용  
                	//req_fileSend|[##] name 님께서 파일 전송을 시도합니다. 수락하시겠습니까?(Y/N)
                	Client.chatState = 5; //상태 변경 (상대방이 현재사용자에게 파일전송을 수락요청한 상태)
                	System.out.println(msgArr[0]); //메세지만 추출
                	System.out.print("▶선택:");   
                	sleep(100);
                	
                }else if(msg.startsWith("fileSender")){ //파일을 보내기위해 파일서버 준비
                	               	
                    //fileSender|filepath;                	
                	System.out.println("fileSender:"+InetAddress.getLocalHost().getHostAddress());
                	System.out.println("fileSender:"+msgArr[0]);
            		//String ip=InetAddress.getLocalHost().getHostAddress();
            		//TODO ip, port number, file name을 어떻게 해결할 지 고민해야함,
					//TODO 이 경우에 과연 전체 참가자에게 파일이 전송될지 궁금함.
                	try {
						new FileSender(msgArr[0]).start(); //쓰레드 실행.
						//new FileSender(msgArr[0], InetAddress.getLocalHost().getHostAddress(), 2000, msgArr[0]).start(); //쓰레드 실행.
					} catch (Exception e) {
						System.out.println("FileSender 쓰레드 오류:");
						e.printStackTrace();
					}
                	
                }else if(msg.startsWith("fileReceiver")){ //파일받기 
                	//fileReceiver|ip|fileName; 
                	
					System.out.println("fileReceiver:"+InetAddress.getLocalHost().getHostAddress());
					System.out.println("fileReceiver:"+msgArr[0]+"/"+msgArr[1]);
				   	
            		String ip = Client.inputIP;  //서버의 아이피를 전달 받음
            		String fileName = msgArr[1]; //서버에서 전송할 파일이름.
            		       	
					try {
						new FileReceiver(ip,fileName).start(); //쓰레드 실행.
					} catch (Exception e) {
						System.out.println("FileSender 쓰레드 오류:");
						e.printStackTrace();
					}
					
					
				}  else if(msg.startsWith("req_exit")){ //종료  
                	
					
					
                }
            
            }catch(SocketException e){            	
            	 System.out.println("예외:"+e);
            	 System.out.println("##접속중인 서버와 연결이 끊어졌습니다.");
            	return;
            	 
            } catch(Exception e){            	
                System.out.println("Receiver:run() 예외:"+e);
              
            }
        }//while----
    }//run()------
}//class Receiver -------
 

/////////////////////////////////////////////////////////////////////

//서버로 메시지를 전송하는 클래스 
class Sender extends Thread {
    Socket socket;
    DataOutputStream out;
    String name;
   
    //생성자 ( 매개변수로 소켓과 사용자 이름 받습니다. )
    public Sender(Socket socket){ //소켓과 사용자 이름을 받는다.
        this.socket = socket;      
        try{
            out = new DataOutputStream(this.socket.getOutputStream());
           
            
        }catch(Exception e){
            System.out.println("예외:"+e);
        }
    }
   
    @Override
    public void run(){ //run()메소드 재정의
       
        Scanner s = new Scanner(System.in);
        //키보드로부터 입력을 받기위한 스캐너 객체 생성
        
        while(out!=null){ //출력스트림이 null이 아니면..반복
        	try { //while문 안에 try-catch문을 사용한 이유는 while문 내부에서 예외가 발생하더라도
                  //계속 반복할수있게 하기위해서이다.                   
                
            	String msg = s.nextLine();
            	
               if(msg==null||msg.trim().equals("")){
            		
            	   msg=" ";
            	   //continue; //콘솔에선 공백으로 넘기는것이 좀더 효과적임.
            	   //System.out.println("공백");
            	}
			   else if(Client.chatState == 0) {//로그온된 상태이며 그룹방을 입력받기위한 상태
            		//req_enterRoom|대화명|지역명
					String[] str = msg.trim().split(" ");
					if(str[0].equals("#CREATE")){

						if(!str[2].equals("")){
							out.writeUTF("req_logon|"+str[2]);
							name = str[2];
						}else{
							System.out.println("[##] 공백을 입력할수없습니다.\r\n" +
									"▶다시 입력해 주세요:");
						}
						if(!str[1].equals("")){
							out.writeUTF("req_makeRoom|"+name+"|"+str[1]);
							Client.roomInfo = 4;
						}else{
							System.out.println("[##] 공백을 입력할수없습니다.\r\n" +
									"▶다시 입력해 주세요:");
						}
					}else if(str[0].equals("#JOIN")){
						if(!str[2].equals("")){
							out.writeUTF("req_logon|"+str[2]);
							name = str[2];
						}else{
							System.out.println("[##] 공백을 입력할수없습니다.\r\n" +
									"▶다시 입력해 주세요:");
						}
						if(!str[1].equals("")){
							out.writeUTF("req_enterRoom|"+name+"|"+str[1]);
							Client.roomInfo = 4;
						}else{
							System.out.println("[##] 공백을 입력할수없습니다.\r\n" +
									"▶다시 입력해 주세요:");
						}
					}
					else {
						System.out.println("[##] 잘못된 입력입니다..\r\n" +
								"▶방 이름을 다시 지정해주세요:");
					}


            	}else if(msg.trim().startsWith("#") && !msg.trim().equals("#GET")){
            		//명령어 기능 추가. ( /접속자 , 전달할메시지... 등 )
            		//클라이언트단에서 체크
            		if(msg.equalsIgnoreCase("#EXIT")){
           			  System.out.println("[##] 클라이언트를 종료합니다.");
           			  System.exit(0);
           			  break;
            		}else{
            			out.writeUTF("req_cmdMsg|"+name+"|"+msg);
             			//req_cmdMsg|대화명|/접속자
            		}
            		
            	}else if(Client.chatState == 5) { //5 : 상대방이 파일전송을 시도하여 사용자의 수락요청을 기다림.
            		//fileSend|result)
            		if(msg.trim().equalsIgnoreCase("#GET")){
        				out.writeUTF("fileSend|yes|" + Client.inputIP);
        			}
					else if(msg.trim().equalsIgnoreCase("y")){
						out.writeUTF("fileSend|yes");
					}else if(msg.trim().equalsIgnoreCase("#NO")){
        				out.writeUTF("fileSend|no");                         		
        			}else{
        				System.out.println("입력한 값이 올바르지 않습니다."); 
        				out.writeUTF("fileSend|no");          
        			}

        			Client.chatState=2; //파일전송수락요청에대한 응답완료 상태
            		
            	}else{
            		//req_say|아이디|대화내용
            		out.writeUTF("req_say|"+name+"|"+msg);      			
            	}   
            	
            }catch(SocketException e){            	
	           	 System.out.println("Sender:run()예외:"+e);
	           	 System.out.println("##접속중인 서버와 연결이 끊어졌습니다.");
	           	return;           	 
           } catch (IOException e) {
                System.out.println("예외:"+e);
           }
        }//while------
      
    }//run()------
}//class Sender-------

/////////////////////////////////////////////////////////////////////