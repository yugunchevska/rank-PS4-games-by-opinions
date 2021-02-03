package bg.sofia.uni.fmi.learn.nlp.summary;

public class SummaryResult {
	
	private transient String status;
	private String summary;
	
	public SummaryResult() {
		this.status = null;
		this.summary = null;
	}
	
	SummaryResult(String status, String summary) {
		this.status = status;
		this.summary = summary;
	}
	
	String getStatus() {
		return this.status;
	}
	
	String getSummary() {
		String cleanSummary = summary.replaceAll("\\[\\.\\.\\.\\]", "");
		return cleanSummary;
	}

}
