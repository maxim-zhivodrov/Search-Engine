package sample;

import java.io.*;
import java.util.*;

public class Ranker
{
    static double b=0.75;
    static double k=1.5;
    private String postingFilesURL;
    private int numOfDocs;
    private double averageDocLength;
    protected HashMap<String,String> docMap;


    public Ranker(String postingFilesURL, int numOfDocs,double averageDocLength){
        this.postingFilesURL=postingFilesURL;
        this.numOfDocs=numOfDocs;
        this.averageDocLength=averageDocLength;
    }

    /**
     * The method ranks all the documents and returns a data structure with maps each document with its rank
     * @throws IOException
     */
    public HashMap<String,Double> Rank(HashMap<String,String[]> postingFilesLinesWithDups) throws IOException {
        HashSet<String> docNums=new HashSet<>();
        for(Map.Entry<String,String[]> termEntry:postingFilesLinesWithDups.entrySet()){
            String[] docsOfTerm=termEntry.getValue()[0].replace(" ",",").split(",");
            for(int i=0;i<docsOfTerm.length;i+=2)
                docNums.add(docsOfTerm[i]);

        }
        docMap=ExtractInfoFromDocDictionary(docNums);


        HashMap<String,Double> docNumsWithBM25=new HashMap<>(); //Key:docNum(numeric) , Value: rank BM25
        HashMap<String,Double> docNumsWithInnerProduct=new HashMap<>();//Key:docNum(numeric) , Value: rank InnerProduct
        int numOfLines=0;
        for(Map.Entry<String,String[]> entry:postingFilesLinesWithDups.entrySet()){
            numOfLines+=Integer.parseInt(entry.getValue()[1]);
        }
        HashMap<String,String>[] hashMapArray=new HashMap[numOfLines]; //[i] Key:docNum(numeric) , Value: numOfApperences in doc
        int i=0;
        for(Map.Entry<String,String[]> postingFilesEntry:postingFilesLinesWithDups.entrySet()){
            for(int j=0;j<Integer.parseInt(postingFilesEntry.getValue()[1]);j++){
                hashMapArray[i++]=transformStringArrToHashMap(postingFilesEntry.getValue()[0].replace(" ",",").split(","));
            }
        }
        for(String currDoc:docNums)
            CalculateDocRank(currDoc,hashMapArray,docNumsWithBM25,docNumsWithInnerProduct);

        HashMap<String, Double> docNumsWithRank = NormalizedRank(docNumsWithBM25, docNumsWithInnerProduct); //Key:docNum(numeric) , Value: rank final

        return docNumsWithRank;
    }

    /**
     * The method returns a HashMap of documents and their final ranks,
     * which are configured from their BM25 rank and their inner product rank
     */
    private HashMap<String, Double> NormalizedRank(HashMap<String, Double> docNumsWithBM25, HashMap<String, Double> docNumsWithInnerProduct) {
        HashMap<String,Double> docNumsWithRank=new HashMap<>();
        double maxInnerProduct=getMaxDoubleValueFromHashMap(docNumsWithInnerProduct);
        double maxBM25=getMaxDoubleValueFromHashMap(docNumsWithBM25);
        for(Map.Entry<String,Double> entry:docNumsWithBM25.entrySet()){
            double innerProduct=docNumsWithInnerProduct.get(entry.getKey());
            double BM25=entry.getValue();
            docNumsWithRank.put(entry.getKey(),0.8*BM25/maxBM25+0.2*innerProduct/maxInnerProduct);
        }
        return docNumsWithRank;
    }

    /**
     * The method returns the highest value from a hashMap
     */
    private double getMaxDoubleValueFromHashMap(HashMap<String,Double> hm){
        double maxValue=0.0;
        for(Map.Entry<String,Double> entry:hm.entrySet()){
            if(entry.getValue()>maxValue)
                maxValue=entry.getValue();
        }
        return maxValue;
    }

    /**
     * The method transforms an Array to HashMap
     */
    private HashMap<String,String> transformStringArrToHashMap(String[] arr){
        HashMap<String,String> hm=new HashMap<>();
        for(int i=0;i<arr.length;i+=2)
            hm.put(arr[i],arr[i+1]);
        return hm;
    }

    /**
     * The method calculates the rank of each document and saves it in a data structure
     */
    private void CalculateDocRank(String docNum,HashMap<String,String>[] hashMapArray,HashMap<String,Double> docNumsWithBM25,HashMap<String,Double> docNumsWithInnerProduct)
    {
        double innerProduct=0.0;
        double BM25=0.0;
        double TF=0.0;
        double numOfAppearancesInDoc=0.0;
        double docLength=0.0;
        double IDF=0.0;
        double numOfDocsTermAppears=0.0;
        for(int i=0;i<hashMapArray.length;i++)
        {
           numOfDocsTermAppears=hashMapArray[i].size();
           String numOfAppearancesInDocString=hashMapArray[i].get(docNum);
           if(numOfAppearancesInDocString!=null)
               numOfAppearancesInDoc=Double.parseDouble(numOfAppearancesInDocString);
           else
               continue;

            try {
                String[] splitDocLine=docMap.get(docNum).split("_");
                docLength=Double.parseDouble(splitDocLine[1]);
                TF=numOfAppearancesInDoc/*/Double.parseDouble(splitDocLine[0])*/;
                IDF=Math.log(numOfDocs-numOfDocsTermAppears+0.5)/Math.log(2)-Math.log(numOfDocsTermAppears+0.5)/Math.log(2);
                //IDF=Math.log(numOfDocs)/Math.log(2)-Math.log(numOfDocsTermAppears)/Math.log(2);
                BM25+=IDF*((TF*(k+1))/(TF+k*(1-b+b*(docLength/averageDocLength)))+1);
                innerProduct+=IDF*(TF/Double.parseDouble(splitDocLine[0]));
            } catch (Exception e) { }
        }
        docNumsWithBM25.put(docNum,BM25);
        docNumsWithInnerProduct.put(docNum,innerProduct);
    }

    /**
     * The method extracts all the needed information from the doc dictionary
     * @throws IOException
     */
    private HashMap<String,String> ExtractInfoFromDocDictionary(HashSet<String> docNums) throws IOException
    {
        int counter=docNums.size();
        HashMap<String,String> docMap=new HashMap<>(); //Key: num of the doc, Value: info of the doc(DocDictionary)
        BufferedReader BR=new BufferedReader(new FileReader(new File(postingFilesURL+"\\DocDictionary")));
        String line="";
        while((line=BR.readLine())!=null){
            String currDoc=line.substring(0,line.indexOf(":"));
            if(docNums.contains(currDoc)){
                docMap.put(currDoc,line.substring(line.indexOf(":")+2));
                counter--;
                if(counter==0)
                    break;
            }
        }
        BR.close();
        return docMap;
    }


}
