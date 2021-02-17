package bg.sofia.uni.fmi.learn.nlp.sentiment.analysis;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyser {
	
	public static double getSentimentResult(String text) throws InvalidObjectException {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
		if (text != null && text.length() > 0) {
			// run all Annotators on the text
			Annotation annotation = pipeline.process(text);
			
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			SentimentTextScore textScore = new SentimentTextScore(sentences.size());
			for (CoreMap sentence : sentences) {
				// this is the parse tree of the current sentence
				Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);

				textScore.addSentimentSentenceScore(RNNCoreAnnotations.getPredictedClass(tree));
				//print the sentiment score using RNNCoreAnnotations
				// System.out.println("Sentiment Score: " + RNNCoreAnnotations.getPredictedClass(tree));
				
				String sentimentType = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
				// System.out.println("sentiment type: " + sentimentType);
			}
			
			return textScore.getSentimentScore();
		}
		
		throw new InvalidObjectException(String.format("Text '%s' can't be sentiment analysed.", text));
	}

	public static void main(String[] args) throws InvalidObjectException {
		String text1 = "Ok, cool thanks. I’ll take a second look. I enjoy the game but I’m an old man and I get ornery when I think a company is trying to gouge me.\r\n" + 
				"\r\n" + 
				"Which in my defense happens ALL time lol.";
		String text2 = "The main plot never seemed to matter much in the last two games.  I always forgot why people needed hitmaned because nothing ever felt connected.  It was just a generic list of bad guys.  I’m glad if it does feel different in this game.";
		
		String text3 = "Yep looks like Lillymo and CLS or last stand media as they are now know. keep capturing lighting in a bottle - bravo Colin . Looking forward to playing this on the vita";
		
		List<String> comments = new ArrayList<>();
		comments.add(text1);
		comments.add(text2);
		String onePlace = comments.toString();
		double score = SentimentAnalyser.getSentimentResult(text3);
		
		System.out.println("the score for the whole comment is: " + score);
	}
}
