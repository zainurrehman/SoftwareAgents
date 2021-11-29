//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

//	  Modified by: 		Zain Ur-Rehman
//	  Date:				November 11, 2021
//	  Expert System using forward chaining method.

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Math;
import java.util.regex.*;
import java.util.ArrayList;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.io.IOException;
import java.util.*;
import java.io.*;

class Brain extends Thread implements SensorInput
{
	// Declare the array lists here. 
	// agenda is the list of facts given in the text file.
	// For this program, it is assumed that there are no facts mentioned in the text file and agent sees all facts when the game starts.
	// Therefore, agenda arraylist is not processed by the program.
	// clauses is the list of all rules
	// count is the number of predicates in each rule
	public static ArrayList<String> agenda = new ArrayList<String>();
	public static ArrayList<String> clauses = new ArrayList<String>();
	public static ArrayList<Integer> count = new ArrayList<Integer>();

	
	// Name of the text file with all rules of soccer playing agent.	
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
	//Knowledge base read from the text file comes here. Each Agent's brain need to know the Knowledgebase
	String FileName = "robocupRules.txt";
	ArrayList<String> TextFileAsArrayList = TextFileContents(FileName);
	String TextFileAsString = KnowledgeBaseFromTextFileAsString(TextFileAsArrayList);
	// Call the init function to initialize the count, clauses and agenda arraylists.
	init(TextFileAsString);
	//System.out.println(TextFileAsString);
	
	ObjectInfo object;
	
	// Objects to get information from the flags in the field.
	//ObjectInfo flagPrt;
	//ObjectInfo flagPrc;
	//ObjectInfo flagPrb;
	ObjectInfo flagGrt;
	ObjectInfo flagGrb;
	ObjectInfo flagRt;
	ObjectInfo flagRb;	
	ObjectInfo rGoal;
	//
	//ObjectInfo flagPlt;
	//ObjectInfo flagPlc;
	//ObjectInfo flagPlb;
	ObjectInfo flagGlt;
	ObjectInfo flagGlb;
	ObjectInfo flagLt;
	ObjectInfo flagLb;		
	ObjectInfo lGoal;
	
	ObjectInfo ball;
	//ObjectInfo goal;
	
	//ObjectInfo flagC;
	//ObjectInfo flagCt;
	//ObjectInfo flagCb;
	
