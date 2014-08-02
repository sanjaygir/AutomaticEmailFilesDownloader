import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class FileDownloader {

	
	private String destination_directory;
	
	private String song_url;
	private String file_name;
	

	
	public FileDownloader(String url, String path){
		
		this.song_url = url;
		this.destination_directory = "";
		this.file_name = path;
				
		splitInToFolderAndFile(path);

		
		//First make sure the file exists...We first create a file then copy the contents from the web to this file
		
		if(!fileExists(path)){
			
			createFile(path);
			
		}
		
	}
	


	public void splitInToFolderAndFile(String path){
	
		if(isNestedFile(path)){
			
			int index = -1;
			
			for(int i=(path.length()-1); i>=0; i--){
				
				if(path.charAt(i) == '/'){
					index = i;
					break;
				}				
				
			}
						
			destination_directory = path.substring(0, index);
			
		}
	}
	
	
	
	public boolean fileExists(String path){
		
		return new File(path).exists();
					
	}
	
	
	
	public void createFile(String file_name){
		
		new File(destination_directory).mkdirs();
		
		try{
			new File(file_name).createNewFile();
		}
		catch(Exception e){
			e.printStackTrace();
		}			
		
	}
	
	
	
	
	public boolean isNestedFile(String path){
		
		for(int i=(path.length()-1); i>=0; i--){
			
			if(path.charAt(i) == '/'){
				return true;
				
			}			
		}		
		
		return false;	
		
	}
	
	
	
	
	
	public long calculateTotalSizeLeftToDownload(){

		long total_size;
		
		URLConnection con1 = null;
		
		while(true){
		
	    	try{
	    	
		    	URL web = new URL(song_url);
	    		
	    		con1 = web.openConnection();
	    	    		
	    		 total_size = con1.getContentLength();
	    		    	    		
	    		((HttpURLConnection)con1).disconnect();
	    	
	    		return total_size;
	    		
			}
			catch(Exception e){
				
			}
	    	
	    	((HttpURLConnection)con1).disconnect();
	    	
		}
		
		
	}
	
	
	public long calculateTotalSizeDownloaded(){
		    	
		
		File f2 = new File(file_name);
		    		
		
		if(f2.exists()){
			    			
			
			return f2.length();
		
	    	
		}
		
		return 0;
		
    	
	}
	
	
	
	
	
	public boolean download(){
	
		
	    	long total_size_to_download = -1;
	    	long total_size_downloaded = 0;
  	
	    	
	    	//Counter to count the number of times it failed to download the file
	    	int num_of_tries = 0;
	    	
	    	
	    	
			while(true){
		
			
				//If it failed more than 10 times then quit
				if(num_of_tries > 10){
					return false;
				}
				
				
				
				//Just to make it so that it doesn't eat a lot of CPU time
				try{
					Thread.sleep(5000);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
										
	    	
				total_size_to_download = calculateTotalSizeLeftToDownload();
				total_size_downloaded = calculateTotalSizeDownloaded();
	    		  
				
				//File download complete
				if(total_size_to_download == total_size_downloaded){ 
					return true;					
				}
				
				
				System.out.println("Downloading...");
								
				
			
				try{
					Thread.sleep(1000);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			
			
				
	    		URLConnection con = null;
				
	    		
	    		
		    	try{
		    				    		
		    		
		    		URL website = new URL(song_url);
		    		
		    		
		    		con = website.openConnection();
		    			    				    		
		    		
		    		String start_byte;
		    				    		
		    		start_byte = String.valueOf(total_size_downloaded);
		    		
		    		
		    		con.setRequestProperty("Range", "bytes="+start_byte+"-");
			    		    			
		    		
		    		//5 min read timeout		    			    		
		    		con.setReadTimeout(300000);
			    			    		
		    		
		    		
		    		File f = new File(file_name);
		    				    		
		    		
		    		if(total_size_downloaded != total_size_to_download){
		    		
			    		if(f.exists()){
			    			    			
			    			ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
					    	FileOutputStream fos = new FileOutputStream(f, true);
					    				    	
					    	
					    	fos.getChannel().transferFrom(rbc, f.length(), Long.MAX_VALUE);
			    			
					    	fos.close();
					    	
					    	
			    		}
			    
			    		
		    		}
		    		 	
				
		    	}
		    	catch(Exception e){
		    	   		
		    	}
	
	    	
	
		    	((HttpURLConnection)con).disconnect();
	        	
		    	
		    	//Number of rounds counter -> if the download succeeded in the first try then num_of_tries will only count till 1
		    	num_of_tries++;
	    					
			
		}
		
			
	}
	
	
}
