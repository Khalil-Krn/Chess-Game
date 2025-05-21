package main;

import javax.swing.JFrame;

public class Window {

	public static void main(String[] args) {
		
		JFrame window = new JFrame("ChessBoard");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		window.setVisible(true);
		
		//Add GamePanel to the window
		GamePanel gp = new GamePanel();
		window.add(gp);
		window.pack();
		
		gp.launchGame();
	}

}
