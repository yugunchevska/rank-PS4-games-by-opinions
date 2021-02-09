package bg.sofia.uni.fmi.learn.sql;

import java.io.InvalidObjectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bg.sofia.uni.fmi.learn.nlp.sentiment.analysis.SentimentAnalyser;

public class MySqlStatements {
	
	public static boolean doesGameExists(Connection con, String site, String title) {
		String query = "SELECT title FROM games" + site +" WHERE title = '" + title +"'";

        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                // System.out.println(rs.getString(1));
                
                return true;
            }
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
		return false;
	}
	
	public static String getLatestUrl(Connection con, String site) {
		String query = "SELECT url FROM games" + site +" ORDER BY date DESC";

        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

        	if(rs.next()){
        		return rs.getString(1);
        	}
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return "";
	}
	
	public static String getSummary(Connection con, String site, String title) {
		String query = "SELECT summary FROM games" + site +" WHERE title = '" + title +"'";

        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

        	if(rs.next()){
        		return rs.getString(1);
        	}
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return "";
	}
	
	public static Map<String, String> getAllTitlesAndSummaries(Connection con, String site) {
		String query = "SELECT title, summary FROM games" + site;

		Map<String, String> gameSummary = new HashMap<>();
        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

        	while (rs.next()) {
        		gameSummary.put(rs.getString(1), rs.getString(2));
        	}
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return gameSummary;
	}
	
	public static List<String> getComments(Connection con, String title) {
		String query = "SELECT comment FROM gamecomment WHERE game = '" + title +"'";

		List<String> comments = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                comments.add(rs.getString(1));
            }
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return comments;
	}
	
	public static Map<String, List<String>> getCommentsSince(Connection con, String site, String date) {
		String query = "SELECT game, comment FROM gamecomment" + site + 
					   " JOIN games" + site + " ON game = title " + 
					   "WHERE date > \"" + date +"\"";

		Map<String, List<String>> gameComments = new HashMap<>();
        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
            	String title = rs.getString(1);
            	if (gameComments.containsKey(title)) {
            		List<String> comments = gameComments.get(title);
            		comments.add(rs.getString(2));
            		gameComments.put(title, comments);
            		
            	} else {
            		List<String> comment = new ArrayList<>();
            		comment.add(rs.getString(2));
            		gameComments.put(title, comment);
            	}
            }
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return gameComments;
	}
	
	public static Map<String, Double> getSentimentScoreSince(Connection con, String site, String date) {
		String query = "SELECT game, AVG(sentimentScore) FROM gamecomment" + site + 
					   " JOIN games" + site + " ON game = title " + 
					   "WHERE date > \"" + date +"\" " + 
					   "GROUP BY game";

		Map<String, Double> gameScore = new HashMap<>();
        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
            	gameScore.put(rs.getString(1), rs.getDouble(2));
            }
            
        } catch (SQLException ex) {
        	System.err.println("ERROR with MySQL");
            ex.printStackTrace();
        }
        
        return gameScore;
	}
	
	public static void insertGameInfo(Connection con, String site, String title, String date, String reviewUrl, String summary) {
		String insertGameSummary = "INSERT INTO games" + site +"(title, date, url, summary) VALUES(?, ?, ?, ?)";
        
		try (PreparedStatement pst = con.prepareStatement(insertGameSummary)) {
			
			pst.setString(1, title);
			pst.setString(2, date);
			pst.setString(3, reviewUrl);
			pst.setString(4, summary);
			pst.executeUpdate();
            
            // System.out.println("A new game has been inserted");
			
		} catch (SQLException e) {
			System.err.println("ERROR with MySQL");
            e.printStackTrace();
		}
	}
	
	public static void insertGameCommentsAndSentimentScores(Connection con, String site, String title, List<String> comments) throws InvalidObjectException {
		for (String comment : comments) {
			if (comment == null || comment.equals("")) {
				continue;
			}
			insertGameCommentAndSentimentScore(con, site, title, comment);
		}
	}
	
	private static void insertGameCommentAndSentimentScore(Connection con, String site, String title, String comment) throws InvalidObjectException {
        String insertGameComments = "INSERT INTO gamecomment" + site +"(game, comment, sentimentScore) VALUES(?, ?, ?)";
        
		// calculate the sentiment score
        double sentimentScore = SentimentAnalyser.getSentimentResult(comment);
        
        try (PreparedStatement pst = con.prepareStatement(insertGameComments)) {
			
			pst.setString(1, title);
			pst.setString(2, comment);
			pst.setDouble(3, sentimentScore);
			pst.executeUpdate();
            
            // System.out.println("A new comment has been inserted");
			
		} catch (SQLException e) {
			System.err.println("ERROR with MySQL");
            e.printStackTrace();
		}
	}
}
