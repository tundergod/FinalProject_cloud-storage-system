import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.io.*;
import java.net.*;
import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.netty.session.ProtocolException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;
import java.util.Random;

public class Login_System {
	private JFrame frmFSuperStorage;
	private JTextField ID;
	private JPasswordField PASSWORD;
	private JLabel yoyo;
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login_System window = new Login_System();
					window.frmFSuperStorage.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	 //Create the application.
	public Login_System() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmFSuperStorage = new JFrame();
		frmFSuperStorage.setTitle("Storage System!");
		frmFSuperStorage.setBounds(100, 100, 299, 181);
		frmFSuperStorage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmFSuperStorage.getContentPane().setLayout(null);
		
		ID = new JTextField();
		ID.setBounds(119, 22, 145, 20);
		frmFSuperStorage.getContentPane().add(ID);
		ID.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("ID:");
		lblNewLabel.setBounds(31, 24, 62, 17);
		frmFSuperStorage.getContentPane().add(lblNewLabel);
		
		PASSWORD = new JPasswordField();
		PASSWORD.setBounds(119, 53, 145, 20);
		frmFSuperStorage.getContentPane().add(PASSWORD);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(27, 55, 82, 17);
		frmFSuperStorage.getContentPane().add(lblPassword);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				/*
				 * 		checking login ID and Password
				 */
				String id = ID.getText();
				String pass = PASSWORD.getText();
				try{
					Socket s = new Socket("localhost",8080);
					DataOutputStream out = new DataOutputStream(s.getOutputStream());
					out.writeInt(5);
					out.writeUTF(id);
					out.flush();
					out.close();
					s.close();
			
					go(id,pass);
				}catch (IOException e){
		            e.printStackTrace();
		        }
			}
			
		});
		btnLogin.setBounds(167, 94, 98, 34);
		frmFSuperStorage.getContentPane().add(btnLogin);
		
		JButton Register = new JButton("Register");
		Register.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//frame.dispose();
				Register_System register = new Register_System();
				register.setVisible(true);
			}
		});
		Register.setBounds(31, 94, 98, 34);
		frmFSuperStorage.getContentPane().add(Register);
		
	}
	void go(String id,String pass){
		try{
			Socket s = new Socket("localhost",6666);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			byte[] key = pass.getBytes();
			byte[] password = new byte[16];
			if(key.length<=16){
				for(int i=0;i<key.length;i++){
					password[i]=key[i];
				}
				if(password.length<16){
					for(int i=password.length;i<16;i++){
						password[i]=1;
					}
				}
				out.write(password);
				out.flush();
				int action = in.readInt();
				in.close();
				out.close();
				s.close();
				if(action==0){
					JOptionPane.showMessageDialog(null, "ID or PASSWORD incorrect!");
				}
				else{
					JOptionPane.showMessageDialog(null, "LOGIN SUCCESS!");
					frmFSuperStorage.dispose();
					Storage_System system = new Storage_System(id,pass);
					system.setVisible(true);
				}
			}
			else{
				for(int i=0;i<16;i++){
					password[i]=key[i];
				}
				out.write(password);
				int action = in.readInt();
				out.flush();
				in.close();
				out.close();
				s.close();
				if(action==0){
					JOptionPane.showMessageDialog(null, "ID or PASSWORD incorrect!");
				}
				else{
					JOptionPane.showMessageDialog(null, "LOGIN SUCCESS!");
					frmFSuperStorage.dispose();
					Storage_System system = new Storage_System(id,pass);
					system.setVisible(true);
				}
			}
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}