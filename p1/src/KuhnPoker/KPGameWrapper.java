/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KuhnPoker;

import KuhnPoker.GameLogic.KuhnPokerGame;
import KuhnPoker.GameLogic.PokerStateMachine;


public class KPGameWrapper {
    //Servidor y Cliente tienen un interpreter, el cual recibe de la comunicacion
    //y lo traduce a comanda 
    protected String input;
    protected KuhnPokerGame game;
    protected String winner;
    protected int showCounter;
    
    static enum State {
        INIT(1), ST(0), BL(1), BET1(0), BET2(0), DL(0), TN(2), GM(2), WN(1), RP(1), EX(1) , ERROR(-1);
        final int stateStatus; // 0 - server's action, 1 client's action 2 - unique game state - server or client action

        State( int status ) {
        this.stateStatus = status;
        }
    }
    static enum Action {
        STRT, BETT, CHCK, FOLD, CALL, RPLY, BLNC, DEAL, TURN, SHOW, WINN, EXIT, ERRO
    }
    protected State currentState;
    private Action[][] possibleActions= {
        {Action.STRT},//INIT
        {Action.BLNC},//STRT
        {Action.BETT},//BLNC
        {Action.BETT},//BET1
        {Action.DEAL},//BET2
        {Action.TURN},//DL
        {Action.BETT,Action.CHCK},//TN
        {Action.BETT,Action.CHCK, Action.FOLD, Action.CALL},//GM
        {Action.RPLY, Action.EXIT},//WN
        {Action.BLNC},//RP
        {},//EX
        {}//ERROR
    };
    private float[][][] ActionChances;//for AI purposes   
    private State[][] transitions;

