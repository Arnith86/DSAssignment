package javaClient;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JavaClientGui {
	private JFrame applicationFrame;
	// the name should be changed to somthing more apropriet
	private JPanel panel;
	private JPanel queuePanel; 
	
	
	private JLabel testLabel = new JLabel("TEst TExt"); 
	public JavaClientGui () {
		applicationFrame = new JFrame("Queue");
		panel = new JPanel(); 
		// not used yet
		// 	panel.setLayout(new BorderLayout(, BoxLayout.Y_AXIS));
		
		applicationFrame.setSize(800, 800);
		applicationFrame.setVisible(true);
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
}
