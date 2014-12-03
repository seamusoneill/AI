package pacman.entries.ghosts;

import java.io.Console;
import java.util.EnumMap;
import java.util.Random;
import java.util.Vector;

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
	
	private final static int PILL_PROXIMITY = 20;		//if Ms Pac-Man is this close to a power pill, back away
	private final static int CHASE_PROXIMITY = 70; //If ghost is close to Ms Pac-Man CHASE
	private final static int MAZE_WIDTH = 108;
	private final static int MAZE_HEIGHT = 116;
	private EnumMap<GHOST, MOVE> myMoves=new EnumMap<GHOST, MOVE>(GHOST.class);
	Random rnd=new Random();
	boolean initialize = false;
	Vector<Integer> TLQuad = new Vector<Integer>();
	Vector<Integer> TRQuad = new Vector<Integer>();
	Vector<Integer> BLQuad = new Vector<Integer>();
	Vector<Integer> BRQuad = new Vector<Integer>();
	Vector<Integer> TLQuadJunctions = new Vector<Integer>();
	Vector<Integer> TRQuadJunctions = new Vector<Integer>();
	Vector<Integer> BLQuadJunctions = new Vector<Integer>();
	Vector<Integer> BRQuadJunctions = new Vector<Integer>();
	private boolean quadrantWanderer = false;
	private boolean suicidalChaser = false;
	private boolean pillInterceptor = false;
	private boolean portalGuard = false;
	private boolean towerGuard = false;
	
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue)
	{
		if (!initialize)
		{
			for (int i = 0; i < game.getNumberOfNodes(); i++)
			{
				int x = game.getNodeXCood(i);
				int y = game.getNodeYCood(i);
				
				if (x <= MAZE_WIDTH/2)
				{
					if (y <= MAZE_HEIGHT/2)
						TLQuad.add(i);
					else
						BLQuad.add(i);
				}
				else
				{
					if (y <= MAZE_HEIGHT/2)
						TRQuad.add(i);
					else
						BRQuad.add(i);
				}
			}
			
			for (int i = 0; i < TLQuad.size(); i++)
			{
				if(game.isJunction(TLQuad.get(i)))
						TLQuadJunctions.add(TLQuad.get(i));
			}
			for (int i = 0; i < TRQuad.size(); i++)
			{
				if(game.isJunction(TRQuad.get(i)))
						TRQuadJunctions.add(TRQuad.get(i));
			}
			for (int i = 0; i < BLQuad.size(); i++)
			{
				if(game.isJunction(BLQuad.get(i)))
						BLQuadJunctions.add(BLQuad.get(i));
			}
			for (int i = 0; i < BRQuad.size(); i++)
			{
				if(game.isJunction(BRQuad.get(i)))
						BRQuadJunctions.add(BRQuad.get(i));
			}
			
			initialize = true;
		}
		
		myMoves.clear();
		
		if(game.doesGhostRequireAction(GHOST.BLINKY)) //if it requires an action
		{
			DecisionTree(game,GHOST.BLINKY);
		}

		if(game.doesGhostRequireAction(GHOST.PINKY)) //if it requires an action
		{
			DecisionTree(game,GHOST.PINKY);
		}
		
		if( game.doesGhostRequireAction(GHOST.INKY))
		{
			DecisionTree(game, GHOST.INKY);
		}
		if(game.doesGhostRequireAction(GHOST.SUE)) //if it requires an action
		{
			DecisionTree(game,GHOST.SUE);
		}
		quadrantWanderer = false;
		portalGuard = false;
		towerGuard = false;
		suicidalChaser = false;
		pillInterceptor = false;
		
		return myMoves;
	}
	
	void DecisionTree(Game game, GHOST ghost)
	{	
		//One ghost should move towards Ms Pac-Man to force a pill pickup
		if (closeToPacman(game, ghost) && !suicidalChaser && game.getGhostEdibleTime(ghost) > 0)
		{
			suicidalChaser = true;
			Chase(game,ghost);
		}

		//When Ms Pac-Man is close to a power pill
		else if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game,ghost))
		{
			Flee(game,ghost);
		}
	
		//Chase when Player is close to Ms Pac-Man
		else if (closeToPacman(game,ghost))
		{
			Chase(game,ghost);
		}
		//Enter same quadrant as Ms Pac-Man if other ghosts haven't claimed it already
		else if (!quadrantWanderer)
		{
			quadrantWanderer = true;
			WanderQuadrant(game,ghost,getPacmanQuadrant(game));
		}
	
		// Try intercept the nearest pill to Ms Pac-Man
		else if (!pillInterceptor)
		{
			pillInterceptor = true;
			InterceptPills(game, ghost);
		}
		
		//Hover over portals on the opposite side of to Ms Pac-Man
		else if(!portalGuard)
		{
			portalGuard = true;
			GuardPortals(game, ghost, getPacmanQuadrant(game));
		}
		
		//Be in the quadrant above Ms Pac-Man
		else
		{	
			WanderQuadrant (game,ghost,getPacmanQuadrant(game));
		}
	}

	/*Ghost Actions ie States
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
	
	//Wander the quadrant that's passed in
	private void WanderQuadrant(Game game, GHOST ghost, Quadrant quad)
	{
		
		if (quad == Quadrant.TOP_LEFT)
		{
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					TLQuadJunctions.get(rnd.nextInt(TLQuadJunctions.size())),game.getGhostLastMoveMade(ghost),DM.PATH));
		}
		else if(quad == Quadrant.TOP_RIGHT)
		{
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					TRQuadJunctions.get(rnd.nextInt(TRQuadJunctions.size())),game.getGhostLastMoveMade(ghost),DM.PATH));
	
		}
		else if (quad == Quadrant.BOTTOM_LEFT)
		{
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					BLQuadJunctions.get(rnd.nextInt(BLQuadJunctions.size())),game.getGhostLastMoveMade(ghost),DM.PATH));
	
		}
		else
		{
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					BRQuadJunctions.get(rnd.nextInt(BRQuadJunctions.size())),game.getGhostLastMoveMade(ghost),DM.PATH));
		}
	}
	
	//Move to the pill nearest Ms Pac-Man
	private void InterceptPills(Game game, GHOST ghost) {
		myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
				game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),game.getActivePillsIndices(),DM.PATH),
				game.getGhostLastMoveMade(ghost),DM.PATH));
	}
	
	private void GuardPortals(Game game, GHOST ghost, Quadrant quad)
	{
		if (quad == Quadrant.TOP_LEFT || quad == Quadrant. BOTTOM_LEFT)
		{
			//Guard right hand side portals
		}
		else
		{
			//Guard left hand side portals
		}
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
		int x = game.getNodeXCood(game.getPacmanCurrentNodeIndex());
		int y = game.getNodeYCood(game.getPacmanCurrentNodeIndex());
		
		if (x <= MAZE_WIDTH/2)
		{
			if (y <= MAZE_HEIGHT/2)
				return Quadrant.TOP_LEFT;
			else
				return Quadrant.BOTTOM_LEFT;
		}
		else
		{
			if (y <= MAZE_HEIGHT/2)
				return Quadrant.TOP_RIGHT;
			else
				return Quadrant.BOTTOM_RIGHT;
		}
		
	}
}