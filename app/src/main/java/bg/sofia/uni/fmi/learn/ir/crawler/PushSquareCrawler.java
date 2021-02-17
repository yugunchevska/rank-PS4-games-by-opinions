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

public class PushSquareCrawler {
	
	private static final String PUSH_SQUARE_URL = "https://www.pushsquare.com/ps4/reviews";
	
	private int currentPage;
	private WebDriver driver; 
	
	public PushSquareCrawler(WebDriver driver) {
		this.currentPage = 1; 
		
		this.driver = driver;
	    driver.manage().window().maximize();
	    driver.get(PUSH_SQUARE_URL);
	    
	    // accept cookies
		try {
			By cookies_accept = By.xpath("/html/body/div[3]/div[2]/div/ul/li[2]/button");
		    WebDriverWait wait = new WebDriverWait(driver, 10);
		    wait.until(ExpectedConditions.elementToBeClickable(cookies_accept)).click();
		    wait.until(ExpectedConditions.invisibilityOfElementLocated(cookies_accept));
		} catch (Exception e) {
			System.err.println("Error while accepting cookies");
			e.printStackTrace();
			throw e;
		}
	}

	private List<Review> getLatestReviews(String fromDate) {
		// get the last review stored in db
		MySqlConnection dbConn = new MySqlConnection("pushsquare");
		String latestUrlInDB = dbConn.getLatestUrl();
		
		List<Review> latestReviews = new ArrayList<>();
		boolean allReviewsFromWantedDateAreCollected = false;
	    while (!allReviewsFromWantedDateAreCollected) {
			// get list of all reviews from this site
		    WebElement reviewList = driver.findElement(By.xpath("//*[@id=\"listing-latest-reviews\"]/div/ul"));
	
		    // Go through each review
		    List<WebElement> reviews = reviewList.findElements(By.tagName("li"));
		    
		    for(WebElement reviewElement : reviews) {
		    	try {
			    	if (reviewElement.getAttribute("data-type").equals("review")) {
			    		
			        	List<WebElement> attributes = reviewElement.findElements(By.tagName("a"));
			        	WebElement heading = attributes.get(2);
			        	
			        	WebElement dateElement = reviewElement.findElement(By.tagName("time"));
			        	String reviewDateAndHours = dateElement.getAttribute("datetime");
			        	String reviewDate = reviewDateAndHours.split("T")[0];
			        	if (!isReviewDateAfterWantedDate(reviewDate, fromDate)) {
			        		// the review is older
			        		allReviewsFromWantedDateAreCollected = true;
			        		break;
			        	}
			        	
			        	// if the review from pushSquare is already in DB 
		                // => no need to store the rest of them again
			        	String reviewLink = heading.getAttribute("href");
		    			if (latestUrlInDB.equals(reviewLink)) {
		    				allReviewsFromWantedDateAreCollected = true;
		    				break;
		    			}
		    			
	//	                System.out.println("Heading    : " + heading.getText());
	//	                System.out.println("Date       : " + reviewDate);
	//	                System.out.println("Link       : " + reviewLink);
	//	                System.out.println("------------------------------");
		                
		    			Review review = new Review(heading.getText(), reviewDate, reviewLink);
		    			latestReviews.add(review);
			    	}
		    	} catch (NullPointerException e) {
		    		continue;
		    	}
		    }
		    
		    // check if the reviews are already collected
		    // if not - should go to the next page
		    if (!allReviewsFromWantedDateAreCollected) {
		    	goToNextPage();
		    }
	    }
	    
	    return latestReviews;
	}
	
	private boolean isReviewDateAfterWantedDate(String reviewDate, String fromDate) {
		LocalDate reviewRealDate = LocalDate.parse(reviewDate);
		LocalDate fromRealDate = LocalDate.parse(fromDate);
	
		return fromRealDate.compareTo(reviewRealDate) < 0;
	}
	
	private void goToNextPage() {
		WebElement nav = driver.findElement(By.className("ui-listing-pagination"));
		List<WebElement> pages = nav.findElements(By.tagName("li"));
		WebElement hrefElement = pages.get(currentPage + 1).findElement(By.tagName("a"));
		String nextPageUrl = hrefElement.getAttribute("href");
		driver.get(nextPageUrl);
	}

	public void storeReviewsInDB(String fromDate) throws IOException {
		List<Review> reviews = getLatestReviews(fromDate);
		
		for (Review review : reviews) {
			PushSquareReviewCrawler reviewCrawler = new PushSquareReviewCrawler(driver, review.getUrl());
			
			MySqlConnection dbConn = new MySqlConnection("pushsquare");
			dbConn.insertGame(reviewCrawler.getGameTitle(), 
							  review.getDate(), 
							  review.getUrl(), 
							  reviewCrawler.getSummaryInfo(), 
							  reviewCrawler.getComments());
		}
	}
	
	public static void main(String[] args) throws IOException, URISyntaxException {
		URL pathToChromeDriver = PushSquareCrawler.class.getClassLoader().getResource("chromedriver.exe");
		File chromeDriver = new File(pathToChromeDriver.toURI());
		System.setProperty("webdriver.chrome.driver", chromeDriver.getAbsolutePath());
		WebDriver driver = new ChromeDriver();
		
		PushSquareCrawler crawler  = new PushSquareCrawler(driver);
		crawler.storeReviewsInDB("2021-01-06");
		driver.quit();
	}
	
}
