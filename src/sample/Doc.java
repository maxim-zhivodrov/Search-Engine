package sample;

import java.util.LinkedList;

public class Doc
{
    private String DocNo;
    private String Date;
    private String TI;
    private String Text;

    public Doc(String docNo, String date, String TI, String text) {
        DocNo = docNo;
        Date = date;
        this.TI = TI;
        Text = text;
    }

    public void setDocNo(String docNo) {
        DocNo = docNo;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setTI(String TI) {
        this.TI = TI;
    }

    public void setText(String text) {
        Text = text;
    }

    public String getDocNo() {
        return DocNo;
    }

    public String getDate() {
        return Date;
    }

    public String getTI() {
        return TI;
    }

    public String getText() {
        return Text;
    }
}
