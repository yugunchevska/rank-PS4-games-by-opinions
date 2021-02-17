package bg.sofia.uni.fmi.learn.ir.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;

public class LuceneSearcher {
	
	private Directory memoryIndex;
	
	public LuceneSearcher() {
		 memoryIndex = new ByteBuffersDirectory(); // or MMapDirectory();
	}

	public void indexDocuments(Map<String, String> titleSumary) throws IOException {
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		IndexWriter writter = new IndexWriter(memoryIndex, indexWriterConfig);
		
		
		for (Map.Entry<String, String> entry : titleSumary.entrySet()) {
			Document document = new Document();
	
			document.add(new TextField("title", entry.getKey(), Field.Store.YES));
			document.add(new TextField("summary", entry.getValue(), Field.Store.YES));
	
			writter.addDocument(document);
		}
		
		writter.close();
	}
	
	public List<Document> searchByTitle(String searchingFor) throws ParseException, IOException {
		Term term = new Term("title", searchingFor);
		Query query = new PrefixQuery(term);

		IndexReader indexReader = DirectoryReader.open(memoryIndex);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TopDocs topDocs = searcher.search(query, 10);
		List<Document> documents = new ArrayList<>();
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
		    documents.add(searcher.doc(scoreDoc.doc));
		}

		return documents;
	}
	
	public static void main(String[] args) throws IOException, ParseException {
		String title1 = "Control";
		String summary1 = "Quite creepy to be honest";
		
		String title2 = "Concrete Genie";
		String summary2 = "Wasn't worth waiting 5 years";
		
		Map<String, String> map = new HashMap<>();
		map.put(title1, summary1);
		map.put(title2, summary2);
		
		LuceneSearcher searcher = new LuceneSearcher();
		searcher.indexDocuments(map);
		
		System.out.println("Search for:");
		Scanner scanner = new Scanner(System.in);
		try {
			String searchingFor = scanner.nextLine();
			
			List<Document> summary = searcher.searchByTitle(searchingFor);
			System.out.println("Docs found: " + summary);
		} finally {
			scanner.close();
		}
	}
}
