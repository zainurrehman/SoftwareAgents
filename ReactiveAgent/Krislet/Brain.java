//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

//    Modified by:      Zain Ur-Rehman
//    Date:             October 4, 2021
//    Reason:			Implemented reactive krislet soccer playing agent.

import java.lang.Math;
import java.util.regex.*;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.*;


class Brain extends Thread implements SensorInput
{
    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to krislet
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
	//m_team = team;
	m_side = side;
	// m_number = number;
	m_playMode = playMode;
	start();
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
    //	3. If we dont know where is opponent goal then turn wait 
    //				and wait for new info
    //
    //	4. Kick ball
    //
    //	To ensure that we don't send commands to often after each cycle
    //	we waits one simulator steps. (This of course should be done better)

    // ***************  Improvements ******************
    // Allways know where the goal is.
    // Move to a place on my side on a kick_off
    // ************************************************

    public void run()
    {
	ObjectInfo object;
	// New objects
	ObjectInfo goal;
	ObjectInfo ball;

	// first put it somewhere on my side
	if(Pattern.matches("^before_kick_off.*",m_playMode))
	    m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );

	while( !m_timeOver )
	    {	
		//my code---------------
		// Parse the text file
        ArrayList<String> listOfLines = new ArrayList<>();
        try {
            BufferedReader bufReader = new BufferedReader(new FileReader("textfile.txt"));
            String line = bufReader.readLine();
            while (line != null) {
                listOfLines.add(line);
                line = bufReader.readLine();
            }
            bufReader.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
		//Initialize the EnvironmentStates variable to store the environment states
        EnvironmentStates TempEnvironmentStates=null;
		
        // Create a HashMap once the text file is parsed. Keys of the hash map are the environment states and values are the corrosponding actions.
		HashMap<String, String> EnvStateToActionMapping = new HashMap<String, String>();
        for(int i = 0; i < listOfLines.size(); i++)
        {
            String temp = listOfLines.get(i);
            temp = temp.trim();
            if(temp.isEmpty() || temp.startsWith("//"))
                continue;
            String[] parts = temp.split("->");
            List<String> list = Arrays.asList(parts);
            //Left side of the individual line is environment state. Right side is action.
            String[] environmentStates = list.get(0).replaceAll(" ", "").split(",");
            TempEnvironmentStates = new EnvironmentStates(environmentStates[0].trim(), environmentStates[1].trim(), environmentStates[2].trim());
            EnvStateToActionMapping.put(TempEnvironmentStates.toString(), list.get(1).trim().replaceAll(" ", ""));
        }
		
		ball = m_memory.getObject("ball");
		
		//Initialize the variables. These will be used to provide values in the EnvironmentState constructor.
		String BallSeenByPlayer; //Yes,No,Front (Y,N,F)
		String BallCloseToPlayer; //Y,N,U				 
		String GoalVisibility; //Yes,No,Front (Y,N,F)  
		
		// This logic is similar to the logic in original Brain.java
		if( ball==null ) //ball not present
		{
			BallSeenByPlayer="N";
			BallCloseToPlayer="U";
		}
		else //ball is not null..it is present
		{
			if (ball.m_distance>1.0) // if ball is too far but visible
			{
				BallCloseToPlayer="N";
			}
			else
			{
				BallCloseToPlayer="Y";
			}
			if (ball.m_direction != 0) //If ball is too far but visible and in the same direction as player
			{
				BallSeenByPlayer="Y";
			}
			else
			{
				BallSeenByPlayer="F";
			}
		}
		if( m_side == 'l' )
		{
			goal = m_memory.getObject("goal r");
		}
		else
		{
			goal = m_memory.getObject("goal l");	
		}
		
		if( goal == null )
		{
			GoalVisibility="N";
		}
		else if (goal.m_direction != 0)
		{
			GoalVisibility="Y";
		}
		else 
		{
			GoalVisibility="F";
		}
		//After getting the environment states in the current cycle, populate the TempEnvironmentStates variable.
		TempEnvironmentStates.BallSeenByPlayer=BallSeenByPlayer;
        TempEnvironmentStates.BallCloseToPlayer=BallCloseToPlayer;
        TempEnvironmentStates.GoalVisibility=GoalVisibility;
		
		// Use hash map variable EnvStateToActionMapping to get the value (action string) against the keys (TempEnvironmentStates)
        String ActionInCurrentIteration = EnvStateToActionMapping.get(TempEnvironmentStates.toString());
        System.out.println("BallSeenByPlayer="+TempEnvironmentStates.BallSeenByPlayer+", BallCloseToPlayer="+TempEnvironmentStates.BallCloseToPlayer+", GoalVisibility="+TempEnvironmentStates.GoalVisibility+" -> "+ActionInCurrentIteration);
        String[] ActionInCurrentIterationSplit = ActionInCurrentIteration.split(",");
        String Action = ActionInCurrentIterationSplit[0].toLowerCase().trim();
        String Power = ActionInCurrentIterationSplit[1].toLowerCase().trim();
        String Direction = ActionInCurrentIterationSplit[2].toLowerCase().trim();
		
		// Perform the actions accordingly based on the values of the hash map. Action Power Direction variables declared above
        if (Action.equals("turn"))
        {
            // Two cases for turn.
            // 1. Turn in the direction of:
            // 1a. ball
            // 1b. goal
            // In both above cases, Power=x in the text file. Example is Turn,x,ball or Turn,x,goal
            if(Direction.equals("ball"))
            {
                m_krislet.turn(ball.m_direction);
                System.out.println("Turn in the direction of the ball.");
            }
            else if(Direction.equals("goal"))
            {
                m_krislet.turn(goal.m_direction);
                System.out.println("Turn in the direction of the goal.");
            }
            // 2. Turn with angle int. In this case, Direction=x in the text file. Example is turn,40,x
            else if(Direction.equals("x") && Power.matches("-?\\d+"))
            {
                if (goal==null)
				{
					m_krislet.turn(Integer.parseInt(Power));
					System.out.println("Turn "+ Power +" to look for goal.");
				}
				else // this will be the case when ball=null.
				{
					m_krislet.turn(Integer.parseInt(Power));
					System.out.println("Turn "+ Power +" to look for ball.");
				}				
            }
            else
            {
                System.out.println("Please correct the text file with correct actions and variables.");
            }
            // At the end, wait for new info.
            m_memory.waitForNewInfo();
        }
        else if (Action.equals("kick"))
        {
            //For kick, kick in the direction of ball or goal
            if(Direction.equals("goal") && Power.matches("-?\\d+"))
            {
                m_krislet.kick(Integer.parseInt(Power), goal.m_direction);
				System.out.println("Kick the ball with power "+ Power +" in the direction of the goal");
            }
            //else if (Direction.equals("ball") && Power.matches("-?\\d+"))
            //{
            //    //System.out.println("m_krislet.kick(Integer.parseInt(Power), ball.m_direction)");
            //    m_krislet.kick(Integer.parseInt(Power), ball.m_direction);
            //}
            else
            {
                System.out.println("Please correct the text file with correct actions and variables.");
            }
        }
        else if (Action.equals("dash"))
        {
            m_krislet.dash(Integer.parseInt(Power));
			System.out.println("Run/Dash towards the ball with power "+ Power);			
        }
        else
        {
            System.out.println("Please correct the text file with correct actions and variables.");
        }
		
		//my code -------------

		// sleep one step to ensure that we will not send
		// two commands in one cycle.
		try{
		    Thread.sleep(2*SoccerParams.simulator_step);
		}catch(Exception e){}
	    }
	m_krislet.bye();
    }


    //===========================================================================
    // Here are suporting functions for implement logic


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
    private SendCommand	                m_krislet;			// robot which is controled by this brain
    private Memory			m_memory;				// place where all information is stored
    private char			m_side;
    volatile private boolean		m_timeOver;
    private String                      m_playMode;
    
}
