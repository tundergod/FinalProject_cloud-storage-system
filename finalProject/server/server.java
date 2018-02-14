import java.util.Scanner;
import java.io.*;
import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.Scanner;
import ezprivacy.netty.session.ProtocolException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.service.authsocket.AuthSocketServer;
import ezprivacy.service.authsocket.EnhancedAuthSocketServerAcceptor;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;
import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;
import javax.swing.JOptionPane;

public class server
{
	public static void main( String[] args ){
	while(true){
		try {
			System.out.println("Start Connecting.");
			
			//get the ID and mode
			ServerSocket s1;
			s1 = new ServerSocket(8080);
			Socket sock = s1.accept();			
			DataInputStream in = new DataInputStream(sock.getInputStream());
			int mode = in.readInt();
			String id = new String(in.readUTF());
			in.close();
			sock.close(); 
			s1.close();
			System.out.println("listening................");

			if(mode==0){
				EnhancedProfileManager profile = EZCardLoader.loadEnhancedProfile(new File("server.card"), "0000");
				EnhancedAuthSocketServerAcceptor serverAcceptor = new EnhancedAuthSocketServerAcceptor(profile);
				serverAcceptor.bind(5471);
				AuthSocketServer server = serverAcceptor.accept();
				server.waitUntilAuthenticated();
				EZCardLoader.saveEnhancedProfile(profile, new File("server.card"), "0000");
				byte[] k = server.getSessionKey().getKeyValue();
				byte[] sk = CipherUtil.copy(k, 0, CipherUtil.KEY_LENGTH);
				byte[] iv = CipherUtil.copy(k, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
				
				server.waitUntilClose();
				server.close();
				serverAcceptor.close();

				Upload s = new Upload(sk,iv,id);
				
			}
			else if(mode==1){
				Download dl = new Download(id);
			}
			else if(mode==2){
				List list =new List(id);
			}
			
			else if(mode==3){
				Delete delete = new Delete(id);
			}
		
			else if(mode==4){
				Register r = new Register(id);
			}
			else if(mode==5){
				Login lll = new Login(id);
			}
			
		} catch ( ProtocolException e ) {
			System.out.println("[ProtocolException]" + e.getLocalizedMessage());
			if(e.getLocalizedMessage().contains("NoSuchIdentifierException"))
				System.out.println("Yours or client's EZCard may be outdated. Please consider register a new one.");
			
		} catch ( Exception e ){
			System.out.println(e);
		}
	}
	}
}

class Login{
	Login(String id){
	try{
		ServerSocket s;
		s = new ServerSocket(6666);
		Socket sock = s.accept();
		BufferedInputStream in = new BufferedInputStream(new DataInputStream(sock.getInputStream()));
		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
		byte[] text = new byte[48];
		byte[] iv = "1234567890123456".getBytes();
		byte[] pass = new byte[16];
		try{
			String file = "USER_INFORMATION";
			File user_file = new File(file);
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(user_file.getAbsolutePath()+"\\"+id+".txt"));
			in.read(pass);
			int check=0;
			int readin;
			while((readin=input.read(text))!=-1){
				try{
					byte[] dec = CipherUtil.authDecrypt(pass,iv,text);	
					for(int i=0;i<16;i++){
						if(pass[i]==dec[i]){
							check++;
						}
					}
					if(check==16){
						out.writeInt(1);		
						out.flush();
						input.close();
						in.close();
						out.close();
						sock.close();
						s.close();
					}
					else{
						out.writeInt(0);		
						out.flush();
						input.close();
						in.close();
						out.close();
						sock.close();
						s.close();
					}
				}catch (IntegrityCheckException e) {
					out.writeInt(0);		
					out.flush();
					input.close();
					in.close();
					out.close();
					sock.close();
					s.close();
				}
			}
			
			
		}catch (IOException e){
			out.writeInt(0);			
			out.flush();
			in.close();
			out.close();
			sock.close();
			s.close();
        }
	}catch (IOException e){
           e.printStackTrace();			
    }
	}
}

