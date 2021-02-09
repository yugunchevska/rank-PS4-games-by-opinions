package bg.sofia.uni.fmi.learn.sql;

import java.io.InvalidObjectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySqlConnection {
	
	private static final String url = "jdbc:mysql://localhost:3306/ps4games";
	private static final String user = "root";
	private static final String password = "root";
	
	private String site;
	
	public MySqlConnection(String site) {
		this.site = site;
	}
	
	public void insertGame(String title, String date, String reviewUrl, String summary, List<String> comments) throws InvalidObjectException {
        try (Connection con = DriverManager.getConnection(url, user, password)) {
        	if (!MySqlStatements.doesGameExists(con, site, title)) {
        		MySqlStatements.insertGameInfo(con, site, title, date, reviewUrl, summary);
        		MySqlStatements.insertGameCommentsAndSentimentScores(con, site, title, comments);
        	} else {
        		// System.out.println("The game already exists in the DB");
        	}
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        } 
	}
	
	public String getLatestUrl() {
		String reviewUrl = "";
		try (Connection con = DriverManager.getConnection(url, user, password)) {
			reviewUrl = MySqlStatements.getLatestUrl(con, site);
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        } 
		
		return reviewUrl;
	}
	
	public String getSummary(String title) {
        String summary = "";
		try (Connection con = DriverManager.getConnection(url, user, password)) {
        	summary = MySqlStatements.getSummary(con, site, title);
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        } 
		
		return summary;
	}
	
	public Map<String, String> getAllTitlesAndSummaries() {
		Map<String, String> gameSummary = new HashMap<>();
		try (Connection con = DriverManager.getConnection(url, user, password)) {
			gameSummary = MySqlStatements.getAllTitlesAndSummaries(con, site);
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        } 
		
		return gameSummary;
	}
	
	public List<String> getComments(String title) {
		List<String> comments = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url, user, password)) {
        	comments = MySqlStatements.getComments(con, title);
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return comments;
	}
	
	public Map<String, List<String>> getCommentsSince(String date) {
		Map<String, List<String>> gameSummary = new HashMap<>();
		try (Connection con = DriverManager.getConnection(url, user, password)) {
			gameSummary = MySqlStatements.getCommentsSince(con, site, date);
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        } 
		
		return gameSummary;
	}
	
	public Map<String, Double> getSentimentScoreSince(String date) {
		Map<String, Double> gameScore = new HashMap<>();
		try (Connection con = DriverManager.getConnection(url, user, password)) {
			gameScore = MySqlStatements.getSentimentScoreSince(con, site, date);
        } catch (SQLException ex) {
            System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        } 
		
		return gameScore;
	}

    public static void main(String[] args) {
        String title = "The Middle2";
        String summary = "What a horror! Buy the game!";
        List<String> comments = new ArrayList<>();
        comments.add("best game");
        comments.add("worst game");
        
        MySqlConnection sql = new MySqlConnection("kotaku");
        // sql.insertGame(title, summary, comments);
        System.out.println("Summary: " + sql.getSummary(title));
        System.out.println("Comments: " + sql.getComments(title));
    }
}
