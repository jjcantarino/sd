/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KuhnPoker.GameLogic;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Alexander Bevzenko
 */
public class KuhnPokerGame {
    
    static enum Card {
        J, Q, K
    }
    static enum Turn {
        FIRST,SECOND
    }
    private PokerStateMachine gameState;
    private Card myCard;
    private Turn turnOrder;
    
    private int potMoney;
    private int myMoney;
    private int turn;
    private boolean preGameReady;//indicates if we have ehough data to start a game (card, money, turn order)
    
    /**
     constructs a KuhnPokerGame instance, please note that params are missing in order to start
     */
    public KuhnPokerGame(){

        gameState = new PokerStateMachine();
        turn = 0;
        preGameReady=false;
        myMoney=0;
    }
    /**
     gives this player a card
     */
    public void setMyCard(char card){
        switch (card) {
            case 'J':
            case 'j':
                myCard= Card.J;
                break;
            case 'Q':
            case 'q':
                myCard= Card.Q;
                break;
            case 'K':
            case 'k':
                myCard= Card.K;
                break;
            default:
                break;
        }
        preGameReadyUpdate();
    }
    public char getMyCard(){
        if(myCard==null){
            return ' ';
        }
        return myCard.name().charAt(0);
    }
    /**
     gives the player information who acts first
     */
    public void myTurnFirst(boolean myTurnFirst){
        if(myTurnFirst){
            turnOrder=Turn.FIRST;
        }else{
            turnOrder=Turn.SECOND;
        }
        preGameReadyUpdate();
    }
    public void setMyMoney(int money){
        myMoney=money;
        //preGameReadyUpdate();
    }
    
    public int getMyMoney(){
        return myMoney;
    }
    public int getTurn(){
        return turn;
    }
    /**
    this method updates internal state machine with 1 of 4 possible actions during poker game
    * right now actions a given as integer
    */
    public void inputAction(int i){
        if((i>0||i<3)&&preGameReady){
            gameState.nextState(i);
            turn++;
        }
    }
    /**
     returns next action
     */
    public String getNextAction(){
        if (gameState.isPlayable()){
            return gameState.getNextAction(myCard.name().charAt(0));
        }else if (gameState.isShowdown()){
            return "SHOW "+myCard.name();
        }else if (gameState.isFoldP1()){
            if(turnOrder==Turn.FIRST){
                return "WINN 0";
            }else{
                return "WINN 1";
            }
        }else if (gameState.isFoldP2()){
            if(turnOrder==Turn.SECOND){
                return "WINN 0";
            }else{
                return "WINN 1";
            }
        }
        return "ERRO 502";//unexpected action
    }
    public void PrintGameState(){
        if(preGameReady){
            System.out.println("Sate: "+gameState.getCurrentState());
            if(gameState.isPlayable()){
                String[] ga = gameState.getActions();
                System.out.println("0) BET, 1)CHECK, 2)CALL, 3) FOLD");
                System.out.println("actions: "+ga[0]+" "+ga[1]);
                if(isMyTurn()){
                    System.out.println("Your turn!");
                }
                System.out.println("AI action would be: "+gameState.getNextAction(myCard.name().charAt(0)));
            }else{
                System.out.println("FINAL STATE");
            }
        }else{
            System.out.println("waiting for data");
        }
    }
    /**
     returns true if it is player's turn
     */
    public boolean isMyTurn(){
        if(preGameReady){
            return (turn%2)==turnOrder.ordinal();
        }
        else if(gameState.isError()){
            return (turn%2)==turnOrder.ordinal();
        }
        return false;
    }
    /**
     this method checks if we have all data for the game to start (card, money, turn order)
     */
    private void preGameReadyUpdate(){
        if(myCard!=null && turnOrder!=null){
            preGameReady=true;
        }else{
            preGameReady=false;
        }
    }
    
    
   
    public void actionBLNC(int sum){
        myMoney=sum;
    }
    public void actionRPLY(){
        gameState.resetState();
        turn = 0;
        preGameReady=false;
        potMoney=0;
        
    }
    
    public void actionDEAL(char card){
        setMyCard(card);
    }
    
    public void actionTURN(int ord){
        if(ord==0){
            myTurnFirst(false);
        }
        else{
            myTurnFirst(true);
                }
        
    }
    //SERVER ONLY
    public char dealCards(){
        Random rng = new Random();
        ArrayList<Character> cartas = new ArrayList<Character>();
        cartas.add('J');
        cartas.add('Q');
        cartas.add('K');
        int choice = rng.nextInt(cartas.size());
        this.setMyCard(cartas.get(choice));
        cartas.remove(choice);
        choice = rng.nextInt(cartas.size());
        preGameReadyUpdate();
        return cartas.get(choice);
    }
    public char dealTurn(){
        Random rng = new Random();
        int choice = rng.nextInt(2);
        this.myTurnFirst(choice==1);
        preGameReadyUpdate();
        if (choice==1) return '1';
        return '0';
    }
    public void takeAction(String action){
        if (action.length()!=4){
            //error
        }
        switch (action){
            case "BETT":
                gameState.nextState(0);
                
                break;
            case "CHCK":
                gameState.nextState(1);
                break;
            case "CALL":
                gameState.nextState(2);
                break;
            case "FOLD":
                gameState.nextState(3);
                break;
        }
        turn++;
        
    }
    public boolean isReady(){
        return preGameReady;
    }
    public boolean isFinished(){
        return !gameState.isPlayable()&&!gameState.isShowdown();
    }
    public void reset(){
        gameState.resetState();
        myCard=null;
        turnOrder=null;
        preGameReady=false;
        turn=0;
        
    }
    
    public int getPot(){
        return this.potMoney;
    }
    public String[] getActions(){
        String[] actions = gameState.getActions();
        if (actions[0]==null&& gameState.isShowdown()){
            actions[0]="SHOW "+myCard.name();
            
        }
        else if(actions[0]==null){
            actions[0]="ERRO 402";
        }
        return actions;
    }
    /**increments pot money counter. to be used with bett/call action
     */
    public void potIncrement(){
        this.potMoney++;
    }
    /**decrements total money counter. to be used with bett/call action
     */
    public void moneyDecrement(){
        this.myMoney--;
    }
    
    public String moneyToString(){
        if (myMoney<1){
            return "000";
        }
        else if (myMoney<10){
            return "00"+String.valueOf(myMoney);
        }
        else if (myMoney<100){
            return "0"+String.valueOf(myMoney);
        }
        else if (myMoney<1000){
            return String.valueOf(myMoney);
        }
        else{
            return "999";
        }
    }
    public void addPot(int multiplier){
        this.myMoney+=this.potMoney*multiplier;
        this.potMoney=0;
    }
    public boolean isErro(){
        return gameState.isError();
    }
    
    /**
     this method sets ai to take random actions if possible*/
    public void setAIRandom(){
        gameState.setRandomAI(true);
    }
}
