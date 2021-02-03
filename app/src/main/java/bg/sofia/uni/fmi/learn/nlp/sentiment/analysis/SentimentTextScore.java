package bg.sofia.uni.fmi.learn.nlp.sentiment.analysis;

public class SentimentTextScore {
	
	private int sumSentimentSentencesScore;
	private int sentencesCount;
	
	public SentimentTextScore(int sentencesCount) {
		this.sumSentimentSentencesScore = 0;
		this.sentencesCount = sentencesCount;
	}

	public void addSentimentSentenceScore(int sentenceScore) {
		this.sumSentimentSentencesScore += sentenceScore;
	}
	
	public double getSentimentScore() {
		return ((double) sumSentimentSentencesScore) / ((double) sentencesCount);
	}
}