    private void initTransitions(){
        transitions = new State[12][13];
        for(int i = 0; i < 12; i++)
            for(int j = 0; j < 13 ; j++ )
                transitions[i][j] = State.ERROR;
        transitions[State.INIT.ordinal()][Action.STRT.ordinal()] = State.ST;
        transitions[State.ST.ordinal()][Action.BLNC.ordinal()] = State.BL;
        transitions[State.BL.ordinal()][Action.BETT.ordinal()] = State.BET1;
        transitions[State.BET1.ordinal()][Action.BETT.ordinal()] = State.BET2;
        transitions[State.BET2.ordinal()][Action.DEAL.ordinal()] = State.DL;
        transitions[State.DL.ordinal()][Action.TURN.ordinal()] = State.TN;
        transitions[State.TN.ordinal()][Action.CHCK.ordinal()] = State.GM;
        transitions[State.TN.ordinal()][Action.BETT.ordinal()] = State.GM;
        transitions[State.GM.ordinal()][Action.CHCK.ordinal()] = State.GM;
        transitions[State.GM.ordinal()][Action.BETT.ordinal()] = State.GM;
        transitions[State.GM.ordinal()][Action.FOLD.ordinal()] = State.GM;
        transitions[State.GM.ordinal()][Action.CALL.ordinal()] = State.GM;
        transitions[State.GM.ordinal()][Action.SHOW.ordinal()] = State.GM;
        transitions[State.GM.ordinal()][Action.WINN.ordinal()] = State.WN;
        transitions[State.WN.ordinal()][Action.WINN.ordinal()] = State.WN;
        transitions[State.WN.ordinal()][Action.SHOW.ordinal()] = State.WN;
        transitions[State.WN.ordinal()][Action.RPLY.ordinal()] = State.ST;
        transitions[State.WN.ordinal()][Action.EXIT.ordinal()] = State.EX;
    }
    public KPGameWrapper(){
        this.currentState = State.INIT;
        this.game = new KuhnPokerGame();
        winner="";
        initTransitions();
        showCounter=0;
    }
    public KPGameWrapper(KuhnPokerGame g){
        this.game = g;
        this.input = "";
        winner="";
        this.currentState = State.INIT;
        initTransitions();
        showCounter=0;
    }
    protected void nextState(Action input){
        Action[] tmp = possibleActions[currentState.ordinal()];
        if(tmp.length!=0){
            currentState = transitions[currentState.ordinal()][input.ordinal()];
        }else{
            currentState = State.ERROR;
        }
      //  if(currentState==PokerStateMachine.State.ERR){
            //THROW NEW EXCEPTION
        //}
    }
        //INIT, ST, BL, BET1, BET2, DL, TN, GM, WN, RP, EX , ERROR
    public String outAction(){
        String nextMessage = "ERRO 404";
        int wch;//tmp int
        switch(this.currentState.name()){
            case "INIT":
                //ERR
                break;
            case "ST":
                //this.nextState(Action.BLNC);
                return "BLNC "+game.moneyToString();//TO DO: falta Implementar
            case "BL":
                //this.nextState(Action.BETT);
                game.moneyDecrement();//server
                return "BETT";
            case "BET1":
                //this.nextState(Action.BETT);
                game.moneyDecrement();//user
                return "BETT";
            case "BET2":
                //this.nextState(Action.DEAL);
                
                return "DEAL " + game.dealCards();
            case "DL":
                //this.nextState(Action.TURN);
                return "TURN "+ game.dealTurn(); //TO DO: Return aleatori de turn
            case "TN":
                //if(game.isMyTurn()){
                    nextMessage = game.getNextAction();

                    return nextMessage;
                //}
                //break;
            case "GM":
                //if(game.isMyTurn()){
                nextMessage = game.getNextAction();

                if (!winner.equals("")&&showCounter!=1){
                    nextMessage = "WINN "+winner;
                    winner="";
                    showCounter=0;
                }
                wch=winCheck(nextMessage);
                if(wch!=-1){
                    game.addPot(wch);
                }
                return nextMessage;
                //}
                
                //break;
            case "WN":
                nextMessage = game.getNextAction();

                if (!winner.equals("")&&showCounter!=1){
                    nextMessage = "WINN "+winner;
                    winner="";
                    showCounter=0;
                }
                wch=winCheck(nextMessage);
                if(wch!=-1){
                    game.addPot(wch);
                }
                return nextMessage;
            case "RP":
                return "BLNC "+game.moneyToString();//TO DO: falta Implementar
            case "EX":
                break;
            case "ERROR":
                return "ERRO 502"; //unexpected command
                
            default:
                break;
        }
        return nextMessage;
    }
    protected Action getActionFromString(String input){        
        Action inputAction = Action.SHOW;
        String inp="";
        if (input.length()==4){
            inp=input;
        }else if (input.length()<4){
            //error
        }else{
            inp = input.substring(0, 4);
        }
            
        switch (inp){
            case "STRT":
                inputAction = Action.STRT;
                 break;
            case "BLNC":
                inputAction = Action.BLNC;
                break;
            case "SHOW":
                inputAction = Action.SHOW;
                if(game.isReady()){
                    game.takeAction(inputAction.name());
                }
   
                winner = compareCards(input.substring(5).charAt(0));
                showCounter++;
                
                break;
            case "WINN":
                inputAction = Action.WINN;
                break;
            case "DEAL":
                inputAction = Action.DEAL;
                break;
            case "EXIT":
                inputAction = Action.EXIT;
                break;
            //INGAME:    
            case "BETT":
                inputAction = Action.BETT;
                if(game.isReady()){
                    if(!game.isMyTurn()){
                        game.moneyDecrement();
                    }
                    game.takeAction(inputAction.name());
                }
                game.potIncrement();
                break;
            case "CHCK":
                inputAction = Action.CHCK;
                if(game.isReady()){
                    game.takeAction(inputAction.name());
                }
                break;
            case "CALL":
                inputAction = Action.CALL;
                if(game.isReady()){
                    if(!game.isMyTurn()){
                        game.moneyDecrement();
                    }
                    game.takeAction(inputAction.name());
                }
                game.potIncrement();
                break;
            case "FOLD":
                inputAction = Action.FOLD;
                if(game.isReady()){
                    game.takeAction(inputAction.name());
                }
                break;
            case "TURN":
                inputAction = Action.TURN;
                break;
            case "RPLY":
                inputAction = Action.RPLY;
                if(currentState==State.RP ||currentState==State.WN){
                    resetGame();
                }
                
                break;
            default:
                inputAction=Action.ERRO;//throw ERRO '401'
                break;
        }
        return inputAction;
    }
    public void updateAction(String stringInput){
        this.input = stringInput.toUpperCase();
        nextState(this.getActionFromString(input));
    }
    /**
     this method indicates if server has another message to send to client
     * @return true if server must send another message
     */
    public boolean isReadyToSend(){
        if(showCounter==2){
            showCounter=0;
            return true;
        }
        else if(this.currentState.stateStatus==0){
            return true;
        }else if(this.currentState.stateStatus==2){
            return (game.isMyTurn() || game.isFinished() );//return (game.isMyTurn() ||game.isFinished());

        }else if (!winner.equals("")){
            return true;
        }
        
        
        return false;
    }
    public String getState(){
        return currentState.name();
    }
    
    protected String compareCards(char card){
        if (card ==game.getMyCard()){
            return winner;
        }
        else if (card =='K'){
            return "0";
        }
        else if (card =='J'){
            return "1";
        }
        else if (card =='Q'&&game.getMyCard()=='J'){
            return "0";
        }
        else if (card =='Q'&&game.getMyCard()=='K'){
            return "1";
        }
        return "";
    }
    protected void resetGame(){
        game.reset();
        winner="";
        showCounter=0;
    }
    public boolean isReadyToPlay(){
        return game.isReady();
    }
    /*
    simple method to check win output,
    * @return returns  -1 if there is no win, 1 if player wins, 0 if house wins
    */
    protected int winCheck(String str){
        if (str.length()<5){
            return -1;
        }else if(str.charAt(0)=='W'&&str.charAt(5)=='0'){
            return 1;
        }else if(str.charAt(0)=='W'&&str.charAt(5)=='1'){
            return 0;
        }
        return -1;
    }
    /**Simple method to check if we are in error state*/
    public boolean isErro(){
        return (currentState==State.ERROR|| game.isErro());
    }
}
