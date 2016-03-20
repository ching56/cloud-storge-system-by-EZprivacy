package finalproj;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridLayout;

import ezprivacy.protocol.IntegrityCheckException;
import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.secret.Signature;
import ezprivacy.service.authsocket.AuthSocketServer;
import ezprivacy.service.authsocket.EnhancedAuthSocketServerAcceptor;
import ezprivacy.service.signature.SignatureClient;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import java.io.*;
import java.net.*;
import java.util.Arrays;

import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class server extends JDialog {
	private JButton connectButton;
	private JButton closeButton;
	private JTextField dtrpnEnterThePort;
	static private JTextArea textArea;
	static private String status= "uninitialized";
	static private String psword;
	static private AuthSocketServer s;
	static private ServerSocket fs ;
	static private EnhancedAuthSocketServerAcceptor serverAcceptor;
	static private DataOutputStream portStream;
	static private DataInputStream modeStream;
	static private InputStream input;
	static private int port;
	static private int randPort;
	static private EnhancedProfileManager Profile;
	static private byte[] ClientProfile;
	private JTextField txtPassword;
	private JScrollPane scrollPane;
	
	/**
	 * Launch the application.
	 */
	//initialize gui
	public server() throws IntegrityCheckException {
		setBounds(100, 100, 305, 441);
		getContentPane().setLayout(null);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(14, 15, 279, 307);
		getContentPane().add(scrollPane);
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
		connectButton = new JButton("connect");
		connectButton.setBounds(216, 332, 75, 43);
		getContentPane().add(connectButton);
		connectButton.addActionListener(new ConnectActionListener());
		connectButton.setActionCommand("connect");
		closeButton = new JButton("close");
		closeButton.setBounds(217, 377, 75, 36);
		getContentPane().add(closeButton);
		closeButton.addActionListener(new CloseActionListener());
		
		txtPassword = new JTextField();
		txtPassword.setBounds(16, 376, 190, 37);
		getContentPane().add(txtPassword);
		txtPassword.setFont(new Font("Lucida Grande", Font.ITALIC, 15));
		txtPassword.setText("password...");
		txtPassword.setColumns(10);
		dtrpnEnterThePort = new JTextField();
		dtrpnEnterThePort.setBounds(16, 334, 190, 34);
		getContentPane().add(dtrpnEnterThePort);
		dtrpnEnterThePort.setFont(new Font("Lucida Grande", Font.ITALIC, 15));
		dtrpnEnterThePort.setText("port...");
		
	}
	//use while loop to get the data
	public static void main(String[] args){
		try {
			server dialog = new server();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);		
				
			while(true){
				Thread.sleep(100);
				if(status=="connected"){
				int mode = 0;
				mode = modeStream.readInt();
					if(mode==1)
						clientUpload();
					else if(mode==2)
						clientDownload();
				}
			}
		} catch (Exception e1) {
					e1.printStackTrace();
		}
	}
	
	class CloseActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(status == "empty"){
				textArea.append("connection doesn't exist\n");
				return;
			}else{
			try {
				
				portStream.close();
				modeStream.close();
				fs.close();
				s.close();
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				status = "empty";
				
				textArea.append("disconnect\n");
				textArea.append("status: "+status+"\n");
				e1.printStackTrace();
			} 
			status = "empty";
			
			textArea.append("disconnect\n");
			textArea.append("status: "+status+"\n");
			}
	      }
	}
	
	class ConnectActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			port =  Integer.valueOf(dtrpnEnterThePort.getText());
			psword = txtPassword.getText();
			if(status == "connected"){
				textArea.append("connection already exists\n");
				return;
			}
			try {
				Profile = 
						EZCardLoader.loadEnhancedProfile(new File("server.card"), psword);
				textArea.append("[server] profile: " + Profile+"\n");
				// Initialize authsocket acceptor
				serverAcceptor = new EnhancedAuthSocketServerAcceptor(Profile);
				serverAcceptor.bind(port);
				textArea.append("Bind port " +port+"\n");
				s = serverAcceptor.accept();
		        textArea.append("Accepted a client from " + s.getRemoteAddress()+"\n");
				s.waitUntilAuthenticated();
				EZCardLoader.saveEnhancedProfile(Profile, new File("server.card"), "0000");
				
				portStream = new DataOutputStream(s.getOutputStream());
				modeStream = new DataInputStream(s.getInputStream());
				
				ServerSocket objSS = new ServerSocket(0);
				int randPort = objSS.getLocalPort();
				portStream.writeInt(randPort);
				portStream.flush();
				Socket objS = objSS.accept();
				
				ObjectInputStream objInStream = new ObjectInputStream(objS.getInputStream());
				ClientProfile = (byte[]) objInStream.readObject();
				
				objInStream.close();
				objSS.close();
				objS.close();
				
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        textArea.append("connect to "+port+"\n");
	        if(s.isAuthenticated()){
	        	status = "connected";
	        	textArea.append("status: "+status+"\n");
	        }
	      }
	}
	
	private static void clientUpload(){
		try{
			status = "client uploading";
			textArea.append("status: "+status+"\n");
			textArea.append("ready to reiceve file\n");
        	File folder = new File("server");
        	if(!folder.exists())
        		folder.mkdir();

            byte[] sk = s.getSessionKey().getKeyValue();
			byte[] key = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			
			
			fs = new ServerSocket(0);
			randPort = fs.getLocalPort();
			portStream.writeInt(randPort);
			portStream.flush();
			Socket tfs = fs.accept();
			
			
			input =tfs.getInputStream();
			DataInputStream stream = new DataInputStream(new BufferedInputStream(input));
			byte[] buf = new byte[4160];
        	int counter  =0;
           
				textArea.append("data uploading\n");

                String c = "";
                int a;

                while((a = stream.read()) != 255){
                    c += String.format("%c",(char)a);
                }
                textArea.append("File name: "+c+"\n");
                DataOutputStream output = new DataOutputStream(new FileOutputStream("server"+File.separator+c));
                int num;

                while((num=stream.read(buf)) == 4160){
                	byte[] plaintext = CipherUtil.authDecrypt(key, iv, buf);
                   	output.write(plaintext,0,plaintext.length);
                    
                }

                if(num>-1){
                	byte[] temp = Arrays.copyOf(buf, num);
                	byte [] plaintext = CipherUtil.authDecrypt(key, iv, temp);
                	output.write(plaintext,0,plaintext.length);
                	textArea.append("Receive File\n");
                }
                output.flush();
                output.close();
                stream.close();
                tfs.close();

                ServerSocket sigSS = new ServerSocket(0);
    			int SigPort = sigSS.getLocalPort();
    			portStream.writeInt(SigPort);
    			portStream.flush();

    			Socket sigS = sigSS.accept();
    			ObjectOutputStream sigOutStream = new ObjectOutputStream(sigS.getOutputStream());
    			
                
                Signature sig = new SignatureClient.SignatureCreater()
        				.initSignerID(Profile.getPrimitiveProfile().getIdentifier())
        				.initMessage("".getBytes())
        				.initReceiverID(ClientProfile)
        				.initSignatureKey(Profile.getPrimitiveProfile().getSignatureKey())
        				.initTimestamp(System.nanoTime()).createSignature();

                sigOutStream.writeObject(sig);
                sigOutStream.flush();
                Thread.sleep(200);
                sigOutStream.close();
                sigS.close();
                sigSS.close();
                
                status = "connected";
    			textArea.append("status: "+status+"\n");
            
        } catch (IOException e){
            e.printStackTrace();
        } catch (IntegrityCheckException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	private static void clientDownload() {
		try{
			
			status = "client downloading";
			textArea.append("status: "+status+"\n");
			textArea.append("ready to transport file\n");
        	
			File folder = new File("server");
        	if(!folder.exists())
        		folder.mkdir();
			
			
			fs = new ServerSocket(0);
			randPort = fs.getLocalPort();
			portStream.writeInt(randPort);
			portStream.flush();
			Socket tfs = fs.accept();
			
			
			File filelist[] = new File("server").listFiles();
			String[] namelist = new String[filelist.length];
			for(int i = 0;i<filelist.length;i++)
				namelist[i] = filelist[i].getName();
			ObjectOutputStream objectStream = new ObjectOutputStream(tfs.getOutputStream());
			objectStream.writeObject(namelist);
			tfs.close();
			fs.close();
			
			fs = new ServerSocket(0);
			randPort = fs.getLocalPort();
			portStream.writeInt(randPort);
			portStream.flush();
			tfs = fs.accept();
			
			byte[] sk = s.getSessionKey().getKeyValue();
			byte[] key = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			
			byte[] buf = new byte[4128];
        	
			int counter = 0;

            	String c = "";
                int a;
    			DataInputStream stream = new DataInputStream(tfs.getInputStream());
                while((a = stream.read()) != 255){
                    c += String.format("%c",(char)a);
                }
                
    			InputStream input = new FileInputStream("server"+File.separator+c);
    			BufferedOutputStream output = new BufferedOutputStream(tfs.getOutputStream());
                
    			textArea.append("Client download file name: "+c+"\n");
    			int num;
                while((num = input.read(buf))==4128){
                	byte [] cipher = CipherUtil.authEncrypt(key, iv, buf);
                   	output.write(cipher,0,cipher.length);
                   	counter++;
                }
                if(num>-1){
					byte[] temp = Arrays.copyOf(buf, num);
					byte [] cipher = CipherUtil.authEncrypt(key, iv, temp);
					output.write(cipher,0,cipher.length);
				}
                
                
                textArea.append("Sending Compelete\n");
                output.flush();
                output.close();
                input.close();
                stream.close();
                tfs.close();
                status = "connected";
                
		textArea.append("status: "+status+"\n");

		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

