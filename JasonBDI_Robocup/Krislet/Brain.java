//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.lang.Math;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;

import jason.asSyntax.*;
import jason.asSemantics.Agent;
import jason.architecture.AgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.TransitionSystem;
import jason.infra.centralised.BaseCentralisedMAS;


class Brain extends AgArch implements SensorInput, Runnable
{
	private Literal percept;
	private ObjectInfo ball;
	private ObjectInfo goal;
	private Thread myThread = null;
	private List<PlayerCondition> playerConditions;
	PitchEnviroment env = new PitchEnviroment();
	
	//---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to Krislet
    // - starts thread for this object
    public Brain(SendCommand krislet, 
		 String team, 
		 char side, 
		 int number, 
		 String playMode)
    {
	m_timeOver = false;
	m_krislet = krislet;
	m_memory = new Memory();
	m_side = side;
	m_playMode = playMode;
	
	try {
		// Setting up the Jason agent
		Agent ag = new Agent();         
		
		// Setting up BDI engine
		new TransitionSystem(ag, null, null, this);
		
		// Initiating agent with Jason code
		ag.initAg("soccerAgent.asl");
		
		// Setting up the pitch enviroment
		PitchEnviroment env = new PitchEnviroment();
		
    } catch (Exception e) {
    	System.out.println("Error in iniating Jason agent/ pitch enviroment");
    }
		
	// Creating thread for our Jason agent
	myThread = new Thread(this, "JasonAgent");
	
	// Starting the thread
	myThread.start();
    }


    //---------------------------------------------------------------------------
    // This is main brain function used to make decision
    // In each cycle we decide which command to issue based on
    // current situation. the rules are:
    //
    //	1. If you don't know where is ball then turn right and wait for new info
    //
    //	2. If ball is too far to kick it then
    //		2.1. If we are directed towards the ball then go to the ball
    //		2.2. else turn to the ball
    //
    //	3. If we don't know where is opponent goal then turn wait 
    //				and wait for new info
    //
    //	4. Kick ball
    //
    //	To ensure that we don't send commands to often after each cycle
    //	we waits one simulator steps. (This of course should be done better)

    // ***************  Improvements ******************
    // Always know where the goal is.
    // Move to a place on my side on a kick_off
    // ************************************************

    public void run()
    {

	// First put the agent somewhere random on it's side
	if(Pattern.matches("^before_kick_off.*",m_playMode))
	    m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );

	// Start of the agent brain
	while( !m_timeOver )
	    {
		// Perform sense, deliberate, act (from /asSemantics/TransitionSystem && /architecture/AgArch) 
		System.out.println("================= START OF CYCLE =================");
		getTS().reasoningCycle();
		
		// Sleep in order to not send extra commands to server
		if (getTS().canSleep()){
			sleep();
		}
		System.out.println("================= END OF CYCLE =================");
		System.out.println("\n\n");
	    }
	m_krislet.bye();
    }
    
    // Override of the act function in order to perform the actions in Krislit
    public String getAgName() {
        return "JasonAgent";
    }
    
    // Override of the perceive function in order to perceive our required information
    @Override
    public List<Literal> perceive() {
    	// List that will be used to store all our cycle perceives
        List<Literal> p = new ArrayList<Literal>();
        
        // Check the environment and get all perceives 
        List<PlayerCondition>  playerconditions = env.CheckConditions(m_memory, m_side);
        
        System.out.println("\n");
        System.out.println("Environment perceives (conditions): ");
        System.out.println(playerconditions);
        System.out.println("\n");
        
        // Return the perceives in the correct manner required for jason
        for(PlayerCondition myCond: playerconditions) {
        	Literal percept = ASSyntax.createLiteral(myCond.name());
        	p.add(percept);
        }
        return p;
    }
    
    // Override of the act function in order to perform the actions in Krislit
    @Override
    public void act(ActionExec action) 
    {	
    	System.out.println("Action item to be performed at the end of cycle:");
    	System.out.println(action.getActionTerm());
    	System.out.println("\n");
    	
    	ball = m_memory.getObject("ball");
    	goal = m_side == 'l' ? m_memory.getObject("goal r") : m_memory.getObject("goal l");
    	
    	
    	// Case 1: Turn
        if( action.getActionTerm().equals(Literal.parseLiteral("turn")) ) 
        {
			m_krislet.turn(40);    
			m_memory.waitForNewInfo();
        }
        // Case 2: Turn towards ball
        else if( action.getActionTerm().equals(Literal.parseLiteral("turn_to_ball")) ) 
        {
        	m_krislet.turn(ball.m_direction);   
			m_memory.waitForNewInfo();
        }
        // Case 3: Dash
        else if( action.getActionTerm().equals(Literal.parseLiteral("dash_to_ball")) )
        {
        	if(ball != null) {
        		m_krislet.dash(5 * ball.m_distance);
        	}
        	else {
        		m_krislet.turn(40);
        	}
			
        }
        // Case 4: Kick
        else if( action.getActionTerm().equals(Literal.parseLiteral("kick")) )
        {
			m_krislet.kick(100, goal.m_direction);
        }
        
        // Perform the action
        action.setResult(true);
        actionExecuted(action);
    }
    
    // Override to always wait between cycles
    @Override
    public boolean canSleep() {
        return true;
    }
    
    // Override as in tutorial but not used
    @Override
    public boolean isRunning() {
        return true;
    }
    
    // Override to wait in the same manner as original Krislet demo
    public void sleep() {
        try {
        	Thread.sleep(2*SoccerParams.simulator_step);
        } catch (InterruptedException e) {}
    }

    // Override as in tutorial but not used
    @Override
    public void sendMsg(jason.asSemantics.Message m) throws Exception {
    }

    // Override as in tutorial but not used
    @Override
    public void broadcast(jason.asSemantics.Message m) throws Exception {
    }

    // Override as in tutorial but not used
    @Override
    public void checkMail() {
    }

    //===========================================================================
    // Here are supporting functions for implement logic


    //===========================================================================
    // Implementation of SensorInput Interface

    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info)
    {
	m_memory.store(info);
    }


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message)
    {
    }

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message)
    {						 
	if(message.compareTo("time_over") == 0)
	    m_timeOver = true;

    }


    //===========================================================================
    // Private members
    private SendCommand	                m_krislet;			// robot which is controlled by this brain
    private Memory			m_memory;				// place where all information is stored
    private char			m_side;
    volatile private boolean		m_timeOver;
    private String                      m_playMode;
    
}