	// first put it somewhere on my side
	if(Pattern.matches("^before_kick_off.*",m_playMode))
	    m_krislet.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );
	
	while( !m_timeOver )
	    {
		/*
		In each iteration of the forever while loop in Brain.java, the agent sees new facts i.e. 
		location/proximity of the flags and ball. These facts are stored in a String ArrayList 
		Current_Iteration_Facts. 
		
		In addition, a local deep copy of the count array list is also created 
		Integer ArrayList count_copy. These array lists will be the most important arguments 
		of the forward chaining function Search_Rule_With_Action_In_RHS(). 
		
		Rules and predicates visited in each iteration of the forever while loop are printed using 
		Forward_Chain_Path and Rules_visited array lists
		
		The action right-hand side is also stored in a Current_Iteration_Actions array list if a rule
		that results in an action holds.
		*/
		
		ArrayList<String> Current_Iteration_Facts = new ArrayList<String>();
		ArrayList<String> Current_Iteration_Actions = new ArrayList<String>();
		ArrayList<String> Forward_Chain_Path = new ArrayList<String>();
		ArrayList<String> Rules_visited = new ArrayList<String>();
		ArrayList<Integer> count_copy = new ArrayList<Integer>();
		
		// This will create a copy of count (array list that holds the number of LHS predicates for each rule). And both the copy_count and count will be independent.
		// Elements of the two array lists won't be shared. Doing copy_count = count causes such issues where both lists point to the same elements.
		for(int e = 0; e < count.size(); e++)
		{
			count_copy.add(count.get(e));  
		}

		////flagPrt =  m_memory.getObject("flag p r t");
		////flagPrc =  m_memory.getObject("flag p r c");
		////flagPrb =  m_memory.getObject("flag p r b");
		flagGrt =  m_memory.getObject("flag g r t");
		flagGrb =  m_memory.getObject("flag g r b");
		flagRt =  m_memory.getObject("flag r t");
		flagRb =  m_memory.getObject("flag r b");		
		rGoal =  m_memory.getObject("goal r");
		////flagPlt =  m_memory.getObject("flag p l t");
		////flagPlc =  m_memory.getObject("flag p l c");
		////flagPlb =  m_memory.getObject("flag p l b");
		flagGlt =  m_memory.getObject("flag g l t");
		flagGlb =  m_memory.getObject("flag g l b");
		flagLt =  m_memory.getObject("flag l t");
		flagLb =  m_memory.getObject("flag l b");			
		lGoal =  m_memory.getObject("goal l");	
		ball =  m_memory.getObject("ball");	

		////if(flagPrt!=null)Current_Iteration_Facts.add("flagPrt_visible");
		////if(flagPrc!=null)Current_Iteration_Facts.add("flagPrc_visible");
		////if(flagPrb!=null)Current_Iteration_Facts.add("flagPrb_visible");
		if(flagGrt!=null)
		{
			Current_Iteration_Facts.add("flagGrt_visible");
			if(flagGrt.m_direction < 0) Current_Iteration_Facts.add("flagGrt_dir_<_0");
			if(flagGrt.m_direction > 0) Current_Iteration_Facts.add("flagGrt_dir_>_0");
		}
		if(flagGrb!=null)
		{	
			Current_Iteration_Facts.add("flagGrb_visible");
			if(flagGrb.m_direction > 0) Current_Iteration_Facts.add("flagGrb_dir_>_0");
			if(flagGrb.m_direction < 0) Current_Iteration_Facts.add("flagGrb_dir_<_0");
		}
		if(flagRt!=null)Current_Iteration_Facts.add("flagRt_visible");
		if(flagRb!=null)Current_Iteration_Facts.add("flagRb_visible");
		if(rGoal!=null)
			Current_Iteration_Facts.add("rGoal_visible");
		else
			Current_Iteration_Facts.add("rGoal_not_visible");
		////if(flagPlt!=null)Current_Iteration_Facts.add("flagPlt_visible");
		////if(flagPlc!=null)Current_Iteration_Facts.add("flagPlc_visible");
		////if(flagPlb!=null)Current_Iteration_Facts.add("flagPlb_visible");
		if(flagGlt!=null)
		{
			Current_Iteration_Facts.add("flagGlt_visible");
			if(flagGlt.m_direction>0)Current_Iteration_Facts.add("flagGlt_dir_>_0");
			if(flagGlt.m_direction<0)Current_Iteration_Facts.add("flagGlt_dir_<_0");
		}
		if(flagGlb!=null)
		{
			Current_Iteration_Facts.add("flagGlb_visible");
			if(flagGlb.m_direction<0)Current_Iteration_Facts.add("flagGlb_dir_<_0");
			if(flagGlb.m_direction>0)Current_Iteration_Facts.add("flagGlb_dir_>_0");
		}
		if(flagLt!=null)Current_Iteration_Facts.add("flagLt_visible");
		if(flagLb!=null)Current_Iteration_Facts.add("flagLb_visible");
		if(lGoal!=null)
			Current_Iteration_Facts.add("lGoal_visible");
		else
			Current_Iteration_Facts.add("lGoal_not_visible");
		
		if( ball==null ) //ball not present
		{
			Current_Iteration_Facts.add("ball_not_visible");
			Current_Iteration_Facts.add("ball_direction_unknown");
		}
		else //ball is not null..it is present
		{	
			Current_Iteration_Facts.add("ball_visible");
		//	if(ball.m_direction == 0) Current_Iteration_Facts.add("ball_dir_=_0");
			if(ball.m_distance > 1.0) Current_Iteration_Facts.add("ball_dist_>_1");
			if(ball.m_distance < 1.0) Current_Iteration_Facts.add("ball_dist_<_1");
			if(ball.m_direction < 10) Current_Iteration_Facts.add("ball_dir_<_10");
			if(ball.m_direction > -10) Current_Iteration_Facts.add("ball_dir_>_-10");
			if(ball.m_direction > 10) Current_Iteration_Facts.add("ball_dir_>_10");
			if(ball.m_direction < -10) Current_Iteration_Facts.add("ball_dir_<_-10");
			if(ball.m_direction > 50) Current_Iteration_Facts.add("ball_dir_>_50");
			if(ball.m_direction < -50) Current_Iteration_Facts.add("ball_dir_<_-50");	
			if(ball.m_direction > 5) Current_Iteration_Facts.add("ball_dir_>_5");
			if(ball.m_direction < -5) Current_Iteration_Facts.add("ball_dir_<_-5");	
		}

		System.out.println("\nCurrent_Iteration_Facts that the agent sees");
		System.out.println("=============================================");
		for(int e = 0; e < Current_Iteration_Facts.size(); e++)
		{
			System.out.println(e+" "+Current_Iteration_Facts.get(e));  
		}
		
		// Calling the forward chin function.
		Search_Rule_With_Action_In_RHS(Current_Iteration_Facts, Current_Iteration_Actions, Forward_Chain_Path, Rules_visited, count_copy);
		//Search_Rule_With_Action_In_RHS_PRINT(Current_Iteration_Facts, Current_Iteration_Actions, Forward_Chain_Path, Rules_visited, count_copy);
		
		System.out.println("\nCurrent_Iteration_Actions (RHS) of a rule the algorithm finds with forward chaining");
		System.out.println("======================================================================================");		
		for(int e = 0; e < Current_Iteration_Actions.size(); e++)
		{
			System.out.println(e+" "+Current_Iteration_Actions.get(e));  
		}	
		System.out.println("\nForward_Chain_Path (predicates checked) in this iteration of while loop");
		System.out.println("===========================================================================");
		for(int e = 0; e < Forward_Chain_Path.size(); e++)
		{
			System.out.println(e+" "+Forward_Chain_Path.get(e));  
		}
		
		System.out.println("\nRules_visited in this iteration of while loop");
		System.out.println("================================================");
		for(int e = 0; e < Rules_visited.size(); e++)
		{
			System.out.println(e+" "+Rules_visited.get(e));  
		}		
		
		// Logic that takes the Current_Iteration_Actions arraylist and performs action.
		outerloopif:
		if (Current_Iteration_Actions.size() > 1)
		{
			if (Current_Iteration_Actions.get(1).equals("turn"))
			{
				System.out.println("\nturn");
				int value = Integer.parseInt(Current_Iteration_Actions.get(2));
				System.out.println("value in int "+value);
				m_krislet.turn(value);
				m_memory.waitForNewInfo();
				break outerloopif;
			}
			else if (Current_Iteration_Actions.get(1).equals("dash"))
			{
				System.out.println("\ndash");
				int value = Integer.parseInt(Current_Iteration_Actions.get(2));
				System.out.println("value in int "+value);
				m_krislet.dash(value);
				break outerloopif;
			}
			else if(Current_Iteration_Actions.get(1).equals("kick"))
			{
				System.out.println("\nkick");
				int value = Integer.parseInt(Current_Iteration_Actions.get(2));
				System.out.println("value in int 0, kick with 50 in direction 0 "+value);
				m_krislet.kick(50, value);
				break outerloopif;
			}
		
		}
		System.out.println("**********************************************************");
		System.out.println("*************END OF ITERATION OF THE WHILE LOOP***********");
		System.out.println("**********************************************************");
		
		
		/*
		Original Brain.java implementation commented out.
		object = m_memory.getObject("ball");
		if( object == null )
		    {
			m_krislet.turn(40);
			m_memory.waitForNewInfo();
		    }
		else if( object.m_distance > 1.0 )
		    {
			if( object.m_direction != 0 )
			    m_krislet.turn(object.m_direction);
			else
			    m_krislet.dash(10*object.m_distance);
		    }
		else 
		    {
			if( m_side == 'l' )
			    object = m_memory.getObject("goal r");
			else
			    object = m_memory.getObject("goal l");

			if( object == null )
			    {
				m_krislet.turn(40);
				m_memory.waitForNewInfo();
			    }
			else
			    m_krislet.kick(100, object.m_direction);
		    }
		*/


		// sleep one step to ensure that we will not send
		// two commands in one cycle.
		try{
		    Thread.sleep(2*SoccerParams.simulator_step);
		}catch(Exception e){}

	    }
	m_krislet.bye();
    }


    //===========================================================================
    // Here are suporting functions for implement logic of Expert System using Forward Chaining.
	
	// Forward chaining algorithm based on the implementation provided in this link:
	//https://snipplr.com/view/56296/ai-forward-chaining-implementation-for-propositional-logic-horn-form-knowledge-bases
