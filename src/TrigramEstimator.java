import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Gives maximum likelihood estimate for trigram probability
 * @author edwardliu
 */
public class TrigramEstimator {

	Map<String, Integer> countMap;
	private static final String SPACE = " ";
	private static final String BIGRAM = "2-GRAM";
	private static final String TRIGRAM = "3-GRAM";


	public TrigramEstimator(String counts_file){

		countMap = new HashMap<String, Integer>();
		build(counts_file);

	}

	private void build(String filename){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line;
			while((line=reader.readLine())!=null){
				String[] tokens = line.split(SPACE);
				String id = tokens[1];

				//If the line is a count for a bigram or trigram
				if(id.equals(BIGRAM) || id.equals(TRIGRAM)){
					StringBuilder gram = new StringBuilder();

					//Assemble the words into a gram
					for(int i = 2; i < tokens.length; i++){
						gram.append(Tag.fromString(tokens[i]) + SPACE);
					}

					//Add the gram to countMap with its associated count
					countMap.put(gram.toString().trim(), Integer.parseInt(tokens[0]));
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Estimates the probability of y conditioned on the tag bigram (prevprev, prev)
	 */
	public double estimate(Tag y, Tag prevprev, Tag prev){
		//Build trigram
		StringBuilder tr = new StringBuilder();
		tr.append(prevprev + SPACE);
		tr.append(prev + SPACE);
		tr.append(y);

		//Build bigram
		StringBuilder br = new StringBuilder();
		br.append(prevprev+ SPACE);
		br.append(prev);

		String trigram=tr.toString();
		String bigram=br.toString();

		//Return probability 0 if either numerator or denominator equals 0
		if(!countMap.containsKey(trigram) || !countMap.containsKey(bigram))
			return 0.0;

		//Get counts
		double numerator = (double) countMap.get(trigram);
		double denominator = (double) countMap.get(bigram);

		//Divide
		return numerator/denominator;
	}

	public double logprob(double x){
		//Represent negative infinity by negative of largest double in Java
		if(x == 0.0)
			return -Double.MAX_VALUE;
		return Math.log(x);
	}
	public static void main(String[] args){
		TrigramEstimator te = new TrigramEstimator("ner_mapped.counts");

		//Print log probability for each trigram in count file
		try {
			Scanner scanner = new Scanner(new File("ner_mapped.counts"));
			while(scanner.hasNextLine()){
				String line = scanner.nextLine();
				String[] tokens = line.split(SPACE);
				String id = tokens[1];

				//If the line is a trigram
				if(id.equals(TRIGRAM)){

					//Build trigram
					Tag prevprev = Tag.fromString(tokens[2]);
					Tag prev = Tag.fromString(tokens[3]);
					Tag y = Tag.fromString(tokens[4]);

					//Print log probability
					System.out.println(line + " " + te.logprob(te.estimate(y, prevprev,prev )));
				}
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		//Tests that my code defines a distribution
		
//		for(Tag prevprev : Tag.tagsAndStar){
//			for(Tag prev : Tag.tagsAndStar){
//				double sum = 0.0;
//				for(Tag t : Tag.values()){
//					sum += te.estimate(t, prevprev, prev);
//				}
//				System.out.println("sum of probabilities conditioned on (" + prevprev + "," + prev + ") = " + sum);
//			}
//		}
		
		//Test transition probability of STOP
//		for(Tag prevprev : Tag.tagsAndStar){
//			for(Tag prev : Tag.tagsAndStar){
//				System.out.println("q(STOP|" + prevprev + "," + prev + ") = " + te.estimate(Tag.STOP, prevprev, prev));
//			}
//		}
	}

}