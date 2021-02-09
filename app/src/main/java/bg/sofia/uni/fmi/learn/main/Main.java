package bg.sofia.uni.fmi.learn.main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import bg.sofia.uni.fmi.learn.sql.MySqlConnection;

public class Main {
	
	public static void main(String[] args) throws IOException, InterruptedException, ParseException, URISyntaxException {
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
			// System.out.println("date after parsing: " + fromDate);
			
			// 1. store info from sites in DB
			// 1.1 should use only one driver, we want it faster
			// 1.2 should crawl from some date 
			URL pathToChromeDriver = PushSquareCrawler.class.getClassLoader().getResource("chromedriver.exe");
			File chromeDriver = new File(pathToChromeDriver.toURI());
			System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
			WebDriver driver = new ChromeDriver();
			
			PushSquareCrawler pushSquareCrawler = new PushSquareCrawler(driver);
			pushSquareCrawler.storeReviewsInDB(fromDate);
			
			KotakuCrawler kotakuCrawler = new KotakuCrawler(driver);
			kotakuCrawler.storeReviewsInDB(fromDate);
			
			driver.quit();
			
			// 1.2 extract from DB all games and summaries
			Map<String, String> gameSummary = new HashMap<>();
			MySqlConnection connPushSquare = new MySqlConnection("pushsquare");
			gameSummary.putAll(connPushSquare.getAllTitlesAndSummaries());
			
			MySqlConnection connKotaku = new MySqlConnection("kotaku");
			gameSummary.putAll(connKotaku.getAllTitlesAndSummaries());
			
			// 1.2 add all documents to lucene
			LuceneSearcher searcher = new LuceneSearcher();
			searcher.indexDocuments(gameSummary);
			
			// 2 take all comments from DB for game reviews since #timePeriod
			Map<String, Double> gameSentimentScore = new HashMap<>();
			gameSentimentScore.putAll(connPushSquare.getSentimentScoreSince(fromDate));
			gameSentimentScore.putAll(connKotaku.getSentimentScoreSince(fromDate));
			
			// 3. rank the games, top 10
			Map<String, Double> gamesScoreSorted = new LinkedHashMap<>();
			gameSentimentScore.entrySet()
							  .stream()
							  .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							  .forEachOrdered(x -> gamesScoreSorted.put(x.getKey(), x.getValue()));
			
			System.out.println("Top 10 games from " + fromDate);
			int bestGamesIndex = 1;
			for (Map.Entry<String, Double> gameScore : gamesScoreSorted.entrySet()) {
				if (bestGamesIndex == 11) {
					break;
				}
				System.out.print(bestGamesIndex + ". " + gameScore.getKey());
				System.out.println();
				++bestGamesIndex;
			}
			
			// 4. ask if it wants to see a summary for some game 
			System.out.println("Show summary for: ");
			String gameTitleToSearchFor = scanner.nextLine();
			
			// 4.1 search for that game
			List<Document> foundTitles = searcher.searchByTitle(gameTitleToSearchFor);
			for (int i = 0; i < foundTitles.size(); ++i) {
				System.out.print(i + ": " + foundTitles.get(i).get("title"));
				System.out.println();
			}
			System.out.println("Choose number of wanted game.");
			int gameNumber = scanner.nextInt();
			String wantedSummary = foundTitles.get(gameNumber).get("summary");
			System.out.println(wantedSummary);
			
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
