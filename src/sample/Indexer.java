package sample;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;

public class Indexer
{
    public static int totalNumOfDos;
    private LinkedList<Doc> DocumentList;
    private HashMap<String,HashMap <Integer,String>> termsToUpdate;
    private HashMap<Integer,String[]> docDictionary; //key: Doc number, value[0]:frequency of most frequent, value[1]: number of unique terms, value[2]: parsed title, value[3]: date
    protected String directoryPath;
    private int postingFileCounter; //Counts the names of the first posting files
    private int entitiesFileCounter;
    public static boolean toStemm;
    protected PostingFileHandler pfh;
    protected Parse p;


    /**
     * The constructor initializes a new Indexer with a path to the Posting files and the knowledge whether to stemm or not
     * @param postingPath
     * @param shouldIStemm
     */
    public Indexer(String postingPath, boolean shouldIStemm)
    {
        totalNumOfDos=0;
        docDictionary = new HashMap<>();
        directoryPath = postingPath;
        pfh=new PostingFileHandler(directoryPath);
        p=new Parse();
        postingFileCounter=0;
        termsToUpdate=new HashMap<>();
        toStemm=shouldIStemm;

    }

    /**
     * The method is responsible for the Indexing stage, it parses all the documents from the current DocumentList
     *  and updates the relevant data structure accordingly.
     * The method then calls further functions in order to handle the entities and posting files
     * @throws IOException
     */
    public void index() throws IOException
    {

        totalNumOfDos+=DocumentList.size();
        TitleParse tp=new TitleParse();
        while(DocumentList.size()>0){
            Doc currDoc=DocumentList.removeFirst();
            HashSet<String> titleBagOfWords=tp.TitleParse(currDoc.getTI());
            HashMap<String,Integer> docBagOfWords=p.Parse(currDoc);
            for(Map.Entry<String,Integer> bagOfWordsEntry:docBagOfWords.entrySet()){
                String key=bagOfWordsEntry.getKey();
                addToTermDictionary(key,bagOfWordsEntry.getValue()+"",p);

            }
            String[] arrForDoc={p.max_TF_Curr+"",docBagOfWords.size()+"",transformBagOfWordsToString(titleBagOfWords),currDoc.getDate(),currDoc.getDocNo()};
            docDictionary.put(p.docNum,arrForDoc);
        }
        makePostingFile();
        handleEntities();

    }

    /**
     * The method is responsible of making the initial posting files and writing them to the disk
     * @throws IOException
     */
    protected void makePostingFile() throws IOException
    {
        File newPostingFile=new File(directoryPath+"\\"+postingFileCounter);
        newPostingFile.createNewFile();
        postingFileCounter++;
        FileWriter FW=new FileWriter(newPostingFile,true);
        ArrayList<String> termsToUpdateList=new ArrayList<>(termsToUpdate.keySet());
        Collections.sort(termsToUpdateList, new SortIgnoreCase());
        for(String term:termsToUpdateList){
            HashMap<Integer,String> hashMapOfTerm=termsToUpdate.get(term);
            ArrayList<Integer> docsToWriteList =  new ArrayList<>(hashMapOfTerm.keySet());;
            Collections.sort(docsToWriteList);
            FW.write(term+": ");
            for(int docNum:docsToWriteList)
                FW.write(docNum+" "+hashMapOfTerm.get(docNum)+",");
            FW.write("\n");
        }
        termsToUpdate=new HashMap<>();
        FW.flush();
        FW.close();
    }

    /**
     * The method takes all the parsed terms from the title and returns them as one long String
     * @param bagOfWords
     * @return
     */
    private String transformBagOfWordsToString(HashSet<String> bagOfWords)
    {
        String s="";
        if(bagOfWords!=null&&bagOfWords.size()!=0)
        {
            for(String word:bagOfWords)
                s=s+word+"@";
            s=s.substring(0,s.length()-1);
        }
        return s;
    }

    /**
     * The method is responsible for updating terms in the TermsToUpdate data Structure,
     *      and is altimatly used to uphold the rule for "Big and Small letters"
     * @param keyOut is the term we want to replace
     * @param keyIn is the new term we want to replace with
     * @param p
     * @param value is number of appearances of keyOut the in doc
     */
    private void switchBetweenCasesInTermsToUpdate(String keyOut,String keyIn,Parse p,String value)
    {
        HashMap<Integer,String> tempMap=termsToUpdate.get(keyOut);
        tempMap.put(p.docNum,value);
        termsToUpdate.put(keyIn,tempMap);
        termsToUpdate.remove(keyOut);
    }

    /**
     * The method adds a document to an existing term in the TermsToUpdate data Structure
     * @param key is the term
     * @param p
     * @param value is number of appearances in doc
     */
    private void addToHashMapInTermsToUpdate(String key,Parse p,String value){
        HashMap<Integer,String> tempMap=termsToUpdate.get(key);
        tempMap.put(p.docNum,value);
        termsToUpdate.put(key,tempMap);

    }

