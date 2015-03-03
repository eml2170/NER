

/**
 * This enum defines a value for each of the possible tags
 * 
 * Note: Includes * and STOP symbols even though they are not technically tags... this makes
 * the code simpler for #5,6 although not semantically precise
 * @author edwardliu
 *
 */
public enum Tag {
	STAR("*", 0),
	IPER("I-PER", 1),
	ILOC("I-LOC", 2), 
	IORG("I-ORG", 3), 
	IMISC("I-MISC", 4),
	
	BPER("B-PER", 5),
	BLOC("B-LOC", 6),
	BORG("B-ORG", 7),
	BMISC("B-MISC", 8),
	
	O("O", 9),
	
	STOP("STOP", 10);
	
	
	private final String name;    
	private final int index;
	public static final Tag[] realTags = new Tag[]{
		IPER, ILOC, IORG, IMISC,
		BPER, BLOC, BORG, BMISC,
		O};
	
	public static final Tag[] tagsAndStar = new Tag[]{
		STAR,
		IPER, ILOC, IORG, IMISC,
		BPER, BLOC, BORG, BMISC,
		O};

    public static final Tag[] justStar = new Tag[]{
            STAR
    };

    private Tag(String s, int i) {
        name = s;
        index = i;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    }

    public String toString(){
       return name;
    }
    
    public static Tag fromString(String text) {
        if (text != null) {
          for (Tag b : Tag.values()) {
            if (text.equals(b.name)) {
              return b;
            }
          }
        }
        return null;
      }
    
    public static Tag fromIndex(int index) {
        if (index >= 0 && index <= 10) {
          for (Tag b : Tag.values()) {
            if (index == b.index) {
              return b;
            }
          }
        }
        return null;
      }
    
    public int getIndex(){
    	return index;
    }
    
    public static int numTags(){
    	return 9;
    }
    
    public static void main(String[] tags){
    	System.out.println(realTags[2]);
    }
    
//    public Tag[] getRealTags(){
//    	Tag[] realTags = new Tag[9];
//    	for(int i = 0; i < 9; i++){
//    		realTags[i] = Tag.fromIndex(i);
//    	}
//    	return realTags;
//    }
    
}
