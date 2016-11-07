package de.htw.grischa.chess;


public class ComputedGame {

	private IChessGame game;
	private int depth;
	private double quality;
	
	// TODO: Game + Tiefe d??rfen nicht zweimal mit unterschiedlicher Qualit??t enthalten sein
	
	/*
	 * Funktion public ComputedGame(IChessGame game, int depth, Double quality)
	 * Konstruktor f??r ComputedGame
	 */	
	public ComputedGame(IChessGame game, int depth, Double quality){
		this.game = game;
		this.depth = depth;
		this.quality = quality;
	}
	
	/*
	 * Funktion public boolean equals(IChessGame game, int depth)
	 * Zum ??berpr??fen ob das Spiel mit entsprechender Tiefe bereits enthalten ist, um sp??ter die entsprechende Qualit??t ausgegeb zu k??nnen.
	 * @return boolean
	 */	
	public boolean equals(IChessGame game, int depth){
		
		if(this.game.equals(game) && this.depth==depth)
			return true;
		else
			return false;
	}
	
	/*
	 * Funktion public boolean equals(IChessGame game, int depth, Double quality)
	 * Zum ??berpr??fen ob das Spiel mit entsprechender Tiefe und Qualit??t bereits enthalten ist..
	 * @return boolean
	 */	
	public boolean equals(IChessGame game, int depth, Double quality){
		
		if(this.game.equals(game) && this.depth==depth && this.quality ==quality)
			return true;
		else
			return false;
	}
	
	/*
	 * Funktion public double getQuality()
	 * Gibt die Qualit??t des gespeicherten Spiels zur??ck
	 * @return double 
	 */
	public double getQuality() {
		return quality;
	}
}