    /**
     * The function initializes a new HashMap in the TermsToUpdate data Structure for a new Term
     * @param key is the term
     * @param p
     * @param value is number of appearances in doc
     */
    private void createNewHashMapForTermsToUpdate(String key,Parse p,String value){
        HashMap<Integer,String> newHashMapForTerm=new HashMap<>();
        newHashMapForTerm.put(p.docNum,value);
        termsToUpdate.put(key,newHashMapForTerm);

    }

    /**
     * The method adds Terms to the TermDictionary
     * @param key is the term
     * @param value is number of appearances in doc
     * @param p
     */
    private void addToTermDictionary(String key,String value,Parse p){
        if(Character.isUpperCase(key.charAt(0)))
        {
            String keyLowerCase=key.toLowerCase();
            String keyUpperCase=key.toUpperCase();
            if(termsToUpdate.containsKey(keyLowerCase))
                addToHashMapInTermsToUpdate(keyLowerCase,p,value);
            else if(termsToUpdate.containsKey(keyUpperCase))
                addToHashMapInTermsToUpdate(keyUpperCase,p,value);
            else
                createNewHashMapForTermsToUpdate(keyUpperCase,p,value);
        }
        else if(Character.isLowerCase(key.charAt(0))){
            String keyLowerCase=key.toLowerCase();
            String keyUpperCase=key.toUpperCase();
            if(termsToUpdate.containsKey(keyUpperCase))
                switchBetweenCasesInTermsToUpdate(keyUpperCase,keyLowerCase,p,value);
            else if(termsToUpdate.containsKey(keyLowerCase))
                addToHashMapInTermsToUpdate(keyLowerCase,p,value);
            else
                createNewHashMapForTermsToUpdate(keyLowerCase,p,value);
        }
        else{
            if(termsToUpdate.containsKey(key))
                addToHashMapInTermsToUpdate(key,p,value);
            else
                createNewHashMapForTermsToUpdate(key,p,value);
        }

    }

    /**
     * The method gets a term and returns the term Stemmed or not (depends of the value of the static parameter "toStemm"
     * @param term
     * @return
     */
    public static String stemmWord(String term)
    {
        PorterStemmer stemmer=new PorterStemmer();
        if(toStemm)
        {
            stemmer.setCurrent(term);
            stemmer.stem();
            term=stemmer.getCurrent();
        }
        return term;
    }

    /**
     * The function makes the final Posting files by following four stages:
     * first, the method merges all the Entity Posting files into one big file which is added to the initial Posting files
     * Second, the method merges all the initial Posting files into one big Posting file
     * Third, the method spilts the Big posting file into 8 Posting files according to our algorithm
     * @throws IOException
     */
    public void handlePostingFiles() throws IOException {
        pfh.mergeAllFiles("\\Entities");
        File entitiesFiles=new File(directoryPath+"\\"+"Entities");
        File[] entitiesFilesArray=entitiesFiles.listFiles();
        pfh.handleCopiesInFile("Entities\\"+entitiesFilesArray[0].getName());
        pfh.copyEntitiesFileToTheRest(postingFileCounter,docDictionary);
        pfh.mergeAllFiles("");
        pfh.finishPostingFiles();
        pfh.handleCopies();
        pfh.makeTermDictionary();
        pfh.makeDocDictionary(docDictionary);
        pfh.makeEntitiesIndex(docDictionary);
    }

    /**
     * The method makes separate Posting Files for all the Entities that appear in more than one document
     * @throws IOException
     */
    public void handleEntities() throws IOException {
        File newPostingFileEntities=new File(directoryPath+"\\"+"Entities"+"\\"+entitiesFileCounter);
        newPostingFileEntities.createNewFile();
        entitiesFileCounter++;
        FileWriter FW=new FileWriter(newPostingFileEntities,true);
        ArrayList<String> entitiesTermsToUpdateList=new ArrayList<>(p.entityTerms.keySet());
        Collections.sort(entitiesTermsToUpdateList, new SortIgnoreCase());
        for(String entityTerm:entitiesTermsToUpdateList){
            HashMap<Integer,Integer> hashMapOfEntity=p.entityTerms.get(entityTerm);
            FW.write(entityTerm+": ");
            ArrayList<Integer> docsToWriteList = new ArrayList<>(hashMapOfEntity.keySet());;
            Collections.sort(docsToWriteList);
            for(int docNum:docsToWriteList){
                FW.write(docNum+" "+hashMapOfEntity.get(docNum)+",");
            }
            FW.write("\n");
        }
        FW.flush();
        FW.close();
        p.entityTerms=new HashMap<>();
    }

    public void setDocumentList(LinkedList<Doc> documentList) {
        DocumentList = documentList;
    }
}