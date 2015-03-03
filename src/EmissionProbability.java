import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class uses the counts file to compute emission probabilities.
 * @author edwardliu
 *
 */
public class EmissionProbability {

	Map<Tag, TagData> tagMap;
	private static final String WORDTAG = "WORDTAG";
	private static final String SPACE = " ";
	
	public EmissionProbability(String counts_file){
		tagMap = new HashMap<Tag, TagData>();
		for(Tag t : Tag.values()){
			tagMap.put(t, new TagData(t));
		}
		build(counts_file);
	}
	
	private void build(String counts_file){
		try {
			Scanner scanner = new Scanner(new File(counts_file));
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				String[] tokens = line.split(SPACE);
				if(tokens[1].equals(WORDTAG)){
					Tag t = Tag.fromString((tokens[2]));
					TagData tagData = tagMap.get(t);
					
					//update tag's total count
					int count = Integer.parseInt(tokens[0]);
					tagData.totalCount += count;
					
					//add specific word count
					String word = tokens[3];
					tagData.counts.put(word, count);
				}
			}
			
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public double e(String x, Tag y){
		TagData data = tagMap.get(y);
		//Emission probability should be 0 not infinity for Count(y)=0
		if(data.totalCount == 0) return 0.0;
		
		//Count(y->x)
		double e = data.counts.containsKey(x) ? (double) data.counts.get(x) : 0.0;
		
		//divide by Count(y)
		return e/((double) data.totalCount);
	}

	public static void main(String[] args){
		EmissionProbability ep = new EmissionProbability("ner_mapped.counts");
		
		//Prints e(_RARE_|y) for each y.
		String word = "_RARE_";
		for(Tag t : Tag.values())
		System.out.println("e(" + word + "|" + t + ")=" + ep.e(word, t));
		
	}
}

/**
 * Helper object stored in HashMap. Contains its own map of counts (for each word)
 * and keeps track of the total count.
 */
class TagData{
	
	Tag tag;
	int totalCount;
	Map<String, Integer> counts;
	
	public TagData(Tag tag){
		this.tag = tag;
		totalCount = 0;
		counts = new HashMap<String, Integer>();
	}
	
}