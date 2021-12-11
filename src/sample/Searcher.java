package sample;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Searcher
{
    public  HashMap<String,Double> relevantDocuments; //Key:docNum(docID) , Value: Rank
    private Ranker ranker;
    private SemanticHandling sh;
    //private HashMap<String,Double> relavantDocsWithNums;


    public Searcher(String postingFilesURL, int numOfDocs,double averageDocLength)
    {
        ranker=new Ranker(postingFilesURL,numOfDocs,averageDocLength);
        sh=new SemanticHandling();
    }

    /**
     * The method receives a query from the user, and returns a list of the 50 most relevant docs for this query
     */
    public void Search(String originalQuery,HashMap<String,String[]> termDictionary,boolean runSemantics,String desc) throws IOException, com.medallia.word2vec.Searcher.UnknownWordException {
        String query="";
        if(runSemantics)
            query = getSemanticWordsForQuery(originalQuery,termDictionary)+" "+desc;
        else
            query=originalQuery+" "+desc;
        Parse p=new Parse();
        Doc queryDoc=new Doc("","","",query);
        HashMap<String,Integer>queryBag=p.Parse(queryDoc); //Key:Term , Value: numOfAppereances in query
        try {
            if(p.entityTerms.size()>0){
                for(Map.Entry<String,HashMap<Integer,Integer>> entityEntry:p.entityTerms.entrySet()){
                    try {
                        queryBag.put(entityEntry.getKey(),entityEntry.getValue().get(1));
                    } catch (Exception e) { }
                }

            }
        } catch (Exception e) { }
        queryBag=turnQueryBagToLowerCase(queryBag);
        HashMap<String,String> postingFilesLines=getAllPostingLines(queryBag,termDictionary); //Key:Term , Value: posting line
        relevantDocuments=SortHashMap.sortHashMapByValueDouble(ranker.Rank(makePostingFilesLinesWithDups(queryBag, postingFilesLines)),"DOWN");
        relevantDocuments=leaveXRelevantDocs(50,relevantDocuments);
        //relavantDocsWithNums=relevantDocuments;
        relevantDocuments=SortHashMap.sortHashMapByValueDouble(addIDToDocNum(ranker.docMap),"DOWN");
    }

    /**
     * The method receives a query, and returns the query with the addition of two "semanticlly close"
     * words for each word in the original query
     */
    private String getSemanticWordsForQuery(String originalQuery,HashMap<String,String[]> termDicionary) throws IOException, com.medallia.word2vec.Searcher.UnknownWordException {
        String query="";
        String[] splitQuery=originalQuery.split(" ");
        for(String q:splitQuery){
            LinkedList<String> semanticWords=sh.handle(q,termDicionary);
            for(String sw:semanticWords){
                query+=sw+" ";
            }
        }
        query=originalQuery+" "+query;
        return query;
    }

    /**
     * The method receives a parsed query, and returns a HashMap of the following structure:
     * Key:Term , Value0:posting line , Value1: number of appearances in query
     */
    private HashMap<String, String[]> makePostingFilesLinesWithDups(HashMap<String, Integer> queryBag, HashMap<String, String> postingFilesLines) {
        HashMap<String,String[]> PostingFilesLinesWithDups=new HashMap<>(); //Key:Term , Value0:posting line , Value1: numOfApearences in query
        for(Map.Entry<String,String> entry:postingFilesLines.entrySet()){
            try {
                String[] newArr=new String[2];
                newArr[0]=entry.getValue();
                newArr[1]=queryBag.get(entry.getKey())+"";
                PostingFilesLinesWithDups.put(entry.getKey(),newArr);
            } catch (Exception e) { }
        }
        return PostingFilesLinesWithDups;
    }

    /**
     * The method receives a parsed query and returns it with all letters turned to Lower Case
     */
    private HashMap<String, Integer> turnQueryBagToLowerCase(HashMap<String, Integer> queryBag) {
        HashMap<String,Integer> queryBagLowerCase=new HashMap<>();
        for(Map.Entry<String,Integer> entry:queryBag.entrySet())
        {
            queryBagLowerCase.put(entry.getKey().toLowerCase(),entry.getValue());
        }
        return queryBagLowerCase;
    }

    /**
     * The method receives a hashMap of documents and adds to each one its "DocNo"
     */
    private HashMap<String,Double> addIDToDocNum(HashMap<String,String> docMap){
        HashMap<String,Double> newReleventDocuments=new HashMap<>(); //Key:docID , Value: Rank
        for(Map.Entry<String,Double> entry:relevantDocuments.entrySet()){
            try {
                String[] docArray=docMap.get(entry.getKey()).split("_");
                String newKey=docArray[docArray.length-1];
                newReleventDocuments.put(newKey,entry.getValue());
            } catch (Exception e) { }
        }
        return newReleventDocuments;

    }

    /**
     * The metod receives a HashMap od documents and leaves the X best ranked ones
     */
    private HashMap<String,Double> leaveXRelevantDocs(int counter,HashMap<String,Double> hm){
        HashMap<String,Double> XRelevantDocs=new HashMap<>();
        int docCounter=0;
        for(Map.Entry<String,Double> docEntry:hm.entrySet()){
            docCounter++;
            if(docCounter==counter+1)
                break;
            XRelevantDocs.put(docEntry.getKey(),docEntry.getValue());
        }
        return XRelevantDocs;
    }

    /**
     * The method goes to the posting files, and for each term, pulls all the documents he appeared in
     */
    private HashMap<String,String> getAllPostingLines(HashMap<String,Integer> queryBag,HashMap<String,String[]> termDictionary) throws IOException
    {
        long sum=0;
        HashMap<String,String> postingFilesLines=new HashMap<>();
        File[] filesToOpen=fileNeedToOpen(queryBag,termDictionary);
        for(int i=0;i<filesToOpen.length;i++){
            int lineCounter=0;
            for(Map.Entry<String,Integer> entry:queryBag.entrySet()){
                try {
                    String queryTerm=entry.getKey();
                    if(filesToOpen[i].getName().contains(queryTerm.charAt(0)+"")) {
                        lineCounter+=(termDictionary.get(queryTerm).length)-3;
                    }
                } catch (Exception e) { }
            }
            BufferedReader BR=new BufferedReader(new FileReader(filesToOpen[i]));
            String line="";
            while((line=BR.readLine())!=null){
                line=line.substring(0,line.lastIndexOf(',')+1);
                String lineTerm=line.substring(0,line.indexOf(":")).toLowerCase();
                if(queryBag.containsKey(lineTerm)){
                    if(postingFilesLines.containsKey(lineTerm)){
                        String[] arrNewTail=(postingFilesLines.get(lineTerm)+line.substring(line.indexOf(":")+2)).split(",");
                        Arrays.sort(arrNewTail,new SortStringNumbers());
                        String newTail="";
                        for(String s:arrNewTail)
                            newTail+=s+",";
                        postingFilesLines.put(lineTerm,newTail);
                    }
                    else{

                        postingFilesLines.put(lineTerm,line.substring(line.indexOf(":")+2));

                    }
                    lineCounter--;
                    if(lineCounter==0)
                        break;
                }

            }
            BR.close();
        }
        return postingFilesLines;

    }

    /**
     * The method returns a list of all the posting files we need to access
     */
    private File[] fileNeedToOpen(HashMap<String,Integer> queryBag,HashMap<String,String[]> termDictionary){
        HashSet<String> filesURL=new HashSet<>();
        for(Map.Entry<String,Integer> entry:queryBag.entrySet()){
            String queryTerm=entry.getKey().toLowerCase();
            try {
                String queryURL=termDictionary.get(queryTerm)[2];
                filesURL.add(queryURL);
            } catch (Exception e) { }
        }
        File[] fileURLArray=new File[filesURL.size()];
        int i=0;
        for(String s:filesURL){
            fileURLArray[i++]=new File(s);
        }
        return fileURLArray;

    }
}

