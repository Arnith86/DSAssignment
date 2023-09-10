package javaClient;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class JavaClientGui {
	// used for test on  the GUI, should be removed when the implementation is finished
	private String[] testArray = {"JP", "ADAM", "MARK", "SVEN", "MIST"};  
	
	
	private JLabel instructionText; 
	
	
	private JFrame applicationFrame;
	
	private JPanel inputPanel;
	private JPanel textInputPanel; 
	private JTextField nameInput; 
	private JButton sendButton; 
	
	
	private JPanel queuePanel; 
	
	
	
	public JavaClientGui () {
		
		applicationFrame = new JFrame("Queue");
		
			
		inputPanel = new JPanel(); 
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		instructionText = new JLabel("Supply your name and press the send button!");
		textInputPanel = new JPanel();
		nameInput = new JTextField("Name");
		sendButton = new JButton("Send");
		textInputPanel.add(nameInput);
		textInputPanel.add(sendButton);
		inputPanel.add(instructionText);
		inputPanel.add(textInputPanel);
		
		
		
		
		// not used yet
		// 	panel.setLayout(new BorderLayout(, BoxLayout.Y_AXIS));
		
		// Frame setup
		applicationFrame.add(inputPanel);
		// Some more configuration might be needed 
		applicationFrame.setSize(800, 800);
		applicationFrame.setVisible(true);
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
}
