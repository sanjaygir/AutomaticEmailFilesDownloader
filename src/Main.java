import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Main {

	
	public static Set<DownloadToken> urls_to_download = new TreeSet<DownloadToken>();
	public static ArrayList<DownloadToken> list = new ArrayList<DownloadToken>();
	
		
	public static String email = "";
	public static String password = "";
	
	
	
	
	public static synchronized void addToListSynchronized(DownloadToken token){
		
		urls_to_download.add(token);
		
	}
	
	public static synchronized void clearListSynchronized(){
		urls_to_download.clear();
	}
	
	
	public static synchronized void copyDownloadList(ArrayList<DownloadToken> li){
		
		for (DownloadToken t : urls_to_download) {
			
			li.add(t);
			
		}		
				
	}
	
	
	
	/*
	 * 
	 * Checks email every 30 seconds to see if any new download file request is received
	 * 
	 */
	
	public static void checkEmail(){
	

		while(true){			
			
			clearListSynchronized();
			
			
			Properties props = new Properties();

	        props.setProperty("mail.store.protocol", "imaps");
	        	        
	        
	        Store store = null;
	        
	        try {
	        	
	            Session session = Session.getInstance(props, null);
	            store = session.getStore();
	            store.connect("imap.gmail.com", email, password);
	            
	            
	            Folder inbox = store.getFolder("INBOX");
	            inbox.open(Folder.READ_WRITE);
	            	            

	            Message[] messages = inbox.getMessages();
	            
	            
	            for(int i=0;i<messages.length;i++){
	            
	            	String[] parts = messages[i].getSubject().split("\\s+");
	            
	            	if(parts.length == 3 && parts[0].toLowerCase().equals("download")){
	            			            		
	            		DownloadToken token = new DownloadToken(parts[2], parts[1]);

	            		addToListSynchronized(token);
	                   		
	            		
	            	}
	            	 	
	            	
	            }
	            

	        }
	        catch(Exception e){
	        	
	        	
	        }
	        
	        try{
	        	store.close();
	        }
	        catch(Exception e){
	        }
	    
	        try{
	        	Thread.sleep(30000);
	        }
	        catch(Exception e){
	        }
	        
	        
		}
		
	}
	
	
	
	/*
	 * Deletes the email that has already been downloaded or the emails that failed to download are also deleted and a new 
	 * email notifying which file download failed is sent
	 * 
	 */
	
	
	public static void deleteEmail(String url, String path){
				
		Properties props = new Properties();

        props.setProperty("mail.store.protocol", "imaps");
        
        
        try {
        	
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", email, password);
            
            
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            

            Message[] messages = inbox.getMessages();
            
            
            for(int i=0;i<messages.length;i++){
            
            	String[] parts = messages[i].getSubject().split("\\s+");
            
            	if(parts.length == 3 && parts[0].toLowerCase().equals("download")){
            		
            		if(parts[1].equals(path) && parts[2].equals(url))            		
            		{
            			
            			messages[i].setFlag(Flags.Flag.DELETED, true);
            			
            		}  	               	            
    	                        		
            		
            	}
            	 	
            	
            }
            
            
            inbox.expunge();
            
            store.close();
            
            
            
        }
        catch(Exception e){
        
        }
        
		
		
		
	}
	
	
	
	
	
	
	/*
	 * 
	 * Reads in the email configuration file and sets the email and password to use
	 * 
	 */
	
	
	public static void setEmailConfiguration(){
		
		try{
			
			BufferedReader br = new BufferedReader(new FileReader("email.txt"));
		    
	        
	        String[] parts_first = br.readLine().split("\\s+");
	        String[] parts_second = br.readLine().split("\\s+");
	        
	
	        email = parts_first[0];
	        password = parts_second[0];
	              
        
	        br.close();
		 
		}
		catch(Exception e){
			System.exit(0);
		}
	
	}
	
	
	
	/*
	 * 
	 * Sends a notification email about failed download
	 * 
	 */
	
	public static void sendUnableToDownloadEmail(String url, String path){
	
		final String username = email;
		final String password = Main.password;
 
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
 
		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });
 
		try {
 
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(email));
			message.setSubject("Downloading a file failed");
			
			message.setText(path + "   " + url);
 
			
			Transport.send(message);
 
			System.out.println("Done sending unable to download email");
			
 
		} catch (MessagingException e) {
			
			
			
		}
				
		
		
	}
	
	
	
	
	
	
	
	public static void main(String[] args){
		
		
		setEmailConfiguration();		       
				
		
		//Checking email in a new thread
		
		Thread th = new Thread(){
			
			public void run(){
				
				checkEmail();
				
			}
			
			
		};
		
		th.start();
		
		
		
		
		while(true){
			
			try{
				Thread.sleep(30000);
			}
			catch(Exception e){
			}
	
			
			//now copy the elements of the set to the list
			
			list.clear();
			copyDownloadList(list);
						

			//iterate the list and download files one by one
			
			for (DownloadToken t : list) {
			
				String song_url = t.getUrl();
				String file_name = t.getPath();
				
				
				System.out.println("Starting Downloading file...");
				System.out.println(song_url);
				System.out.println(file_name);
				
								
				FileDownloader fd = new FileDownloader(song_url, file_name);
				boolean success = fd.download();
				
				if(success){
					deleteEmail(song_url, file_name);
				}
				else{
					
					deleteEmail(song_url, file_name);
					sendUnableToDownloadEmail(song_url, file_name);
					
				}
				
			}	
			
									
		}
		
						
		
	}
	
}
