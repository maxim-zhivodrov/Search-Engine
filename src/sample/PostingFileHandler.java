package sample;

import java.io.*;
import java.util.*;

public class PostingFileHandler
{
    public static int numOfTerms;
    private String directoryPath;

    public PostingFileHandler(String directoryPath)
    {
        this.directoryPath = directoryPath;
        File postingFilesFolder=new File(this.directoryPath);
        openDirectory(postingFilesFolder);
        File entitiesFolder=new File(directoryPath+"\\"+"Entities");
        openDirectory(entitiesFolder);

    }

    /**
     * The method is merging the first two given files into the third given file,
     * in addition the method is upholding the "Big Small letters" rule and merging duplicate terms
     * @param firstPath
     * @param secondPath
     * @param goalPath
     * @throws IOException
     */
    protected void mergeTwoFiles(File firstPath, File secondPath, File goalPath) throws IOException
    {
        FileWriter pw = new FileWriter(goalPath,true);
        BufferedReader first = new BufferedReader(new FileReader(firstPath));
        BufferedReader second = new BufferedReader(new FileReader(secondPath));

        String firstLine = first.readLine();
        String secondLine = second.readLine();
        while (firstLine != null && secondLine != null)
        {
            String firstName=firstLine.substring(0,firstLine.indexOf(":"));
            String secondName=secondLine.substring(0,secondLine.indexOf(":"));
            String firstLineWithoutWhiteSpaces=firstLine.substring(0,firstLine.lastIndexOf(',')+1);
            String secondLineWithoutWhiteSpaces=secondLine.substring(0,secondLine.lastIndexOf(',')+1);
            if(firstName.compareToIgnoreCase(secondName)<0)
            {
                pw.write(firstLineWithoutWhiteSpaces);
                firstLine = first.readLine();
            }
            else if(firstName.compareToIgnoreCase(secondName)>0)
            {
                pw.write(secondLineWithoutWhiteSpaces);
                secondLine = second.readLine();
            }
            else if(firstName.compareToIgnoreCase(secondName)==0){
                if(checkIfTwoWords(firstName,secondName)){
                    if(Character.isLowerCase(firstName.charAt(0))||Character.isLowerCase(secondName.charAt(0))){
                        String firstContent=firstLine.substring(firstLine.indexOf(":")+2,firstLine.lastIndexOf(',')+1);
                        String secondContent=secondLine.substring(secondLine.indexOf(":")+2,secondLine.lastIndexOf(',')+1);
                        pw.write(firstName.toLowerCase()+": "+firstContent+secondContent);
                    }
                    else{
                        String secondContent=secondLine.substring(secondLine.indexOf(":")+2,secondLine.lastIndexOf(',')+1);
                        pw.write(firstLineWithoutWhiteSpaces+secondContent);
                    }

                    firstLine = first.readLine();
                    secondLine = second.readLine();
                }
                else{
                    if(firstName.compareTo(secondName)<0)
                    {
                        pw.write(firstLineWithoutWhiteSpaces);
                        firstLine = first.readLine();
                    }
                    else if(firstName.compareTo(secondName)>0)
                    {
                        pw.write(secondLineWithoutWhiteSpaces);
                        secondLine = second.readLine();
                    }
                    else if(firstName.compareTo(secondName)==0){
                        String secondContent=secondLine.substring(secondLine.indexOf(":")+2,secondLine.lastIndexOf(',')+1);
                        pw.write(firstLineWithoutWhiteSpaces+secondContent);
                        firstLine = first.readLine();
                        secondLine = second.readLine();
                    }

                }

            }
            pw.write("\n");
        }
        if(firstLine==null)
        {
            while(secondLine!=null)
            {
                String secondLineWithoutWhiteSpaces=secondLine.substring(0,secondLine.lastIndexOf(',')+1);
                pw.write(secondLineWithoutWhiteSpaces);
                pw.write("\n");
                secondLine = second.readLine();
            }
        }
        else if(secondLine==null)
        {
            while(firstLine!=null)
            {
                String firstLineWithoutWhiteSpaces=firstLine.substring(0,firstLine.lastIndexOf(',')+1);
                pw.write(firstLineWithoutWhiteSpaces);
                pw.write("\n");
                firstLine = first.readLine();
            }
        }


        pw.flush();
        first.close();
        second.close();
        pw.close();

    }



