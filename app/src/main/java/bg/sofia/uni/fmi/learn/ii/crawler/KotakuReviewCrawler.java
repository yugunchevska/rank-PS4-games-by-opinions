package bg.sofia.uni.fmi.learn.ii.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import bg.sofia.uni.fmi.learn.ee.summary.TextSummarization;

public class KotakuReviewCrawler {
	private WebDriver driver;
	
	public KotakuReviewCrawler(WebDriver driver, String reviewUrl) {
		this.driver = driver;
		driver.get(reviewUrl);
	}

	public String getReviewTitle() {	
		// find tagger header 
		WebElement header = driver.findElement(By.tagName("header"));
		WebElement h1Header = header.findElement(By.tagName("h1"));
		return h1Header.getText();
	}
	
	public String getReviewInfo() {
		List<WebElement> divs = driver.findElements(By.tagName("div"));
		
		StringBuilder review = new StringBuilder();
		for (WebElement div : divs) {
			try {
			String divClass = div.getAttribute("class");
				if (divClass.contains("js") && divClass.contains("post") && divClass.contains("content")) {
					List<WebElement> paragraphs = div.findElements(By.tagName("p"));
					for (WebElement paragraph : paragraphs) {
						review.append(paragraph.getText()).append("\n");
					}
				}
			} catch (NullPointerException e) {
				System.out.println("The div in Kotaku review doesn't has class.");
			}
		}
		
		// remove the review box
		List<WebElement> sections = driver.findElements(By.tagName("section"));
		
		StringBuilder reviewBox = new StringBuilder();
		for (WebElement section : sections) {
			try {
			String sectionClass = section.getAttribute("class");
				if (sectionClass.contains("reviewbox-inset")) {
					List<WebElement> paragraphs = section.findElements(By.tagName("p"));
					for (WebElement paragraph : paragraphs) {
						reviewBox.append(paragraph.getText()).append("\n");
					}
				}
			} catch (NullPointerException e) {
				System.out.println("The div in Kotaku review doesn't has class.");
			}
		}
		
		int i = review.indexOf(reviewBox.toString());
		if (i != -1) {
			review.delete(i, i + reviewBox.length());
		}
		
		// remove the releted reviews
		List<WebElement> asides = driver.findElements(By.tagName("aside"));
		
		for (WebElement aside : asides) {
			try {
				String asideParagraph = aside.findElement(By.tagName("p")).getText();
				
				int j = review.indexOf(asideParagraph);
				if (j != -1) {
					review.delete(j, j + asideParagraph.length());
				}
			} catch (Exception e) {
				System.err.println("WARN: Some asides doesnt have paragraphs");
			}
		}
		
		return review.toString();
	}
	
	public String getSummaryInfo() throws IOException {
		String review = getReviewInfo();
		
		return TextSummarization.summarize(review);
	}
	
	public List<String> getComments() throws InterruptedException {
		// load more comments first
		By seeAllRepliesButton = By.className("js_comments-button--show");
	    WebDriverWait wait = new WebDriverWait(driver, 10);
	    wait.until(ExpectedConditions.elementToBeClickable(seeAllRepliesButton)).click();
	    wait.until(ExpectedConditions.invisibilityOfElementLocated(seeAllRepliesButton));
		
		System.err.println("SUCCESS: clicked");
		
		// change to iframe
		List<String> comments = new ArrayList<String>();
		List<WebElement> iframeCommentsTags = driver.findElements(By.className("js_comments-iframe"));
		
		for (WebElement tag : iframeCommentsTags) {
			
			if (tag.getTagName().equals("iframe")) {
				
				driver.switchTo().frame(tag);
				System.err.println("SUCCESS: in the comments iframe");
				
				// load the rest of the comments
				while (true) {
					try {
						By moreComments = By.className("js_button--pagination");
						wait.until(ExpectedConditions.elementToBeClickable(moreComments)).click();
					    wait.until(ExpectedConditions.invisibilityOfElementLocated(moreComments));
					} catch (Exception e) {
						//e.printStackTrace();
						break;
					}
				}
				
				System.err.println("SUCCESS: all comments are loaded.");
				
				// store all comments 
				List<WebElement> commentElements = driver.findElements(By.className("js_reply-content"));
				for(WebElement commentElement : commentElements) {
					List<WebElement> commentParagraphs  = commentElement.findElements(By.tagName("p"));
					StringBuilder comment = new StringBuilder();
					
					for (WebElement paragraph : commentParagraphs) {
						comment.append(paragraph.getText()).append("\n");
					}
					
					comments.add(comment.toString());
				}
				 
				
				break;
			}
		}
		driver.switchTo().defaultContent();
		
		return comments;
	}
}
