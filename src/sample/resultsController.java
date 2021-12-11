package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;



public class resultsController implements Initializable {
    public static Searcher searcher;
    private String docPicked;

    @FXML
    public TableView<Doc> docTable;
    public TableColumn<Doc,String> docCol;
    public Button searchEntitiesButton;
    public TableView<Doc> entityTable;
    public TableColumn<Doc,String> EntityNameCol;
    public TableColumn<Doc,String> EntityScoreCol;
    public Label docAmountLabel;



    @Override
    /**
     * The method initialized and updates the document table
     */
    public void initialize(URL location, ResourceBundle resources) {
        docAmountLabel.setText("Num of documents: "+Controller.relevantDocsForQueries.size());
        docCol.setCellValueFactory(new PropertyValueFactory<>("DocNo"));
        docCol.setCellFactory(TextFieldTableCell.forTableColumn());
        docCol.setOnEditCommit(e ->
        {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setDocNo(e.getNewValue());
        });

        ObservableList<Doc> table_data= FXCollections.observableArrayList();
        for(Map.Entry<String,Double> entry:Controller.relevantDocsForQueries.entrySet())
        {
            table_data.add(new Doc(entry.getKey(),"","",""));
        }
        docTable.setItems(table_data);
//        Controller.relevantDocsForQueries=null;
    }

    /**
     * The method saves the document the user picked
     */
    public void pickDocument()
    {
        try {
            docPicked=docTable.getSelectionModel().getSelectedItem().getDocNo();
            searchEntitiesButton.setDisable(false);

            entityTable.setVisible(false);
        } catch (Exception e) { }
    }


    /**
     * The method initializes and updates the Entities table
     * @throws IOException
     */
    public void initEntityTable() throws IOException
    {
        entityTable.setVisible(true);

        EntityNameCol.setCellValueFactory(new PropertyValueFactory<>("DocNo"));
        EntityNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        EntityNameCol.setOnEditCommit(e ->
        {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setDocNo(e.getNewValue());
        });

        EntityScoreCol.setCellValueFactory(new PropertyValueFactory<>("Date"));
        EntityScoreCol.setCellFactory(TextFieldTableCell.forTableColumn());
        EntityScoreCol.setOnEditCommit(e ->
        {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setDate(e.getNewValue());
        });

        ObservableList<Doc> table_data= FXCollections.observableArrayList();

        HashMap<String,Double> entityMap=findEntitiesInRelevantDoc();

        for(Map.Entry<String,Double> entry:entityMap.entrySet())
        {
            table_data.add(new Doc(entry.getKey(),entry.getValue()+"","",""));
        }
        entityTable.setItems(table_data);
    }

    /**
     * The method returns the 5 most dominant entities in the selected doc
     * @throws IOException
     */
    public HashMap<String,Double> findEntitiesInRelevantDoc() throws IOException {
        HashMap<String,Double> entitiesForDoc=new HashMap<>();
        BufferedReader BR=new BufferedReader(new FileReader(new File(dicController.staticPostingPath+"\\EntitiesIndex")));
        String line="";
        while((line=BR.readLine())!=null)
        {
            String fileDocNo=line.substring(0,line.indexOf(':'));
            if(!fileDocNo.equals(docPicked)) continue;
            else break;
        }
        if(line==null) return entitiesForDoc;
        String[] entitiesAndRanks=line.substring(line.indexOf(':')+2,line.lastIndexOf(',')).split(",");
        for(String entry:entitiesAndRanks)
        {
            String entityName=entry.substring(0,entry.lastIndexOf(' '));
            String entityRank=entry.substring(entry.lastIndexOf(' ')+1);
            entitiesForDoc.put(entityName,Double.parseDouble(entityRank));
        }
        entitiesForDoc=SortHashMap.sortHashMapByValueDouble(entitiesForDoc,"Down");
        BR.close();

        return entitiesForDoc;
    }

}
