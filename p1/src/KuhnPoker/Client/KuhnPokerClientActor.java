/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KuhnPoker.Client;

import KuhnPoker.KPGClient;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Bevz
 */
public class KuhnPokerClientActor {
    private KPGClient gameWrapper;//access to game logic
    private boolean isAI; //indicates if this actor is AI controlled
    private int numberOfPlays;// number of games to play by AI actor
    private String[] actions;
    Scanner reader;
    
    public KuhnPokerClientActor(KPGClient exGame, boolean Ai){
        this.gameWrapper=exGame;
        this.isAI=Ai;
        this.numberOfPlays=100;
        reader = new Scanner(System.in);
    }
    
    public String nextComment(String input){
     if(isAI){
        if(canPlayAgain()){
            if(!gameWrapper.isReadyToPlay()&&!gameWrapper.isWon()){
                return gameWrapper.outAction();
            }
            else if(!gameWrapper.isWon()){
                return gameWrapper.outAction();
            }
            else{
                numberOfPlays--;
                if(numberOfPlays<1){
                    System.out.println("FINISHED, money: "+ String.valueOf(gameWrapper.getMoney()));
                    return "EXIT";
                }
                System.out.println("[" +String.valueOf(numberOfPlays)+"] plays left, money: "+ String.valueOf(gameWrapper.getMoney()));
                return "RPLY";

            }
        }
        else {
            System.out.println("FINISHED, money: "+ String.valueOf(gameWrapper.getMoney()));
            return "EXIT";
        }
     }else{
         if(!gameWrapper.isReadyToPlay()&&!gameWrapper.isWon()){
            return gameWrapper.outAction();
         }
         
         
         
         return statusAndMenu(input);
     }
    }
    
    private String statusAndMenu(String input){
        if(input.substring(0,3).equals("WIN")){
            if(input.charAt(5)=='0'){
                System.out.print("You have WON! ");
            }else{
                System.out.print("You have LOST! ");
            }
            System.out.println("Your money: "+String.valueOf(gameWrapper.getMoney()));
        }
        
        actions = gameWrapper.getActions();
        if(gameWrapper.getMoney()<1){
            System.out.println("Not ehough money");
            return "EXIT";
        }
        else if (actions[1]==null){
            return actions[0];
        }
        if(gameWrapper.isReadyToPlay()){
            System.out.println("Card: "+gameWrapper.getCard()+", Pot: "+String.valueOf(gameWrapper.getPot())+", Money: "+String.valueOf(gameWrapper.getMoney()));
        }
        System.out.println("Actions:");
        for(int i=0;i<actions.length;i++ ){
            if(actions[i]!=null){
                System.out.println(String.valueOf(i+1)+". "+actions[i]);
            }
            
        }
        int tmpInt=-1;
        String inp;
        while(true){
            inp = reader.nextLine().toUpperCase();
            try{
                tmpInt=Integer.valueOf(inp)-1;
                if(tmpInt==0||tmpInt==1){
                    return  actions[tmpInt];
                }
                System.out.println("Please enter option number or type command directly");
            }
            catch(NumberFormatException ex){
                break;
            }
        }
        if (commandCheck(inp)){
            return inp;
        }
        return "EXIT";
        /*try{
            
        }catch()*/
    }
    
    private boolean canPlayAgain(){
        if (numberOfPlays>0 &&gameWrapper.getMoney()>-1){
            return true;
        }
        return false;
    }
    /**this method checks if user input is one of the allowed commands
     * 
     
     */
    private boolean commandCheck(String inp){
        switch (inp){
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
            case "RPLY":
                return true;
            default:
                return false;
        }
    
    }
}
