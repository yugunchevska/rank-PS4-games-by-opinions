package bg.sofia.uni.fmi.learn.ii.crawler;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class PushSquareReviewCrawler {
	
	private WebDriver driver;
	
	public PushSquareReviewCrawler(WebDriver driver, String reviewUrl) {
		this.driver = driver;
		driver.get(reviewUrl);
	}

	public String getGameName() {	
		// find h1 with class "title"
		WebElement titleSection = driver.findElement(By.xpath("//*[@id=\"article\"]/header/section[1]/h1"));
		
		return titleSection.getText();
	}
	
	public String getReviewInfo() {
		// find the section with class "text"
		WebElement reviewSections = driver.findElement(By.xpath("//*[@id=\"article\"]/div/section[1]"));

		return reviewSections.getText();
	}
	
	public List<String> getComments() {
		List<String> comments = new ArrayList<String>();
		
		// find <div class="comments-list">
		WebElement commentsList = driver.findElement(By.xpath("//*[@id=\"content\"]/div[2]/div[2]"));
		List<WebElement> webComments = commentsList.findElements(By.tagName("article"));
		for (WebElement webComment : webComments) {
			try {
				if (webComment.getAttribute("class").equals("comment")) {
					WebElement text = webComment.findElement(By.ByClassName.className("text"));
					comments.add(text.getText());
				}
			} catch (NullPointerException e) {
				continue;
			}
		}
		
		return comments;
	}
}