package de.htw.grischa.chess;

import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.log4j.Logger;

public class AlphaBetaSearchGridResults extends AlphaBetaSearch
{
	private final static Logger _log = Logger.getLogger(AlphaBetaSearch.class);
	private ArrayList<String> sendedGames;
	private TreeMap<String, Integer> results;
	
	public AlphaBetaSearchGridResults(ArrayList<String> sendedGames,
									  TreeMap<String, Integer> results)
	{
		this.sendedGames=sendedGames;
		this.results=results;
	}
	
	@Override
	protected int getQuality(IChessGame game) 
	{
		if(results.containsKey(game.getStringRepresentation())) 
		{
			_log.debug("berechnetes spiel: "+game.getStringRepresentation()+" " +
					   "errechnete qualit??t: "+results.get(game.getStringRepresentation()));
			return results.get(game.getStringRepresentation());
		}
		else
		{
			_log.debug("Node Ergebnis nicht vorhanden! -rechne lokal. Spiel: "+game.getStringRepresentation());
			AlphaBetaSearchFixedDepth abs=new AlphaBetaSearchFixedDepth();
			if(game.getPlayerToMakeTurn()==maximizingPlayer)
			{
				int v= abs.getAlphaBetaTurn(0, game);
				return v;
			}
			else
			{
				int v= (abs.getAlphaBetaTurn(1, game)* (-1));
				return v;
			}
		}
	}

	@Override
	protected boolean isLeaf(IChessGame game, int depth)
	{
		if(sendedGames.contains(game.getStringRepresentation())) 
		{
			return true;
		}
		return false;
	}

	@Override
	protected double getPosQuality(IChessGame game) {
		// TODO Auto-generated method stub
		return getQuality(game);
	}

}
