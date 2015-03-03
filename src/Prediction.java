/**
 * Helper object which consists of a Tag and a (log) probability.
 */
public class Prediction{
	Tag t;
	double logp;
	
	public Prediction(Tag t, double logp){
		this.t = t;
		this.logp = logp;
	}

	
	public String toString() {
		return t.toString() + " " + logp;
	}
	
	
	
}