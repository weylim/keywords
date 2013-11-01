package keywords;

import java.util.ArrayList;
import java.util.List;

public class FeatureGenerator {

    public static List<Record> generateRecords(Sample sample) {
        List<Record> records = new ArrayList<>();
        
        // Clean and POS-tag document body
        Texter texter = new Texter();
        String body = texter.clean(sample.body); // clean the text
        String tagged = texter.tag(body); // POS tagged, mixed case
        
        body = body.toLowerCase().replaceAll("\\s+", "-"); // body, lower case, hyphenated
        
        String[] words = tagged.split("\\s+"); // explode via blankspaces to words array
        String phrase = "";
        int numWords = 0;
        int MsgLen = words.length;
                
        // For each NN* phrases, instantiate a record
        for (int i = 0; i < words.length + 1; i++) { 
            if(i < words.length && words[i].matches(".*_NN\\w?")) {
                words[i] = words[i].replace("\\","");
                phrase += '-' + words[i].substring(0, words[i].lastIndexOf("_"));                
                numWords++;
            }
            else if (!phrase.isEmpty()) {
                int termFreq = texter.substrFreq(phrase, body);
                int label = sample.tags.contains(phrase) ? 1 : 0; // determine if phrase is one of the groundtruth tags
                int Position = i - numWords;
                Record record = new Record(phrase, Position, (float)Position/MsgLen, termFreq, phrase.length(), numWords, label);
                records.add(record);
                
                phrase = ""; // clear phrase
                numWords = 0;
            }
        }
        return records;
    }
	

	
}
