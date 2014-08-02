
//Data structure to represent a file to be downloaded

public class DownloadToken implements Comparable<DownloadToken>{

	private String url;
	private String path;
		
	
	public DownloadToken(String url, String path){
		
		this.url = url;
		this.path = path;
		
	}
	
	public String getUrl(){
		return url;
	}

	public String getPath(){
		return path;
	}
	
	public void setUrl(String u){
		this.url = u;
	}
	
	public void setPath(String p){
		this.path = p;
	}
	
	  public boolean equals(Object o) {
	      return (o instanceof DownloadToken) && (((DownloadToken)o).getUrl()).equals(this.url) && (((DownloadToken)o).getPath()).equals(this.path);
	      
	  }

	  public int hashCode() {
	      return (url+path).hashCode();
	  }

	  
	@Override
	public int compareTo(DownloadToken arg0) {
		// TODO Auto-generated method stub
		DownloadToken target = (DownloadToken)arg0;
		
		return (url+path).compareTo(target.getUrl()+ target.getPath());

	}
	
}
