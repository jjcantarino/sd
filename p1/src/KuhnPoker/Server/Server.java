package KuhnPoker.Server;


import java.io.*;
import java.net.*;
import KuhnPoker.ComUtils;
import KuhnPoker.GameLogic.KuhnPokerGame;
import KuhnPoker.KPGameWrapper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Server {
    ConcurrentHashMap<String,Integer> userMap; //hash map of all users that have played on this server (not saved after exit)
    ConcurrentHashMap<String,Integer> currentUserMap;//hash map of users currently playing a game
    
    private final int timeOut=60000;// 60 seconds is given to client to respond
    private final int startMoney=10;// start money for all new users
    private boolean randomAI;// server ai behaviour
    public Server(){
        userMap=new ConcurrentHashMap<>();
        currentUserMap=new ConcurrentHashMap<>();
        randomAI=false;
    }
    

  public static void main(String[] args) {
    Server server = new Server();
    ServerSocket serverSocket = null;
    //KuhnPokerGame kpg = new KuhnPokerGame();
    //Interpreter interpreter = new Interpreter(kpg);
    Socket socket = null;
    //ComUtils comUtils;
    int portServidor = 1111;
    
    if(args.length ==4&&!args[0].equals("-p")&&args[2].equals("-i")){
      System.out.println("Usage: KuhnPoker.Server.Server -p [<numPort>] -i [0|1] where 1 is random actions, 0 is better AI");
      System.exit(1);
    }
    else if(args.length ==4){
        try{
        portServidor = Integer.parseInt(args[1]);
        }catch(NumberFormatException e23){
            System.out.println("Port is invalid, using default");
        }
        server.randomAI=args[3].equals("1");
    }
    else if(args.length ==2){
        try{
        portServidor = Integer.parseInt(args[1]);
        }catch(NumberFormatException e23){
            System.out.println("port is invalid");
        }
        
    }
    else if(args.length==0){
        System.out.println("No arguments given. using default configuration");
    }
    
    if(server.randomAI){
        System.out.println("Server AI takes random actions");
    }
        try {   
            /* Creem el servidor */
            serverSocket = new ServerSocket(portServidor);
            System.out.println("Server socket ready, port: " + portServidor);
            //boolean socketConnected = false;
            while (true) {
                //System.out.println("Esperant una connexió d'un client.");
                /* Esperem a que un client es connecti amb el servidor */
                socket = serverSocket.accept();
                //-> THREAD
                if (socket.isConnected()){
                    (server.new ParalelThread(socket)).start();
                }
            } 
        }catch (IOException ex) {
            System.out.println("Server socket error "+ex.toString());
    }finally{
        /* Tanquem la comunicacio amb el client */
		try {
			if(serverSocket != null) serverSocket.close();
		}
		catch (IOException ex) {
			System.out.println("IOException on server");
		} // fi del catch
	}
	
  } // fi del main
  
  //client-handling thread, does everything related to communication with one client
  public class ParalelThread extends Thread{
      Socket socket = null;
      String clientID;
      int clientMoney;
      ComUtils comUtils;
      KuhnPokerGame kpg;
      KPGameWrapper interpreter;
      PrintWriter logWriter;
      
      public ParalelThread(Socket sc) throws IOException{//Runnable runnable){
          socket=sc;
          comUtils = new ComUtils(socket);
          socket.setSoTimeout(timeOut);
          kpg = new KuhnPokerGame();
          interpreter = new KPGameWrapper(kpg);
          clientMoney=10;//getClientMoney(clientID);
          
          


          
      }
      /*private int getClientMoney(String id){
          return 10;
      }*/
      @Override
      public void run(){
        if(socket==null){
              System.exit(MIN_PRIORITY);
        }
        if (socket.isConnected()){
            System.out.println("User connected");

            String input_output="test Error";
            try {
                logWriter = new PrintWriter("Server"+this.currentThread().getName()+".log", "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException ex) {
                System.out.println("file error");
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                input_output  = comUtils.read_PtMsg();
                logWriter.println(input_output);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                if(!toServerProtocolCheck(input_output)){
                    System.out.println("Error in recieved message");
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    logWriter.close();
                    return;

                }
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);

            }
            if(input_output.length()==8&&input_output.substring(0,4).equals("STRT")){
                clientID=input_output.substring(5);
            }
            
            
            System.out.println("client ID:"+clientID);
            System.out.println("["+clientID+"]C: "+input_output);
            if(randomAI){
                kpg.setAIRandom();
            }
            //obtaining users money. if user havent played before he is given 10 money
            if(!currentUserMap.containsKey(clientID)){
                


                currentUserMap.put(clientID, startMoney);

                if(!userMap.containsKey(clientID)){
                    userMap.put(clientID, startMoney);
                    clientMoney=startMoney;
                }
                else{
                    clientMoney=userMap.get(clientID);

                }
                kpg.setMyMoney(clientMoney);
                //kpg.
                while (!input_output.equals("EXIT")) {
                    interpreter.updateAction(input_output);//updating with data from client
                    
                    input_output = interpreter.outAction();//creating command for client

                    interpreter.updateAction(input_output);// updating internal state machine to maintain sync.
                    //System.out.println("S["+interpreter.getState()+"]: "+input_output);
                    System.out.println("["+clientID+"]S: "+input_output);
                    try {
                        comUtils.write_PtMsg(input_output);//sending command for client
                        logWriter.println(input_output);
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if(interpreter.isErro()){
                        break;//erro 502 handle
                    }

                    //in case server needs to send multiple commands
                    while(interpreter.isReadyToSend()){
                        input_output = interpreter.outAction();

                        interpreter.updateAction(input_output);
                        //System.out.println("S["+interpreter.getState()+"]: "+input_output);
                        System.out.println("["+clientID+"]S: "+input_output);
                        try {
                            comUtils.write_PtMsg(input_output);
                            logWriter.println(input_output);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            break;
                        }

                    }
                    try {
                        input_output  = comUtils.read_PtMsg();
                        logWriter.println(input_output);
                    } catch (SocketTimeoutException ex2) {
                        //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex2);
                        System.out.println("["+clientID+"]S: client timeout, sending error message");
                        try {
                            comUtils.write_PtMsg("ERRO 503");
                            logWriter.println("ERRO 503");
                            System.out.println("["+clientID+"]S: ERRO 503");
                        } catch (IOException ex) {
                            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                            break;
                        }
                        break;
                    }
                     catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    try {
                        if(!toServerProtocolCheck(input_output)){
                            System.out.println("Error in recieved message");
                            break;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        break;
                    }
                    
                    System.out.println("["+clientID+"]C: "+input_output);
                }
                System.out.println("["+clientID+"]: has disconnected, money: "+String.valueOf(kpg.getMyMoney()));
            }
            else{
                System.out.println("ERROR user "+clientID+" already in game");
                try {
                    comUtils.write_PtMsg("ERRO 510");
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
                logWriter.println("ERRO 510");
            }
            try {
                    socket.close();
                } 
            catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            logWriter.close();

            //System.out.println("["+clientID+"]: has disconnected, money: "+String.valueOf(kpg.getMyMoney()));
            currentUserMap.remove(clientID);
            userMap.replace(clientID, kpg.getMyMoney());
        }
          //System.out.println("Thread died");
      }
      /**
   checks if recieved message belongs to The Protocol (client side) and if correct comment has correct params
   */
    private boolean toServerProtocolCheck(String input) throws IOException{
        switch (input.substring(0, 4)){
              case "STRT":
                  if(input.length()==8){
                    if(input.charAt(4)==' '){
                        try{
                          Integer.valueOf(input.substring(5));
                          return true;

                          }
                        catch(NumberFormatException ex){
                            comUtils.write_PtMsg("ERRO 501");
                            logWriter.println("ERRO 501");
                            return false;
                        }
                    }
                  }
                  return false;
              case "BLNC":
                  comUtils.write_PtMsg("ERRO 502");
                  logWriter.println("ERRO 502");
                  return false;
              case "SHOW":
                  if(input.length()==6){
                      if(input.charAt(5)=='J'|| input.charAt(5)=='Q' || input.charAt(5)=='K'){
                          return true;
                      }
                      comUtils.write_PtMsg("ERRO 501");
                      logWriter.println("ERRO 501");
                      return false;
                  }
                  comUtils.write_PtMsg("ERRO 501");
                  logWriter.println("ERRO 501");
                  return false;
              case "WINN":
                  comUtils.write_PtMsg("ERRO 502");
                  logWriter.println("ERRO 502");
                  return false;
              case "DEAL":
                  comUtils.write_PtMsg("ERRO 502");
                  logWriter.println("ERRO 502");
                  return false;
              case "EXIT":
                  return true;   
              case "BETT":
                  return true;
              case "CHCK":
                  return true;
              case "CALL":
                  return true;
              case "FOLD":
                  return true;
              case "TURN":
                  comUtils.write_PtMsg("ERRO 502");
                  logWriter.println("ERRO 502");
                  return false;
              case "RPLY":
                  return true;
              case "ERRO":
                  if(input.length()==8){
                    if(input.charAt(4)==' '){
                        try{
                          Integer.valueOf(input.substring(5));
                          return true;

                          }
                        catch(NumberFormatException ex){
                            comUtils.write_PtMsg("ERRO 501");
                            logWriter.println("ERRO 501");
                            return false;
                        }
                    }
                  }
                  comUtils.write_PtMsg("ERRO 501");
                  logWriter.println("ERRO 501");
                  return false;
              default:
                  comUtils.write_PtMsg("ERRO 501");//we recieved something strange
                  logWriter.println("ERRO 501");
                  return false;

          }

    }
      
  }
/*  public class runnable implements Runnable{
      public runnable(){
          
      }
      @Override
      public void run(){
      }
  }*/
  
} // fi de la classe



/*
C:\Program Files\Java\jdk1.8.0_162\bin>javac -cp \D:\Oneoneoneone\U4it\SD\code D
:\Oneoneoneone\U4it\SD\code\KuhnPoker\Server\Server.java
*/

//D:\Oneoneoneone\U4it\SD\code>java KuhnPoker.Server.Server
