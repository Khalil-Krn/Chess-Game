package main;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import chessPiece.*;


public class GamePanel extends JPanel implements Runnable{

	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public final int FPS = 60;
	Thread gameThread;
	TheBoard board  = new TheBoard();
	Mouse mouse = new Mouse();
	
	
	//COLOR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;

	// PIECES
	public static ArrayList<Piece> pieces = new ArrayList();
	public static ArrayList<Piece> simPieces = new ArrayList();
	Piece activeP, checkingP;
	public static Piece castlingP;
	ArrayList<Piece> promoPieces = new ArrayList<>();

	//BOOLEANS
	boolean canMove;
	boolean validSquare;
	boolean promotion;
	boolean gameOver;
	boolean stalemate;
	
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.black);
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		
		setPieces();
		copyPieces(pieces, simPieces);
	}
	
	
	
	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start();
	}
	@Override
	public void run() {
		//Game Loop
		double drawInterval = 1000000000/FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		
		while(gameThread != null) {
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime)/drawInterval;
			lastTime = currentTime;
			
			if(delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}
	
	
	private void update() {
		
		if(promotion) {
			promoting();
		}else if(gameOver == false && stalemate == false){
		
		
		//MOUSE BUTTON IS PRESSED
		if(mouse.pressed) {
			if(activeP == null) {
				//IF ACTIVE P IS NULL, VERIFY THAT YOU CAN PICK UP A PIECE
				for(Piece piece : simPieces) {
					
					//IF THE MOUSE IS ON AN ALLY PIECE, YOU CAN PICK IT UP AS THE ACTIVEP 
					if(piece.color == currentColor && piece.col == mouse.x/TheBoard.SQUARE_SIZE && piece.row == mouse.y/TheBoard.SQUARE_SIZE) {
						activeP = piece;
					}
				}
			}else {
				//IF THE PLAYER IS HOLDING A PIECE, SIMULATE THE MOVE
				simulate();
			}
		}
		
		// MOUSE BEING RELEASED
		if(mouse.pressed == false) {
			if(activeP != null) {
				if(validSquare) { //CONFIRMED MOVE
					
					//UPDATE THE PIECE LIST IN CASE A PIECE HAS BEEN CAPTURED AND REMOVED DURING SIMULATION
					copyPieces(simPieces, pieces);
					activeP.updatePosition();
					if(castlingP !=null) {
						castlingP.updatePosition();
					}
					if(isKingInCheck() && isCheckmate()) {
						gameOver = true;
						
					}else if(isStalemate() && isKingInCheck() == false) {
						stalemate = true;
					}
					else { //The game is still going on
						if(canPromote()) {
							promotion = true;
						}else {
							changePlayer();
						}
					}
				}else {
					//THE MOVE IS NOT VALID SO RESET EVERYTHING
					copyPieces(pieces, simPieces);
					activeP.resetPosition();
				activeP = null;
				}
			}
		}
		}
	}
	public void simulate() {
		
		canMove = false;
		validSquare = false;
		
		//RESET THE PIECE LIST IN EVERY LOOP
		//THIS IS FOR RESTORING THE REMOVED PIECE DURING SIMULATION
		copyPieces(pieces, simPieces);
		
		//RESET the castling piece's position
		if(castlingP != null) {
			castlingP.col = castlingP.preCol;
			castlingP.x = castlingP.getX(castlingP.col);
			castlingP = null;
		}
		
		//IF A PIECE IS BEING HELD, UPDATE ITS POSITION
		activeP.x = mouse.x - TheBoard.HALF_SQUARE_SIZE;
		activeP.y = mouse.y - TheBoard.HALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);
		
		//CHECK IF THE PIECE IS HOVERING OVER A REACHABLE SQUARE
		if(activeP.canMove(activeP.col, activeP.row)) {
			
			canMove = true;
			//IF HITTING A PIECE THEN REMOVE IT FROM THE LIST
			if(activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP.getIndex());
			}
			checkCastling();
			
			if(isIllegal(activeP) == false && opponentCanCaptureKing() == false) {
				validSquare = true;
			}
		}
	}
	private boolean isStalemate() {
		int count = 0;
		//Count the number of pieces
		
		for(Piece piece : simPieces) {
			if(piece.color != currentColor) {
				count++;
			}
		}
		
		//If only the king is left
		if(count == 1) {
			if(kingCanMove(getKing(true)) == false) {
				return true;
			}
		}
		return false;
	}
	private boolean isIllegal(Piece king) {
		if(king.type == Type.KING) {
			for(Piece piece : simPieces) {
				if(piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean opponentCanCaptureKing() {
		
		Piece king = getKing(false);
		
		for(Piece piece : simPieces) {
			if(piece.color != king.color && piece.canMove(king.col, king.row)) {
				return true;
			}
		}
		
		return false;
	}
	private boolean isKingInCheck() {
		
		Piece king  = getKing(true);
		if(activeP.canMove(king.col,king.row)) {
			checkingP = activeP;
			return true;
		}else {
			checkingP = null;
		}
		
		
		return false;
	}
	private Piece getKing(boolean opponent) {
		Piece king = null;
		for(Piece piece : simPieces) {
			if(opponent) {
				if(piece.type == Type.KING && piece.color != currentColor) {
					king = piece;
				}
			}else {
				if(piece.type == Type.KING && piece.color == currentColor) {
					king = piece;
				}
			}
		} return king;
	}
	private boolean isCheckmate() {
		
		Piece king = getKing(true);
		
		if(kingCanMove(king)) {
			return false;
		}else {
			//But you still have a chance, check if you can block the attack with a piece
			//Verify the position of the checking piece and the king in check
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);	
			
			if(colDiff == 0) {
				//The checking piece is attacking vertically
				if(checkingP.row < king.row) {
					//The checking piece is above the king
					
					for(int row = checkingP.row; row < king.row; row++) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				if(checkingP.row > king.row) {
					//The checking piece is below the king
					for(int row = checkingP.row; row > king.row; row--) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)) {
								return false;
							}
						}
					}
				}
				
			}else if(rowDiff == 0) {
				//The checking piece is attacking horizontally
				if(checkingP.col < king.col) {
					//The checking piece is attacking on the left
					
					for(int col = checkingP.col; col < king.col; col++) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if(checkingP.col > king.col) {
					//The checking piece is attacking on the right
					
					for(int col = checkingP.col; col > king.col; col--) {
						for(Piece piece : simPieces) {
							if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				
				
			}else if(colDiff == rowDiff) {
				//The checking piece is attacking diagonally
				if(checkingP.row < king.row) {
					//The checking piece is above the king
					if(checkingP.col < king.col) {
						//The checking piece is in the upper left
						for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
									return false;
								}
							}
						}
					}
					if(checkingP.col > king.col) {
						//The checking piece is in the upper right
						for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
									return false;
								}
							}
						}
					}
				}
				if(checkingP.row > king.row) {
					//The checking piece is below the king
					if(checkingP.col < king.col) {
						//The checking piece is in the lower left
						for(int col = checkingP.col, row = checkingP.row; col < king.col; col++, row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
									return false;
								}
							}
						}
						
					}
					if(checkingP.col > king.col) {
						//The checking piece is in the lower right
						for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col,row)) {
									return false;
								}
							}
						}
					}
				}
				
			}else {
				//The checking piece is Knight
				//Knight attack cannot be block by any piece, can remove this section
				
			}
		}
		
		return true;
	}
	private boolean kingCanMove(Piece king) {
		
		//Simulte if there is any square  where the king can move
		if(isValidMove(king, -1, -1)) { return true;}
		if(isValidMove(king, 0, -1)) { return true;}
		if(isValidMove(king, 1, -1)) { return true;}
		if(isValidMove(king, -1, 0)) { return true;}
		if(isValidMove(king, 1, 0)) { return true;}
		if(isValidMove(king, -1, 1)) { return true;}
		if(isValidMove(king, 0, 1)) { return true;}
		if(isValidMove(king, 1, 1)) { return true;}
		
		
		return false;
	}
	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
		
		boolean isValidMove = false;
		
		//Update the king's position for a second
		king.col += colPlus;
		king.row += rowPlus;
		
		if(king.canMove(king.col, king.row)) {
			if(king.hittingP != null) {
				simPieces.remove(king.hittingP.getIndex());
			}
			if(isIllegal(king) == false) {
				isValidMove = true;
			}
		}
		//Reset the king's position and restore the removed piece
		king.resetPosition();
		copyPieces(pieces, simPieces);
		
		return false;
	}
	private void checkCastling() {
		if(castlingP != null) {
			if(castlingP.col == 0) {
				castlingP.col += 3;
			}else if(castlingP.col == 7){
				castlingP.col -= 2;
			}
			castlingP.x = castlingP.getX(castlingP.col);
		}
	}
	public void changePlayer() {
		if(currentColor == WHITE) {
			currentColor = BLACK;
			
			//Reset black's twoStepped
			for(Piece piece : pieces) {
				if(piece.color == BLACK) {
					piece.twoStepped = false;
				}
			}
		}else {
			currentColor = WHITE;
			//Reset white's twoStepped
			for(Piece piece : pieces) {
				if(piece.color == WHITE) {
					piece.twoStepped = false;
				}
			}
		}
		activeP = null;
	}
	
	private boolean canPromote() {
		
		if(activeP.type == Type.PAWN) {
			if(currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
				promoPieces.clear();
				promoPieces.add(new Rook(currentColor,9,2));
				promoPieces.add(new Knight(currentColor,9,3));
				promoPieces.add(new Bishop(currentColor,9,4));
				promoPieces.add(new Queen(currentColor,9,5));
				return true;
				
			}
		}
		return false;
	}
	public void promoting() {
		if(mouse.pressed) {
			for(Piece piece : promoPieces) {
				if(piece.col == mouse.x/TheBoard.SQUARE_SIZE && piece.row == mouse.y/TheBoard.SQUARE_SIZE) {
					switch(piece.type) {
					case ROOK: simPieces.add(new Rook(currentColor, activeP.col, activeP.row)); break;
					case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row)); break;
					case BISHOP: simPieces.add(new Bishop(currentColor, activeP.col, activeP.row)); break;
					case QUEEN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row)); break;
					default: break;
					}
					simPieces.remove(activeP.getIndex());
					copyPieces(simPieces, pieces);
					activeP = null;
					promotion = false;
					changePlayer();
				}
			}
		}
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//BOARD
		Graphics2D g2 = (Graphics2D) g;
		board.draw(g2);
		//PIECES
		for(Piece p : simPieces) {
			p.draw(g2);
		}
		
		if(activeP != null) {
			if(canMove) {
				if(isIllegal(activeP) || opponentCanCaptureKing()) {
					g2.setColor(Color.gray);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
					g2.fillRect(activeP.col*TheBoard.SQUARE_SIZE, activeP.row*TheBoard.SQUARE_SIZE, TheBoard.SQUARE_SIZE, TheBoard.SQUARE_SIZE);
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}else {
				g2.setColor(Color.white);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g2.fillRect(activeP.col*TheBoard.SQUARE_SIZE, activeP.row*TheBoard.SQUARE_SIZE, TheBoard.SQUARE_SIZE, TheBoard.SQUARE_SIZE);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}
			}
			
			//DRAW THE ACTIVE PIECE IN THE END SO IT WONT BE HIDDEN BY THE BOARD OR THE COLORED SQUARE
			activeP.draw(g2);
		}
		
		//MESSAGES
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(new Font("Arial", Font.PLAIN, 20));
		g2.setColor(Color.white);
		
		if(promotion) {
			g2.drawString("Promote to:", 640, 150);
			for(Piece piece : promoPieces) {
				g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row), TheBoard.SQUARE_SIZE, TheBoard.SQUARE_SIZE, null);
			}
		}else {
			if(currentColor == WHITE) {
				g2.drawString("White's turn", 640, 500);
				if(checkingP != null && checkingP.color == BLACK) {
					g2.setColor(Color.red);
					g2.drawString("The king", 640, 550);
					g2.drawString("is in check", 640, 580);
				}
			}else {
				g2.drawString("Black's turn", 640, 100);
				if(checkingP != null && checkingP.color == WHITE) {
					g2.setColor(Color.red);
					g2.drawString("The king", 640, 150);
					g2.drawString("is in check", 640, 180);
				}
			}
		}
		if(gameOver) {
			String s = "";
			if(currentColor == WHITE) {
				s = "White Wins";
			}else {
				s = "Black Wins";
			}
			g2.setFont(new Font("Arial", Font.PLAIN, 40));
			g2.setColor(Color.GREEN);
			g2.drawString(s, 200, 420);
		}
		if(stalemate) {
			g2.setFont(new Font("Arial", Font.PLAIN, 40));
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawString("Stalemate", 200, 420);
		}
	}
	
	public void setPieces() {
		//WHITE
		pieces.add(new Pawn(WHITE,0,6));
		pieces.add(new Pawn(WHITE,1,6));
		pieces.add(new Pawn(WHITE,2,6));
		pieces.add(new Pawn(WHITE,3,6));
		pieces.add(new Pawn(WHITE,4,6));
		pieces.add(new Pawn(WHITE,5,6));
		pieces.add(new Pawn(WHITE,6,6));
		pieces.add(new Pawn(WHITE,7,6));

		pieces.add(new Rook(WHITE,0,7));
		pieces.add(new Rook(WHITE,7,7));
		pieces.add(new Knight(WHITE,1,7));
		pieces.add(new Knight(WHITE,6,7));
		pieces.add(new Bishop(WHITE,2,7));
		pieces.add(new Bishop(WHITE,5,7));
		pieces.add(new Queen(WHITE,3,7));
		pieces.add(new King(WHITE,4,7));
		// BLACK
		pieces.add(new Pawn(BLACK, 0, 1));
		pieces.add(new Pawn(BLACK, 1, 1));
		pieces.add(new Pawn(BLACK, 2, 1));
		pieces.add(new Pawn(BLACK, 3, 1));
		pieces.add(new Pawn(BLACK, 4, 1));
		pieces.add(new Pawn(BLACK, 5, 1));
		pieces.add(new Pawn(BLACK, 6, 1));
		pieces.add(new Pawn(BLACK, 7, 1));

		pieces.add(new Rook(BLACK, 0, 0));
		pieces.add(new Rook(BLACK, 7, 0));
		pieces.add(new Knight(BLACK, 1, 0));
		pieces.add(new Knight(BLACK, 6, 0));
		pieces.add(new Bishop(BLACK, 2, 0));
		pieces.add(new Bishop(BLACK, 5, 0));
		pieces.add(new Queen(BLACK, 3, 0));
		pieces.add(new King(BLACK, 4, 0));
	}
	
	public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		target.clear();
		for(int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}
}
