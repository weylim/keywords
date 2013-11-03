package keywords;

// Structure for features for each candidate
public class Record {
    public String phrase; // candidate phrase
    public int keyphraseness; // frequency of term as a groundtruth tag indicated previously
    public int absPosition; // absolute position in document
    public float relativePosition; // relative position in document (0 to 1)
    public int numChars; // number of chars in phrase (including whitespaces)
    public int numWords; // number of words in phrase
    public int TF; // term-frequency
    public int TFIDF; // term frequency-inverse document frequency
    public int label; // class label {-1,0,1} for unknown, no, yes, respectively 
	
    public Record(String i_phrase, int i_keyphraseness, int i_absPosition, float i_relativePosition, int i_numChars, int i_numWords, int i_TF, int i_TFIDF, int i_label) {
    	phrase = i_phrase;
        keyphraseness = i_keyphraseness;
    	absPosition = i_absPosition;
    	relativePosition = i_relativePosition;
    	numChars = i_numChars;
    	numWords = i_numWords;
        TF = i_TF;
        TFIDF = i_TFIDF;
        label = i_label;
        assert(label == 1 || label == 0);
    }
    
    public void setObjTF(int Freq) {
    	TF = Freq;
    }

}
