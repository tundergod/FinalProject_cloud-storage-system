import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Scanner;
import java.io.*;
import java.net.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.Font;
import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.netty.session.ProtocolException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.register.EnhancedProfileRegistrationClient;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import java.util.Random;

public class Storage_System<E> extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Storage_System frame = new Storage_System();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public Storage_System(String id,String password) {

		byte[] mainiv = "0987654321098765".getBytes();
		byte[] maink = new byte[16];
		byte[] pass = password.getBytes();
		if(pass.length<=16){
			for(int i=0;i<pass.length;i++)
				maink[i]=pass[i];
			if(maink.length<16){
				for(int i=maink.length;i<16;i++)
					maink[i]=1;
			}
		}
		else{
			for(int i=0;i<16;i++)
				maink[i]=pass[i];
		}

		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Storage System!");
		setBounds(100, 100, 390, 298);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JList list = new JList();
		list.setBounds(33, 96, 160, 143);
		contentPane.add(list);
		
		
		/*
		 * Delete item
		 */
		JButton Delete = new JButton("Delete");
		Delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String delete_item = (String)list.getSelectedValue();
				int action = JOptionPane.showConfirmDialog(null,"Delete "+delete_item+"?","Delete",JOptionPane.YES_NO_OPTION);
				if(action==0){
					try{
						Socket s = new Socket("localhost",8080);
						DataOutputStream out = new DataOutputStream(s.getOutputStream());
						out.writeInt(3);
						out.writeUTF(id);
						out.flush();
						out.close();
						s.close();
						Delete_item(delete_item,id);
						
					}catch (IOException e1){
			            e1.printStackTrace();
			        }	
					try{
						Socket s = new Socket("localhost",8080);
						DataOutputStream out = new DataOutputStream(s.getOutputStream());
						out.writeInt(2);
						out.writeUTF(id);
					
						out.close();
						s.close();
					
						get_list(list);
					}catch ( Exception e1 ){
						e1.printStackTrace();
					}
				}
			}
		});
		Delete.setFont(new Font("Tahoma", Font.PLAIN, 14));
		Delete.setBounds(221, 204, 114, 35);
		contentPane.add(Delete);
		
		
//download
		JButton Download = new JButton("Download");
		Download.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String download_item = (String)list.getSelectedValue();
				int action;
				if(download_item!=null){
					action = JOptionPane.showConfirmDialog(null,"Download the item "+download_item+"?","Download",JOptionPane.YES_NO_OPTION);
					if(action==0){
						try{
							File user_file = new File(id);
							String key_file = new String(user_file.getAbsolutePath()+"\\"+download_item);
							File sk_text = new File(key_file);
							FileInputStream in_sk = new FileInputStream(sk_text);
							byte[] sk = new byte[16];
							byte[] iv = new byte[16];
							byte[] read = new byte[65];
							byte[] skandiv = new byte[64];
							int i;
							int textlength = 0;
							while(in_sk.available()>0){
								int readin = in_sk.read(read);
								textlength = read[64];
								
								for(i=0;i<64;i++)
									skandiv[i]=read[i];
								byte[] dec_skiv = CipherUtil.authDecrypt(maink,mainiv,skandiv);	//decrypt the sk and iv
								
								/*
								 * 	get sk and iv
								 */
								
								for(i=31;i>=16;i--)
									iv[i-16]=dec_skiv[i];
								for(i=15;i>=0;i--)
									sk[i]=dec_skiv[i];
								
								//connect to server and get the download item
								Socket s = new Socket("localhost",8080);
								DataOutputStream out = new DataOutputStream(s.getOutputStream());
								out.writeInt(1);
								out.writeUTF(id);
								out.flush();
								out.close();
								s.close();
							}
							in_sk.close();
							Download d = new Download(download_item,sk,iv,textlength);
							
						}catch(Exception e){ 
				            e.printStackTrace(); 
				        }
						try{
							Socket s = new Socket("localhost",8080);
							DataOutputStream out = new DataOutputStream(s.getOutputStream());
							out.writeInt(2);
							out.writeUTF(id);
						
							out.close();
							s.close();
						
							get_list(list);
						}catch ( Exception e ){
							e.printStackTrace();
						}
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Please Select a item to download");
			}
		});
		Download.setBounds(221, 158, 114, 35);
		contentPane.add(Download);

