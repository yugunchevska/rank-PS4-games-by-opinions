package bg.sofia.uni.fmi.learn.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlStatements {
	
	public static boolean doesGameExists(Connection con, String site, String title) {
		String query = "SELECT title FROM games" + site +" WHERE title = '" + title +"'";

        try (PreparedStatement pst = con.prepareStatement(query);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                System.out.println(rs.getString(1));
                
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
	
	public static void insertGameInfo(Connection con, String site, String title, String date, String reviewUrl, String summary) {
		String insertGameSummary = "INSERT INTO games" + site +"(title, date, url, summary) VALUES(?, ?, ?, ?)";
        
		try (PreparedStatement pst = con.prepareStatement(insertGameSummary)) {
			
			pst.setString(1, title);
			pst.setString(2, date);
			pst.setString(3, reviewUrl);
			pst.setString(4, summary);
			pst.executeUpdate();
            
            System.out.println("A new game has been inserted");
			
		} catch (SQLException e) {
			System.err.println("ERROR with MySQL");
            e.printStackTrace();
		}
	}
	
	public static void insertGameComments(Connection con, String site, String title, List<String> comments) {
		for (String comment : comments) {
			insertGameComment(con, site, title, comment);
		}
	}
	
	private static void insertGameComment(Connection con, String site, String title, String comment) {
        String insertGameComments = "INSERT INTO gamecomment" + site +"(game, comment) VALUES(?, ?)";
        
		try (PreparedStatement pst = con.prepareStatement(insertGameComments)) {
			
			pst.setString(1, title);
			pst.setString(2, comment);
			pst.executeUpdate();
            
            System.out.println("A new comment has been inserted");
			
		} catch (SQLException e) {
			System.err.println("ERROR with MySQL");
            e.printStackTrace();
		}
	}
}