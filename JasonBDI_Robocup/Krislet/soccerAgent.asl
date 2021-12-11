////////////////////////////// Inital goals ////////////////////////////
!see.


/////////////////////////////// SEE plans /////////////////////////////

//Plan 1: Can't see the ball -> turn to find it
+!see
    :   not canSeeBall
    <-  turn;
        !see.



//Plan 2: Can see the ball -> dash towards it
+!see
    :   canSeeBall
    <-  dash_to_ball;
        -canSeeBall;
        !move.



//Plan 3: Back up scenario
-!see
    <-  turn;
        !see.

/////////////////////////////// MOVE plans /////////////////////////////

//Plan 1: Can see the ball but it's still far -> dash towards it
+!move
    :   canSeeBall & farFromBall
    <-  dash_to_ball;
        -canSeeBall;
		-farFromBall;
        !move.



//Plan 2: Around the ball but not directly infront of it -> face the ball
+!move
    :   canSeeBall & aroundBall & not directlyFacingBall
    <-  turn_to_ball;
        -canSeeBall;
		-aroundBall;
        !move.



//Plan 3: Around the ball and directly facing it -> dash towards it
+!move
    :   canSeeBall & aroundBall & directlyFacingBall
    <-  dash_to_ball;
        -canSeeBall;
		-aroundBall;
		-directlyFacingBall;
        !move.
		


//Plan 4: Beside the ball and ready to kick but can't see the opponent's goal -> turn to find it		
+!move
    :   closeToBall & not canSeeOpponentGoal
    <-  turn;
        -canSeeBall;
		-closeToBall;
        !move.



//Plan 5: Beside the ball and ready to kick -> kick at the goal
+!move
    :   closeToBall & canSeeOpponentGoal & aimingAtOpponentGoal
    <-  kick;
        -canSeeBall;
        -closeToBall;
		-canSeeOpponentGoal;
		-aimingAtOpponentGoal;
        !move.
		
		

//Plan 6: Can't see the ball anymore -> turn to find it
+!move
    :   not canSeeBall
    <-  turn;
        !see.
		
		
		
//Plan 7: Back up scenario
-!move
    <-  turn;
        !move.