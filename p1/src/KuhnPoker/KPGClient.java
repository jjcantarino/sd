/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KuhnPoker;

import KuhnPoker.GameLogic.KuhnPokerGame;

/**
 *
 * @author mat.aules
 */
public class KPGClient extends KPGameWrapper {
    private String msgWin= "YOU HAVE WON";
    private String msgLoose= "YOU HAVE LOST";
    
    //Genera output a traves de la comanda + comunicacion con cliente
    public KPGClient (KuhnPokerGame kpg){
        super(kpg);
    }
@Override
    protected Action getActionFromString(String input){        
            Action inputAction = Action.SHOW;
            String inp="";
            int wch;
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
                    game.setMyMoney(balance2int(input.substring(5)));
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
                    wch=winCheck(input);
                    if(wch!=-1){
                        game.addPot(wch);
                    }
                    this.resetGame();
                    break;
                case "DEAL":
                    inputAction = Action.DEAL;
                    game.setMyCard(input.substring(5).charAt(0));
                    break;
                case "EXIT":
                    inputAction = Action.EXIT;
                    break;
                //INGAME:    
                case "BETT":
                    inputAction = Action.BETT;
                    if(game.isReady()){
                        if(game.isMyTurn()){
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
                        if(game.isMyTurn()){
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
                    game.myTurnFirst(input.substring(5).charAt(0)=='0');
                    break;
                case "RPLY":
                    inputAction = Action.RPLY;
                    super.resetGame();
                    break;
                default:
                    //throw ERRO '401'
                    break;
            }
            return inputAction;
        }
    @Override
        public void updateAction(String stringInput){
            this.input = stringInput.toUpperCase();
            nextState(this.getActionFromString(input));
        }
        
        
    @Override
        public boolean isReadyToSend(){

        if(showCounter==2){
            showCounter=0;
            return false;
        }
        else if(super.currentState.stateStatus==1){

            return true;
        }
        else if(this.currentState.stateStatus==2){
            return (game.isMyTurn() && !game.isFinished());
        }
        
        
        return false;
    }
    public boolean isWon(){
        return currentState==State.WN;//(!winner.equals("") || currentState==State.WN);
    }
    public int getPot(){
        return game.getPot();
    }
    public int getMoney(){
        return game.getMyMoney();
    }
    public String[] getActions(){
        String[] tmp =game.getActions();
        if(isWon()){
            String[] actions={"RPLY","EXIT"};
            return actions;
        }
        else if(game.getMyMoney()<1){
            tmp[1]=null;
        }
        return tmp;
    }
    
    private int balance2int(String str){
        return Integer.valueOf(str);// TO DO exception
    }
    public char getCard(){
        return game.getMyCard();
    }
}