public static void Search_Rule_With_Action_In_RHS(ArrayList<String> Current_Iteration_Facts, ArrayList<String> Current_Iteration_Actions, ArrayList<String> Forward_Chain_Path, ArrayList<String> Rules_visited, ArrayList<Integer> count_copy){
// loop through while there are unprocessed facts
outerloop:
	while(!Current_Iteration_Facts.isEmpty()){

		// take the first item and process it
	 	String p = Current_Iteration_Facts.remove(0);
		// add to entailed
		Forward_Chain_Path.add(p);
		// for each of the clauses/rules....
		for (int i=0;i<clauses.size();i++){
			// .... that contain p in its premise
			if (premiseContains(clauses.get(i),p)){
				Integer j = count_copy.get(i);
				// reduce count : unknown elements in each premise
				count_copy.set(i,--j);
				//--j means decrement and then assign.
				// all the elements in the premise are now known
				if (count_copy.get(i)==0){
					// the conclusion has been proven so put into Current_Iteration_Facts
					String RHS = clauses.get(i).split("=>")[1];
					RHS = RHS.trim();
					// If RHS has , separated parameters for action, then it's action RHS.
					if (RHS.contains(","))
					{
						String[] RHS_parts = RHS.split(",");
						// Add the action and all comma seperated parameters in the Current_Iteration_Actions ArrayList<String>
						for (int x=0; x<RHS_parts.length; x++)
						{
							Current_Iteration_Actions.add(RHS_parts[x]);
						}
						Rules_visited.add(clauses.get(i));
						break outerloop;
					}
					// RHS only has one predicate..one patameter and no comma seperated string with action and its details.
					// That can be added as a fact in the array list of facts.
					else 
					{
						// If it's a fact in RHS, first check if the fact already exists in the list of facts being checked. If not, add it.
						if (!Current_Iteration_Facts.contains(RHS))
							Current_Iteration_Facts.add(RHS);
						Rules_visited.add(clauses.get(i));
					}					
				}
			}	
		}
	}
} 

