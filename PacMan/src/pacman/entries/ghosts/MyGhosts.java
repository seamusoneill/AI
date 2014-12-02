package pacman.entries.ghosts;

import java.util.EnumMap;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getActions() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.ghosts.mypackage).
 */

public class MyGhosts extends Controller<EnumMap<GHOST,MOVE>>
{
	private enum Quadrant {
			TOP_RIGHT, 
			TOP_LEFT, 
			BOTTOM_RIGHT, 
			BOTTOM_LEFT
	};
	
	private EnumMap<GHOST, MOVE> myMoves=new EnumMap<GHOST, MOVE>(GHOST.class);
	
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue)
	{
		myMoves.clear();
		
		if(game.doesGhostRequireAction(GHOST.BLINKY)) //if it requires an action
		{
			myMoves.put(GHOST.BLINKY,DecisionTree(game,GHOST.BLINKY));
			/*if (game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.BLINKY), game.getGhostLastMoveMade(GHOST.BLINKY), DM.PATH) < 10)
			{
				myMoves.put(GHOST.BLINKY,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.BLINKY),
						game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(GHOST.BLINKY),DM.PATH));
			}
			else if (game.getDistance(game.getGhostCurrentNodeIndex(GHOST.BLINKY), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(GHOST.BLINKY), DM.PATH) > 10) 
			{
				myMoves.put(GHOST.BLINKY, game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.BLINKY),
						game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(GHOST.BLINKY), DM.PATH));
			}*/
		}

		if(game.doesGhostRequireAction(GHOST.PINKY)) //if it requires an action
		{
			myMoves.put(GHOST.PINKY,DecisionTree(game,GHOST.PINKY));
			/*myMoves.put(GHOST.PINKY,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.PINKY),
					game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),game.getActivePillsIndices(),DM.PATH),
					game.getGhostLastMoveMade(GHOST.PINKY),DM.PATH));*/
		}
		
		if( game.doesGhostRequireAction(GHOST.INKY))
		{
			myMoves.put(GHOST.INKY, DecisionTree(game, GHOST.INKY));
			/*if (game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.INKY), game.getGhostLastMoveMade(GHOST.INKY),DM.PATH) < 10)
			{
				myMoves.put(GHOST.INKY,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.INKY),
						game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(GHOST.INKY),DM.PATH));
			}
			else if(game.getDistance(game.getGhostCurrentNodeIndex(GHOST.INKY), game.getPacmanCurrentNodeIndex(), game.getGhostLastMoveMade(GHOST.INKY), DM.PATH) > 10) 
			{
			}*/
		}
		if(game.doesGhostRequireAction(GHOST.SUE)) //if it requires an action
		{
			myMoves.put(GHOST.SUE, DecisionTree(game,GHOST.SUE));
			/*if (game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.SUE), game.getGhostLastMoveMade(GHOST.SUE), DM.PATH) < 10)
			myMoves.put(GHOST.SUE,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.SUE),
					game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(GHOST.SUE),DM.PATH));
			
			else if (game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(GHOST.SUE), game.getGhostLastMoveMade(GHOST.SUE), DM.PATH) > 10)
			{
				
			}
			*/
		}
		return myMoves;
	}
	
	MOVE DecisionTree(Game game, GHOST g)
	{
		//When Pacman is close to a power pill
		if (game.GetDistance(game.getPacmanCurrentNodeIndex(),nearestpowerpill))
		{
			Flee();
		}
		
		//Chase when Player is close to Pacman
		else if (game.GetDistance(from ghost to Pacman))
		{
			Chase();
		}
		
		//Enter same quadrant as pacman if other ghosts haven't claimed it already
		else if ()
		{
			WanderQuadrant(Pacmans Quadrant);
			Claim lock same quadrant wanderer.
		}
		
		// Try intercept the nearest pill to pacman
		else if(same quadrant lock is claimed)
		{
			if ( Pacman is far from power pill)
			{
				myMoves.put(GHOST.Pinky,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.PINKY),
					game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),game.getActivePillsIndices(),DM.PATH),
					game.getGhostLastMoveMade(GHOST.PINKY),DM.PATH));
			}
			else 
			{
				go to nearest pill to pacman which is at least x distance from power pill
				return myMoves.put(GHOST.SUE,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.SUE),
						game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(GHOST.SUE),DM.PATH));
				
			}
		}
		
		//Hover over portals
		else if(nearest pill lock is claimed)
		{
			get pacman half
			WanderHalf (opposite half) // Go from portal x to portal y and back again
		}
		
		//Be in the quadrant above pacman
		else if( hover lock is taken)
		{	
			get pacman quadrant
			WanderQuadrant (Quadrant above pacman);
		}
	}
	
	void Flee(){

	}
	void Chase()
	{
	}
	
	Quadrant GetQuadrant(int nodeIndex)
	{
		if (nodeIndex < 25)
		{
			return Quadrant.TOP_LEFT;
		}
		else if( nodeIndex > 25 && nodeIndex < 50)
		{
			return Quadrant.TOP_RIGHT;
		}
		else if( nodeIndex > 50 && nodeIndex < 75)
		{
			return Quadrant.BOTTOM_LEFT;
		}
		else {
			return Quadrant.BOTTOM_RIGHT;
		}
	}
	
	void WanderQuadrant()
	{
	}
	void GetHalf(){
		
	}
	void WanderPortals()
	{
	}
}