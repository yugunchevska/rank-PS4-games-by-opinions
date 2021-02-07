package bg.sofia.uni.fmi.learn.main;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import bg.sofia.uni.fmi.learn.ir.crawler.KotakuCrawler;
import bg.sofia.uni.fmi.learn.ir.crawler.PushSquareCrawler;
import bg.sofia.uni.fmi.learn.ir.search.LuceneSearcher;

public class Main {
	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
		Scanner scanner = new Scanner(System.in);
		try {
			// 0. ask for which dates to see the rank
			System.out.println("Show top 10 PS4 games from: [1 week/1 month/2 months]");
			String timePeriod = scanner.nextLine();
			// 0.1 parse it to a real date
			String fromDate = parseToDate(timePeriod);
			if (fromDate.equals("wrongTimePeriod")) {
				System.out.println("This is not a valid time period. Please, choose one from above mentioned.");
				return; // continue;
			}
			// should be a log message
			System.out.println("date after parsing: " + fromDate);
			
			// 1. store info from sites in DB
			// 1.1 should use only one driver, we want it faster
			// 1.2 should crawl from some date 
			System.setProperty("webdriver.chrome.driver", "D:\\java-projects\\InfRetr\\chromedriver.exe");
			WebDriver driver = new ChromeDriver();
			
			PushSquareCrawler pushSquareCrawler = new PushSquareCrawler(driver);
			pushSquareCrawler.storeReviewsInDB(fromDate);
			
			KotakuCrawler kotakuCrawler = new KotakuCrawler(driver);
			kotakuCrawler.storeReviewsInDB(fromDate);
			
			driver.quit();
			
//			// 1.2 extract from DB all games and reviews
//			Map<String, String> gameReview = new HashMap<>();
//			
//			// 1.2 add all documents to lucene
//			LuceneSearcher searcher = new LuceneSearcher();
//			searcher.indexDocuments(gameReview);
//			
//			// 2.0 take all comments from DB since #timePeriod
//			
//			// 2.1 run sentiment analyser on those comments for every game since #timeperiod
//			
//			
//			// 3. rank the games, top 10
//			
//			// 4. ask if it wants to see a summary for some game 
//			System.out.println("Show summary for: ");
//			String gameTitleToSearchFor = scanner.nextLine();
//			
//			// 4.1 search for that game
//			List<Document> foundTitles = searcher.searchByTitle(gameTitleToSearchFor);
//			
//			// 4.2 list all titles and the user should choose one of them
//			System.out.println("Titles found: " + foundTitles.get(0).get("summary"));
//			
//			// 5. show the summary
//			System.out.println(foundTitles.get(0).get("summary"));
			
		} finally {
			scanner.close();
		}
	}
	
	private static String parseToDate(String timePeriod) {
		LocalDate today = LocalDate.now();
		switch(timePeriod) {
			case "1 week": return today.minusDays(7).toString();
			case "1 month": return today.minusMonths(1).toString();
			case "2 months": return today.minusMonths(2).toString();
			default: return "wrongTimePeriod";
		}
	}

}
