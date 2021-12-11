package sample;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class Controller implements Initializable
{
    private Indexer i;
    private ReadFile rf;
    protected static String stopWordsPath;
    private String postingPath;
    private HashMap<String,String[]> TermDictionary;
    private HashMap<Integer,String> browsedQueries=null; //key: query number , value: query content
    public static HashMap<String,Double> relevantDocsForQueries;
    private HashMap<Integer,String> queryDesc=null;


    @FXML
    public Button browseC;
    public Button browsePF;
    public Button saveQueryButton;
    public Button browseQueryButton;
    public Button startButton;
    public TextField corpusPathText;
    public TextField postingPathText;
    public TextField saveQueryText;
    public TextField writeQueryText;
    public CheckBox stemmCheck;
    public CheckBox semanticsCheckBox;
    public Button showButton;
    public Button loadButton;
//    public TableView<Doc> docTable;
//    public TableColumn<Doc,String> docCol;


    public TextField getPostingPathText() {
        return postingPathText;
    }


    /**
     * he method allows the user to choose a path, according to the button he clicked
     * @param evt
     * @throws IOException
     */
    public void browse(Event evt) throws IOException {
        DirectoryChooser fc=new DirectoryChooser();
        File selectedFile=fc.showDialog(null);
        if(selectedFile!=null)
        {
            if(evt.getSource().equals(browseC))
            {
                corpusPathText.setText(selectedFile.getAbsolutePath());
                checkStemm();
                updateStopWordsPath();
            }
            else if(evt.getSource().equals(browsePF))
            {
                postingPathText.setText(selectedFile.getAbsolutePath());
                checkStemm();
                updateStopWordsPath();

            }
            else if(evt.getSource().equals(saveQueryButton))
            {
                saveQueryText.setText(selectedFile.getAbsolutePath());
            }
        }
    }


    /**
     * The method updates the path for the Posting files according to the user's choice whether to use Stemming or not
     */
    public void checkStemm()
    {

        postingPath=postingPathText.getText();
        if(stemmCheck.isSelected())
        {
            postingPath+="\\sttemed";
            Indexer.toStemm=true;
        }
        else
        {
            postingPath+="\\notSttemed";
            Indexer.toStemm=false;
        }
        dicController.staticPostingPath=postingPath;
    }

    /**
     * The function is responsible for the entire process of Reading, Parsing and Indexing the Corpus
     * @throws IOException
     */
    public void readParseAndIndex() throws IOException
    {
        if(corpusPathText.getText().length()==0 || postingPathText.getText().length()==0)
        {
            showAlert("Please fill the text boxes :(", Alert.AlertType.WARNING);
            return;
        }
        checkStemm();
        updateStopWordsPath();
        boolean finishedRead=false;
        rf = new ReadFile(corpusPathText.getText());
        i=new Indexer(postingPath,stemmCheck.isSelected());
        startButton.setDisable(true);
        long start = System.currentTimeMillis();
        while(!finishedRead)
        {
            rf.Read();
            if (rf.getDocumentsList().size()>0) {
                i.setDocumentList(rf.getDocumentsList());
                i.index();
                rf.clearDocumentsList();
            }
            else
                finishedRead=true;
        }
        i.handlePostingFiles();
        long end = System.currentTimeMillis();
        startButton.setDisable(false);
        ReadFile.docsRead=0;
        showButton.setDisable(false);
        loadButton.setDisable(false);

        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Indexing Information");
        alert.setContentText("Number of indexed document: " + Indexer.totalNumOfDos + "\n\n" + "Number of unique terms: "+ PostingFileHandler.numOfTerms + "\n\n" + "Total working time: " + (end-start)/1000+" seconds");
        alert.show();
    }

    /**
     * The function deletes all the Posting Files and resets all the data structures to be null
     */
    public void resetSystem()
    {
        if(postingPathText.getText().length()==0 || postingPathText.getText().length()==0)
        {
            showAlert("Please fill the text boxes :(", Alert.AlertType.WARNING);
            return;
        }
        deleteDirectory(postingPathText.getText()+"\\sttemed");
        deleteDirectory(postingPathText.getText()+"\\notSttemed");
        i=null;
        rf=null;
        TermDictionary=null;
    }

    /**
     * The function deletes the given directory, and all it's subdirectories
     * @param path
     */
    protected void deleteDirectory(String path){
        File folder=new File(path);
        if(folder.exists())
        {
            String[]entries = folder.list();
            for(String s: entries)
            {
                File currentFile = new File(folder.getPath(),s);
                currentFile.delete();
            }
            folder.delete();
        }
        else
        {
            showAlert("Nothing to erase", Alert.AlertType.INFORMATION);
        }
    }

    /**
     * The function show a new Window in which the Dictionary is presented
     * @throws IOException
     */
    public void showDic() throws IOException
    {
        checkStemm();
        File postingFiles=new File(postingPath+"\\"+"TermDictionary");
        if(postingPathText.getText().length()==0 || !postingFiles.exists())
        {
            showAlert("No Posting Files Found", Alert.AlertType.WARNING);
            return;
        }
        Parent root = FXMLLoader.load(getClass().getResource("dicSample.fxml"));
        Stage dicStage=new Stage();
        dicStage.setScene(new Scene(root, 410, 400));
        dicStage.show();
    }

    /**
     * The method reads all the Posting Files and loads them into a data structure in the main memory
     * @throws IOException
     */
    public void loadDictionary() throws IOException {
        checkStemm();
        updateStopWordsPath();
        TermDictionary=new HashMap<>();
        File postingFiles=new File(postingPath+"\\"+"TermDictionary");
        if(postingPathText.getText().length()==0 || !postingFiles.exists())
        {
            showAlert("No Posting Files Found", Alert.AlertType.WARNING);
            return;
        }

        BufferedReader BR=new BufferedReader(new FileReader(postingFiles));
        String line="";
        while((line=BR.readLine())!=null)
        {
            String term=line.substring(0,line.indexOf(":"));
            if(TermDictionary.containsKey(term.toLowerCase())){
                String[] oldTermArray=TermDictionary.get(term.toLowerCase());
                String[] newTermArray=line.substring(line.indexOf(":")+2).split(",");
                String[] combinedTermArray=new String[oldTermArray.length+1];
                for(int i=0;i<oldTermArray.length;i++){
                    if(i==0||i==1){
                        combinedTermArray[i]=(Integer.parseInt(oldTermArray[i])+Integer.parseInt(newTermArray[i]))+"";
                    }
                    else{
                        combinedTermArray[i]=oldTermArray[i];
                    }
                }
                combinedTermArray[combinedTermArray.length-1]=term;
                TermDictionary.put(term.toLowerCase(),combinedTermArray);
            }
            else{
                String[] termArrayFromLine=line.substring(line.indexOf(":")+2,line.lastIndexOf('F')+1).split(",");
                String[] termArrayToPut=new String[termArrayFromLine.length+1];
                for(int i=0;i<termArrayFromLine.length;i++)
                    termArrayToPut[i]=termArrayFromLine[i];
                termArrayToPut[3]=term;
                termArrayToPut[2]=postingPath+"\\"+termArrayToPut[2].substring(termArrayToPut[2].lastIndexOf('\\')+1);
                TermDictionary.put(term.toLowerCase(),termArrayToPut);

            }
        }
        BR.close();
        showAlert("Term Dictionary loaded succesfully", Alert.AlertType.INFORMATION);
    }

    /**
     * The function shows an alert with the given text
     * @param context
     */
    public void showAlert(String context, Alert.AlertType type)
    {
        Alert a=new Alert(type);
        a.setContentText(context);
        a.show();
    }

    /**
     * The method reads the Queries file, and organizes it into a hashMap
     * @throws IOException
     */
    public void parseQueries() throws IOException
    {
        writeQueryText.setText("");
        browsedQueries=new HashMap<>();
        queryDesc=new HashMap<>();
        FileChooser fc=new FileChooser();
        File selectedFile=fc.showOpenDialog(null);
        if(selectedFile!=null) {
            BufferedReader BR=new BufferedReader(new FileReader(selectedFile));
            String line="";
            String queryNum="";
            while((line=BR.readLine())!=null){
                if(line.contains("<num>")){
                    queryNum=line.substring(line.indexOf(":")+1);
                }
                else if(line.contains("<title>")){
                    browsedQueries.put(Integer.parseInt(queryNum.replace(" ","")),line.substring(line.indexOf(">")+1));
                }
                else if(line.contains("<desc>")){
                    String desc="";
                    while((line=BR.readLine())!=null&&!(line.contains("<narr>")||line.contains("</top>")))
                        desc=desc+" "+line;
                    queryDesc.put(Integer.parseInt(queryNum.replace(" ","")),desc);
                }
            }
            BR.close();

            browsedQueries=SortHashMap.sortHashMapByKeyInteger(browsedQueries,"UP");
            queryDesc=SortHashMap.sortHashMapByKeyInteger(queryDesc,"UP");

        }

    }


    /**
     * the method find the 50 of the most relevant documents for the submitted query/queries
     * @throws IOException
     * @throws InterruptedException
     */
    public void executeQuery() throws IOException, InterruptedException, com.medallia.word2vec.Searcher.UnknownWordException {
        relevantDocsForQueries=new HashMap<>();
        if(postingPathText.getText().length()==0||TermDictionary==null)
        {
            showAlert("make sure you loaded the term dictionary", Alert.AlertType.WARNING);
            return;
        }
        checkStemm();
        updateStopWordsPath();
        double [] avgAndSize=docDictionaryAverageAndSize();
        Searcher searcher=new Searcher(postingPath,(int)avgAndSize[0],avgAndSize[1]);
        if(browsedQueries==null && writeQueryText.getText().equals(""))
        {
            showAlert("No Query Inserted", Alert.AlertType.WARNING);
            return;
        }
        else if(!(writeQueryText.getText().equals(""))) //Single query
        {
            searcher.Search(writeQueryText.getText(),TermDictionary,semanticsCheckBox.isSelected(),"");
            saveQueriesToFile(searcher,1,true);
            resultsController.searcher=searcher;
            updateRelevantDocsForQueries(searcher);

        }
        else //multiple queries
        {
            executeMultipleQueries(searcher);
        }


        Parent root = FXMLLoader.load(getClass().getResource("results.fxml"));
        Stage dicStage=new Stage();
        dicStage.setScene(new Scene(root, 600, 600));
        dicStage.show();
    }

    /**
     * the method find the 50 of the most relevant documents for each of the queries in the submitted file
     * @param searcher
     * @throws IOException
     */
    private void executeMultipleQueries(Searcher searcher) throws IOException, com.medallia.word2vec.Searcher.UnknownWordException {
//        if(saveQueryText.getText().equals(""))
//        {
//            showAlert("Please choose a place to save the results", Alert.AlertType.WARNING);
//        }
//        else
//        {
            boolean firstQuery=true;
            for(Map.Entry<Integer,String> entry:browsedQueries.entrySet())
            {
                searcher.Search(entry.getValue(),TermDictionary, semanticsCheckBox.isSelected(),queryDesc.get(entry.getKey()));
                saveQueriesToFile(searcher,entry.getKey(),firstQuery);
                firstQuery=false;
                updateRelevantDocsForQueries(searcher);

            }
//            showAlert("Finished processing, results save to file", Alert.AlertType.INFORMATION);
//        }
    }

    /**
     * the method saves the results of the query/queries into a file
     * @param searcher
     * @param queryNum
     * @param makeNewFile
     * @throws IOException
     */
    private void saveQueriesToFile(Searcher searcher, int queryNum, boolean makeNewFile) throws IOException
    {
        if(saveQueryText.getText().equals("")) return;
        File resultFile=new File(saveQueryText.getText()+"\\results.txt");
//        if(!resultFile.exists()) resultFile.createNewFile();
        if(makeNewFile)
        {
            while(!resultFile.createNewFile())
                resultFile.delete();
        }
        FileWriter fw=new FileWriter(resultFile,true);
        for(Map.Entry<String,Double> entry:searcher.relevantDocuments.entrySet())
        {
            fw.write(queryNum+" 0 "+entry.getKey()+" 1 "+entry.getValue()+" mt"+"\n");
        }
        fw.flush();
        fw.close();

    }

    public void initialize(URL location, ResourceBundle resources) { }


    /**
     * The method returns the average and size of the docDictionary
     * @return
     * @throws IOException
     */
    private double[] docDictionaryAverageAndSize() throws IOException
    {
        BufferedReader BR=new BufferedReader(new FileReader(new File(postingPath+"\\"+"DocDictionary")));
        double[] arr=new double[2];
        int numOfDocs=0;
        int sum=0;
        String line="";
        while((line=BR.readLine())!=null){
            numOfDocs++;
            sum+=Integer.parseInt(line.substring(line.indexOf(":")+2).split("_")[1]);
        }
        BR.close();
        arr[0]=numOfDocs;
        arr[1]=sum/numOfDocs;
        return arr;

    }

    /**
     * the method the relevant docs for the current query
     */
    private void updateRelevantDocsForQueries(Searcher searcher)
    {
        for(Map.Entry<String,Double> entry:searcher.relevantDocuments.entrySet())
            relevantDocsForQueries.put(entry.getKey(),entry.getValue());
    }

    /**
     *  The method updates the location of the stop words
     */
    private void updateStopWordsPath(){
        if(new File(corpusPathText.getText()+"\\05 stop_words.txt").exists())
            stopWordsPath=corpusPathText.getText()+"\\05 stop_words.txt";
        else if(new File(postingPathText.getText()+"\\05 stop_words.txt").exists())
            stopWordsPath=postingPathText.getText()+"\\05 stop_words.txt";
    }
}