// Same function as above but has many print statements to check the working of the algorithm. Hence kept in the file for reference.
public static void Search_Rule_With_Action_In_RHS_PRINT(ArrayList<String> Current_Iteration_Facts, ArrayList<String> Current_Iteration_Actions, ArrayList<String> Forward_Chain_Path, ArrayList<String> Rules_visited, ArrayList<Integer> count_copy){
// loop through while there are unprocessed facts
outerloop:
while(!Current_Iteration_Facts.isEmpty()){
		
		// Printing Current_Iteration_Facts in the beginning of the while loop.
		System.out.println("\nPrinting Current_Iteration_Facts in the beginning of the while loop");
		System.out.println("===================="); 
		System.out.println("====================");		
		for(int c = 0; c < Current_Iteration_Facts.size(); c++)
		{
			System.out.println(c+" "+Current_Iteration_Facts.get(c));  
		}

		System.out.println("\nPrinting clauses in the beginning of the while loop");
		System.out.println("===================="); 
		System.out.println("====================");		
		for(int c = 0; c < clauses.size(); c++)
		{
			System.out.println(c+" "+clauses.get(c));  
		}
		
	
		// take the first item and process it
	 	String p = Current_Iteration_Facts.remove(0);
		System.out.println("p = Current_Iteration_Facts.remove(0) --- ---"+ p);
		System.out.println("\nPrinting Current_Iteration_Facts after element 0 is removed.");
		System.out.println("===================="); 
		System.out.println("====================");	
		for(int c = 0; c < Current_Iteration_Facts.size(); c++)
		{
			System.out.println(c+" "+Current_Iteration_Facts.get(c));  
		}
		
		// add to entailed
		Forward_Chain_Path.add(p);
		// for each of the clauses/rules....
		for (int i=0;i<clauses.size();i++){
			System.out.println("-------------------------------------------------------------------------------------------");
			System.out.println(i+" FORLOOP ITERATION OVER ALL RULES/CLAUSES. Current RULE/CLAUSE being checked in FOR LOOP:");
			System.out.println(clauses.get(i));
			System.out.println("p = "+p);
			System.out.println("-------------------------------------------------------------------------------------------");
			// .... that contain p in its premise
			if (premiseContains(clauses.get(i),p)){
			System.out.println("\npremiseContains(clauses.get(i),p is true. In the If statement.");	
			System.out.println("In the If statement "+i+" p="+p+"  "+clauses.get(i));
			Integer j = count_copy.get(i);
			System.out.println("In the If statement count_copy.get(i) "+i+" "+count_copy.get(i) + " prior to subtraction. j=" + j);
			// reduce count_copy : unknown elements in each premise
			count_copy.set(i,--j);
			//System.out.println("In the If statement count_copy.get(i) "+i+" "+count_copy.get(i) + " after subtraction. j=" + j);
			//--j means decrement and then assign.
				// all the elements in the premise are now known
				if (count_copy.get(i)==0){
					System.out.println("\n count_copy.get(i)==0 all the elements in the premise are now known. In the If if statement.");
					System.out.println("In the If if statement "+i+" p="+p+"  "+clauses.get(i));
					System.out.println("In the If if statement "+i+" "+count_copy.get(i));
					// the conclusion has been proven so put into Current_Iteration_Facts
					//String head = clauses.get(i).split("=>")[1];
					String RHS = clauses.get(i).split("=>")[1];
					RHS = RHS.trim();
					// If RHS has , separated parameters for action, then it's action RHS.
					if (RHS.contains(","))
					{
						String[] RHS_parts = RHS.split(",");
						//ActionFoundInRHSofRule = true;
						// Add the action and all comma seperated parameters in the Current_Iteration_Actions ArrayList<String>
						for (int x=0; x<RHS_parts.length; x++)
						{
							Current_Iteration_Actions.add(RHS_parts[x]);
						}
						Rules_visited.add(clauses.get(i));
						System.out.println ("breaking from outerloop");
						break outerloop;
					}
					else 
					{
						if (!Current_Iteration_Facts.contains(RHS))
							Current_Iteration_Facts.add(RHS);
						Rules_visited.add(clauses.get(i));					
					}					
				}
			}	
		}
	}
} 
// method which checks if p appears in the premise of a given clause	
// input : clause, p
// output : true if p is in the premise of clause
public static boolean premiseContains(String clause, String p){
	String premise = clause.split("=>")[0];
	String[] conjuncts = premise.split("&");
	// check if p is in the premise
	if (conjuncts.length==1)
		return premise.equals(p);
	else
		return Arrays.asList(conjuncts).contains(p);
}
	
	// Function to initialize the count, clauses and agenda arraylists
	public void init(String tell){
	String[] sentences = tell.split(";");
		for (int i=0;i<sentences.length;i++){
 
			if (!sentences[i].contains("=>")) 
				// add facts to be processed
				agenda.add(sentences[i]);
			else{
				// add sentences
				clauses.add(sentences[i]);
				count.add(sentences[i].split("&").length);
			}
		}		
	}

	// Function to read the textfile
	public ArrayList<String> TextFileContents(String FileName){
	    
		ArrayList<String> listOfLines = new ArrayList<>();
        try {
            BufferedReader bufReader = new BufferedReader(new FileReader(FileName));
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
		return listOfLines;
	}	
	// Function to retrun the entire text file based knowledge base as a single string
	public String KnowledgeBaseFromTextFileAsString(ArrayList<String> TextFileContentsAsArrayList){
		String KB="";
		boolean ignore = false;
        for(int i = 0; i < TextFileContentsAsArrayList.size(); i++)
        {
			String temp = TextFileContentsAsArrayList.get(i);
			if(temp.trim().isEmpty() || temp.startsWith("//"))
				ignore = true;
			else
				ignore = false;
			
			if (!ignore){
				temp = temp.trim();
				temp = temp+";";
				KB=KB+temp;
			}
		}
		KB=KB.replaceAll("\\s+","");
		return KB;
	}
	

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
		// Call the set_Last_Message Memory function newly created in Memory.java
		// Hear messages may originate from players who say things (including the speaking
		// agent itself), or from the game referee. The server maintains a state machine of the
		// current game mode (kick off, game on, goals, penalties, etc.) and announces scoring
		// and state changes through these messages.
	
		//m_memory.set_Last_Message(message);
		
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
