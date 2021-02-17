package bg.sofia.uni.fmi.learn.ir.crawler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import bg.sofia.uni.fmi.learn.pojo.Review;
import bg.sofia.uni.fmi.learn.sql.MySqlConnection;

public class KotakuCrawler {
	private static final String KOTAKU_URL = "https://kotaku.com/c/review/video-games";
	
	private WebDriver driver;
	
	public KotakuCrawler(WebDriver driver) {
		this.driver = driver;
	    driver.manage().window().maximize();
	    driver.get(KOTAKU_URL);
	    
	    // accept cookies
	    driver.switchTo().frame("sp_message_iframe_388533");
	    
		try {
			By cookies_accept = By.xpath("/html/body/div/div[3]/div[5]/button[2]");
		    WebDriverWait wait = new WebDriverWait(driver, 10);
		    wait.until(ExpectedConditions.elementToBeClickable(cookies_accept)).click();
		    wait.until(ExpectedConditions.invisibilityOfElementLocated(cookies_accept));
		} catch (Exception e) {
			System.err.println("Error while accepting cookies");
			e.printStackTrace();
			throw e;
		}
		
		driver.switchTo().defaultContent();
	}
	
	// TODO should go to the next page automatically - another method maybe; stops when the date is met
	private List<Review> getLinksOfLatestReviews(String fromDate) {	    
		// get the last review stored in db
		MySqlConnection dbConn = new MySqlConnection("kotaku");
		String latestUrlInDB = dbConn.getLatestUrl();
				
		// get list of all reviews	
	    WebElement reviewList = driver.findElement(By.className("sc-17uq8ex-0"));
	    // Go through each review article 
	    List<WebElement> articles = reviewList.findElements(By.tagName("article"));
	    List<Review> reviews = new ArrayList<>();
	    for(WebElement article : articles) {
	    	try {
	    		WebElement heading = article.findElement(By.tagName("h2"));	
	    	
	    		List<WebElement> attributes = article.findElements(By.tagName("a"));
		        WebElement headingRef = attributes.get(3);
		        	
		        WebElement date = article.findElement(By.tagName("time"));
		        String reviewDateAndHours = date.getAttribute("datetime");
		        String reviewDate = reviewDateAndHours.split("T")[0];
		        if (!isReviewDateAfterWantedDate(reviewDate, fromDate)) {
	        		// the review is older
	        		break;
	        	}
	        	
	        	// if the review from pushSquare is already in DB 
                // => no need to store the rest of them again
	        	String reviewLink = headingRef.getAttribute("href");
    			if (latestUrlInDB.equals(reviewLink)) {
    				break;
    			}
		        	
    			// this should be a log 
//	            System.out.println("Heading    : " + heading.getText());
//	            System.out.println("Date       : " + date.getAttribute("datetime"));
//	            System.out.println("Link       : " + headingRef.getAttribute("href"));
//	            System.out.println("------------------------------");
	                
	            Review review = new Review(heading.getText(), reviewDate, reviewLink);
	            reviews.add(review);
	    	} catch (NullPointerException e) {
	    		System.err.println("Error while searching for reviews from Kotaku.");
	    		e.printStackTrace();
	    		continue;
	    	}
	    }
	    
	    return reviews;
	}
	
	private boolean isReviewDateAfterWantedDate(String reviewDate, String fromDate) {
		LocalDate reviewRealDate = LocalDate.parse(reviewDate);
		LocalDate fromRealDate = LocalDate.parse(fromDate);
	
		return fromRealDate.compareTo(reviewRealDate) < 0;
	}
	
	public void storeReviewsInDB(String fromDate) throws InterruptedException, IOException {
		List<Review> reviews = getLinksOfLatestReviews(fromDate);
		
		for (Review review : reviews) {
			KotakuReviewCrawler reviewCrawler = new KotakuReviewCrawler(driver, review.getUrl());
			
			// check if this game is for ps4
			if (reviewCrawler.isSuitableForPS4()) {
				MySqlConnection dbConn = new MySqlConnection("kotaku");
				dbConn.insertGame(reviewCrawler.getGameTitle(), 
							  review.getDate(), 
							  review.getUrl(), 
							  reviewCrawler.getSummaryInfo(), 
							  reviewCrawler.getComments());
			}
		}
	}

	public static void main(String[] args) throws InterruptedException, IOException, URISyntaxException {
		URL pathToChromeDriver = PushSquareCrawler.class.getClassLoader().getResource("chromedriver.exe");
		File chromeDriver = new File(pathToChromeDriver.toURI());
		System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
		WebDriver driver = new ChromeDriver();
		
		KotakuCrawler crawler  = new KotakuCrawler(driver);
		crawler.storeReviewsInDB("2020-12-14");	
		driver.quit();
	}
}
