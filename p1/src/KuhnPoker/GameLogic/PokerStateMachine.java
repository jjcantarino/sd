/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KuhnPoker.GameLogic;

import java.util.Random;

/**
 *
 * @author Alexander Bevzenko
 */
public class PokerStateMachine {
    public static enum Action {
        BETT, CHCK, CALL, FOLD
    }
    static enum State {
        S(0), SCh(0), SB(0), SChCh(1),SChB(0),SBF(2),SBCa(1),SChBF(3),SChBCa(1),ERR(0);
        final int stateStatus; // 0 - normal node, 1 - showdown, 2 - player2 folds, 3 - player1 folds

        State( int status ) {
        this.stateStatus = status;
    }
    }
    
    private State currentState;
    private boolean randomAI;// random AI purposes
    //                                    S,                        SCh,                        SB,                    SChCh,SChB,                     SBF,SBCa,SChBF,SChBCa,ERR
    private Action[][] possibleActions= {{Action.STRT,Action.BETT},{Action.CHCK,Action.BETT},{Action.FOLD,Action.CALL},{},  {Action.FOLD,Action.CALL},{},{},{},{},{}};
    private float[][][] ActionChances;//for AI purposes
    Random rng;//for AI purposes
    
    //                                  S,                                      SCh,                                        SB,                                     SChCh,                                      SChB,                                          SBF,                                     SBCa,                                       SChBF,                                  SChBCa,                                     ERR
    private State[][] transitions ={{State.SB,State.SCh,State.ERR,State.ERR},{State.SChB,State.SChCh,State.ERR,State.ERR},{State.ERR,State.ERR,State.SBCa,State.SBF},{State.ERR,State.ERR,State.ERR,State.ERR},{State.ERR,State.ERR,State.SChBCa,State.SChBF},{State.ERR,State.ERR,State.ERR,State.ERR},{State.ERR,State.ERR,State.ERR,State.ERR},{State.ERR,State.ERR,State.ERR,State.ERR},{State.ERR,State.ERR,State.ERR,State.ERR},{State.ERR,State.ERR,State.ERR,State.ERR}};
    public PokerStateMachine(){
        currentState= State.S;
        randomAI=false;
        
        //AI 
        rng = new Random();
        float a = rng.nextFloat() / 3.0f;  //we choose alpha to be random value between 0 and 1/3
        //We will calculate transition chances, results will be accessed as float[state][card][action float]
        //                                  S                                   SCh                              SB                               SChCh      SchB                               SBF         SBCa      SChBF      SChBCa     ERR
        ActionChances = new float[][][]{{{1.0f-a,a},{1.0f,0f},{1.0f-3*a,3*a}},{{2f/3f,1f/3f},{1.0f,0f},{0f,1f}}, {{1f,0f},{2f/3f,1f/3f},{0f,1f}},{{},{},{}},{{1f,0f},{2f/3f-a,a+1f/3f},{0f,1f}},{{},{},{}},{{},{},{}},{{},{},{}},{{},{},{}},{{},{},{}}   };

        
        
    }
    public void nextState(int input){
        Action[] tmp = possibleActions[currentState.ordinal()];
        if(tmp.length!=0){
            currentState = transitions[currentState.ordinal()][input];
        }else{
            currentState=State.ERR;
        }
        if(currentState==State.ERR){
            //THROW NEW EXCEPTION
        }
    }
    
    public String[] getActions(){
        Action[] res =possibleActions[currentState.ordinal()];
        String [] out= new String [2];
        if (res.length!=0){
            
            out[0]=res[0].name();
            out[1]=res[1].name();
        }
        return out;
    }
    
    public String getCurrentState(){
        
        return  currentState.toString();
    }
    /**
     indicates if we can take any actions
     */
    public boolean isPlayable(){
        return currentState.stateStatus==0;
    }
    
    /**
     indicates if end state is showdown
     */
    public boolean isShowdown(){
        return currentState.stateStatus==1;
    }
    /**
     indicates if p1 has folded
     */
    public boolean isFoldP1(){
        return currentState.stateStatus==3;
    }
    
    /**
     indicates if p2 has folded
     */
    public boolean isFoldP2(){
        return currentState.stateStatus==2;
    }
    
    public void resetState(){
        currentState= State.S;
    }
    
    /**
     * 
     * @param C card name, J, Q or K
     * @return this method returns next action in game's protocol format
     */
    public String getNextAction(char C){
        if(isError()){
            return "ERRO 402";
        }
        float chance = rng.nextFloat();
        int intC;
        switch (C){
            case 'J':
                intC=0;
                break;
            case 'Q':
                intC=1;
                break;
            case 'K':
                intC=2;
                break;
            default:
                intC=-1;//testing
                
        }
        if(!randomAI){
            if (chance<=ActionChances[currentState.ordinal()][intC][0]){
                return possibleActions[currentState.ordinal()][0].name();
                }
            else{
                return possibleActions[currentState.ordinal()][1].name();
            }
        }else{
            if (chance<=0.5f){
                return possibleActions[currentState.ordinal()][0].name();
                }
            else{
                return possibleActions[currentState.ordinal()][1].name();
            }
        }
    }
    public boolean isError(){
        return currentState==State.ERR;
    }
    /**
     turns ai into the moron if true*/
    public void setRandomAI(boolean val){
        randomAI=val;
    }
}
