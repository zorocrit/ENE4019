import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
 
/*콘솔 멀티채팅 서버 프로그램*/
public class Server {
    HashMap<String,HashMap<String,ServerRecThread>> globalMap; //지역별 해쉬맵을 관리하는 해시맵
    ServerSocket serverSocket = null; 
    Socket socket = null;
    static int connUserCount = 0; //서버에 접속된 유저 카운트

	static int firstPort;

	static int secondPort;

    //생성자
    public Server(){
       globalMap = new HashMap<String,HashMap<String, ServerRecThread>>();
       
    	//clientMap = new HashMap<String,DataOutputStream>(); //클라이언트의 출력스트림을 저장할 해쉬맵 생성.
        Collections.synchronizedMap(globalMap); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> group01 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group01); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> group02 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group02); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> group03 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group03); //해쉬맵 동기화 설정.
       
        HashMap<String,ServerRecThread> group04 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group04); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> group05 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group05); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> group06 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group06); //해쉬맵 동기화 설정.
        
        HashMap<String,ServerRecThread> group07 = new HashMap<String,ServerRecThread>();
        Collections.synchronizedMap(group07); //해쉬맵 동기화 설정.
        
        
        globalMap.put("방1",group01);
        globalMap.put("방2",group02);
        globalMap.put("방3",group03);
        globalMap.put("방4",group04);
        globalMap.put("방5",group05);
        globalMap.put("방6",group06);
        globalMap.put("방7",group07);
        
        
    }//생성자----
   
    public void init(){
        try{
            serverSocket = new ServerSocket(firstPort); //first포트로 서버소켓 객체생성.
            System.out.println("##서버가 시작되었습니다.");
           
            while(true){ //서버가 실행되는 동안 클라이언트들의 접속을 기다림.
                socket = serverSocket.accept(); //클라이언트의 접속을 기다리다가 접속이 되면 Socket객체를 생성.
                System.out.println(socket.getInetAddress()+":"+socket.getPort()); //클라이언트 정보 (ip, 포트) 출력
               
                Thread msr = new ServerRecThread(socket); //쓰레드 생성.
                msr.start(); //쓰레드 시동.
            }      
           
        }catch(Exception e){
            e.printStackTrace();
        }
    }
   
    
    
    /** 접속된 모든 클라이언트들에게 메시지를 전달. */
    public void sendAllMsg(String msg){
       
        Iterator global_it = globalMap.keySet().iterator();
       
        while(global_it.hasNext()){
            try{
            	HashMap<String, ServerRecThread> it_hash = globalMap.get(global_it.next());
            	Iterator it = it_hash.keySet().iterator();
            	while(it.hasNext()){
            		ServerRecThread st = it_hash.get(it.next());
            		st.out.writeUTF(msg);
            	}               
            }catch(Exception e){
                System.out.println("예외:"+e);
            }
        }
    }//sendAllMsg()-----------
    
    
    /**해당 클라이언트가 속해있는 그룹에대해서만 메시지 전달.*/
    public void sendGroupMsg(String loc,String msg){       
       
    	HashMap<String, ServerRecThread> gMap = globalMap.get(loc);    	
    	Iterator<String> group_it = globalMap.get(loc).keySet().iterator();        
        while(group_it.hasNext()){
            try{ 	
            		ServerRecThread st = gMap.get(group_it.next());
            		if(!st.chatMode){ //1:1대화모드가 아닌 사람에게만.
            			st.out.writeUTF(msg);	
            		}
            }catch(Exception e){
                System.out.println("예외:"+e);
            }
        }   
    }//sendGroupMsg()-----------
    
    
    /**1:1 대화*/ 
    public void sendPvPMsg(String loc,String fromName, String toName, String msg){
     
    		try {
				globalMap.get(loc).get(toName).out.writeUTF(msg);
				globalMap.get(loc).get(fromName).out.writeUTF(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
         
    }//sendPvPMsg()-----------
    

    
    
    /**각그룹의 접속자수와 서버에 접속된 유저를 반환
     * 하는 메소드**/
    public String getEachMapSize(){
    	return getEachMapSize(null);     
    }//getEachMapSize()-----------
    
    /**각그룹의 접속자수와 서버에 접속된 유저를 반환 하는 메소드
     * 추가 지역을 전달받으면 해당 지역을 체크
     * */
    public String getEachMapSize(String loc){
    	
        Iterator global_it = globalMap.keySet().iterator();
        StringBuffer sb = new StringBuffer();
        int sum=0;
        //sb.append("=== 그룹 목록 ==="+System.getProperty("line.separator"));
        while(global_it.hasNext()){
            try{
            	String key = (String) global_it.next();
            	
            	HashMap<String, ServerRecThread> it_hash = globalMap.get(key);
            	//if(key.equals(loc)) key+="(*)"; //현재 유저가 접속된 곳 표시
            	int size = it_hash.size();
            	sum +=size;
            	//sb.append(key+": ("+size+"명)"+(key.equals(loc)?"(*)":"")+"\r\n");
                
            }catch(Exception e){
                System.out.println("예외:"+e);
            }
        }
        //sb.append("⊙현재 대화에 참여하고있는 유저수 :"+ MultiServer.connUserCount);
        sb.append("⊙현재 대화에 참여하고있는 유저수 :"+ sum+ "명 \r\n");
        //System.out.println(sb.toString());
        return sb.toString();
    }//getEachMapSize()-----------
    
    
    /**접속된 유저 중복체크*/        
public boolean isNameGlobla(String name){
    	boolean result=false;
        Iterator<String> global_it = globalMap.keySet().iterator();
        while(global_it.hasNext()){
            try{
            	String key = global_it.next();            	
            	HashMap<String, ServerRecThread> it_hash = globalMap.get(key);
                if(it_hash.containsKey(name)){
                	result= true; //중복된 아이디가 존재.
                	break;
                }
            	
            }catch(Exception e){
                System.out.println("isNameGlobla()예외:"+e);
            }
        }
     
        return result;
    }//isNameGlobla()-----------

    
    /**문자열 null 값 및 "" 은 대체 문자열로 삽입가능.*/
    public String nVL(String str, String replace){
    	String output="";
    	if(str==null || str.trim().equals("")){
    		output = replace; 		
    	}else{
    		output = str;
    	}
    	return output;    	
    }
    
    
    
    //main메서드
    public static void main(String[] args) {
		String firstTemp = args[0];
		String secondTemp = args[1];
		firstPort = Integer.parseInt(firstTemp);
		secondPort = Integer.parseInt(secondTemp);
		//TODO Port 번호 설정 기능 추가해야함
        Server ms = new Server(); //서버객체 생성.
        ms.init();//실행.
    }//main()------  
   
    
    
    ////////////////////////////////////////////////////////////////////////
    //----// 내부 클래스 //--------//
	int checker = 1;
	java.lang.String[] roomStr = new java.lang.String[8];
    // 클라이언트로부터 읽어온 메시지를 다른 클라이언트(socket)에 보내는 역할을 하는 메서드
    class ServerRecThread extends Thread {
       
        Socket socket;
        DataInputStream in;
        DataOutputStream out;
        String name=""; //이름 저장
        String loc="";  //지역 저장
        String toNameTmp = null;//1:1대화 상대  
        String fileServerIP; //파일서버 아이피 저장
        String filePath; //파일 서버에서 전송할 파일 패스 저장.
        boolean chatMode; //1:1대화모드 여부


        
        //생성자.
        public ServerRecThread(Socket socket){
            this.socket = socket;
            try{
                //Socket으로부터 입력스트림을 얻는다.
                in = new DataInputStream(socket.getInputStream());
                //Socket으로부터 출력스트림을 얻는다.
                out = new DataOutputStream(socket.getOutputStream());
            }catch(Exception e){
                System.out.println("ServerRecThread 생성자 예외:"+e);
            }
        }//생성자 ------------
        
        
        
        /**접속된 유저리스트  문자열로 반환*/        
        public String showUserList(){
         	
         	StringBuilder output = new StringBuilder("==채팅방 상태==\r\n");
			if(loc.equals("방1")) {
				output.append("채팅방 이름:" + roomStr[1] + "\r\n");
			}
			else if(loc.equals("방2")) {
				output.append("채팅방 이름:" + roomStr[2] + "\r\n");
			}
			else if(loc.equals("방3")) {
				output.append("채팅방 이름:" + roomStr[3] + "\r\n");
			}
			else if(loc.equals("방4")) {
				output.append("채팅방 이름:" + roomStr[4] + "\r\n");
			}
			else if(loc.equals("방5")) {
				output.append("채팅방 이름:" + roomStr[5] + "\r\n");
			}
			else if(loc.equals("방6")) {
				output.append("채팅방 이름:" + roomStr[6] + "\r\n");
			}
			else if(loc.equals("방7")) {
				output.append("채팅방 이름:" + roomStr[7] + "\r\n");
			}
			output.append("참여자 이름:" + "\r\n");
         	Iterator it = globalMap.get(loc).keySet().iterator(); //해쉬맵에 등록된 사용자이름을 가져옴.
         	while(it.hasNext()){ //반복하면서 사용자이름을 StringBuilder에 추가
                 try{
                 	String key= (String) it.next();                 	            	
                    //out.writeUTF(output);
                 	if(key.equals(name)){ //현재사용자 체크
                 		key += " (*) ";
                 	}    
                 	
                 	output.append(key+"\r\n");                 	
                 }catch(Exception e){
                     System.out.println("예외:"+e);
                 }
             }//while---------
         	output.append("=="+ globalMap.get(loc).size()+"명 접속중==\r\n");
         	System.out.println(output.toString());
     		return output.toString();
         }//showUserList()-----------
        
       
       /**메시지 파서 */     
       public String[] getMsgParse(String msg){
        	System.out.println("msgParse():msg?   "+ msg);        	
        	String[] tmpArr = msg.split("[|]");        	
        	return tmpArr;
        }
        
        
        @Override
        public void run(){ //쓰레드를 사용하기 위해서 run()메서드 재정의
        	HashMap<String, ServerRecThread> clientMap=null;   //현재 클라이언트가 저장되어있는 해쉬맵        
           
        	try{   
                while(in!=null){ //입력스트림이 null이 아니면 반복.
                    String msg = in.readUTF(); //입력스트림을 통해 읽어온 문자열을 msg에 할당.                	
                    String[] msgArr = getMsgParse(msg.substring(msg.indexOf("|")+1));
                    
                    //메세지 처리 ----------------------------------------------
                    if(msg.startsWith("req_logon")){ //로그온 시도 (대화명)                    	
                    	//req_logon|대화명
                    	                    	
                    	if(!(msgArr[0].trim().equals(""))&&!isNameGlobla(msgArr[0])){                    	
	                    	name = msgArr[0]; //넘어온 대화명은 전역변수 name에 저장
	                    	Server.connUserCount++; //접속자수 증가. (스택틱변수를 사용하기에 어울려서 한번 사용해봄.)
	                    	out.writeUTF("logon#yes|"+getEachMapSize()); //접속된 클라이언트에게 그룹목록 제공
	                    }else{
	                    	 out.writeUTF("logon#no|err01");
	                    }
                    	
                    }else if(msg.startsWith("req_makeRoom")){ //그룹입장을 시도
						//TODO 이미 있는 채팅방일 때 메시지를 보내야함.
						//req_enterRoom|대화명|지역
						java.lang.String tempStr = msgArr[1];
						int tCh = 0;
						for(int i = 1; i < 8; i++) {
							if(tempStr.equals(roomStr[i])) {
								out.writeUTF("logon#no|이미 존재하는 채팅방입니다.");
								tCh = 1;
							}
						}
						if(checker == 1) {
							loc = "방1";
							roomStr[checker] = tempStr;
							checker = 2;
						}
						else if(checker == 2) {
							loc = "방2";
							roomStr[checker] = tempStr;
							checker = 3;
						}
						else if(checker == 3) {
							loc = "방3";
							roomStr[checker] = tempStr;
							checker++;
						}
						else if(checker == 4) {
							loc = "방4";
							roomStr[checker] = tempStr;
							checker++;
						}
						else if(checker == 5) {
							loc = "방5";
							roomStr[checker] = tempStr;
							checker++;
						}
						else if(checker == 6) {
							loc = "방6";
							roomStr[checker] = tempStr;
							checker++;
						}
						else if(checker == 7) {
							loc = "방7";
							roomStr[checker] = tempStr;
							checker++;
						}
						else {
							out.writeUTF("logon#no|채팅방 공간이 부족합니다.");
						}

						//loc = msgArr[1]; //메시지에서 지역부분만 추출하여 전역변수에 저장
						if(tCh == 0) {
							if(isNameGlobla(msgArr[0])){
								out.writeUTF("logon#no|"+name);

							}else if(globalMap.containsKey(loc)){
								sendGroupMsg(loc, "show|[##] "+name + "님이 입장하셨습니다.");
								clientMap= globalMap.get(loc); //현재그룹의 해쉬맵을 따로 저장.
								clientMap.put(name, this); //현재 MultiServerRec인스턴스를 클라이언트맵에 저장.
								System.out.println(getEachMapSize()); //서버에 그룹리스트 출력.
								out.writeUTF("enterRoom#yes|"+tempStr); //접속된 클라이언트에게 그룹목록 제공

							}else{
								out.writeUTF("enterRoom#no|"+tempStr);
							}
						}
					}else if(msg.startsWith("req_enterRoom")){ //그룹입장을 시도
                    	
                    	//req_enterRoom|대화명|지역
                    	 //loc = msgArr[1]; //메시지에서 지역부분만 추출하여 전역변수에 저장
						java.lang.String tempStr = msgArr[1];
						if(tempStr.equals(roomStr[1])) {
							loc = "방1";
						}
						else if(tempStr.equals(roomStr[2])) {
							loc = "방2";
						}
						else if(tempStr.equals(roomStr[3])) {
							loc = "방3";
						}
						else if(tempStr.equals(roomStr[4])) {
							loc = "방4";
						}
						else if(tempStr.equals(roomStr[5])) {
							loc = "방5";
						}
						else if(tempStr.equals(roomStr[6])) {
							loc = "방6";
						}
						else if(tempStr.equals(roomStr[7])) {
							loc = "방7";
						}



                    	 if(isNameGlobla(msgArr[0])){
                    		 out.writeUTF("logon#no|"+name);   
                    		 
                    	 }else if(globalMap.containsKey(loc)){
                    		 sendGroupMsg(loc, "show|[##] "+name + "님이 입장하셨습니다.");
                        	 clientMap= globalMap.get(loc); //현재그룹의 해쉬맵을 따로 저장.
                        	 clientMap.put(name, this); //현재 MultiServerRec인스턴스를 클라이언트맵에 저장.
                        	 System.out.println(getEachMapSize()); //서버에 그룹리스트 출력.                    	 
                        	 out.writeUTF("enterRoom#yes|"+tempStr); //접속된 클라이언트에게 그룹목록 제공
                        	 
                    	 }else{                    		
                    		 out.writeUTF("enterRoom#no|"+tempStr);
                    	 }
                    	
                    	 
                    	
                    	 
                    	 
                    }else if(msg.startsWith("fileSend")){ //파일전송
						//fileSend|result
						String[] result = msgArr[0].split(" ");
						if(result[0].equals("yes")){
							System.out.println("##파일전송##YES");
							try {
								String tmpfileServerIP = result[1];
								String tmpfilePath = filePath;
								sendGroupMsg(loc, "fileSender|"+tmpfilePath);
								//fileSender|filepath;
								//clientMap.get(toNameTmp).out.writeUTF("fileSender|"+tmpfilePath);
								//파일을 전송할 클라이언트에서 서버소켓을 열고 filePath로 저장된 파일을 읽어와서 OutputStream으로 출력

								//fileReceiver|ip|fileName;
								//String fileName = tmpfilePath.substring(tmpfilePath.lastIndexOf("\\")+1); //파일 명만 추출
								String fileName = new File(tmpfilePath).getName();
								out.writeUTF("fileReceiver|"+tmpfileServerIP+"|"+fileName);

								/*리셋*/
								filePath="";
								fileServerIP="";

							}  catch (IOException e) {
								e.printStackTrace();
							}
						}else /*(result.equals("no"))*/{
							sendGroupMsg(loc, "show|[##] "+name+" 님께서 파일전송을 거절하였습니다.");
							//clientMap.get(toNameTmp).out.writeUTF("show|[##] "+name+" 님께서 파일전송을 거절하였습니다.");
						}//if

					}else if(msg.startsWith("req_cmdMsg")){ //명령어 전송
                    	//req_cmdMsg|대화명|/접속자
                    	if(msgArr[1].trim().equals("#STATUS")){
                			out.writeUTF("show|"+showUserList()); //접속자 출력  
                		
							
                		}else if(msgArr[1].trim().startsWith("#PUT")){
							/*if(!chatMode){
								out.writeUTF("show|[##] 1:1대화중일때만 사용할수있는 명령어입니다. ");
								continue;
							}*/
                			
                			String[] msgSubArr = msgArr[1].split(" ",2);
                			if(msgSubArr.length!=2){
                				out.writeUTF("show|[##] 파일전송 명령어 사용법이 잘못되었습니다.\r\n usage : #PUT [전송할파일경로]");
                				continue;                				
                			}
							out.writeUTF("error check 1");
                			filePath = msgSubArr[1];                			
                			File sendFile = new File(filePath);
                			String availExtList = "txt,java,jpeg,jpg,png,gif,bmp";

							out.writeUTF("error check 1");
							if(sendFile.isFile()){
								String fileExt = filePath.substring(filePath.lastIndexOf(".")+1);
								out.writeUTF("error check 2");
								if(availExtList.contains(fileExt)){
									Socket s = globalMap.get(loc).get(toNameTmp).socket;
									//파일서버역할을 하는 클라이언트 아이피 주소 알기위해 소켓 객체 얻어옴.
									out.writeUTF("error check 3");
									//System.out.println("s.getLocalSocketAddress()=>"+s.getLocalSocketAddress());
									//System.out.println("s.getLocalAddress()=>"+s.getLocalAddress());
									System.out.println("s.getInetAddress():파일서버아이피=>"+s.getInetAddress());
									//파일서버역할을 하는 클라이언트 아이피 출력
									out.writeUTF("error check 4");
									fileServerIP = s.getInetAddress().getHostAddress();
									sendGroupMsg(loc, "req_fileSend|[##] "+name +"님께서 파일["+sendFile.getName()+"] 전송을 시도합니다. \r\n수락하시겠습니까?(#GET)");
									//clientMap.get(toNameTmp).out.writeUTF("req_fileSend|[##] "+name +"님께서 파일["+sendFile.getName()+"] 전송을 시도합니다. \r\n수락하시겠습니까?(Y/N)");
									out.writeUTF("show|[##] "+toNameTmp +"님께 파일["+sendFile.getAbsolutePath()+"] 전송을 시도합니다.");

								}else{
                					
                					out.writeUTF("show|[##] 전송가능한 파일이 아닙니다. \r\n["+availExtList+"] 확장자를 가진 파일만 전송가능합니다.");                				
                				} //if               			
                			
                			}else{                				
                				out.writeUTF("show|[##] 존재하지 않는 파일입니다.");                				
                			} //if
                		}else{
                			out.writeUTF("show|[##] 잘못된 명령어입니다.");
                		}//if
                    	
                    }else if(msg.startsWith("req_say")){ //대화내용 전송
                    	  if(!chatMode){
                    		//req_say|아이디|대화내용
                          	sendGroupMsg(loc, "say|"+name+"|"+msgArr[1]);
                          	//출력스트림으로 보낸다.
                          }else{
                          	sendPvPMsg(loc, name,toNameTmp , "say|"+name+"|"+msgArr[1]);
                          }
                    } else if(msg.startsWith("req_exit")){ //종료
	                	
	                }
                    //------------------------------------------------- 메세지 처리
                    
              
                }//while()---------
            }catch(Exception e){
                System.out.println("ServerRec:run():"+e.getMessage() + "----> ");
                //e.printStackTrace();
            }finally{
                //예외가 발생할때 퇴장. 해쉬맵에서 해당 데이터 제거.
                //보통 종료하거나 나가면 java.net.SocketException: 예외발생
                if(clientMap!=null){
                	clientMap.remove(name);
                	sendGroupMsg(loc,"## "+ name + "님이 퇴장하셨습니다.");
                	System.out.println("##현재 서버에 접속된 유저는 "+(--Server.connUserCount)+"명 입니다.");
                }               
            }
        }//run()------------
    }//class MultiServerRec-------------
    //////////////////////////////////////////////////////////////////////
}
 