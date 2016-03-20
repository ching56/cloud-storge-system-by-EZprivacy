package finalproj;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Random;

import ezprivacy.secret.EnhancedProfileManager;
import ezprivacy.secret.Signature;
import ezprivacy.service.authsocket.EnhancedAuthSocketClient;
import ezprivacy.service.signature.SignatureClient;
import ezprivacy.toolkit.CipherUtil;
import ezprivacy.toolkit.EZCardLoader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.JTextPane;

public class client extends JDialog {
	private JTextField portField;
	private JTextField ipField;
	private JButton connectButton;
	private JList list;
	private JTextArea textArea;
	private JLabel StatusLabel;
	private JProgressBar progressBar;
	private String psword;
	
	//data
	private String ip = null;
	private int port;
	static private String status;
	static private EnhancedAuthSocketClient c;
	static private Socket fs;
	static private OutputStream writer;
	static private DataInputStream portStream;
	static private DataOutputStream modeStream;
	static private int randPort;
	static private String masterKey="";
	static private EnhancedProfileManager profile;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			client dialog = new client();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			File msk = new File("client"+File.separator+"ms.k");
			DataInputStream mskStream = new DataInputStream(new FileInputStream(msk));
			byte [] buf = new byte[1];
			int counter=0;
			while(mskStream.read(buf)!=-1&&counter<16){
				String bufs = new String(buf);
				masterKey +=bufs;
				counter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	//initialize gui
	public client() {
		
		setBounds(100, 100, 301, 656);
		getContentPane().setLayout(null);
		
		connectButton = new JButton("connect");
		connectButton.setBounds(207, 24, 77, 80);
		getContentPane().add(connectButton);
		connectButton.setActionCommand("OK");
		getRootPane().setDefaultButton(connectButton);	
		
		portField = new JTextField();
		portField.setBounds(65, 78, 130, 26);
		getContentPane().add(portField);
		portField.setColumns(10);
		
		ipField = new JTextField();
		ipField.setBounds(65, 50, 130, 26);
		getContentPane().add(ipField);
		ipField.setColumns(10);
		
		JButton SendButton = new JButton("Go");
		SendButton.setBounds(17, 378, 267, 42);
		getContentPane().add(SendButton);
		
		JLabel lblNewLabel = new JLabel("Ip");
		lblNewLabel.setBounds(17, 55, 30, 16);
		getContentPane().add(lblNewLabel);
		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		
		JLabel lblNewLabel_1 = new JLabel("Port");
		lblNewLabel_1.setBounds(17, 88, 30, 16);
		getContentPane().add(lblNewLabel_1);
		lblNewLabel_1.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);
		
		JButton UploadButton = new JButton("Upload");
		UploadButton.setBounds(16, 116, 80, 64);
		getContentPane().add(UploadButton);
		
		JButton DownloadButton = new JButton("Download");
		DownloadButton.setBounds(113, 116, 80, 64);
		getContentPane().add(DownloadButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(17, 192, 267, 176);
		getContentPane().add(scrollPane);
		
		list = new JList();
		list.setBounds(39, 94, 203, 177);
		scrollPane.setViewportView(list);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(17, 446, 267, 16);
		getContentPane().add(progressBar);
		progressBar.setFont(new Font("Lucida Grande", Font.PLAIN, 9));
		progressBar.setMaximum(100);
		progressBar.setStringPainted(true);
		
		 StatusLabel = new JLabel("status");
		 StatusLabel.setBounds(54, 424, 193, 16);
		 getContentPane().add(StatusLabel);
		 StatusLabel.setFont(new Font("Lucida Grande", Font.BOLD, 15));
		 StatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		 
		 JButton CloseButton = new JButton("Close");
		 CloseButton.setBounds(204, 116, 80, 64);
		 getContentPane().add(CloseButton);
		 
		 JScrollPane scrollPane_1 = new JScrollPane();
		 scrollPane_1.setBounds(17, 474, 267, 154);
		 getContentPane().add(scrollPane_1);
		 
		 textArea = new JTextArea();
		 scrollPane_1.setViewportView(textArea);
		 
		 textField = new JTextField();
		 textField.setBounds(65, 24, 130, 26);
		 getContentPane().add(textField);
		 textField.setColumns(10);
		 
		 JLabel lblNewLabel_2 = new JLabel("Password");
		 lblNewLabel_2.setBounds(17, 30, 61, 16);
		 getContentPane().add(lblNewLabel_2);
		 lblNewLabel_2.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		 lblNewLabel_2.setHorizontalAlignment(SwingConstants.LEFT);
		 CloseButton.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent e) {
		 		try {
		 			
		 			modeStream.close();
		 			portStream.close();
		 			fs.close();
		 			c.close();
		 			
		 			
		 		} catch (Exception e1) {
		 			status = "unconnected";
		 			StatusLabel.setText(status);
		 			e1.printStackTrace();
		 		}
		 		status = "unconnected";
		 		StatusLabel.setText(status);
		 	}
		 });
		DownloadButton.addActionListener(new DownloadActionListener());
		UploadButton.addActionListener(new UploadActionListener());
		SendButton.addActionListener(new SendActionListener());
		connectButton.addActionListener(new ConnectActionListener());
	}
	
	class ConnectActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(status=="connected")return;
			ip = ipField.getText();
			port = Integer.parseInt(portField.getText());
			psword = textField.getText();

	    	profile = 
					EZCardLoader.loadEnhancedProfile(new File("client.card"), psword);
            textArea.append("[client] profile: " + profile+"\n");

			c = new EnhancedAuthSocketClient(profile);
			try{
				c.connect(ip, port);
				
				c.doEnhancedKeyDistribution();
	            textArea.append("[client] sk: " + c.getSessionKey()+"\n");

				c.doRapidAuthentication();
				textArea.append("[client] auth: " + c.isAuthenticated()+"\n");
				EZCardLoader.saveEnhancedProfile(profile, new File("client.card"), "0000");
				status = "connected";
				StatusLabel.setText(status);
				textArea.append("MasterKey:"+masterKey);
				
				portStream = new DataInputStream(c.getInputStream());
				modeStream = new DataOutputStream(c.getOutputStream());
				
				int randPort = portStream.readInt();
				Socket objS = new Socket(ip,randPort);
				ObjectOutputStream objOutStream = new ObjectOutputStream(objS.getOutputStream());
				
				objOutStream.writeObject(profile.getPrimitiveProfile().getIdentifier());
				objOutStream.flush();
				objOutStream.close();
				objS.close();
			}catch(Exception e1){
				status = "unconnected";
				StatusLabel.setText(status);
				e1.printStackTrace();
			}
		}
	}
	
	class DownloadActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(status!="connected")return;
			try {
				
				modeStream.writeInt(2);
				modeStream.flush();
			status = "Select file to download";
			StatusLabel.setText(status);
			
			try {
				randPort = portStream.readInt();
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			
			try {
				fs = new Socket(ip,randPort);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			ObjectInputStream objectStream = new ObjectInputStream(fs.getInputStream());
			String[] namelist = (String[]) objectStream.readObject();
			list.setListData(namelist);
			fs.close();
			
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				status = "unconnected";
				StatusLabel.setText(status);
				e1.printStackTrace();
			}
		}
	}
	
	class UploadActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if(status!="connected")return;
			File filelist[] = new File("client").listFiles();
	    	String[] namelist = new String[filelist.length];
	    	for(int i = 0;i<filelist.length;i++)
	    		namelist[i] = filelist[i].getName();
	    	list.setListData(namelist);
	    	status = "Select file to upload";
	    	StatusLabel.setText(status);
	    	
	    	try {
				modeStream.writeInt(1);
				modeStream.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				status = "unconnected";
				StatusLabel.setText(status);
				e1.printStackTrace();
			}
		}
	}
	
	class SendActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(status == "Select file to upload")
				upload();
			else if(status == "Select file to download")
				download();
	      }

		private void download() {
			status = "downloading";
			StatusLabel.setText(status);
			int num;
			int counter = 0;
			byte[] buf = new byte[4160];
			byte[] fbuf = new byte[32];
			try {
			
				 progressBar.setValue(0);
				
			
			randPort = portStream.readInt();
			
			fs = new Socket(ip,randPort);
			
	        BufferedOutputStream output = new BufferedOutputStream(fs.getOutputStream());
			BufferedInputStream input =  new BufferedInputStream(fs.getInputStream());
	        String name = (String)list.getSelectedValue();
			DataOutputStream writer = new DataOutputStream(new FileOutputStream("client"+File.separator+name));
			
			for(int i=0; i<name.length(); i++){
				output.write( (int)name.charAt(i) );
            }
			output.write((byte)-1);
			output.flush();
			
			DataInputStream FileKey = new DataInputStream(new FileInputStream("client"+File.separator+"fk"+File.separator+name+".fk"));
			FileKey.read(fbuf);
			fbuf = CipherUtil.decrypt(masterKey.getBytes(), fbuf);
			
			progressBar.setValue(15);
			byte[] key = CipherUtil.copy(fbuf, 0, CipherUtil.KEY_LENGTH);
			byte[] iv = CipherUtil.copy(fbuf, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			
			
			byte[] sk = c.getSessionKey().getKeyValue();
			byte[] skey = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
			byte[] siv =  CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
			
			while((num=input.read(buf))==4160){
				byte[] MacText = CipherUtil.authDecrypt(skey, siv, buf);
				byte[] plaintext = CipherUtil.authDecrypt(key, iv, MacText);
				writer.write(plaintext,0,plaintext.length);
               	counter++;
			}
			
			if(num>-1){
				byte[] temp = Arrays.copyOf(buf, num);
				byte[] MacText = CipherUtil.authDecrypt(skey, siv, temp);
				byte[] plaintext = CipherUtil.authDecrypt(key, iv, MacText);
				writer.write(plaintext,0,plaintext.length);
			}

			textArea.append("Recieve the file:"+ name+"\n");
			progressBar.setValue(93);
			list.setListData(new Object[0]);
			status = "connected";
			StatusLabel.setText(status);
			progressBar.setValue(100);
			writer.flush();
			writer.close();
			output.close();
			input.close();
			
			}catch(Exception e){
				e.printStackTrace();
				status = "unconnected";
				StatusLabel.setText(status);
			}
		}

		private void upload() {

			try {
				randPort = portStream.readInt();
			} catch (IOException e3) {
				e3.printStackTrace();
			}
			progressBar.setValue(0);
			
			status = "uploading";
			StatusLabel.setText(status);
			String name = (String)list.getSelectedValue();
			
			if(!name.isEmpty()){
				int counter = 0;
				
				byte[] sk = c.getSessionKey().getKeyValue();
				int sigPort;
				try {
				Random r = new Random();
			    byte[] randByte = new byte[32];
			    r.nextBytes(randByte);
			    
			    byte[] key = CipherUtil.copy(randByte, 0, CipherUtil.KEY_LENGTH);
				byte[] iv = CipherUtil.copy(randByte, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
				
				byte[] skey = CipherUtil.copy(sk, 0, CipherUtil.KEY_LENGTH);
				byte[] siv =  CipherUtil.copy(sk, CipherUtil.KEY_LENGTH, CipherUtil.BLOCK_LENGTH);
				
				File folder = new File("client"+File.separator+"fk");
		        if(!folder.exists())
		        	folder.mkdir();
		        
		        File filekey = new File("client"+File.separator+name+File.separator+name+".fk");
				DataOutputStream FileKey = new DataOutputStream(new FileOutputStream("client"+File.separator+"fk"+File.separator+name+".fk"));
				byte[] CipherRandByte = CipherUtil.encrypt(masterKey.getBytes(),randByte);
				FileKey.write(CipherRandByte);
				FileKey.close();
				progressBar.setValue(15);
				fs = new Socket(ip,randPort);
				
		        byte[] buf = new byte[4096];
		        BufferedOutputStream writer = new BufferedOutputStream(fs.getOutputStream());
		        InputStream input = new FileInputStream("client"+File.separator+name);
            	String k = name;
            	
            	//write the name
            	for(int i=0; i<k.length(); i++){
					writer.write( (int)k.charAt(i) );
                }
				writer.write((byte)-1);
				
				int num;

				while((num=input.read(buf))==4096){
					byte [] cipher = CipherUtil.authEncrypt(key, iv, buf);
					byte [] MacCipher = CipherUtil.authEncrypt(skey, siv,cipher);
					writer.write(MacCipher,0,MacCipher.length);
					counter++;
				}
				if(num>-1){
					byte[] temp = Arrays.copyOf(buf, num);
					byte [] cipher = CipherUtil.authEncrypt(key, iv, temp);
					byte [] MacCipher = CipherUtil.authEncrypt(skey, siv, cipher);
					writer.write(MacCipher,0,MacCipher.length);
				}

                progressBar.setValue(83);
				writer.flush();
				Thread.sleep(counter/10);

				status = "verifying the signature";
				StatusLabel.setText(status);
				sigPort = portStream.readInt();
				Socket sigS = new Socket(ip,sigPort);
				ObjectInputStream sigStream = new ObjectInputStream(sigS.getInputStream());
                Signature sig = (Signature) sigStream.readObject();
                boolean result = SignatureClient.verifyWithoutArbiter(sig, profile.getPrimitiveProfile());
                
                File Sigfolder = new File("client"+File.separator+"sig");
		        if(!Sigfolder.exists())
		        	Sigfolder.mkdir();
		        File sigFIle = new File("client"+File.separator+"sig"+File.separator+name+".sig");
				ObjectOutputStream sigFileStream = new ObjectOutputStream(new FileOutputStream("client"+File.separator+"sig"+File.separator+name+".sig"));
                sigFileStream.writeObject(sig);
                progressBar.setValue(100);
                
                if(result){
                	textArea.append("Signature is verified\n");
                }else{
                	textArea.append("Signature isn't verified\n");
                }
                
				
				writer.close();
            	input.close();
            	fs.close();
            	
              	 textArea.append("data upload success : "+name+"\n");
				}catch(Exception e1){
					e1.printStackTrace();
					status = "unconnected";
					StatusLabel.setText(status);
				}
				list.setListData(new Object[0]);
				status = "connected";
				StatusLabel.setText(status);
            	
			}
			
		}
	}
}
