package sample;

import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import javax.xml.soap.Text;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SemanticHandling {


    public LinkedList<String> handle(String inputWord,HashMap<String,String[]>termDictionary) throws IOException, Searcher.UnknownWordException {
        LinkedList<String> semanticWordsList=new LinkedList<>();
        try {
            Word2VecModel model=Word2VecModel.fromTextFile(new File("word2vec.c.output.model.txt"));
            Searcher semanticSearcher=model.forSearch();
            List<Searcher.Match> matches=semanticSearcher.getMatches(inputWord,20);
            int counter=0;
            for(Searcher.Match match:matches){
                String semWord=match.match();
                if(semWord.contains(" "))
                    semWord=ChangeToEntity(semWord);
                if(semWord.equals(inputWord)||!termDictionary.containsKey(semWord.toLowerCase()))continue;
                semanticWordsList.add(semWord);
                if((++counter)==2)
                    break;
            }
            return semanticWordsList;
        }
        catch (IOException e) { }
        catch (Searcher.UnknownWordException e) { }
        catch (Exception e) { }
        semanticWordsList.add("");
        return semanticWordsList;
    }

    private String ChangeToEntity(String word)
    {
        if(word.length()==0)
            return "";
        String ans="";
        ans+=Character.toUpperCase(word.charAt(0));
        for(int i=1;i<word.length();i++)
        {
            if(word.charAt(i-1)==' ')
                ans+=Character.toUpperCase(word.charAt(i));
            else
                ans+=word.charAt(i);
        }
        return ans+",";
    }
}
