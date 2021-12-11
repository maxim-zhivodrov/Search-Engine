package sample;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class ReadFile
{
    private LinkedList<Doc> documentsList;
    private String Path;
    protected static int docsRead=0;


    public ReadFile(String path) {
        Path = path;
        documentsList=new LinkedList<>();
    }

    /**
     * The method reads 12 files from the corpus and adds them to the "documentsList"
     */
    public void Read()
    {
        File corpus=new File(Path);
        File[] foldersArray=corpus.listFiles();
        boolean finishedTwelve=false;
        while(!finishedTwelve){
            for(int i=0;i<12&&docsRead<foldersArray.length;docsRead++){
                if(!foldersArray[docsRead].isDirectory()) continue;
                for(File fileInCorpus:foldersArray[docsRead].listFiles()){
                    try {
                        Document htmlDoc=Jsoup.parse(fileInCorpus,"UTF-8");
                        Elements elements=htmlDoc.getElementsByTag("DOC");

                        for(Element el:elements){
                            addNewDocToDocumentList(el);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                i++;
            }
            finishedTwelve=true;
        }






    }

    /**
     * The method adds a single document to the "documentsList"
     * @param el
     */
    private void addNewDocToDocumentList(Element el){
//        String docID=((Element)el.childNode(1)).select("docno").text();
//        String docDate=((Element)el.childNode(5)).select("date1").text();
//        String docTi=el.select("ti").text();
//        String docText=(((Element)el.childNode(el.childNodeSize()-2)).select("text")).text();
        String docID=el.getElementsByTag("DOCNO").text();
        String docDate=el.getElementsByTag("DATE1").text();
        String docTi=el.getElementsByTag("TI").text();
        String docText=el.getElementsByTag("TEXT").text();
        documentsList.add(new Doc(docID,docDate,docTi,docText));
    }

    public LinkedList<Doc> getDocumentsList() {
        return documentsList;
    }

    public void clearDocumentsList(){
        documentsList=new LinkedList<>();
    }
}