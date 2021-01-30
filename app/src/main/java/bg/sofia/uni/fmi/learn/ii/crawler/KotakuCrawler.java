package bg.sofia.uni.fmi.learn.ii.crawler;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class KotakuCrawler {
	private static final String KOTAKU_URL = "https://kotaku.com/c/review";
	
	private WebDriver driver;
	
	public KotakuCrawler() {
		System.setProperty("webdriver.chrome.driver", "D:\\java-projects\\InfRetr\\chromedriver.exe");
		
	    driver = new ChromeDriver();
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
	
	// TODO add arg date - from which date to take reviews
	// TODO should go to the next page automatically - another method maybe; stops when the date is met
	public List<String> getLinksToLatestReviews() {	    
	    // get list of all reviews
	    WebElement reviewList = driver.findElement(By.xpath("/html/body/div[3]/div[5]/main/div/div[4]"));

	    // Go through each review article 
	    List<WebElement> articles = reviewList.findElements(By.tagName("article"));
	    List<String> reviewLinks = new ArrayList<String>();
	    for(WebElement article : articles) {
	    	try {
	    		WebElement heading = article.findElement(By.tagName("h2"));	
	    	
	    		List<WebElement> attributes = article.findElements(By.tagName("a"));
		        WebElement headingRef = attributes.get(3);
		        	
		        WebElement date = article.findElement(By.tagName("time"));
		        	
	            System.out.println("Heading    : " + heading.getText());
	            System.out.println("Date       : " + date.getAttribute("datetime"));
	            System.out.println("Link       : " + headingRef.getAttribute("href"));
	            System.out.println("------------------------------");
	                
	            reviewLinks.add(headingRef.getAttribute("href"));
	    	} catch (NullPointerException e) {
	    		System.err.println("Error while searching for reviews from Kotaku.");
	    		e.printStackTrace();
	    		continue;
	    	}
	    }
	    
	    return reviewLinks;
	}
	
	// TODO store grame's info in DB
	public void storeReviewsInDB() throws InterruptedException {
		List<String> urls = getLinksToLatestReviews();
		
		for (String url : urls) {
			KotakuReviewCrawler reviewCrawler = new KotakuReviewCrawler(driver, url);
			
			System.out.println(reviewCrawler.getReviewTitle());
			//System.out.println(reviewCrawler.getReviewInfo());
			System.out.println(reviewCrawler.getComments());
		}
	}
	
	public void closeDriver() {
		driver.quit();
	}

	public static void main(String[] args) throws InterruptedException {
		KotakuCrawler crawler  = new KotakuCrawler();
		crawler.storeReviewsInDB();	
		crawler.closeDriver();
	}
}
