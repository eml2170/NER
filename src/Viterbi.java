import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


/**
 * My implementation of the Viterbi Algorithm
 * @author edwardliu
 *
 */
public class Viterbi {
	
	//Need these to compute pi
	TrigramEstimator te;
	EmissionProbability ep;
	
	//Need this to determine rare words
	InfrequentWordMapper counter;
	
	//Need this for one word sentences
	EntityTagger et;
	
	double[][][] pi;
	Tag[][][] bp;
	
	private static final String RARE = "_RARE_";
	private static final String NEWLINE = "\n";
	private static final String SPACE = " ";
	
	
//	private static final String INITCAP = "initCap";
	private static final String OTHERNUM = "otherNum";
	private static final String CONTAINS_DIGIT_AND_DASH = "containsDigitAndSlash";
	private static final String CONTAINS_DIGIT_AND_PUNCTUATION = "containsDigitAndPunctuation";
	//private static final String ALLCAPS = "allCaps";
	//private static final String PUNCTUATION = "punctuation";
	
	public Viterbi(String counts_file){
		te = new TrigramEstimator(counts_file);
		ep = new EmissionProbability(counts_file);
		counter = new InfrequentWordMapper("ner.counts");
		et = new EntityTagger(ep, counter);
	}
	
	/**
	 * Initializes dynamic programming tables
	 */
	private void initDPTables(int n){
		//pi[0..n][tags + STAR][tags + STAR]
		pi = new double[n+1][Tag.numTags() + 1][Tag.numTags() + 1];
		bp = new Tag[n+1][Tag.numTags() + 1][Tag.numTags() + 1];
		
		pi[0][Tag.STAR.getIndex()][Tag.STAR.getIndex()] = 1.0;
	}
	
	public Tag[] tag(String[] sentence){
		int n = sentence.length;
		
		Tag[] predSequence = new Tag[n];
		
		initDPTables(n);
		
		//Outermost loop
		for(int k = 1; k <= n; k++){
			String xk = sentence[k-1];
			
			
			
			//Replace rare word with RARE symbol
			if(counter.hasNumber(xk)){
				if(counter.hasNumberAndDash(xk))
					xk = CONTAINS_DIGIT_AND_DASH;
				else if(counter.hasNumberAndCommaOrPeriod(xk))
					xk = CONTAINS_DIGIT_AND_PUNCTUATION;
				else
					xk = OTHERNUM;
			}
//			else if(counter.isPunctuation(xk))
//				xk = PUNCTUATION;
//			else if(counter.isAllCaps(xk))
//				xk = ALLCAPS;
//			else if(counter.isInitCap(xk)){
//				xk = INITCAP;
//			}
			else if(counter.isRare(xk)){
				//System.out.println(xk + "\n");
				xk = RARE;}
			
//			
			
			for(Tag u : K(k-1)){
				for(Tag v : K(k)){
					
					pi[k][u.getIndex()][v.getIndex()] = max(k,u,v,xk);
					bp[k][u.getIndex()][v.getIndex()] = argmax(k,u,v,xk);
				}
			}
		}
		
		//Bookkeeping at the end
		predSequence = argmax(predSequence, K(n), n);
		
		//Recover tags
		for(int k = n-3; k >= 0; k--){
			try{
			predSequence[k] = bp[k+3][predSequence[k+1].getIndex()][predSequence[k+2].getIndex()];
			}
			catch(Exception e){
				System.out.println("k=" + k + ", bp=" );
				predSequence = argmax(predSequence, K(n), n);
			}
		}
		
		return predSequence;
	}
	