    /**
     * The methos is taking the big existing Posting File and splitting it into 8 final Posting files
     * @throws IOException
     */
    protected void finishPostingFiles() throws IOException
    {
        File postingFiles=new File(directoryPath);
        File goal=postingFiles.listFiles()[0];

        BufferedReader bf = new BufferedReader(new FileReader(goal));
        FileWriter[] fw = new FileWriter[8];
        File[] all=new File[8];
        all[0]=new File(directoryPath+"\\ab");
        all[1]=new File(directoryPath+"\\cde");
        all[2]=new File(directoryPath+"\\fghij");
        all[3]=new File(directoryPath+"\\klm");
        all[4]=new File(directoryPath+"\\nop");
        all[5]=new File(directoryPath+"\\qrs");
        all[6]=new File(directoryPath+"\\tuvwxyz");
        all[7]=new File(directoryPath+"\\ZZnum");

        for(int i=0;i<all.length;i++)
        {
            all[i].createNewFile();
            fw[i]=new FileWriter(all[i],true);
        }

        String line = bf.readLine();
        line=line.substring(0,line.lastIndexOf(',')+1);
        while(line!=null)
        {
            int fileNum=-1;
            char ch=Character.toLowerCase(line.charAt(0));
            if(ch>='a' && ch<='b') fileNum=0;
            else if(ch>='c' && ch<='e') fileNum=1;
            else if(ch>='f' && ch<='j') fileNum=2;
            else if(ch>='k' && ch<='m') fileNum=3;
            else if(ch>='n' && ch<='p') fileNum=4;
            else if(ch>='q' && ch<='s') fileNum=5;
            else if(ch>='t' && ch<='z') fileNum=6;
            else fileNum=7;

            fw[fileNum].write(line);
            fw[fileNum].write("\n");
            line = bf.readLine();
            if(line!=null)
                line=line.substring(0,line.lastIndexOf(',')+1);
        }
        for(int i=0;i<fw.length;i++)
        {
            fw[i].flush();
            fw[i].close();
        }
        bf.close();
        goal.delete();
    }

    /**
     * The method is merging all the initial posting files into one big file
     * @param EntitiesFolder
     * @throws IOException
     */
    protected void mergeAllFiles(String EntitiesFolder) throws IOException {
        //Tournament merge//
        File postingFiles=new File(directoryPath+EntitiesFolder);
        File[] postingFilesArray=sortFileArray(postingFiles.listFiles());
        int folderCounter=0;
        File newPostingFilesFolder=null;
        while(postingFilesArray.length>1){
            folderCounter++;
            newPostingFilesFolder=new File(directoryPath+EntitiesFolder+folderCounter);
            openDirectory(newPostingFilesFolder);
            if(postingFilesArray.length%2==0){
                File[] newPostingFiles=new File[postingFilesArray.length/2];
                for(int i=0;i<postingFilesArray.length;i+=2){
                    String newFilePath=newPostingFilesFolder.getPath()+"\\"+((i+1)/2);
                    File newFile=new File(newFilePath);
                    newFile.createNewFile();
                    newPostingFiles[(i+1)/2]=newFile;
                    if(EntitiesFolder.length()==0)
                        mergeTwoFiles(postingFilesArray[i],postingFilesArray[i+1],newPostingFiles[(i+1)/2]);
                    else
                        mergeTwoEntitiesFiles(postingFilesArray[i],postingFilesArray[i+1],newPostingFiles[(i+1)/2]);
                    postingFilesArray[i].delete();
                    postingFilesArray[i+1].delete();
                }
                postingFilesArray=sortFileArray(newPostingFiles);
            }
            else if(postingFilesArray.length%2==1){
                File[] newPostingFiles=new File[postingFilesArray.length/2+1];
                for(int i=0; i<postingFilesArray.length-1;i+=2){
                    String newFilePath=newPostingFilesFolder.getPath()+"\\"+((i+1)/2);
                    File newFile=new File(newFilePath);
                    newFile.createNewFile();
                    newPostingFiles[(i+1)/2]=newFile;
                    if(EntitiesFolder.length()==0)
                        mergeTwoFiles(postingFilesArray[i],postingFilesArray[i+1],newPostingFiles[(i+1)/2]);
                    else
                        mergeTwoEntitiesFiles(postingFilesArray[i],postingFilesArray[i+1],newPostingFiles[(i+1)/2]);
                    postingFilesArray[i].delete();
                    postingFilesArray[i+1].delete();
                }
                String remainingFilePath=newPostingFilesFolder.getPath()+"\\"+(newPostingFiles.length-1);
                File remainigFile=new File(remainingFilePath);
                remainigFile.createNewFile();
                copyTheRemainingFile(postingFilesArray[postingFilesArray.length-1],remainigFile);
                newPostingFiles[newPostingFiles.length-1]=remainigFile;
                postingFilesArray[postingFilesArray.length-1].delete();
                postingFilesArray=sortFileArray(newPostingFiles);


            }
            if(folderCounter==1){
                File deleteFile=new File(directoryPath+EntitiesFolder);
                deleteFile.delete();
            }
            else{
                File deleteFile=new File(directoryPath+EntitiesFolder+(folderCounter-1));
                deleteFile.delete();
            }
        }
        if(folderCounter!=0&&newPostingFilesFolder!=null){
            File newName=new File(directoryPath+EntitiesFolder);
            newPostingFilesFolder.renameTo(newName);
        }



    }

