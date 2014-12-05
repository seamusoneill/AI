package pacman.entries.ghosts;

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
	private final static int CHASE_PROXIMITY = 50; //If ghost is close to Ms Pac-Man CHASE
	private final static int SUICIDAL_CHASE_PROXIMITY = 100; //If ghost is this close to Ms Pac-Man, chase and don't back off if near a power pill
	private final static int MAZE_WIDTH = 108;
	private final static int MAZE_HEIGHT = 116;
	private final static int NODES_TO_JUNCTION = 5; //Limit for interception check
	
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
	Vector<Integer> LHalf = new Vector<Integer>();
	Vector<Integer> RHalf = new Vector<Integer>();
	Vector<Integer> LHalfJunctions = new Vector<Integer>();
	Vector<Integer> RHalfJunctions = new Vector<Integer>();
	
	private boolean quadrantWanderer;
	private boolean suicidalChaser;
	private boolean pillInterceptor;
	private boolean portalGuard;
	private boolean interceptChaser;
	
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue)
	{
		//Do once (called like this to be competition entry viable)
		if (!initialize)
		{
			for (int i = 0; i < game.getNumberOfNodes(); i++)
			{
				int x = game.getNodeXCood(i);
				int y = game.getNodeYCood(i);
				
				if (x <= MAZE_WIDTH/2)
				{
					LHalf.add(i);
					if (y <= MAZE_HEIGHT/2)
						TLQuad.add(i);
					else
						BLQuad.add(i);
				}
				else
				{
					RHalf.add(i);
					if (y <= MAZE_HEIGHT/2)
						TRQuad.add(i);
					else
						BRQuad.add(i);
				}
			}
			
			for (int i = 0; i < TLQuad.size(); i++)
			{
				if(game.isJunction(TLQuad.get(i)))
				{
					TLQuadJunctions.add(TLQuad.get(i));
					LHalfJunctions.add(TLQuad.get(i));
				}
			}
			for (int i = 0; i < TRQuad.size(); i++)
			{
				if(game.isJunction(TRQuad.get(i)))
				{
					TRQuadJunctions.add(TRQuad.get(i));
					RHalfJunctions.add(TRQuad.get(i));
				}
			}
			for (int i = 0; i < BLQuad.size(); i++)
			{
				if(game.isJunction(BLQuad.get(i)))
				{
					BLQuadJunctions.add(BLQuad.get(i));
					LHalfJunctions.add(BLQuad.get(i));
				}
			}
			for (int i = 0; i < BRQuad.size(); i++)
			{
				if(game.isJunction(BRQuad.get(i)))
				{
					BRQuadJunctions.add(BRQuad.get(i));
					RHalfJunctions.add(BRQuad.get(i));
				}
			}
			
			quadrantWanderer = false;
			suicidalChaser = false;
			pillInterceptor = false;
			portalGuard = false;
			interceptChaser = false;
			
			initialize = true;
		}
		
		myMoves.clear();
		
		if(game.doesGhostRequireAction(GHOST.BLINKY)) //if ghost requires an action
		{
			DecisionTree(game,GHOST.BLINKY);
		}

		if(game.doesGhostRequireAction(GHOST.PINKY)) //if ghost requires an action
		{
			DecisionTree(game,GHOST.PINKY);
		}
		
		if( game.doesGhostRequireAction(GHOST.INKY)) //if ghost requires an action
		{
			DecisionTree(game, GHOST.INKY);
		}
		if(game.doesGhostRequireAction(GHOST.SUE)) //if ghost requires an action
		{
			DecisionTree(game,GHOST.SUE);
		}
		
		return myMoves;
	}
	
	void DecisionTree(Game game, GHOST ghost)
	{	
		// Try intercept the nearest pill to Ms Pac-Man
		if (pillInterceptor == false && game.getNumberOfActivePills() < 5)
		{
			pillInterceptor = true;
			InterceptPills(game, ghost);
		}
		
		//One ghost should move towards Ms Pac-Man to force a pill pickup
		else if (closeToPacman(game, ghost,SUICIDAL_CHASE_PROXIMITY) && !suicidalChaser && !game.isGhostEdible(ghost))
		{
			suicidalChaser = true;
			Chase(game,ghost);
		}

		//When Ms Pac-Man is close to a power pill
		else if(game.isGhostEdible(ghost) || closeToPower(game,ghost))
		{
			Flee(game,ghost);
		}
	
		//Chase when Player is close to Ms Pac-Man
		else if (closeToPacman(game,ghost,CHASE_PROXIMITY) && interceptChaser == false && suicidalChaser == true)
		{
			interceptChaser = true;
			InterceptChase(game,ghost);
			suicidalChaser = false;
		}
		//Enter same quadrant as Ms Pac-Man if other ghosts haven't claimed it already
		else if (quadrantWanderer == false)
		{
			quadrantWanderer = true;
			WanderQuadrant(game,ghost,getPacmanQuadrant(game));
		}
			
		//Hover over portals on the opposite side of to Ms Pac-Man
		else if(portalGuard == false)
		{
			portalGuard = true;
			GuardPortals(game, ghost, getPacmanQuadrant(game));
			
			//Couldn't find a good place to relinquish locks. Doing it here half works
			quadrantWanderer = false;
			interceptChaser = false;
			pillInterceptor = false;
			portalGuard = false;
		}
		//Be in the quadrant above or below Ms Pac-Man
		else
		{	
			if (getPacmanQuadrant(game) == Quadrant.TOP_LEFT)
				WanderQuadrant (game,ghost,Quadrant.BOTTOM_LEFT);
			else if(getPacmanQuadrant(game) == Quadrant.TOP_RIGHT)
				WanderQuadrant (game, ghost, Quadrant.BOTTOM_RIGHT);
			else if (getPacmanQuadrant(game) == Quadrant.BOTTOM_LEFT)
				WanderQuadrant (game,ghost,Quadrant.TOP_LEFT);
			else
				WanderQuadrant(game,ghost,Quadrant.TOP_RIGHT);
			
			
		}
	}

	/*Ghost Actions*/
	
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
	
	private void InterceptChase(Game game, GHOST ghost)
	{
		int interceptPoint = game.getNeighbour(game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade()); // A node in front of Ms Pac-Man
		
		//Check if there's an empty node in node in front of Ms- Pac-Man
		for (int i = 0; i < NODES_TO_JUNCTION; i++)
		{
			if(interceptPoint != -1)
			{
				//If the node in front of Ms Pac-Man is a junction move to block off
				if (game.isJunction(interceptPoint))
				{
					myMoves.put(ghost, game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost), interceptPoint,game.getGhostLastMoveMade(ghost), DM.PATH));
					return;
				}
				else
				{
					//set interceptPoint to the node in front of it and try again
					interceptPoint = game.getNeighbour(interceptPoint, game.getPacmanLastMoveMade());
				}
			}
		}
		
		//Direct Chase, no node in front of Ms Pac-Man means she is in a corner i.e. trapped
		Chase(game,ghost);
	}
	
	//Wander the quadrant that's passed in by moving to a random junction
	private void WanderQuadrant(Game game, GHOST ghost, Quadrant quad)
	{
		int junctionId;
		if (quad == Quadrant.TOP_LEFT)
		{
			junctionId = rnd.nextInt(TLQuadJunctions.size());
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					TLQuadJunctions.get(junctionId),game.getGhostLastMoveMade(ghost),DM.PATH));
		}
		else if(quad == Quadrant.TOP_RIGHT)
		{
			junctionId = rnd.nextInt(TRQuadJunctions.size());
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					TRQuadJunctions.get(junctionId),game.getGhostLastMoveMade(ghost),DM.PATH));
	
		}
		else if (quad == Quadrant.BOTTOM_LEFT)
		{
			junctionId = rnd.nextInt(BLQuadJunctions.size());
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					BLQuadJunctions.get(junctionId),game.getGhostLastMoveMade(ghost),DM.PATH));
	
		}
		else
		{
			junctionId = rnd.nextInt(BRQuadJunctions.size());
			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					BRQuadJunctions.get(junctionId),game.getGhostLastMoveMade(ghost),DM.PATH));
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
		int junctionId;
		if (quad == Quadrant.TOP_LEFT || quad == Quadrant. BOTTOM_LEFT)
		{
				junctionId = rnd.nextInt(TRQuadJunctions.size() + BRQuadJunctions.size());
				if (junctionId < TLQuadJunctions.size())
				{
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
						TRQuadJunctions.get(junctionId),game.getGhostLastMoveMade(ghost),DM.PATH));
				}
				else
				{
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							BRQuadJunctions.get(junctionId - TRQuadJunctions.size()),game.getGhostLastMoveMade(ghost),DM.PATH));
				}
		}
		else
		{
			junctionId = rnd.nextInt(TLQuadJunctions.size() + BLQuadJunctions.size());
		
			if (junctionId < TLQuadJunctions.size())
			{
				myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
					TLQuadJunctions.get(junctionId),game.getGhostLastMoveMade(ghost),DM.PATH));
			}
			else
			{
				myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
						BLQuadJunctions.get(junctionId - TLQuadJunctions.size()),game.getGhostLastMoveMade(ghost),DM.PATH));
			}
		}
	}

	/*Game state events*/
	
	
    //Check if Ms Pac-Man is close to an available power pill and is closer than a ghost
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
	private boolean closeToPacman(Game game, GHOST ghost, int proximity)
	{
		if (game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost),game.getPacmanCurrentNodeIndex()) < proximity)
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