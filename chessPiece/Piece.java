package chessPiece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.Type;

import main.GamePanel;
import main.TheBoard;

public class Piece {

	public Type type;
	public BufferedImage image;
	public int x, y;
	public int col, row, preCol, preRow;
	public int color;
	public Piece hittingP;
	public boolean moved, twoStepped;
	
	public Piece(int color, int col, int row) {
		this.color = color;
		this.col = col;
		this.row = row;
		x = getX(col);
		y = getY(row);
		preCol = col;
		preRow = row;
	}
	
	public BufferedImage getImage(String imagePath) {
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
			
		}catch(IOException e) {
			e.printStackTrace();
		}return image;
	}
	public int getX(int col) {
		return col * TheBoard.SQUARE_SIZE;
	}
	public int getY(int row) {
		return row * TheBoard.SQUARE_SIZE;
	}
	public int getCol(int x) {
		return (x + TheBoard.HALF_SQUARE_SIZE)/TheBoard.SQUARE_SIZE;
	}
	public int getRow(int y) {
		return (y + TheBoard.HALF_SQUARE_SIZE)/TheBoard.SQUARE_SIZE;
	}
	public int getIndex() {
		for(int index = 0; index  < GamePanel.simPieces.size(); index++) {
			if(GamePanel.simPieces.get(index)== this) {
				return index;
			}
		}
		return 0;
	}
	public void updatePosition() {
		// Chech En Passant
		if(type == Type.PAWN) {
			if(Math.abs(row - preRow) == 2) {
				twoStepped = true;
			}
		}
		x = getX(col);
		y = getY(row);
		preCol = getCol(x);
		preRow = getRow(y);
		moved = true;
	}
	public void resetPosition() {
		col = preCol;
		row = preRow;
		x = getX(col);
		y = getY(row);
	}
	public boolean canMove(int targetCol, int targetRow) {
		return false;
	}
	public boolean isWithinBoard(int targetCol, int targetRow) {
		if(targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
			return true;
		}
		return false;
	}
	
	public Piece getHittingP(int targetCol, int targetRow) {
		for(Piece piece : GamePanel.simPieces) {
			if(piece.col == targetCol && piece.row == targetRow && piece != this) {
				return piece;
			}
		}return null;
	}
	public boolean isValidSquare(int targetCol, int targetRow) {
		
		hittingP = getHittingP(targetCol, targetRow);
		
		if(hittingP == null) { // THE SQUARE IS VACANT
			return true;
		}else { // THE SQUARE IS OCCUPIED
			if(hittingP.color != this.color) { // IF THE COLOR IS DIFFERENT, YOU CAN CAPTURE IT
				return true;
			}else {
				hittingP = null;
			}
		}return false;
	}
	public boolean isSameSquare(int targetCol, int targetRow) {
		if(targetCol == preCol && targetRow == preRow) {
			return true;
		}return false;
	}
	public boolean pieceIsOnStraightLine(int targetCol, int targetRow) {
		//When piece is moving left
		for(int i = preCol-1; i > targetCol; i--) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == i && piece.row == targetRow) {
					hittingP = piece;
					return true;
				}
			}
		}
		//When piece is moving right
		for(int i = preCol+1; i < targetCol; i++) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == i && piece.row == targetRow) {
					hittingP = piece;
					return true;
				}
			}
		}
		//When piece is moving up
		for(int i = preRow-1; i > targetRow; i--) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == targetCol && piece.row == i) {
					hittingP = piece;
					return true;
				}
			}
		}
		//When piece is moving down
		for(int i = preRow+1; i < targetRow; i++) {
			for(Piece piece : GamePanel.simPieces) {
				if(piece.col == targetCol && piece.row == i) {
					hittingP = piece;
					return true;
				}
			}
		}
		return false;
	}
	public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow) {
		
		if(targetRow < preRow) {
			//Up left
			for(int i = preCol-1; i > targetCol; i--) {
				int diff = Math.abs(i - preCol);
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == i && piece.row == preRow - diff) {
						hittingP = piece;
						return true;
					}
				}
			}
			
			//Up right
			for(int i = preCol+1; i < targetCol; i++) {
				int diff = Math.abs(i - preCol);
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == i && piece.row == preRow - diff) {
						hittingP = piece;
						return true;
					}
				}
			}
		}
		if(targetRow > preRow) {
			//Down left
			for(int i = preCol-1; i > targetCol; i--) {
				int diff = Math.abs(i - preCol);
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == i && piece.row == preRow + diff) {
						hittingP = piece;
						return true;
					}
				}
			}
			
			//Down right
			for(int i = preCol+1; i < targetCol; i++) {
				int diff = Math.abs(i - preCol);
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == i && piece.row == preRow + diff) {
						hittingP = piece;
						return true;
					}
				}
			}
		}
		return false;
	}
	public void draw(Graphics2D g2) {
		g2.drawImage(image, x, y, TheBoard.SQUARE_SIZE, TheBoard.SQUARE_SIZE, null);
	}
}
