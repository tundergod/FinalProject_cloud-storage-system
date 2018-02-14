import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;
import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.netty.session.ProtocolException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;

public class Register_System extends JFrame {

	private JPanel contentPane;
	private JTextField id_field;
	private JPasswordField pass_field;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Register_System frame = new Register_System();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Register_System() {
		setTitle("Register");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 332, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblId = new JLabel("ID:");
		lblId.setBounds(37, 11, 87, 28);
		contentPane.add(lblId);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setBounds(37, 59, 87, 28);
		contentPane.add(lblPassword);
		
		id_field = new JTextField();
		id_field.setBounds(134, 18, 129, 20);
		contentPane.add(id_field);
		id_field.setColumns(10);
		
		JButton btnNewButton = new JButton("Register");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String id = id_field.getText();
				String pass = pass_field.getText();
					try{
						Socket s = new Socket("localhost",8080);
						DataOutputStream out = new DataOutputStream(s.getOutputStream());
						out.writeInt(4);
						out.writeUTF(id);
						out.flush();
						out.close();
						s.close();
	
						go(id,pass);

					}catch (IOException e1){
			            e1.printStackTrace();
			        }
			}
		});
		btnNewButton.setBounds(144, 97, 98, 36);
		contentPane.add(btnNewButton);
		
		pass_field = new JPasswordField();
		pass_field.setBounds(134, 66, 129, 20);
		contentPane.add(pass_field);
		
		
	}
	void go(String id,String pass){
		try{
			Socket s = new Socket("localhost",2222);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			DataInputStream in = new DataInputStream(s.getInputStream());
			int check = in.readInt();
			if(check==0){
				JOptionPane.showMessageDialog(null, "\""+id+"\" existed!");
				out.close();
				in.close();
				s.close();
			}
			else{
				byte[] iv = "1234567890123456".getBytes();
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
					byte[] cipher = CipherUtil.authEncrypt(password, iv, password);

					out.write(cipher);
					out.flush();
					out.close();
					s.close();
				}
				else{
					for(int i=0;i<16;i++){
						password[i]=key[i];
					}
					byte[] cipher = CipherUtil.authEncrypt(password, iv, password);
					out.write(cipher);
					out.flush();
					out.close();
					s.close();
				}
				JOptionPane.showMessageDialog(null, "Register successful!");
			}
		}catch (IOException e1){
            e1.printStackTrace();
        }
	}
}
