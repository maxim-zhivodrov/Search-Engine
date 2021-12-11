package sample;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class TitleParse {
    private HashSet<String> stopWords;
    protected HashSet<String> bagOfWords;
    protected PorterStemmer stemmer;


    public TitleParse(){
        stopWords=new HashSet<>();
        stemmer=new PorterStemmer();
        try {
            File stopWordsTxt=new File(Controller.stopWordsPath);
            BufferedReader bfReader=new BufferedReader(new FileReader(stopWordsTxt));
            String newStopWord="";
            while((newStopWord=bfReader.readLine())!=null)
                stopWords.add(newStopWord);
            bfReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The method parses the title of a document
     * @param title
     * @return
     */
    public HashSet<String> TitleParse(String title)
    {
        if(title.length()==0) return null;
        bagOfWords=new HashSet<>();
        SpecialParsing sp=new SpecialParsing();
        String specialTerm="";
        String[] titleSplittedWords=title.split("[ \\!\"\\?\\;\\:\\&\\+\\*\\=\\<\\>\\^\\#\\_\\@\\`\\|\\~\\)\\(\\}\\{\\]\\[\\\t\\\\]");
        for(int i=0;i<titleSplittedWords.length;i++){
            if(titleSplittedWords[i].equals("")||titleSplittedWords[i].equals(",")||titleSplittedWords[i].equals(".")||titleSplittedWords[i].equals("/")) continue;
//            -------------------------- check slash existence -------------------------------
            if(titleSplittedWords[i].contains("/") &&titleSplittedWords[i].indexOf('/')!=0 &&
                    titleSplittedWords[i].indexOf('/')!=titleSplittedWords[i].length()-1&&
                    !Character.isDigit(titleSplittedWords[i].charAt(titleSplittedWords[i].indexOf("/")-1)) &&
                    !Character.isDigit(titleSplittedWords[i].charAt(titleSplittedWords[i].indexOf("/")+1)))
            {
                String[] slashSplit=titleSplittedWords[i].split("/");
                for(String s:slashSplit)
                    addTermCases(s);
                continue;
            }
//          ------------------------- check four word special combinations --------------------

            if(i+3<titleSplittedWords.length)
            {
                specialTerm=sp.SpecialParseFour(titleSplittedWords[i],titleSplittedWords[i+1],titleSplittedWords[i+2],titleSplittedWords[i+3]);
            }
            if(addTerm(specialTerm)){
                i+=3;
                continue;
            }

//        ---------------- check three word special combinations --------------------
            if(i+2<titleSplittedWords.length)
            {
                specialTerm=sp.SpecialParseThree(titleSplittedWords[i],titleSplittedWords[i+1],titleSplittedWords[i+2]);
            }
            if(addTerm(specialTerm)){
                i+=2;
                continue;
            }

//        ---------------- check two word special combinations --------------------
            if(i+1<titleSplittedWords.length)
            {
                specialTerm=sp.SpecialParseTwo(titleSplittedWords[i],titleSplittedWords[i+1]);
            }
            if(addTerm(specialTerm)) {
                i++;
                continue;
            }

//        ---------------- check one word special combinations --------------------
            specialTerm=sp.SpecialParseOne(titleSplittedWords[i]);
            if(addTerm(specialTerm)) continue;


//        -------------------------- Remove stop words -----------------------------

            if(stopWords.contains(titleSplittedWords[i]) || stopWords.contains(titleSplittedWords[i].toLowerCase())) continue;

//        --------------------------Big and Small Letters--------------------------

            String[] arrayAfterCombinations=titleSplittedWords[i].split("[.\\,\\/\\-]");
            for(int k=0;k<arrayAfterCombinations.length;k++)
                addTermCases(arrayAfterCombinations[k]);
        }


//        -------------------------------------------------------------------------
        return bagOfWords;

    }

    /**
     * The method adds a term to "bagOfWords", ignoring Stemming
     * @param term
     * @return
     */
    private boolean addTerm(String term)
    {
        if(term.length()>0)
        {
            bagOfWords.add(term.toUpperCase());
            return true;
        }
        return false;
    }

    /**
     * The method adds a term to "bagOfWords"
     * @param term
     * @return
     */
    private boolean addTermCases(String term){

        if(term.length()>0){

            term=Indexer.stemmWord(term);
            if(stopWords.contains(term.toLowerCase()))
                return false;
            if(term.length()==0)return false;
            bagOfWords.add(term.toUpperCase());

            return true;
        }
        return false;
    }
}