    /**
     * The methods gets two terms and checks if they are english words or numeric
     * @param s1
     * @param s2
     * @return
     */
    private boolean checkIfTwoWords(String s1,String s2){
        char c1=s1.charAt(0);
        char c2=s2.charAt(0);

        return ((Character.isUpperCase(c1)||Character.isLowerCase(c1))&&(Character.isUpperCase(c2)||Character.isLowerCase(c2)));

    }

    /**
     * if the size of the initial posting files is odd, the function is copying the last file to the big Posting File
     * @param f1
     * @param f2
     * @throws IOException
     */
    private void copyTheRemainingFile(File f1,File f2) throws IOException {
        BufferedReader BF=new BufferedReader(new FileReader(f1));
        FileWriter FW=new FileWriter(f2,true);
        String newLine="";
        while((newLine=BF.readLine())!=null){
            String newLineWithoutWhitespaces=newLine.substring(0,newLine.lastIndexOf(',')+1);
            FW.write(newLineWithoutWhitespaces+"\n");
        }
        FW.flush();
        BF.close();
        FW.close();

    }

    /**
     * The method sorts the initial Posting Files from smallest to biggest
     * @param fileArray
     * @return
     */
    private File[] sortFileArray(File[] fileArray)
    {
        File[] newFileArray=new File[fileArray.length];
        String[] oldFileArrayName=new String[fileArray.length];

        for(int i=0;i<oldFileArrayName.length;i++)
            oldFileArrayName[i]=fileArray[i].getName();

        int[] oldFileArrayNumber=new int[fileArray.length];
        for(int i=0;i<oldFileArrayNumber.length;i++)
            oldFileArrayNumber[i]=Integer.parseInt(oldFileArrayName[i]);

        Arrays.sort(oldFileArrayNumber);

        for(int i=0;i<newFileArray.length;i++){
            for(int j=0;j<fileArray.length;j++){
                if(fileArray[j].getName().equals(oldFileArrayNumber[i]+"")){
                    newFileArray[i]=fileArray[j];
                    break;
                }

            }
        }

       return newFileArray;

    }

    /**
     * The method creates the final Term Dictionary File, which contains:
     * the term, number of appearances ot the term, number of docs containing the term, path to relevant posting file
     * @throws IOException
     */
    protected void makeTermDictionary() throws IOException {
        numOfTerms=0;
        File postingFilesFolder=new File(directoryPath);
        File newTermDictionary=new File(directoryPath+"\\"+"TermDictionary");
        newTermDictionary.createNewFile();
        FileWriter FW=new FileWriter(newTermDictionary,true);
        for(File postingFile:postingFilesFolder.listFiles()){
            if(postingFile.getName().equals("TermDictionary"))continue;
            BufferedReader BF=new BufferedReader(new FileReader(directoryPath+"\\"+postingFile.getName()));
            String line="";
            while((line=BF.readLine())!=null){
                line=line.substring(0,line.lastIndexOf(',')+1);
                String numOFApereancesAndNumOfDocs=getAppearancesAndNumOfDocs(line);
                FW.write(line.substring(0,line.indexOf(':'))+": "+numOFApereancesAndNumOfDocs+","+directoryPath+"\\"+postingFile.getName());
                FW.write("\n");
                numOfTerms++;
             }
            BF.close();
        }
        FW.flush();
        FW.close();

    }

