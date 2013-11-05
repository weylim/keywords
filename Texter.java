/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package keywords;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Class for manipulating text
 * @author WeeYong
 */
public class Texter {
    static String TagPathName = "C:\\Users\\WeeYong\\Documents\\keywords\\lib\\models\\english-left3words-distsim.tagger";	
    private MaxentTagger tagger = null;
    
    /** Append newline for each <p> tag. Remove all HTML tags. Collapse multiple whitespace to just 1 space.
     * @param text text to be cleaned
     * @return cleaned text */
    public String clean(String text) {
        Document doc = Jsoup.parse(text); // parse HTML
        doc.select("p").append("\\n"); // adds in newline for each <p>
        
        String cleaned = doc.text().replaceAll("\\\\n", System.getProperty("line.separator")); // grab just the text (not the HTML)
        cleaned = cleaned.replaceAll("\\s+", " "); // collapse multiple whitespaces to just 1 space
        return cleaned;
    }
    
    /** Call on the tagger program to tag a given string
     * @param text text to be POS tagged
     * @return the tagged text (e.g. A_DT lot_NN of_IN frameworks_NNS use_VBP URL_NN conventions_NNS) */
    public String tag(String text) {
        if (tagger == null) {
            tagger = new MaxentTagger(TagPathName); // Initialize the tagger
        }
        String tagged = tagger.tagString(text); 
        return tagged;
    }
    
    /** Count the occurrences of phrase A in in string AA 
     * @param AA text containing the substring
     * @param A the substring of which occurrences in AA will be counted in this function 
     * @return number of occurrences of A in AA */
    int substrFreq(String AA, String A) {      
        int lastIndex = -1;
        int count = 0;

        while(true) {
            lastIndex = AA.indexOf(A, lastIndex + 1);
            if (lastIndex == -1) {
                break;
            }
            char before = lastIndex > 1 ? AA.charAt(lastIndex-1) : ' ';
            char after = lastIndex + A.length() >= AA.length() ? ' ' : AA.charAt(lastIndex + A.length());
            if (!Character.isLetterOrDigit(before) && !Character.isLetterOrDigit(after)) {
                count++;
            }
        }
        return count;
    }
    
    /** Escape all characters that matches the specified escape string with a specified prefix */
    public String escapeChars(String A, String escape, String prefix) {
        StringBuilder B = new StringBuilder();
        for (int i = 0; i < A.length(); i++) {
            if (escape.contains(Character.toString(A.charAt(i)))) {
                B.append(prefix);
            }
            B.append(A.charAt(i));
        }
        return B.toString();
    }
    
}


    /** Grab out all candidate Noun Phrases from a tagged document/string 
    public HashSet<String> getNNPs(String tagged) {
       HashSet<String> NNPs = new HashSet<String>() {};
       String[] words = tagged.split("\\s+"); // explode via blankspaces to words array
       String temp = "";
       
       // search for consecutive words with NN* ending and store these phrases into NNPs
       for (String word: words) {
           if(word.matches(".*_NN\\w?")) {
               word = word.replace("\\","");
               temp += '-' + word.substring(0, word.lastIndexOf("_"));
           }
           else if (!temp.isEmpty()) {
               NNPs.add(temp.substring(1));
               temp = ""; // clear temp
           }
       }
       if(!temp.isEmpty()) {
           NNPs.add(temp);
       }
       return NNPs;
    }*/