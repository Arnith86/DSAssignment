package javaClient;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class JavaClientGui implements ActionListener{
	// used for test on  the GUI, should be removed when the implementation is finished
	
	private String user; 
	
	private JavaClient javaClient = new JavaClient(this);
	
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
		
		
		// Frame setup
		applicationFrame.add(inputPanel, BorderLayout.NORTH);
		applicationFrame.add(queuePanel, BorderLayout.CENTER);
		// Some more configuration might be needed 
		applicationFrame.setSize(400, 800);
		applicationFrame.setVisible(true);
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		sendButton.addActionListener(this);
	}
	
	public void setStudentQueue(LinkedList<Students> studentList){
		
		queuePanel.removeAll();
		
		studentList.forEach(students -> {
			//System.out.println(students.getName()+" "+ students.getTicket());   // HERE FOR TESTING REASONS to be removed before finish
			newQueueEntry = new JPanel(); 
			JLabel tempLabel = new JLabel(students.getName());
			
			if(students.getName().equals(user)) {
				
				// underlines the label 
				Font font = tempLabel.getFont();
				Map attributes = font.getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
				tempLabel.setFont(font.deriveFont(attributes));
				
			}
			
			newQueueEntry.add(tempLabel);
			queuePanel.add(newQueueEntry);
		});
		
		applicationFrame.revalidate(); 
		applicationFrame.repaint();
	}

	// 
	@Override
	public void actionPerformed(ActionEvent e) {
		// registers supplied name, and initiates placement in queue
		if (e.getSource().equals(sendButton)) {
			String textNameInput = nameInput.getText();
			this.user = textNameInput;
			javaClient.setUser(user);
		}
		
	}
	
}