    /**
     * The method returns the number of appearances ot the term and the number of docs containing the term
     * @param line
     * @return
     */
    private String getAppearancesAndNumOfDocs(String line)
    {
        int numOfApperances=0;
        int numOfDocs=0;
        String[] data= null;
        data = line.substring(line.indexOf(":")+2,line.length()-1).replaceAll(" ",",").split(",");
        for(int i=0;i<data.length;i++)
        {
            if(i%2==1)
                numOfApperances+=Integer.parseInt(data[i]);
            else
                numOfDocs++;
        }
        return ""+numOfApperances+","+numOfDocs;
    }

    /**
     * The method creates the Doc Dictionary File, which contains:
     * the document number, the frequency of most frequent term, number of unique terms, parsed title, date
     * @param docDictionary
     * @throws IOException
     */
    protected void makeDocDictionary(HashMap<Integer,String[]> docDictionary) throws IOException {
        File newDocDictionary=new File(directoryPath+"\\"+"DocDictionary");
        newDocDictionary.createNewFile();
        FileWriter FW=new FileWriter(newDocDictionary,true);
        for(Map.Entry<Integer,String[]> docInfo:docDictionary.entrySet()){
            FW.write(docInfo.getKey()+": "+docInfo.getValue()[0]+"_"+docInfo.getValue()[1]+"_"+docInfo.getValue()[2]+"_"+docInfo.getValue()[3]+"_"+docInfo.getValue()[4]);
            FW.write("\n");
        }
        FW.flush();
        FW.close();
    }

    /**
     * The method handles all the duplicate terms in a posting file
     * @param fileName
     * @throws IOException
     */
    protected void handleCopiesInFile(String fileName) throws IOException
    {
        File oldLettersFile=new File(directoryPath+"\\"+fileName);
        BufferedReader BF=new BufferedReader(new FileReader(oldLettersFile));
        HashMap<String,String> hashMapForTerms=new HashMap<>();
        File newFileForLetters=new File(directoryPath+"\\"+fileName+"PF");
        newFileForLetters.createNewFile();
        FileWriter FW=new FileWriter(newFileForLetters,true);
        String line="";
        char[] firstChars={'?','?'};
        while((line=BF.readLine())!=null){
            line=line.substring(0,line.lastIndexOf(',')+1);
            String term=line.substring(0,line.indexOf(':'));
            if (term.charAt(0)==firstChars[0]||term.charAt(0)==firstChars[1]) {
                if(hashMapForTerms.containsKey(term)){
                    String[] oldValue=hashMapForTerms.get(term).split(",");
                    String[] newValue=line.substring(line.indexOf(":")+2).split(",");
                    String[] combinedValue=new String[oldValue.length+newValue.length];
                    int index=0;
                    while(index<oldValue.length){
                        combinedValue[index]=oldValue[index];
                        index++;
                    }
                    while(index<combinedValue.length){
                        combinedValue[index]=newValue[index-oldValue.length];
                        index++;
                    }
                    List<String> combinedValueList=Arrays.asList(combinedValue);
                    Collections.sort(combinedValueList,new SortStringNumbers());
                    String newTailForTerm="";
                    for(String s:combinedValueList)
                        newTailForTerm=newTailForTerm+s+",";
                    hashMapForTerms.put(term,newTailForTerm);
                }
                else
                    hashMapForTerms.put(term,line.substring(line.indexOf(":")+2));
            } else {
                if(hashMapForTerms.size()>0){
                    ArrayList<String> keys=new ArrayList<>(hashMapForTerms.keySet());
                    Collections.sort(keys,new SortIgnoreCase());
                    for(String key:keys){
                        String value=hashMapForTerms.get(key);
                        FW.write(key+": "+value);
                        FW.write("\n");
                    }
                    hashMapForTerms=new HashMap<>();
                }

                hashMapForTerms.put(term,line.substring(line.indexOf(':')+2));
                firstChars[0]=Character.toLowerCase(term.charAt(0));
                firstChars[1]=Character.toUpperCase(term.charAt(0));


            }
        }
        if(hashMapForTerms.size()>0){
            ArrayList<String> keys=new ArrayList<>(hashMapForTerms.keySet());
            Collections.sort(keys,new SortIgnoreCase());
            for(String key:keys){
                String value=hashMapForTerms.get(key);
                FW.write(key+": "+value);
                FW.write("\n");
            }
        }


        FW.flush();
        FW.close();
        BF.close();
        oldLettersFile.delete();

    }

