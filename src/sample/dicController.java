package sample;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class dicController implements Initializable
{
    @FXML
    public TableView<Dictionary> dicTable;
    public TableColumn<Dictionary,String> colTerm;
    public TableColumn<Dictionary,String> colAppearances;
    public static String staticPostingPath;


    /**
     * This is aa function of the Interface "Initializable", and is responsible to initialize the Table by calling the needed methods
     * @param location
     * @param resources
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        initTable();
        try {
            loadData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void initTable()
    {
        initCols();
    }

    /**
     * The method starts the initialization process by setting the names of the columns, and continues it by calling further methods
     */
    private void initCols()
    {
        colTerm.setCellValueFactory(new PropertyValueFactory<>("term"));
        colAppearances.setCellValueFactory(new PropertyValueFactory<>("appearances"));
        editTableCols();
    }

    /**
     * The methos finishes th initialization process by assigning to each column with field of the class "Dictionary" it will represent.
     */
    private void editTableCols()
    {
        colTerm.setCellFactory(TextFieldTableCell.forTableColumn());
        colTerm.setOnEditCommit(e ->
        {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setTerm(e.getNewValue());
        });
        colAppearances.setCellFactory(TextFieldTableCell.forTableColumn());
        colAppearances.setOnEditCommit(e ->
        {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setAppearances(e.getNewValue());
        });

    }

    /**
     * The methos is responsible for reading the dictionary from the Psoting Files and loading it to the Table
     * @throws IOException
     */
    private void loadData() throws IOException {
        ObservableList<Dictionary> table_data=FXCollections.observableArrayList();
        File termDictionary=new File(staticPostingPath+"\\"+"TermDictionary");
        BufferedReader bf = new BufferedReader(new FileReader(termDictionary));
        String line=bf.readLine();
        while(line!=null)
        {
            String[] appearances=line.substring(line.indexOf(':')+2).split(",");
            table_data.add(new Dictionary(line.substring(0,line.indexOf(":")),appearances[0]));
            line=bf.readLine();
        }
        bf.close();
        dicTable.setItems(table_data);
    }


}
