package bg.sofia.uni.fmi.learn.pojo;

public class Review {
	
	private String name;
	private String date;
	private String url;
	
	public Review(String name, String date, String url) {
		this.name = name;
		this.date = date;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}	
	
}