    /**
     * The method handles all the duplicate terms in all the final posting file
     * @throws IOException
     */
    protected void handleCopies() throws IOException {
        handleCopiesInFile("ab");
        handleCopiesInFile("cde");
        handleCopiesInFile("fghij");
        handleCopiesInFile("klm");
        handleCopiesInFile("nop");
        handleCopiesInFile("qrs");
        handleCopiesInFile("tuvwxyz");
        handleCopiesInFile("ZZnum");
    }

    /**
     * The method opens a new directory for the Posting Files (while deleting any existing ones)
     * @param folder
     */
    protected void openDirectory(File folder){
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
        folder.mkdir();
    }


    /**
     * The method merges two entity files
     */
    protected void mergeTwoEntitiesFiles(File firstPath, File secondPath, File goalPath) throws IOException {

        FileWriter pw = new FileWriter(goalPath,true);
        BufferedReader first = new BufferedReader(new FileReader(firstPath));
        BufferedReader second = new BufferedReader(new FileReader(secondPath));

        String firstLine = first.readLine();
        String secondLine = second.readLine();

        while(firstLine != null && secondLine != null){
            String firstName=firstLine.substring(0,firstLine.indexOf(":"));
            String secondName=secondLine.substring(0,secondLine.indexOf(":"));
            String firstLineWithoutWhiteSpaces=firstLine.substring(0,firstLine.lastIndexOf(',')+1);
            String secondLineWithoutWhiteSpaces=secondLine.substring(0,secondLine.lastIndexOf(',')+1);

            if(firstName.compareTo(secondName)<0)
            {
                pw.write(firstLineWithoutWhiteSpaces);
                firstLine = first.readLine();
            }
            else if(firstName.compareTo(secondName)>0)
            {
                pw.write(secondLineWithoutWhiteSpaces);
                secondLine = second.readLine();
            }
            else if(firstName.compareTo(secondName)==0){
                String secondContent=secondLine.substring(secondLine.indexOf(":")+2,secondLine.lastIndexOf(',')+1);
                pw.write(firstLineWithoutWhiteSpaces+secondContent);
                firstLine = first.readLine();
                secondLine = second.readLine();
            }
            pw.write("\n");
        }

        if(firstLine==null)
        {
            while(secondLine!=null)
            {
                String secondLineWithoutWhiteSpaces=secondLine.substring(0,secondLine.lastIndexOf(',')+1);
                pw.write(secondLineWithoutWhiteSpaces);
                pw.write("\n");
                secondLine = second.readLine();
            }
        }
        else if(secondLine==null)
        {
            while(firstLine!=null)
            {
                String firstLineWithoutWhiteSpaces=firstLine.substring(0,firstLine.lastIndexOf(',')+1);
                pw.write(firstLineWithoutWhiteSpaces);
                pw.write("\n");
                firstLine = first.readLine();
            }
        }


        pw.flush();
        first.close();
        second.close();
        pw.close();
    }

