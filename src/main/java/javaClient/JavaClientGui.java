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
	private JPanel newQueueEntry; 
	
	
	public JavaClientGui () {
		
		applicationFrame = new JFrame("Queue");
		
		// TOP PANEL  
		// contains, instructions and name input	
		inputPanel = new JPanel(); 
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		
		instructionText = new JLabel("Supply your name and press the send button!");
		
		// Panel containing Name input field and send button 
		// will be placed under the instruction panel
		textInputPanel = new JPanel();
		nameInput = new JTextField("Name");
		sendButton = new JButton("Send");
		
		textInputPanel.add(nameInput);
		textInputPanel.add(sendButton);
		
		inputPanel.add(instructionText);
		inputPanel.add(textInputPanel);
		
		// BOTTOM PANEL 
		// Contains the current queue
		
		queuePanel = new JPanel();
		queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
		
		// Used for testing, will most likely not remain here. 
		for (String string : testArray) {
			newQueueEntry = new JPanel(); 
			newQueueEntry.add(new JLabel(string));
			queuePanel.add(newQueueEntry);
			// queuePanel.add(new JLabel(string));
		}
		
		
		// Frame setup
		applicationFrame.add(inputPanel, BorderLayout.NORTH);
		applicationFrame.add(queuePanel, BorderLayout.CENTER);
		// Some more configuration might be needed 
		applicationFrame.setSize(400, 800);
		applicationFrame.setVisible(true);
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}
	
}
