package chessPiece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece{

	public Pawn(int color,int col, int row) {
		super(color,col,row);
		
		type = Type.PAWN;
		
		if(color == GamePanel.WHITE) {
			image = getImage("/pieces/WHITE pawn");
		}else {
			image = getImage("/pieces/BLACK pawn");
		}
	}
	public boolean canMove(int targetCol, int targetRow) {
		if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
		
			//Move depending on the color
			int moveValue;
			if(color == GamePanel.WHITE) {
				moveValue = -1;
			}else {
				moveValue = 1;
			}
			
			//Check the hitting piece
			hittingP = getHittingP(targetCol, targetRow);
			
			//One square movement
			if(targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
				return true;
			}
			//Two square movement
			if(targetCol == preCol && targetRow == preRow + moveValue*2 && hittingP == null && moved == false && pieceIsOnStraightLine(targetCol, targetRow) == false) {
				return true;
			}
			
			//Capturing pieces
			if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null && hittingP.color != color) {
				return true;
			}
			//En Passant
			if(Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
						hittingP = piece;
						return true;
					}
				}
			}
		}return false;
	}
			
}
