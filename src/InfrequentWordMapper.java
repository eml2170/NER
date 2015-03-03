import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class is used to close the vocabulary, replacing
 * rare words with "_RARE"
 * @author edwardliu
 */
public class InfrequentWordMapper {

	private Map<String, Integer> freqMap;
	private static final String RARE = "_RARE_";
	private static final String SPACE = " ";
	private static final String NEWLINE = "\n";
	private static final String WORDTAG = "WORDTAG";
	
	//Modification for #6
	//private static final String INITCAP = "initCap";
	//private Set<String> initCapSet;
	private static final String OTHERNUM = "otherNum";
	//private static final String ALLCAPS = "allCaps";
	private static final String CONTAINS_DIGIT_AND_DASH = "containsDigitAndSlash";
	private static final String CONTAINS_DIGIT_AND_PUNCTUATION = "containsDigitAndPunctuation";
	//private static final String PUNCTUATION = "punctuation";
	//Takes ner.counts
	public InfrequentWordMapper(String count_filename){
		freqMap = new HashMap<String, Integer>();
		//initCapSet = new HashSet<String>();
		try {
			Scanner scanner = new Scanner(new File(count_filename));
			
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				
				if(line.isEmpty()){
					continue;
				}
				
				String[] tokens = line.split(SPACE);
				
				//If the line is a count for a word
				if(tokens[1].equals(WORDTAG)){
					String word = tokens[3];

					//Increment freq by count given
					int count = Integer.parseInt(tokens[0]);
					freqMap.put(word, !freqMap.containsKey(word) ? count :
						freqMap.get(word) + count);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Replace original training data set with _RARE_ words based on freqMap
	 * @param oldFilename
	 */
	public void map(String oldFilename){
		FileWriter filewriter;
		String newFilename = oldFilename + "_mapped.dat";
		try {
			filewriter = new FileWriter(newFilename);
			Scanner scanner = new Scanner(new File(oldFilename + ".dat"));
			//boolean isStart = true;
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				
				//skip empty lines
				if(line.isEmpty()){
				//	isStart = true;
					filewriter.write(NEWLINE);
					continue;
				}
				
				String[] tokens = line.split(SPACE);
				String word = tokens[0];
				
				//if the word has appeared less than 5 times, it's rare
				if(hasNumber(word)){
					if(hasNumberAndDash(word))
						line = CONTAINS_DIGIT_AND_DASH + SPACE + tokens[1];
					else if(hasNumberAndCommaOrPeriod(word))
						line = CONTAINS_DIGIT_AND_PUNCTUATION + SPACE + tokens[1];
					else
						line = OTHERNUM + SPACE + tokens[1];
				}
//				else if(isPunctuation(word))
//					line = PUNCTUATION + SPACE + tokens[1];
//				else if(isAllCaps(word))
//					line = ALLCAPS + SPACE + tokens[1];
//				else if//If the word is capitalized and not the first word of a sentence
//				(!isStart && Character.isUpperCase(word.charAt(0))){
//				initCapSet.add(word);
//				System.out.println(word);
//				line = INITCAP + SPACE + tokens[1];
//			}
				else if(isRare(word))
					line = RARE + SPACE + tokens[1];
				
				
			
					

				filewriter.write(line + NEWLINE);
//				isStart = false;
			}
			scanner.close();
			filewriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRare(String word){
		if(!freqMap.containsKey(word)) return true;
		return freqMap.get(word) < 5;
	}
	
//	public boolean isInitCap(String word){
//		 return initCapSet.contains(word); 
//	}
	
	public boolean hasNumber(String word){
		return word.matches(".*\\d+.*");
	}
	
	public boolean hasNumberAndDash(String word){
		for(int i = 0; i < word.length(); i++){
			if(word.charAt(i) == '-')
				return true;
		}
		return false;
	}
	
	public boolean hasNumberAndCommaOrPeriod(String word){
		for(int i = 0; i < word.length(); i++){
			if(word.charAt(i) == '.' || word.charAt(i) == ',')
				return true;
		}
		return false;
	}
	
	public boolean isAllCaps(String word){
		for(int i = 0; i < word.length(); i++){
			if(!Character.isUpperCase(word.charAt(i)))
				return false;
		}
		return true;
	}
	
	public boolean isPunctuation(String word){
		return word.matches("\\p{Punct}");
	}
	
	public static void main(String[] args){
		InfrequentWordMapper iwm = new InfrequentWordMapper("ner.counts");
		iwm.map("ner_train");
	}
}
