package KuhnPoker.Client;

import java.io.*;
import java.net.*;
import KuhnPoker.ComUtils;
import KuhnPoker.GameLogic.KuhnPokerGame;
import KuhnPoker.KPGClient;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private ComUtils comUtils;
    private static final int timeOut=60000;// 60 seconds timeout from server
    

  public static void main(String[] args){
    Client client= new Client();
    String nomMaquina, str;
    int numPort, value=0;
    KuhnPokerGame kpg = new KuhnPokerGame();
    KPGClient kpInterface = new KPGClient(kpg);
    KuhnPokerClientActor KPActor;
    
    //Random rng = new Random();
    //String clientID="01"+String.valueOf(rng.nextInt()%3);//lets try random client ids
    String clientID="011";

    InetAddress maquinaServidora;
    Socket socket = null;
    
    /*if (args.length != 2){
      System.out.println("Us: java Client <maquina_servidora> <port>");
      System.exit(1);
    }*/

    nomMaquina = "127.0.0.1";//args[0];
    numPort    = 1111;//Integer.parseInt(args[1]); 
    
    if(args.length ==6&&(!args[0].equals("-s") || !args[2].equals("-p") || !args[4].equals("-i"))){
      System.out.println("Usage: KuhnPoker.Client.Client -s [<numPort>] -p [<numPort>] -i [0|1|2] where 2 is manual, 1 is random actions, 0 is better AI");
      System.exit(1);
    }
    else if(args.length ==6){
        nomMaquina = args[1];
        try{
        numPort=Integer.parseInt(args[3]);
        value = Integer.parseInt(args[5]);
        }catch(NumberFormatException e23){
            System.out.println("port or ai mode is invalid");
        }
    }
    else if(args.length ==4){
        nomMaquina = args[1];
        try{
        numPort=Integer.parseInt(args[3]);
        value = 0;
        }catch(NumberFormatException e23){
            System.out.println("port or ai mode is invalid");
        }
        
    }
    else if(args.length==0){
        System.out.println("No (valid) arguments given. using default configuration");
    }
    
    System.out.println("Connecting to "+nomMaquina+":"+String.valueOf(numPort));
    if(value==2){
        KPActor = new KuhnPokerClientActor(kpInterface,false);
        System.out.println("Manual control");
    }
    else if(value==1){
        KPActor = new KuhnPokerClientActor(kpInterface,true);
        kpg.setAIRandom();
        System.out.println("AI is set to random");
    }else{
        KPActor = new KuhnPokerClientActor(kpInterface,true);
        System.out.println("AI mode");
    }
    

    try
    {
      /* Obtenim la IP de la maquina servidora */
        maquinaServidora = InetAddress.getByName(nomMaquina);        
        /* Obrim una connexio amb el servidor */
        socket = new Socket(maquinaServidora, numPort);
        socket.setSoTimeout(timeOut);
        /* Obrim un flux d'entrada/sortida amb el servidor */
        client.comUtils = new ComUtils(socket); 
        String input_output = "STRT "+clientID;
        
        kpInterface.updateAction(input_output);
        client.comUtils.write_PtMsg(input_output);
        
        input_output  = client.comUtils.read_PtMsg();
        kpInterface.updateAction(input_output);
        
        System.out.println("S: "+input_output);
        if(!client.toClientProtocolCheck(input_output)){
            System.out.println("Protocol error on server side");
            try {
                socket.close();
            }catch (IOException ex) {
                System.out.println("Error closing socket");
                System.exit(-1);
            }
            System.exit(-1);
        }
        /* Ens esperem a rebre un valor del client */
        Scanner reader = new Scanner(System.in);  // Reading from System.in
         // Scans the next token of the input as an int.
        //once finished
        
        if(kpg.getMyMoney()<1){
            System.out.println("Not ehough money to continue");
            client.comUtils.write_PtMsg("EXIT");
            try {
                socket.close();
            }catch (IOException ex) {
                System.out.println("Error closing socket");
            }
            System.exit(0);
        }
        input_output = "BETT";
        kpg.moneyDecrement();
        client.comUtils.write_PtMsg(input_output);
        kpInterface.updateAction(input_output);
        
        input_output  = client.comUtils.read_PtMsg();
        System.out.println("S: "+input_output);
        kpInterface.updateAction(input_output);
        if(!client.toClientProtocolCheck(input_output)){
            System.out.println("Protocol error on server side");
            try {
                socket.close();
            }catch (IOException ex) {
                System.out.println("Error closing socket");
                System.exit(-1);
            }
            System.exit(-1);
        }
        
        while (!input_output.equals("EXIT")) {
            //nextMessage = kpInterface.updateAction(input);
            while(!kpInterface.isReadyToSend()){
                input_output  = client.comUtils.read_PtMsg();
                System.out.println("S: "+input_output);
                kpInterface.updateAction(input_output);
                if(!client.toClientProtocolCheck(input_output)){
                    System.out.println("Protocol error on server side");
                    try {
                        socket.close();
                    }catch (IOException ex) {
                        System.out.println("Error closing socket");
                        System.exit(-1);
                    }
                    System.exit(-1);
                }
                
                
            }
            if((kpg.getMyMoney()<1&&!kpg.isReady()) ||(kpg.getMyMoney()<0&&(kpg.isReady()||kpg.isFinished()))){
                System.out.println("Not ehough money to continue");
                client.comUtils.write_PtMsg("EXIT");
                try {
                    socket.close();
                }catch (IOException ex) {
                    System.out.println("Error closing socket");
                }
                System.exit(0);
            }
            
            //input_output = reader.nextLine();
            input_output = KPActor.nextComment(input_output);
            client.comUtils.write_PtMsg(input_output);
            kpInterface.updateAction(input_output);
            if(kpInterface.isErro()){
                break;
            }
            System.out.println("C: "+input_output);
            if(input_output.toUpperCase().equals("FOLD")||kpInterface.isWon()){//input_output.subSequence(0, 3).equals("SHO")
                input_output  = client.comUtils.read_PtMsg();
                System.out.println("S: "+input_output);
                kpInterface.updateAction(input_output);
                if(!client.toClientProtocolCheck(input_output)){
                    System.out.println("Protocol error on server side");
                    try {
                        socket.close();
                    }catch (IOException ex) {
                        System.out.println("Error closing socket");
                        System.exit(-1);
                    }
                    System.exit(-1);
                }
                //System.out.println("["+kpInterface.getState()+"]lalala "+String.valueOf(kpInterface.isReadyToSend()));
                
            }

        }
        socket.close();
        reader.close();
    }
    catch(SocketTimeoutException exc){
        System.out.println("Connection timeout");
        try {
            client.comUtils.write_PtMsg("ERRO 403");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    catch (IOException e){
        System.out.println("Socket error");
    }
    finally{
        try {
            if(socket != null) socket.close();
        }catch (IOException ex) {
			System.out.println("Socket closing error");
        }  

    }
  } //end main
     /**
   checks if recieved message belongs to The Protocol (server side) and if correct comment has correct params
   */
    private boolean toClientProtocolCheck(String input) throws IOException{
        switch (input.substring(0, 4)){
              case "STRT":
                  comUtils.write_PtMsg("ERRO 402");
                  return false;
              case "BLNC":
                  
                  if(input.length()==8){
                    if(input.charAt(4)==' '){
                        try{
                          Integer.valueOf(input.substring(5));
                          return true;

                          }
                        catch(NumberFormatException ex){
                            comUtils.write_PtMsg("ERRO 401");
                            return false;
                        }
                    }
                  }
                  
                  return false;
              case "SHOW":
                  if(input.length()==6){
                      if(input.charAt(5)=='J'|| input.charAt(5)=='Q' || input.charAt(5)=='K'){
                          return true;
                      }
                  }
                  comUtils.write_PtMsg("ERRO 401");
                  return false;
              case "WINN":
                  if(input.length()==6){
                      if(input.charAt(5)=='0'|| input.charAt(5)=='1'){
                          return true;
                      }
                  }
                  comUtils.write_PtMsg("ERRO 401");
                  return false;
              case "DEAL":
                  if(input.length()==6){
                      if(input.charAt(5)=='J'|| input.charAt(5)=='Q' || input.charAt(5)=='K'){
                          return true;
                      }
                  }
                  comUtils.write_PtMsg("ERRO 401");
                  return false;
              case "EXIT":
                  comUtils.write_PtMsg("ERRO 402");
                  return false;   
              case "BETT":
                  return true;
              case "CHCK":
                  return true;
              case "CALL":
                  return true;
              case "FOLD":
                  return true;
              case "TURN":
                  if(input.length()==6){
                      if(input.charAt(5)=='0'|| input.charAt(5)=='1'){
                          return true;
                      }
                  }
                  comUtils.write_PtMsg("ERRO 401");
                  return false;
              case "RPLY":
                  comUtils.write_PtMsg("ERRO 402");
                  return false;
              case "ERRO":
                  if(input.length()==8){
                    if(input.charAt(4)==' '){
                        try{
                          Integer.valueOf(input.substring(5));
                          return true;

                          }
                        catch(NumberFormatException ex){
                            comUtils.write_PtMsg("ERRO 401");
                            return false;
                        }
                    }
                  }
                  comUtils.write_PtMsg("ERRO 401");
                  return false;
              default:
                  comUtils.write_PtMsg("ERRO 401");//we recieved something strange
                  return false;

          }

    }
      
  }
