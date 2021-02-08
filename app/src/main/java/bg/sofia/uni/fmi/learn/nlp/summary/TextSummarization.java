package bg.sofia.uni.fmi.learn.nlp.summary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;

import bg.sofia.uni.fmi.learn.util.HttpRequest;

public class TextSummarization {
	private static final String SUMMARIZATION_URL = "https://api.meaningcloud.com/summarization-1.0";
	private static final String MEANING_CLOUD_KEY = "6c64768248d57c69b7036c1b7328361f";
    
    public static String summarize(String text) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("key", MEANING_CLOUD_KEY));
        urlParameters.add(new BasicNameValuePair("txt", text));
        urlParameters.add(new BasicNameValuePair("sentences", "10"));
        
    	String summaryResponseInJson = HttpRequest.sendPostRequest(SUMMARIZATION_URL, urlParameters);
    	System.out.println("Json response from MeaningCloud: " + summaryResponseInJson);
    	
    	return extractSummaryFromJson(summaryResponseInJson);
    }
    
    private static String extractSummaryFromJson(String jsonResult) {
    	Gson gson = new Gson();
    	SummaryResult summaryResult = gson.fromJson(jsonResult, SummaryResult.class);
    	
    	return summaryResult.getSummary();
    }

}