//upload
		JButton Upload = new JButton("Upload");
		Upload.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				JFileChooser fc_up = new JFileChooser();
				fc_up.setDialogTitle("Upload");
				fc_up.setApproveButtonText("Upload");
				int action = fc_up.showOpenDialog(null);	//get action
				if(action == JFileChooser.APPROVE_OPTION){
					File f = fc_up.getSelectedFile();
					File user_file = new File(id);
					user_file.mkdir();
					int mode=1;
					if(user_file.isDirectory()){
						String[] ss = user_file.list();
						for(int i=0;i<ss.length;i++){
							if((f.getName()).equals(ss[i])){
								mode=0;
								break;
							}
						}
					}
					if(mode==0){
						int act = JOptionPane.showConfirmDialog(null,"The item \""+f.getName()+"\" is already existed. Do you want to replace it?","Replace",JOptionPane.YES_NO_OPTION);
						if(act==0){
							Upload u = new Upload(f,id,maink,mainiv);
						}
					}
					else{
						Upload u = new Upload(f,id,maink,mainiv);
					}
					try{
						Socket s = new Socket("localhost",8080);
						DataOutputStream out = new DataOutputStream(s.getOutputStream());
						out.writeInt(2);
						out.writeUTF(id);
					
						out.close();
						s.close();
					
						get_list(list);
					}catch ( Exception e ){
						e.printStackTrace();
					}
					
				}
		}});
		Upload.setBounds(221, 112, 114, 35);
		contentPane.add(Upload);
		
		JLabel ID_label = new JLabel("");
		ID_label.setText("ID: "+id);
		ID_label.setBounds(14, 28, 143, 35);
		contentPane.add(ID_label);
	}

	public Storage_System() {
	}
	void get_list(JList list){
		try{
			Socket s = new Socket("localhost",5888);
			DataInputStream in = new DataInputStream(s.getInputStream());
			DefaultListModel DLM = new DefaultListModel();
			int check=0;
			while(true){
				String filename = new String(in.readUTF());
				if("-1".equals(filename))
					break;
				else{
					check=1;
					DLM.addElement(filename);
					list.setModel(DLM);
				}
			}
			list.setModel(DLM);
			in.close();
			s.close();
		}catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}

	void Delete_item(String delete_item,String id){
		try{
			Socket ss = new Socket("localhost",3333);
			File user_file = new File(id);
			String key_file = new String(user_file.getAbsolutePath()+"\\"+delete_item);
			File sk_text = new File(key_file);
			DataOutputStream output = new DataOutputStream(ss.getOutputStream());
			output.writeUTF(delete_item);
			output.flush();
			ss.close();		
			sk_text.delete();
			JOptionPane.showMessageDialog(null,delete_item+" deleted success!");
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}
class Upload{
	Upload(File f,String id,byte[] maink,byte[] mainiv){
		try{
			/*
			 * 	send the ID and mode to server
			 */
			Socket s = new Socket("localhost",8080);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			out.writeInt(0);
			out.writeUTF(id);
			out.close();
			s.close();
			
			/*
			 * 		Generate session key and upload
			 */
			EnhancedProfileManager profile = EZCardLoader.loadEnhancedProfile(new File("client.card"), "0000");
			EnhancedAuthSocketClient client = new EnhancedAuthSocketClient(profile);
		
			client.connect("localhost", 5471);
			client.doEnhancedKeyDistribution();
			client.doRapidAuthentication();
		
			EZCardLoader.saveEnhancedProfile(profile, new File("client.card"), "0000");

			byte[] k = client.getSessionKey().getKeyValue();
			byte[] sk = CipherUtil.copy(k, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(k, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			
			Random ran = new Random();
			byte[] file_key = new byte[16];
			byte[] file_iv = new byte[16];
			for(int i=0;i<16;i++){
				file_key[i]=(byte) ran.nextInt(1000000+1);
				file_iv[i]=(byte) ran.nextInt(1234555+i);
			}
			client.close();
			Client_Upload c = new Client_Upload(file_key,file_iv,f,id,maink,mainiv);
			
		}catch ( ProtocolException e ) {	
			System.out.println("[ProtocolException]" + e.getLocalizedMessage());
			if(e.getLocalizedMessage().contains("NoSuchIdentifierException"))
				System.out.println("Yours or server's EZCard may be outdated. Please consider register a new one or contact with server.");
		}catch ( Exception e ){
			e.printStackTrace();
		}
	}
}

class Download{
	Download(String filename,byte[] sk,byte[] iv,int textlength){
		JFileChooser j = new JFileChooser();
		j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		j.setDialogTitle("Download");
		j.setApproveButtonText("OK");
		int action = j.showOpenDialog(null);
		if(action==0){
			try{
				Socket s = new Socket("localhost",5888);
				DataOutputStream output = new DataOutputStream(s.getOutputStream());
				output.writeUTF(filename);
				output.flush();
				s.close();
				String path = j.getSelectedFile().toString();
				Thread.yield();
				go(path,filename,sk,iv,textlength);
			}catch (IOException e){
	            e.printStackTrace();
	        }
		}
	}
	void go(String path,String filename,byte[] sk,byte[] iv,int textlength){
		byte[] text = new byte[33];
		byte[] ciphertext = new byte[32];
		try{
			Socket s = new Socket("localhost",5777);
			BufferedInputStream in = new BufferedInputStream(s.getInputStream());
			FileOutputStream out = new FileOutputStream(path+"\\"+filename); 
			int readin;
			while((readin = in.read(text))!=-1){
				for(int i=0;i<32;i++)
					ciphertext[i]=text[i];
				try{
					byte dtext[]=CipherUtil.authDecrypt(sk,iv,ciphertext);
					if(text[32]==0)
						out.write(dtext);
					else{
						for(int i=0;i<textlength;i++)
							out.write(dtext[i]);
					}
				}catch (IntegrityCheckException e) {
					e.printStackTrace();
				}
			}
			out.flush();
			in.close();
			out.close();
			s.close();
			JOptionPane.showMessageDialog(null,filename+" Download successful!");
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class Client_Upload{	
	Client_Upload(byte[] sk,byte[] iv,File f,String id,byte[] maink,byte[] mainiv){
		byte[] text = new byte[8];
		String file = new String(f.toString());
		String filename = f.getName();
		int readin=0;
		try{
			Socket s = new Socket("localhost",5888);
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			out.writeUTF(filename);		
			while(in.available()>0){
				readin = in.read(text);
				out.writeInt(readin);
				byte[] ciphertext = CipherUtil.authEncrypt(sk, iv, text);
				out.write(ciphertext);
				Thread.yield();
			}
			out.close();
			in.close();
			s.close();
			get_key(f,id,readin,maink,mainiv,sk,iv);
		}catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}
	
	void get_key(File f,String id,int readin,byte[] maink,byte[] mainiv,byte[] sk,byte[] iv){
		try{
			byte[] skandiv = new byte[32];
			for(int i=0;i<32;i++){
				if(i<16)
					skandiv[i]=sk[i];
				else
					skandiv[i]=iv[i-16];
			}
			File user_file = new File(id);
			String key_file = new String(user_file.getAbsolutePath()+"\\"+f.getName());
			File sk_text = new File(key_file);
			sk_text.createNewFile();
			DataOutputStream out_sk = new DataOutputStream(new FileOutputStream(sk_text));
			byte[] cipher_skiv = CipherUtil.authEncrypt(maink , mainiv , skandiv);
			out_sk.write(cipher_skiv);
			out_sk.write(readin);
			out_sk.flush();
			out_sk.close();
			JOptionPane.showMessageDialog(null,f.getName()+" Upload successful!");
		}catch(Exception e){ 
            e.printStackTrace(); 
        } 
	}
}