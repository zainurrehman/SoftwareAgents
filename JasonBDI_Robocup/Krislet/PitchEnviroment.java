// Class that defines the pitch environment

import java.util.ArrayList;
import java.util.List;;

public class PitchEnviroment {
	
	// Function that checks the player's pitch conditions 
	protected List<PlayerCondition> CheckConditions(Memory memory, char side){
		List<PlayerCondition> conditions = new ArrayList<>();
		
		ObjectInfo ball;
		ObjectInfo opponentGoal;
        ObjectInfo topPost;
        ObjectInfo bottomPost;
        
        // Get ball and opponent's goal objects
        ball = memory.getObject("ball");
        opponentGoal = side == 'l' ? memory.getObject("goal r") : memory.getObject("goal l");
        
        // Get goal posts info
        topPost = side == 'l' ? memory.getObject("flag g r t") : memory.getObject("flag g l t");
        bottomPost = side == 'l' ? memory.getObject("flag g r b") : memory.getObject("flag g l b");
        
        
        // Perceive 1: Agent can see the ball
        if( ball != null ){
            conditions.add(PlayerCondition.canSeeBall);
        }
        
        // Perceive 2: Agent is directly facing the ball
        if( ball != null && (ball.m_direction<=10.0) &&  (ball.m_direction>=-10.0)) {
        	conditions.add(PlayerCondition.directlyFacingBall);
        }
        
        if( ball != null && ball.m_distance <= 1.0 ){
        	// Perceive 3: Agent close to the ball
            conditions.add(PlayerCondition.closeToBall);
        } else if(ball != null && (ball.m_distance > 1.0) && (ball.m_distance <= 3.0)) {
        	// Perceive 4: Agent far from the ball
            conditions.add(PlayerCondition.aroundBall);
        } else if(ball != null){
        	// Perceive 5: Agent far from the ball
            conditions.add(PlayerCondition.farFromBall);
        }
        
        // Perceive 6: Agent can see the opponent's goal
        if( opponentGoal != null ){
            conditions.add(PlayerCondition.canSeeOpponentGoal);
            
            if(topPost != null && bottomPost != null) {
            	if(side == 'l') {
            		// Perceive 7: Agent aiming at the opponent's goal
                	if(topPost.m_direction<0 && bottomPost.m_direction>0) {
                		conditions.add(PlayerCondition.aimingAtOpponentGoal);
                	}
                }else {
                	if(topPost.m_direction>0 && bottomPost.m_direction<0) {
                		conditions.add(PlayerCondition.aimingAtOpponentGoal);
                	}
                }
            	
            }
        }
        
        
		return conditions;
	}
}