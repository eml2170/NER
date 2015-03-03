import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;


/**
 * This class tags each word, according to the emission probabilities
 * It also uses the InfrequentWordMapper to determine if a word is rare.
 * @author edwardliu
 */
public class EntityTagger {

	EmissionProbability ep;
	InfrequentWordMapper counter;
	private static final String RARE = "_RARE_";
	private static final String NEWLINE = "\n";
	private static final String SPACE = " ";
	
	
	//private static final String INITCAP = "initCap";
	private static final String OTHERNUM = "otherNum";
	//private static final String ALLCAPS = "allCaps";
	private static final String CONTAINS_DIGIT_AND_DASH = "containsDigitAndSlash";
	private static final String CONTAINS_DIGIT_AND_PUNCTUATION = "containsDigitAndPunctuation";
	//private static final String PUNCTUATION = "punctuation";
	
	public EntityTagger(EmissionProbability ep, InfrequentWordMapper counter){
		this.ep = ep;
		this.counter = counter;
	}
	
	public Prediction tag(String x){
		double max = 0.0;
		Tag argmax = null;
		
		
		
		//If the word is rare, substitute it with the rare symbol
		if(counter.hasNumber(x)){
			if(counter.hasNumberAndDash(x))
				x = CONTAINS_DIGIT_AND_DASH;
			else if(counter.hasNumberAndCommaOrPeriod(x))
				x = CONTAINS_DIGIT_AND_PUNCTUATION;
			else 
				x = OTHERNUM;
		}
//		else if(counter.isPunctuation(x))
//			x = PUNCTUATION;
//		else if(counter.isAllCaps(x))
//			x = ALLCAPS;
//		else if(counter.isInitCap(x))
//		x = INITCAP;
		else if(counter.isRare(x))
			x = RARE;
		

		
		//Find argmax over all y
		for(Tag t : Tag.values()){
			double p = ep.e(x,t);
			
			if(p > max){
				max = p;
				argmax = t;
			}
		}
		
		return new Prediction(argmax, Math.log(max));
	}
	
	public static void main(String[] args) throws IOException{
		
		EntityTagger tagger = new EntityTagger(new EmissionProbability("ner_mapped.counts"),
				new InfrequentWordMapper("ner.counts"));
		
		Scanner scanner = new Scanner(new File("ner_dev.dat"));
		FileWriter filewriter = new FileWriter(new File("prediction_file"));
		
		while(scanner.hasNextLine()){
			String word = scanner.nextLine();
			if(word.isEmpty()){
				filewriter.write(NEWLINE);
				continue;
			}
			Prediction pred = tagger.tag(word);
			filewriter.write(word + SPACE + pred.t + SPACE + pred.logp + NEWLINE);
		}
		
		filewriter.close();
		scanner.close();
	}
	
}