	public Prediction[] output(Tag[] y, String[] sentence){
		Prediction[] preds = new Prediction[y.length];
		if(preds.length == 1){
			preds[0] = et.tag(sentence[0]);
			return preds;
		}
		for(int i = 0; i < y.length ;i++){
			Tag curr = y[i];
			Tag prev = i == 0 ? Tag.STAR : y[i-1];
			try{
			preds[i] = new Prediction(
					curr,
					Math.log(pi[i+1][prev.getIndex()][curr.getIndex()])
			);
			}
			catch(NullPointerException e){
				System.out.println(Arrays.toString(y));
			}
		}
		return preds;
	}
	
	/**
	 * Returns the max probability of kth word ending in tags u,v 
	 */
	private double max(int k, Tag u, Tag v, String word){
		double max = -Double.MAX_VALUE;
		for(Tag w : K(k-2)){
			double prob = pi[k-1][w.getIndex()][u.getIndex()] * te.estimate(v, w,u) * ep.e(word, v);
			if(prob > max)
				max = prob;
		}
		return max;
	}
	
	/**
	 * Returns the kth backpointer
	 */
	private Tag argmax(int k, Tag u, Tag v, String xk){
		double max = -Double.MAX_VALUE;
		Tag argmax = null;
		for(Tag w : K(k-2)){
			double prob = pi[k-1][w.getIndex()][u.getIndex()] * te.estimate(v, w,u) * ep.e(xk, v);
			if(prob > max){
				max = prob;
				argmax = w;
			}
		}
		return argmax;
	}
	
	/**
	 * Sets (y_{n-1}, y_{n})
	 * @param y prediction sequence
	 * @param k set of possible tags
	 */
	private Tag[] argmax(Tag[] y, Tag[] k, int n){
		double max = -Double.MAX_VALUE;
		Tag argmaxu = null;
		Tag argmaxv = null;
//		double[][] last_matrix = pi[n];
//		for(int i = 0; i < last_matrix.length; i++){
//			System.out.println(Arrays.toString(last_matrix[i]));
//		}
		for(Tag u :k){
			for(Tag v : k){
				
				double prob = pi[n][u.getIndex()][v.getIndex()] * te.estimate(Tag.STOP, u, v);
				if(prob > max){
					max = prob;
					argmaxu = u;
					argmaxv = v;
				}
			}
		}
		
		if(n > 1){
			y[n-2] = argmaxu;
			y[n-1] = argmaxv;
		}
		return y;
	}
	
	/**
	 * Handles the Kk logic
	 */
	private Tag[] K(int i){
		if(i <= 0)
			return Tag.justStar;
		else
			return Tag.realTags;
	}
	

	
	public static void main(String[] args) throws IOException{
		
		//String[] sentence = new String[]{"The", "man", "saw", "the", "woman"};
		Viterbi v = new Viterbi("ner_mapped.counts");
//		String[] sentence = new String[]{"The", "European", "Commission", "said", "on", "Thursday",
//				"it", "disagreed", "with", "German", "advice", "."};
//		Tag[] tags = v.tag(sentence);
//		System.out.println(Arrays.toString(v.output(tags)));
		
		List<String[]> sentences = new ArrayList<String[]>();
		Scanner scanner = new Scanner(new File("ner_dev.dat"));
		
		//Extract all sentences
		List<String> sentence = new ArrayList<String>();

		while(scanner.hasNextLine()){
			String word = scanner.nextLine();
			
			if(word.isEmpty()){
				sentences.add(sentence.toArray(new String[sentence.size()]));
				sentence = new ArrayList<String>();
			}
			else
				sentence.add(word);
		}
		sentences.add(sentence.toArray(new String[sentence.size()]));
		scanner.close();
		

		//Write new file with predictions
		FileWriter filewriter = new FileWriter(new File("prediction_file"));
		for(int i = 0; i < sentences.size(); i++){
			String[] s = sentences.get(i);
			Prediction[] preds = v.output(v.tag(s), s);
			for(int j = 0; j < s.length; j++){
				filewriter.write(s[j] + SPACE + preds[j] + NEWLINE);
			}
			filewriter.write(NEWLINE);
		}
		
		filewriter.close();
	}
	
}
