package bg.sofia.uni.fmi.learn.ii.crawler;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class PushSquareCrawler {
	
	private static final String PUSH_SQUARE_URL = "https://www.pushsquare.com/ps4/reviews";
	
	private WebDriver driver;
	
	public PushSquareCrawler() {
		System.setProperty("webdriver.chrome.driver", "D:\\java-projects\\InfRetr\\chromedriver.exe");
		
	    driver = new ChromeDriver();
	    driver.manage().window().maximize();
	    driver.get(PUSH_SQUARE_URL);
	}
	
	// TODO add arg date - from which date to take reviews
	// TODO should go to the next page automatically - another method maybe; stops when the date is met
	public List<String> getLinksToLatestReviews() {	    
	    // get list of all reviews
	    WebElement reviewList = driver.findElement(By.xpath("//*[@id=\"listing-latest-reviews\"]/div/ul"));

	    // Go through each review
	    List<WebElement> reviews = reviewList.findElements(By.tagName("li"));
	    List<String> reviewLinks = new ArrayList<String>();
	    for(WebElement review : reviews) {
	    	try {
		    	if (review.getAttribute("data-type").equals("review")) {
	    	
		        	List<WebElement> attributes = review.findElements(By.tagName("a"));
		        	WebElement heading = attributes.get(2);
		        	
		        	WebElement date = review.findElement(By.tagName("time"));
		        	
	                System.out.println("Heading    : " + heading.getText());
	                System.out.println("Date       : " + date.getAttribute("datetime"));
	                System.out.println("Link       : " + heading.getAttribute("href"));
	                System.out.println("------------------------------");
	                
	                reviewLinks.add(heading.getAttribute("href"));
		    	}
	    	} catch (NullPointerException e) {
	    		continue;
	    	}
	    }
	    
	    return reviewLinks;
	}
	
	public void storeReviewsInDB() {
		List<String> urls = getLinksToLatestReviews();
		
		for (String url : urls) {
			PushSquareReviewCrawler reviewCrawler = new PushSquareReviewCrawler(driver, url);
			// TODO store grame's info in DB
			System.out.println(reviewCrawler.getGameName());
//			System.out.println(reviewCrawler.getReviewInfo());
//			System.out.println(reviewCrawler.getComments());
		}
	}
	
	public void closeDriver() {
		driver.quit();
	}
	
	public static void main(String[] args) {
		PushSquareCrawler crawler  = new PushSquareCrawler();
		crawler.storeReviewsInDB();
		crawler.closeDriver();
	}
	
}
