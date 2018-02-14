import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.swing.JOptionPane;
import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.toolkit.CipherUtil;


public class Project3 {
    public static void main( String argv[]){
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Server / 2. Client\n");  
        int opt = scanner.nextInt();
        if( opt==1 ){
            Server s = new Server();
        }else if(opt==2){
            Client c = new Client();
        }
    }
}

class Server {
	ServerSocket s;
	byte[] key = "oiEAitVbJHJFdkjW".getBytes();
	byte[] iv = "0101010101010101".getBytes();
	byte[] ciphertext = new byte[80];
    Server(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the port: ");
        int p = scanner.nextInt();
        go(p);
    }
    void go(int port){
        try{
			int count = 0, text = 0;
            s = new ServerSocket(port);
			File server = new File("server");
			if(!server.exists())//if there is no server file ,create a new one
				server.mkdir();
            while(true){
                Socket sock = s.accept();//build connection
				System.out.println("Server listening request.......");
				BufferedInputStream input_stream = new BufferedInputStream(sock.getInputStream());//new a input buffer stream
				String filename = new DataInputStream(input_stream).readUTF();//to get filename
				String file = new String(server.getAbsolutePath() + "\\" + filename);//save path of file
				BufferedOutputStream output_stream = new BufferedOutputStream(new FileOutputStream(file));//new a input buffer stream
                while(input_stream.available()>0){
					String text_lenght = new DataInputStream(input_stream).readUTF();//read plaintext as UTF-8
					text = input_stream.read(ciphertext);//read from client
					try{
						byte[] plaintext = CipherUtil.authDecrypt(key,iv,ciphertext);
						count = Integer.parseInt(text_lenght);//to count the round for writing
						for(int i=0; i<count; i++)
							output_stream.write(plaintext[i]);
						Thread.yield();//to make program become executable state
					}catch(IntegrityCheckException e){
						e.printStackTrace();
					}
                }
                System.out.println(filename + " uploaded success!"); 
				input_stream.close();
				output_stream.close();
                sock.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
class Client {
    Socket s;
	byte[] plaintext = new byte[48];
    byte[] key = "oiEAitVbJHJFdkjW".getBytes();
	byte[] iv = "0101010101010101".getBytes();
    Client(){
        Scanner scanner = new Scanner(System.in);
        String tip = JOptionPane.showInputDialog(null,"Enter the IP:");
		String port = JOptionPane.showInputDialog(null,"Enter the port:");
		int tport = Integer.parseInt(port);
		int text=0;
        try {
			File client = new File("client");
			while(true){
				connect(tip, tport);
				DataOutputStream output_stream = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));//new a output stream for data used
				BufferedInputStream input_stream = null;
				String filename = JOptionPane.showInputDialog(null,"Enter filename to upload:");//to get filename
				String file = new String(client.getAbsolutePath() + "\\" + filename);//get file's path
				try{
					output_stream.writeUTF(filename);
					input_stream = new BufferedInputStream(new FileInputStream(file));
				}
				catch(IOException e){
					JOptionPane.showMessageDialog(null,"File " + filename + " does not exist.","ERROR",JOptionPane.ERROR_MESSAGE);
				}
				while(input_stream.available()>0){
					
					text = input_stream.read(plaintext);//read data
					String text_lenght = text + "";
					output_stream.writeUTF(text_lenght);//write UTF
					byte[] ciphertext = CipherUtil.authEncrypt(key,iv,plaintext);
					output_stream.write(ciphertext);//write data into server
					Thread.yield();//to make program become executable state
				}				  
				output_stream.close();
				input_stream.close(); 
				s.close();
				JOptionPane.showMessageDialog(null, "uploaded success!", "SUCCESS",JOptionPane.WARNING_MESSAGE);
			}
        } catch(IOException e){
            e.printStackTrace();
        }
    }
	void connect(String ip, int port) throws IOException{
        s = new Socket(ip, port);
    }
}