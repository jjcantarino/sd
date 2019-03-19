/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KuhnPoker;

import java.io.File;
import java.io.IOException;
import KuhnPoker.GameLogic.KuhnPokerGame;
import KuhnPoker.Server.Server;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author mat.aules
 */
public class Tests extends Thread {

    public void run() {
        System.out.println("Hello from a thread!");
    }

    public static void main(String args[]) {
        Server server = new Server();
    ServerSocket serverSocket = null;
    //KuhnPokerGame kpg = new KuhnPokerGame();
    //Interpreter interpreter = new Interpreter(kpg);
    Socket socket = null;
    //ComUtils comUtils;
    int portServidor = 1111;
    if (args.length > 1){
      System.out.println("Usage: Server -p [<numPort>] -i [0|1] where 1 is random actions, 0 is better AI");
      System.exit(1);
    }
    if (args.length == 1)
      portServidor = Integer.parseInt(args[0]);
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
                    Scanner reader = new Scanner(System.in);
                    String io;
                    ComUtils comUtils = new ComUtils(socket);
                    while (true){
                        io=reader.nextLine();
                        if(io.equals("")){
                            io= comUtils.read_PtMsg();
                            System.out.println("C: "+io);
                        }else{
                            comUtils.write_PtMsg(io.toUpperCase());
                        }
                    }
                }
            } 
        }catch (IOException ex) {
            System.out.println("Server socket error "+ex.toString());
    }
    }

}
/*
File file = new File("test.txt");
          try {
              file.createNewFile();
              ComUtils cmUtils = new ComUtils(file);
              cmUtils.writeTest();
              System.out.println(cmUtils.readTest());
            }
            catch(IOException e)
            {
                System.out.println("Error Found during Operation:" + e.getMessage());
                e.printStackTrace();
            }

*/