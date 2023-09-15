package javaClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class JavaClientGui implements ActionListener{
	// used for test on  the GUI, should be removed when the implementation is finished
	
	private String user; 
	private String serverAddress;
	private int inPort; 
	private int outPort; 

	private Boolean fullAddressSupplied;

	private JavaClient javaClient = new JavaClient(this);
	
	private JLabel instructionText; 
	private JLabel availableSupervisors;   
	
	private JFrame applicationFrame;
	
	private JPanel inputPanel;
	// IMPLEMENT THE ADDRESS PANEL !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	private JPanel addressPanel;
	private JLabel serverLabel; 
	private JTextField addressInput;
	private JPanel portPanel;
	private JPanel inPortPanel;
	private JPanel outPortPanel; 
	private JLabel inPortLabel;
	private JLabel outPortLabel; 
	private JTextField portInInput;
	private JTextField portOutInput;
	private JButton connectButton; 

	private JPanel textInputPanel; 
	private JTextField nameInput; 
	private JButton sendButton;
	
	private JPanel supervisorsPanel;    
	
	private JPanel centerPanel;
	
	private JPanel queuePanel; 
	private JPanel newQueueEntry; 
	
	
	public JavaClientGui () {

		fullAddressSupplied = false; 

		
		applicationFrame = new JFrame("Queue");
		
		
		// TOP PANEL  
		// contains, instructions, address inputs and name input	
		inputPanel = new JPanel(); 
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		inputPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		instructionText = new JLabel("Supply your name and press the send button!");
		
		// Panel containing server address and port input fields and apply button
		addressPanel = new JPanel();
		serverLabel = new JLabel("Server: ");
		addressInput = new JTextField("Address");
		inPortLabel = new JLabel("Port in: ");
		portInInput = new JTextField("5555");
		outPortLabel = new JLabel("Port out: ");
		portOutInput = new JTextField("5556");
		connectButton = new JButton("Connect");

		inPortPanel = new JPanel();
		outPortPanel = new JPanel(); 
		inPortPanel.add(inPortLabel);
		inPortPanel.add(portInInput);
		outPortPanel.add(outPortLabel);
		outPortPanel.add(portOutInput);
		
		portPanel = new JPanel();
		portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.Y_AXIS));
		portPanel.add(inPortPanel);
		portPanel.add(outPortPanel);

		addressPanel.add(serverLabel);
		addressPanel.add(addressInput);
		addressPanel.add(portPanel);
		addressPanel.add(connectButton);

		// Panel containing Name input field and send button 
		// will be placed under the instruction panel
		textInputPanel = new JPanel();
		nameInput = new JTextField("Name");
		sendButton = new JButton("Send");
		
		textInputPanel.add(nameInput);
		textInputPanel.add(sendButton);
		
		inputPanel.add(addressPanel);
		//inputPanel.add(instructionText);
		inputPanel.add(textInputPanel);
		
		// CENTER PANEL - top part  
		// contains current supervisors and there messages 
		supervisorsPanel = new JPanel(); 
		supervisorsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		//availableSupervisors = new JLabel("supervisors");
		//supervisorsPanel.add(availableSupervisors);
		
		
		// CENTER PANEL - bottom part
		// Contains the current queue
		queuePanel = new JPanel();
		queuePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
		
		// CENTER PANEL - Setup
		centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		//centerPanel.add(availableSupervisors);
		
		// Frame setup
		applicationFrame.add(inputPanel, BorderLayout.NORTH);
		//applicationFrame.add(supervisorsPanel, BorderLayout.CENTER);
		applicationFrame.add(centerPanel, BorderLayout.CENTER);
		// Some more configuration might be needed 
		applicationFrame.setSize(400, 800);
		applicationFrame.setVisible(true);
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		sendButton.addActionListener(this);
		connectButton.addActionListener(this);
	}
	
	public void setStudentQueue(LinkedList<Students> studentList){
		
		queuePanel.removeAll();
		
		studentList.forEach(students -> {
			//System.out.println(students.getName()+" "+ students.getTicket());   // HERE FOR TESTING REASONS to be removed before finish
			newQueueEntry = new JPanel(); 
			JLabel tempLabel = new JLabel(students.getName());
			
			// is the supplied username is in the queue it will be underlined
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
		centerPanel.add(queuePanel);
		// this will reprint the applicationFrame for bouth setStudentQueue and setCurrentSupervisors
		applicationFrame.revalidate(); 
		applicationFrame.repaint();
	}

	public void setCurrentSupervisors(LinkedList<Supervisors> superervisorList){
		
		supervisorsPanel.removeAll();

		superervisorList.forEach(superervisors -> {
			
			if(superervisors.getStudentName().equals(null)){
				availableSupervisors = new JLabel(superervisors.getSupervisorName()+": Status: "+superervisors.getStatus());
			} else {
				availableSupervisors = new JLabel(superervisors.getSupervisorName()+": Helping: "+superervisors.getStudentName());
			}

			supervisorsPanel.add(availableSupervisors);
			
		});	

		centerPanel.add(supervisorsPanel);
	}

	// 
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// registers supplied name, and initiates placement in queue
		if (e.getSource().equals(sendButton)) {
			
			if(fullAddressSupplied == true){
				this.user = nameInput.getText();
				 
				// String textNameInput = nameInput.getText();
				// this.user = textNameInput;
				javaClient.setUser(user);
			}
		
		}

		// regesters the values for server address and selected ports
		if (e.getSource().equals(connectButton)) {
			
			// THESE INPUTS NEEDS TO BE FAULT TOLARENT !	
			// IT SHOULD HAVE AN WORKING IF STATEMENT THAT CHECKS THAT ALL INPUTS WERE RECIVED CORRECTLY 
			this.serverAddress = addressInput.getText();
						
			try {
				this.inPort = Integer.valueOf(portInInput.getText());    // NEEDS TO BE FAULT TOLARENT !	
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}

			try {
				this.outPort = Integer.valueOf(portOutInput.getText());    // NEEDS TO BE FAULT TOLARENT !	
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}

			javaClient.setAddressAndPorts(serverAddress, inPort, outPort);

			fullAddressSupplied = true; // THE BEFORE MENTIONED IF STATEMENT SHOULD BE TRUE BEFORE THIS IS APPLIED
		}
		
	}
	
}
