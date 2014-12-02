package pacman.entries.ghosts;

import java.io.Console;
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
	
	private final static int NUMBER_NODES = 1292;
	private final static int PILL_PROXIMITY = 20;		//if Ms Pac-Man is this close to a power pill, back away
	private final static int CHASE_PROXIMITY = 70; //If ghost is close to Ms Pac-Man CHASE
	private EnumMap<GHOST, MOVE> myMoves=new EnumMap<GHOST, MOVE>(GHOST.class);
	
	private boolean quadrantWanderer = false;
	private boolean portalGuard = false;
	private boolean towerGuard = false;
	
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue)
	{
		myMoves.clear();
		
		if(game.doesGhostRequireAction(GHOST.BLINKY)) //if it requires an action
		{
			DecisionTree(game,GHOST.BLINKY);
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
			DecisionTree(game,GHOST.PINKY);
			/*myMoves.put(GHOST.PINKY,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(GHOST.PINKY),
					game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),game.getActivePillsIndices(),DM.PATH),
					game.getGhostLastMoveMade(GHOST.PINKY),DM.PATH));*/
		}
		
		if( game.doesGhostRequireAction(GHOST.INKY))
		{
			DecisionTree(game, GHOST.INKY);
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
			DecisionTree(game,GHOST.SUE);
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
	
	void DecisionTree(Game game, GHOST ghost)
	{
		//When Ms Pac-Man is close to a power pill
		if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game,ghost))
		{
			Flee(game, ghost);
		}
		
		//Chase when Player is close to Ms Pac-Man
		else if (closeToPacman(game,ghost))
		{
			WanderQuadrant(game,ghost,getPacmanQuadrant(game));
			
		}
		//Enter same quadrant as pacman if other ghosts haven't claimed it already
		else if (quadrantWanderer == false)
		{
			quadrantWanderer = true;
			WanderQuadrant(game,ghost,getPacmanQuadrant(game));
		}
	
		// Try intercept the nearest pill to pacman
		else
		{
			InterceptPills(game, ghost);
		}
		/*
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
		}*/
	}

/*
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
	
	void GetHalf(){
		
	}
	void WanderPortals()
	{
	}
	
	/*Ghost Actions ie States
	 * 
	 * 
	 * 
	 */
	
	//Run away from Ms Pac-Man
	private void Flee( Game game, GHOST ghost)
	{
		myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(game.getGhostCurrentNodeIndex(ghost),
			game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
	}
	
	//Run after Ms-Pacman
	private void Chase( Game game, GHOST ghost)
	{
		myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
	}
	
	private void WanderQuadrant(Game game, GHOST ghost, Quadrant quad)
	{
		if (quad == Quadrant.TOP_LEFT)
		{
			for (int i = 0; i < NUMBER_NODES/4; i++)
				if(game.isJunction(i))
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							i,game.getGhostLastMoveMade(ghost),DM.PATH));
		}
		else if (quad == Quadrant.TOP_RIGHT)
		{
			for (int i = NUMBER_NODES/4; i < NUMBER_NODES/4 * 2; i++)
				if(game.isJunction(i))
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							i,game.getGhostLastMoveMade(ghost),DM.PATH));
		
		}
		else if (quad == Quadrant.BOTTOM_LEFT)
		{
			for (int i = NUMBER_NODES /4 * 2; i < NUMBER_NODES/4 * 3; i++)
				if(game.isJunction(i))
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							i,game.getGhostLastMoveMade(ghost),DM.PATH));
		
		}
		else if (quad == Quadrant.BOTTOM_RIGHT)
		{
			for (int i = NUMBER_NODES /4 * 3; i < NUMBER_NODES; i++)
				if(game.isJunction(i))
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							i,game.getGhostLastMoveMade(ghost),DM.PATH));
		}
		quadrantWanderer = false;
	}
	
	private void InterceptPills(Game game, GHOST ghost) {
		myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),game.getActivePillsIndices(),DM.PATH),
				game.getGhostLastMoveMade(ghost),DM.PATH));

		/*if ( Pacman is far from power pill)
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
			
		}*/
	}
	
	/*Game state checks
	 * 
	 * 
	 * 
	 * 
	 */
	
    //Check if Ms Pac-Man is close to an available power pill and is closer than a ghost //TODO one ghost need to force you to eat the pill
	private boolean closeToPower(Game game, GHOST ghost)
    {
    	int[] powerPills=game.getPowerPillIndices();
    	
    	for(int i=0;i<powerPills.length;i++)
    		if(game.isPowerPillStillAvailable(i) 
    				&& game.getShortestPathDistance(powerPills[i],game.getPacmanCurrentNodeIndex())<PILL_PROXIMITY
    				&& game.getShortestPathDistance(powerPills[i],game.getPacmanCurrentNodeIndex()) <  
    				game.getShortestPathDistance(powerPills[i],game.getGhostCurrentNodeIndex(ghost)))
    			return true;
    
   			return false;
    }
	
	//Check if a ghost is close to Ms Pac-Man
	private boolean closeToPacman(Game game, GHOST ghost)
	{
		if (game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost),game.getPacmanCurrentNodeIndex()) < CHASE_PROXIMITY)
			return true;
		else
			return false;
	}
	
	//Get the quadrant Ms Pac-Man is in by checking which power pill she is closest to
	private Quadrant getPacmanQuadrant(Game game)
	{
    	int[] powerPills=game.getPowerPillIndices();
    	
    	int pacmanQuadrant = 0;
    	int shortestPath = 0;
    	for(int i=0;i<powerPills.length;i++)
    	{
    		int path = game.getShortestPathDistance(powerPills[i],game.getPacmanCurrentNodeIndex());
    		if (i == 0)
    		{
    			shortestPath = path;
    		}
    		if (path < shortestPath)
    		{
    			shortestPath = path;
    			pacmanQuadrant = i;
    		}
    	}
    	
		if (pacmanQuadrant == 0)
			return Quadrant.TOP_LEFT;
		else if (pacmanQuadrant == 1)
			return Quadrant.TOP_RIGHT;
		else if (pacmanQuadrant == 2)
			return Quadrant.BOTTOM_LEFT;
		else
			return Quadrant.BOTTOM_RIGHT;		
	}
}