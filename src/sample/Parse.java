package sample;



import org.tartarus.snowball.ext.PorterStemmer;

import javax.xml.crypto.dsig.keyinfo.KeyValue;
import java.io.*;
import java.util.*;

public class Parse
{

    private HashSet<String> stopWords;
    protected HashMap<String,Integer> bagOfWords; // key: Term, value: number of appearances in the doc

    //    private HashSet<String> Terms;
    protected  HashMap<String,HashMap<Integer,Integer>> entityTerms; //key:Entity, value: HashMap of docs and num of appereances

    protected PorterStemmer stemmer;
    boolean toStemm;

    protected int docNum;
    protected int max_TF_Curr;



    public Parse()
    {
        docNum=0;
        stopWords=new HashSet<>();
        stemmer=new PorterStemmer();
        entityTerms=new HashMap<>();
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
     * The method is parses the given Document into a HashMap data structure, while maintaining all the rules
     * @param doc
     * @return
     */
    public HashMap<String,Integer> Parse(Doc doc)
    {
        bagOfWords=new HashMap<>();
        SpecialParsing sp=new SpecialParsing();
        String specialTerm="";
        docNum++;
        max_TF_Curr=0;

//        --------------- split the document --------------------
            String[] docSplittedWords=doc.getText().split("[ \\!\"\\?\\;\\:\\&\\+\\*\\=\\<\\>\\^\\#\\'\\_\\@\\`\\|\\~\\)\\(\\}\\{\\]\\[\\\t\\\\]");

//        ---------------- Iterate through all words in the split document text --------------------
            for(int i=0;i<docSplittedWords.length;i++)
            {
                if(docSplittedWords[i].equals("")||docSplittedWords[i].equals(",")||docSplittedWords[i].equals(".")||docSplittedWords[i].equals("/")) continue;
//        -------------------------- check slash existence -------------------------------
                try {
                    if(docSplittedWords[i].contains("/") &&docSplittedWords[i].indexOf('/')!=0 &&
                            docSplittedWords[i].indexOf('/')!=docSplittedWords[i].length()-1&&
                            !Character.isDigit(docSplittedWords[i].charAt(docSplittedWords[i].indexOf("/")-1)) &&
                            !Character.isDigit(docSplittedWords[i].charAt(docSplittedWords[i].indexOf("/")+1)))
                    {
                        String[] slashSplit=docSplittedWords[i].split("/");
                        for(String s:slashSplit)
                            addTermCases(s);
                        continue;
                    }
                } catch (Exception e) { }
//        -------------------------- check entities -------------------------------
                try {
                    specialTerm="";
                    String entityTerm="";
                    int numOfWords=0;
                    boolean checkIfEntityFound=false;
                    docSplittedWords[i]=removeCommasAndDotsFromBeggining(docSplittedWords[i]);
                    while((i<docSplittedWords.length) && (docSplittedWords[i].length()>0) && docSplittedWords[i].charAt(0)>=65 && docSplittedWords[i].charAt(0)<=90)
                    {
                        if(docSplittedWords[i].charAt(docSplittedWords[i].length()-1)==',' || docSplittedWords[i].charAt(docSplittedWords[i].length()-1)=='.')
                        {
                            entityTerm+=removeCommasAndDotsFromEnd(docSplittedWords[i]);
                            numOfWords++;
                            if (numOfWords>1)
                            {

                                if(entityTerms.containsKey(entityTerm))
                                {

                                    HashMap<Integer,Integer> mapOfEntityTerm=entityTerms.get(entityTerm);
                                    if(mapOfEntityTerm.containsKey(docNum))
                                        mapOfEntityTerm.put(docNum,mapOfEntityTerm.get(docNum)+1);
                                    else
                                        mapOfEntityTerm.put(docNum,1);
                                }
                                else
                                {

                                    HashMap<Integer,Integer> newMapForEntity=new HashMap<>();
                                    newMapForEntity.put(docNum,1);
                                    entityTerms.put(entityTerm,newMapForEntity);
                                }
                                String[] splittedEntity=entityTerm.split(" ");
                                for(String s:splittedEntity)
                                    addTermCases(s);
                                checkIfEntityFound=true;
                                numOfWords=0;
                                entityTerm="";
                                break;
                            }
                            else{
                                numOfWords=0;
                                entityTerm="";
                                break;
                            }

                        }
                        else{
                            entityTerm+=docSplittedWords[i]+" ";
                            numOfWords++;
                            i++;
                        }

                    }
                    if(entityTerm.length()>0)
                    {
                        if(i>0)
                            i--;
                        if (numOfWords>1)
                        {
                            if(entityTerm.charAt(entityTerm.length()-1)==' ') entityTerm=entityTerm.substring(0,entityTerm.length()-1);
                            if(entityTerms.containsKey(entityTerm))
                            {

                                HashMap<Integer,Integer> mapOfEntityTerm=entityTerms.get(entityTerm);
                                if(mapOfEntityTerm.containsKey(docNum))
                                    mapOfEntityTerm.put(docNum,mapOfEntityTerm.get(docNum)+1);
                                else
                                    mapOfEntityTerm.put(docNum,1);
                            }
                            else
                            {

                                HashMap<Integer,Integer> newMapForEntity=new HashMap<>();
                                newMapForEntity.put(docNum,1);
                                entityTerms.put(entityTerm,newMapForEntity);
                            }
                            String[] splittedEntity=entityTerm.split(" ");
                            for(String s:splittedEntity)
                                addTermCases(s);
                            checkIfEntityFound=true;
                        }
                    }
                    if(checkIfEntityFound)continue;
                } catch (Exception e) { }

//        ---------------- check four word special combinations --------------------

                try {
                    if(i+3<docSplittedWords.length)
                    {
                        specialTerm=sp.SpecialParseFour(docSplittedWords[i],docSplittedWords[i+1],docSplittedWords[i+2],docSplittedWords[i+3]);
                    }
                    if(addTerm(specialTerm)){
                        i+=3;
                        continue;
                    }
                } catch (Exception e) { }

//        ---------------- check three word special combinations --------------------
                try {
                    if(i+2<docSplittedWords.length)
                    {
                        specialTerm=sp.SpecialParseThree(docSplittedWords[i],docSplittedWords[i+1],docSplittedWords[i+2]);
                    }
                    if(addTerm(specialTerm)){
                        i+=2;
                        continue;
                    }
                } catch (Exception e) { }

//        ---------------- check two word special combinations --------------------
                try {
                    if(i+1<docSplittedWords.length)
                    {
                        specialTerm=sp.SpecialParseTwo(docSplittedWords[i],docSplittedWords[i+1]);
                    }
                    if(addTerm(specialTerm)) {
                        i++;
                        continue;
                    }
                } catch (Exception e) { }

//        ---------------- check one word special combinations --------------------
                try {
                    if (i<docSplittedWords.length) {
                        specialTerm=sp.SpecialParseOne(docSplittedWords[i]);
                    }
                    if(addTerm(specialTerm)) continue;
                } catch (Exception e) { }


//        -------------------------- Remove stop words -----------------------------

                if (i<docSplittedWords.length) {
                    if(stopWords.contains(docSplittedWords[i]) || stopWords.contains(docSplittedWords[i].toLowerCase())) continue;
                }

//        --------------------------Big and Small Letters--------------------------

                if (i<docSplittedWords.length) {
                    String[] arrayAfterCombinations=docSplittedWords[i].split("[.\\,\\/\\-]");
                    for(int k=0;k<arrayAfterCombinations.length;k++)
                        addTermCases(arrayAfterCombinations[k]);
                }
            }


//        -------------------------------------------------------------------------
        return bagOfWords;
    }


    /**
     * adding term to the hashSet
     * @param term
     * @return
     */
    private boolean addTerm(String term)
    {
        if(term.length()>0)
        {
            if(bagOfWords.containsKey(term))
            {
                bagOfWords.put(term,bagOfWords.get(term)+1);
            }
            else
            {
                bagOfWords.put(term,1);
            }
            if(bagOfWords.get(term)>max_TF_Curr)
                max_TF_Curr = bagOfWords.get(term);
            return true;
        }
        return false;
    }

    /**
     * adding term to bagOfWords regarding cases
     * @param term
     * @return
     */
    private boolean addTermCases(String term){

        if(term.length()>0){

            term=Indexer.stemmWord(term);
            if(stopWords.contains(term.toLowerCase()))
                return false;
            if(term.length()==0)return false;
            String termInserted="";
            if(Character.isUpperCase(term.charAt(0))){ //if starts with big letter
                String termLowerCase=term.toLowerCase();
                String termUpperCase=term.toUpperCase();
                if(bagOfWords.containsKey(termLowerCase)){
                    bagOfWords.put(termLowerCase,bagOfWords.get(termLowerCase)+1);
                    termInserted=termLowerCase;
                }

                else if(bagOfWords.containsKey(termUpperCase)){
                    bagOfWords.put(termUpperCase,bagOfWords.get(termUpperCase)+1);
                    termInserted=termUpperCase;
                }

                else{
                    bagOfWords.put(termUpperCase,1);
                    termInserted=termUpperCase;
                }
            }
            else if(Character.isLowerCase(term.charAt(0))){//if starts with small letter
                String termUpperCase=term.toUpperCase();
                String termLowerCase=term.toLowerCase();
                if(bagOfWords.containsKey(termUpperCase)){
                    bagOfWords.put(termLowerCase,bagOfWords.get(termUpperCase)+1);
                    bagOfWords.remove(termUpperCase);
                    termInserted=termLowerCase;
                }
                else if(bagOfWords.containsKey(termLowerCase)){
                    bagOfWords.put(termLowerCase,bagOfWords.get(termLowerCase)+1);
                    termInserted=termLowerCase;
                }
                else{
                    bagOfWords.put(termLowerCase,1);
                    termInserted=termLowerCase;
                }
            }
            else{
                if(bagOfWords.containsKey(term))
                    bagOfWords.put(term,bagOfWords.get(term)+1);
                else
                    bagOfWords.put(term,1);
                termInserted=term;
            }

            if(bagOfWords.get(termInserted)>max_TF_Curr)
                max_TF_Curr = bagOfWords.get(termInserted);
            return true;
        }
        return false;
    }


    /**
     * if a String has a comma or a dot in the end, the function removes it.
     * @param s
     * @return
     */
    protected String removeCommasAndDotsFromBeggining(String s){
        try {
            if(s.length()==0)return "";
            if(s.charAt(0)=='.'||s.charAt(0)==',')
                s=s.substring(1);
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * if a String has a comma or a dot in the beginning, the function removes it.
     * @param s
     * @return
     */
    protected String removeCommasAndDotsFromEnd(String s){
        try {
            if(s.length()==0)return "";
            if(s.charAt(s.length()-1)=='.'||s.charAt(s.length()-1)==',')
                s=s.substring(0,s.length()-1);
            return s;
        } catch (Exception e) {
            return "";
        }
    }
}