class Register{
	Register(String id){
		ServerSocket s;
		try{
			s = new ServerSocket(2222);
			Socket sock = s.accept();
			String file = "USER_INFORMATION";
			File user_file = new File(file);
			user_file.mkdir();
			String file_path = new String(user_file.getAbsolutePath()+"\\"+id+".txt");
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
			int check=1;
			if(user_file.isDirectory()){			
				String[] ss = user_file.list();
				String y = new String(id+".txt");
				for(int i=0;i<ss.length;i++){
					if(y.equals(ss[i])){		
						check=0;
						break;
					}
				}
			}
			if(check==0){				
				out.writeInt(0);
				in.close();
				sock.close();
				s.close();
			}
			else{						
				FileOutputStream output = new FileOutputStream(file_path);
				out.writeInt(1);
				byte[] pass = new byte[48];
				int readin;
				while((readin = in.read(pass))!=-1);
				output.write(pass);
				output.flush();
				output.close();
				in.close();
				sock.close();
				s.close();
			}
		}catch (IOException e){
            e.printStackTrace();
        }
		
	}
}

class Delete{
	Delete(String id){
		ServerSocket s;
		try{
			File server = new File(id);
			s = new ServerSocket(3333);
			Socket sock = s.accept();
			DataInputStream in = new DataInputStream(sock.getInputStream());
			String delete_item = in.readUTF();
			String delete_path = new String(server.getAbsolutePath()+"\\"+delete_item);
			File delete_file = new File(delete_path);
			delete_file.delete();
			in.close();
			sock.close();
			s.close();
			
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class Download{
	Download(String id){
		ServerSocket s;
		try{
			File server = new File(id);
			s = new ServerSocket(5888);		
			Socket sock = s.accept();
			BufferedInputStream input = new BufferedInputStream(new DataInputStream(sock.getInputStream()));
			String d = new String(new DataInputStream(input).readUTF());	
			input.close();
			sock.close();
			s.close();
			go(d,server);
		}catch (IOException e){
            e.printStackTrace();
        }
	}
	void go(String d,File server){
		ServerSocket s1;
		byte[] text = new byte[32];
		byte[] check = new byte[33];	
		check[32]=0;	
		try{
			s1 = new ServerSocket(5777);
			Socket sock = s1.accept();
			FileInputStream in = new FileInputStream(server.getAbsolutePath()+"\\"+d);
			BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
			while(in.available()>0){
				int readin = in.read(text);
				for(int i=0;i<32;i++)
					check[i]=text[i];
				if(in.available()<=0)	
					check[32]=1;
				else check[32]=0;
				out.write(check);
				Thread.yield();
			}
			out.flush();
			in.close();
			out.close();
			sock.close();
			s1.close();	
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class List{
	List(String id){
		ServerSocket s;
		try{
			s = new ServerSocket(5888);	
			File user_file = new File(id);
			Socket sock = s.accept();
			DataOutputStream out = new DataOutputStream(sock.getOutputStream());
			if(user_file.isDirectory()){
				String[]ss = user_file.list();
				for(int k=0;k<ss.length;k++){		
					out.writeUTF(ss[k]);
				}
				out.writeUTF("-1");		
			}
			else
				out.writeUTF("-1");		
			out.close();
			sock.close(); 
			s.close();
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}

class Upload{
	Upload(byte[] sk,byte[] iv,String ID){
		ServerSocket s;
		byte[] text = new byte[32];
		try{
			s = new ServerSocket(5888);			
			File server = new File(ID);
			server.mkdir();
            Socket sock = s.accept();

			BufferedInputStream inputStream = new BufferedInputStream(sock.getInputStream());	
			String fileName = new DataInputStream(inputStream).readUTF();		
			String file = new String(server.getAbsolutePath()+"\\"+fileName);
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));  
            while(inputStream.available()>0){
				int textLength = new DataInputStream(inputStream).readInt();		
				inputStream.read(text);		
				outputStream.write(text);
				Thread.yield();
            }  			
			outputStream.close();                
            inputStream.close();  
			sock.close(); 
			s.close();
		}catch (IOException e){
            e.printStackTrace();
        }
	}
}