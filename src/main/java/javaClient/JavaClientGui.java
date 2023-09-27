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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class JavaClientGui implements ActionListener{

	// variables used during execution 
	protected String kindOfClient;
	private String supervisorString = "supervisor";
	private String clientString = "student";
	private String portError = "PortError"; 
	private String turn = "turn";
	private String reconnectNotify = "reconnectNotify";
	private String noAddress = "noAddress";
	private Boolean notificationSent = false; 
	private String supervisorStatusInputArray[] = {"pending", "available","occupied"};
	protected String currentSupervisorStatus="pending";  
	private static String user=""; 
	private static String serverAddress;
	private static int inPort; 
	private static int outPort; 
	private Boolean fullAddressSupplied;

	// used to get access to the backend code for the client
	private JavaClient javaClient;
	private SupervisorJavaClient supervisorJavaClient; 
	 
  
	
	private JFrame applicationFrame;
	
	// used in the top part of the gui
	private JPanel inputPanel;
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

	// used in the supervisor version of the gui 
 	private JPanel supervisorInputPanel;
	private JLabel availableSupervisors; 	
	private JTextField messageInputField;
	private JButton applyMessageButton; 
	private JButton nextStudentButton;
	private JComboBox<String> supervisorStatusInput;

	// used to build the gui
	private JPanel supervisorsPanel;
	private JLabel supervisorMessageLable = new JLabel();    
	
	private JPanel centerPanel;
	
	private JPanel queuePanel; 
	private JPanel newQueueEntry;
	private JLabel studentLabel;  
	

	// Gets notified of which version of the client that is used
	public void setSupervisorClientObject(SupervisorJavaClient supervisorJavaClient){
		this.supervisorJavaClient = supervisorJavaClient;
	}
	public void setClientObject(JavaClient client){
		this.javaClient = client;
	}
	
	// Builds the GUI 
	public JavaClientGui (String kindOfClient) {

		fullAddressSupplied = false; 
		this.kindOfClient = kindOfClient;
		
		if(kindOfClient.equals(supervisorString)){
			applicationFrame = new JFrame("Supervisor");	
		} else {
			applicationFrame = new JFrame("Queue");
		}
		
		
		// TOP PANEL  
		// contains, instructions, address inputs and name input	
		inputPanel = new JPanel(); 
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		inputPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
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
		
		//supervisor specific panel
		if(kindOfClient.equals(supervisorString)){
			supervisorInputPanel = new JPanel();
			
			supervisorStatusInput = new JComboBox<>(supervisorStatusInputArray);
			applyMessageButton = new JButton("Apply");		
			nextStudentButton = new JButton("Next Student");

			messageInputField = new JTextField("where should the students go?");
			supervisorInputPanel.add(supervisorStatusInput);
			supervisorInputPanel.add(messageInputField);
			supervisorInputPanel.add(applyMessageButton);
			supervisorInputPanel.add(nextStudentButton);
			
		}
		
		inputPanel.add(addressPanel);
		inputPanel.add(textInputPanel);
		if(kindOfClient.equals(supervisorString)){inputPanel.add(supervisorInputPanel);}

		// CENTER PANEL - top part  
		// contains current supervisors and there messages 
		supervisorsPanel = new JPanel();
		supervisorsPanel.setLayout(new BoxLayout(supervisorsPanel, BoxLayout.Y_AXIS)); 
		
		
		// CENTER PANEL - bottom part
		// Contains the current queue
		queuePanel = new JPanel();
		queuePanel.setBorder(BorderFactory.createLineBorder(Color.black));
		queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.Y_AXIS));
		
		// CENTER PANEL - Setup
		centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
 		centerPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Frame setup
		applicationFrame.add(inputPanel, BorderLayout.NORTH);
		applicationFrame.add(centerPanel, BorderLayout.CENTER);
		// Some more configuration might be needed 
		if(kindOfClient.equals(supervisorString)){
			applicationFrame.setSize(500, 800);
		} else {
			applicationFrame.setSize(400, 800);
		}
		applicationFrame.setVisible(true);
		applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// actionlistners to varius inputs
		addressInput.addActionListener(this);
		sendButton.addActionListener(this);
		nameInput.addActionListener(this);
		connectButton.addActionListener(this);
		if(kindOfClient.equals(supervisorString)){
			nextStudentButton.addActionListener(this);
			supervisorStatusInput.addActionListener(this);
			messageInputField.addActionListener(this);
			applyMessageButton.addActionListener(this);
		}
	}

	// Builds the student queue
	public void setStudentQueue(LinkedList<Students> studentList){
		
		queuePanel.removeAll();
	
		if (studentList.isEmpty()) {

			studentLabel = new JLabel("There are no students in the queue!");
			newQueueEntry = new JPanel(); 
			newQueueEntry.add(studentLabel);
			queuePanel.add(newQueueEntry);
		
		} else {

			studentList.forEach(students -> {
				studentLabel = new JLabel(students.getName());
				
				if (students.getName().equals(user) && kindOfClient.equals(clientString)) {
					// Underline the label
					Font font = studentLabel.getFont();
					Map attributes = font.getAttributes();
					attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
					studentLabel.setFont(font.deriveFont(attributes));
				}
				
				newQueueEntry = new JPanel(); 
				newQueueEntry.add(studentLabel);
				queuePanel.add(newQueueEntry);
			});
		}
		
		centerPanel.add(queuePanel);
		// this will reprint the applicationFrame for bouth setStudentQueue and setCurrentSupervisors
		applicationFrame.revalidate(); 
		applicationFrame.repaint();
	}

	
	// Builds the Supevisor queue, and handles the notification to the students
	public void setCurrentSupervisors(LinkedList<Supervisors> superervisorList){
		
		supervisorsPanel.removeAll();
		supervisorMessageLable.setText("");;
		if(superervisorList.size() == 0){
			
			availableSupervisors = new JLabel("There are no supervisors:");
			supervisorsPanel.add(availableSupervisors);

		} else {
			
			superervisorList.forEach(superervisors -> {
				
				if(superervisors.getStudentName().equals("undefined")){
					availableSupervisors = new JLabel(superervisors.getSupervisorName()+": "+superervisors.getStatus());
				} else {
					availableSupervisors = new JLabel(superervisors.getSupervisorName()+": "+superervisors.getStatus()+ "  - "+"Helping: "+superervisors.getStudentName());
				}

				if((superervisors.getSupervisorMessage() != null && kindOfClient.equals(clientString))){
					
					supervisorMessageLable.setText(superervisors.getSupervisorMessage());

					// notifys student that its his turn
					if(notificationSent==false){
						notifications(turn);
						notificationSent = true;
					} 
				}	
				supervisorsPanel.add(availableSupervisors);
				
			});
		}	
		centerPanel.add(supervisorMessageLable);
		centerPanel.add(supervisorsPanel);
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		 
		// registers name, and initiates placement in queue
		if ((e.getSource().equals(sendButton)) || (e.getSource().equals(nameInput))) {
			
			if(fullAddressSupplied == true){
				String gatherdString = nameInput.getText();
				// Remove trailing spaces
				this.user = gatherdString.trim().replaceAll("\\s+$", "");
				
				if(kindOfClient.equals(supervisorString)){
					supervisorJavaClient.setUser(user, currentSupervisorStatus);
					supervisorJavaClient.placeInSupervisorQueue();
				} else {
					javaClient.setUser(user);
					javaClient.placeInQueue();
					this.notificationSent = false;
				}
			} else {
				notifications(noAddress);
			}
		}

		// regesters the values of the server address and selected ports
		if ((e.getSource().equals(connectButton)) || (e.getSource().equals(addressInput))) {
			
		
			this.serverAddress = addressInput.getText();
						
			try {
				this.inPort = Integer.valueOf(portInInput.getText()); 	
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				notifications(portError);
			} 

			try {
				this.outPort = Integer.valueOf(portOutInput.getText()); 
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
				notifications(portError);
			}

			if(kindOfClient.equals(supervisorString)){
					supervisorJavaClient.setAddressAndPorts(serverAddress, inPort, outPort);
				} else {
					javaClient.setAddressAndPorts(serverAddress, inPort, outPort);
				}
			
			fullAddressSupplied = true;
		}

		// used to get the next student in the queue
		if(e.getSource().equals(nextStudentButton)){
			if(!user.isBlank()){
				supervisorJavaClient.takeOnAStudent();
			}
		}
		
		// registers changes to the supervisors status 
		if(e.getSource().equals(supervisorStatusInput)){
			if(!user.isBlank()){
				supervisorJavaClient.changeSupervisorStatus(supervisorStatusInput.getSelectedItem().toString());
			}	
		}

		// regesters the currently written supervisor message
		if(e.getSource().equals(applyMessageButton) || e.getSource().equals(messageInputField)){
			if(!user.isBlank()){
				supervisorJavaClient.registerSupervisorMessage(messageInputField.getText());	
			}
		}
	}

	// contains some popup notifications
	protected void notifications(String kind){
		
		if(kind.equals(reconnectNotify)){
			JOptionPane.showMessageDialog(
				null,             
				"Lost the connection, attempting to reconnect!",   
				"Lost connection!",   
				JOptionPane.INFORMATION_MESSAGE
			);
			JOptionPane.getRootFrame().setAlwaysOnTop(true);	
		}

		if(kind.equals(turn)){
			JOptionPane.showMessageDialog(
				null,             
				"It's Your Turn Now!",   
				"notification",   
				JOptionPane.INFORMATION_MESSAGE
			);
			JOptionPane.getRootFrame().setAlwaysOnTop(true);	
		}			
		
		if(kind.equals(portError)){
			JOptionPane.showMessageDialog(
			null,             
			"Port numbers cannot contain letters!",   
			"Port error",   
			JOptionPane.INFORMATION_MESSAGE
			);
			JOptionPane.getRootFrame().setAlwaysOnTop(true);
		}

		if(kind.equals(noAddress)){
			JOptionPane.showMessageDialog(
			null,             
			"server information must first be supplied!",   
			"No server address",   
			JOptionPane.INFORMATION_MESSAGE
			);
			JOptionPane.getRootFrame().setAlwaysOnTop(true);
		}
	} 
}