    /**
     * The method adds the entity file to the rest
     */
    protected void copyEntitiesFileToTheRest(int postingFileCounter,HashMap<Integer,String[]> docDictionary) throws IOException {
        File entitiesFolder=new File(directoryPath+"\\"+"Entities");
        File[] entitiesFolderArray=entitiesFolder.listFiles();
        BufferedReader BF=new BufferedReader(new FileReader(entitiesFolderArray[0]));
        File entitiesPostingFile=new File(directoryPath+"\\"+postingFileCounter);
        entitiesPostingFile.createNewFile();
        FileWriter FW=new FileWriter(entitiesPostingFile,true);
        String line="";
        while((line=BF.readLine())!=null){
            line=line.substring(0,line.lastIndexOf(',')+1);
            String[] numOfDocs=line.substring(line.indexOf(':')+2).replace(" ",",").split(",");
            if(numOfDocs.length>2){
                FW.write(line);
                FW.write("\n");
                for(int i=0;i<numOfDocs.length;i+=2){
                    int docNum=Integer.parseInt(numOfDocs[i]);
                    String[] arrayForDocDictionary=docDictionary.get(docNum);
                    arrayForDocDictionary[1]=(Integer.parseInt(arrayForDocDictionary[1])+1)+"";
                    docDictionary.put(docNum,arrayForDocDictionary);
                }
            }


        }


        FW.flush();
        FW.close();
        BF.close();
        entitiesFolderArray[0].delete();
        entitiesFolder.delete();
        int n=0;
    }

    /**
     * The method creates an index file for entities, the line in the file will include
     * the document ID and the 5 most dominant entities in it
     * @throws IOException
     */
    protected void makeEntitiesIndex(HashMap<Integer,String[]> docDictionary) throws IOException {
        HashMap<String,HashMap<String,Double>> docsWithEntitiesNum=new HashMap<>();
        File entitiesIndex=new File(directoryPath+"\\"+"EntitiesIndex");
        if(entitiesIndex.exists())
            entitiesIndex.delete();
        entitiesIndex.createNewFile();
        File[] arrPF=(new File(directoryPath)).listFiles();
        for(File PF:arrPF){
            if(PF.getName().equals("TermDictionary")||PF.getName().equals("DocDictionary")||PF.getName().equals("EntitiesIndex"))
                continue;
            BufferedReader BR=new BufferedReader(new FileReader(PF));
            String line="";
            while((line=BR.readLine())!=null){
                String lineTerm=line.substring(0,line.indexOf(":"));
                if(isEntity(lineTerm)){
                    String[] lineContent=line.substring(line.indexOf(":")+2,line.lastIndexOf(',')+1).replace(" ",",").split(",");
                    if(lineContent.length<=2)
                        continue;
                    for(int i=0;i<lineContent.length;i+=2){
                        if(docsWithEntitiesNum.containsKey(lineContent[i])){
                            HashMap<String,Double> tempHM=docsWithEntitiesNum.get(lineContent[i]);
                            tempHM.put(lineTerm,Double.parseDouble(lineContent[i+1]));
                            docsWithEntitiesNum.put(lineContent[i],tempHM);
                        }
                        else{
                            HashMap<String,Double> newHM=new HashMap<>();
                            newHM.put(lineTerm,Double.parseDouble(lineContent[i+1]));
                            docsWithEntitiesNum.put(lineContent[i],newHM);
                        }
                    }
                }
            }
            BR.close();
        }

        FileWriter FW=new FileWriter(entitiesIndex,true);
        for(Map.Entry<String,HashMap<String,Double>> outEntry:docsWithEntitiesNum.entrySet()){
            HashMap<String,Double> hmOfDoc=SortHashMap.sortHashMapByValueDouble(outEntry.getValue(),"DOWN");
            String[] docInfo=docDictionary.get(Integer.parseInt(outEntry.getKey()));
            String docID=docInfo[docInfo.length-1];
            FW.write(docID+": ");
            int counter=0;
            for(Map.Entry<String,Double> inEntry:hmOfDoc.entrySet()){
                String appreances=inEntry.getValue()+"";
                FW.write(inEntry.getKey()+" "+appreances.substring(0,appreances.indexOf("."))+",");
                if((++counter)==5)
                    break;
            }
            FW.write("\n");
        }
        FW.flush();
        FW.close();
    }



    /**
     * The method check whether or not a tern is an entity
     */
    private boolean isEntity(String term)
    {
        if(term.contains(" "))
        {
            String[] split=term.split(" ");
            for(int i=0;i<split.length;i++){
                if(split[i].length()==0)
                    continue;
                if(!Character.isUpperCase(split[i].charAt(0)))
                    return false;
            }

            return true;
        }
        return false;

    }


}
