package de.htw.grischa.chess;

import java.util.ArrayList;

/*
 * In der Klasse GameList werden die schon berechneten Bretter mit der jeweils genutzten Tiefe gespeichert und k??nnen wieder ausgelesen werden.
 * Es werden die Brettstellungen mit m??glicher Rochade und welche Farbe als n??chstes dran ist gespeichert.
 * Bei einer positiven Anfrage wird der Wert der Bewertungsfunktion zur??ckgegeben um so dem Node das erneute auswerten des Suchbaums unter dieser Brettstellung zu ersparen. 
 * @author Thor
 *
 */
public class GameList {

	private ArrayList<ComputedGame> computedGames;
	private int found=0;
	
	
	public GameList(){
		this.computedGames = new ArrayList<ComputedGame>();
	}
	
	public GameList(ArrayList<ComputedGame> gameList){
		this.computedGames = new ArrayList<ComputedGame>();
		this.computedGames.addAll(gameList);
	}
	
	/*
	 * Funktion public boolean isInList(IChessGame game, Integer depth)
	 * Pr??ft ob das gesuchte Spiel mit entsprechender Tiefe in der Liste ArrayList<ComputedGame> computedGames enthalten ist.
	 * @return boolean
	 */
	public boolean isInList(IChessGame game, int depth) {
		for (int i=0;i<computedGames.size();i++)
		{
			// Wenn das game vorhanden ist und entsprechende depth hat true zur??ckgeben
			if (computedGames.get(i).equals(game, depth))
			{
				found = i;
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Funktion public double getGameValue(IChessGame game, int depth)
	 * Holt die Qualit??t des Spiels. Erst verwenden NACH aufruf von isInList!
	 * @return double
	 */
	public double getGameValue(IChessGame game, int depth){
		return computedGames.get(found).getQuality();
	}
	
	/*
	 * Funktion public void setGame(IChessGame game, int depth, double Quality)
	 * Erzeugt ein Objekt ComputedGame und f??llt es mit Werten und f??gt diese Objekt der Liste computedGames hinzu.
	 * @return void
	 */
	public void setGame(IChessGame game, int depth, double Quality){
		computedGames.add(new ComputedGame(game, depth, Quality));
	}
}
