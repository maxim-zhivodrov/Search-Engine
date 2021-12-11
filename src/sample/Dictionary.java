package sample;

public class Dictionary
{
    String term;
    String appearances;

    public Dictionary(String term, String appearances) {
        this.term = term;
        this.appearances = appearances;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getAppearances() {
        return appearances;
    }

    public void setAppearances(String appearances) {
        this.appearances = appearances;
    }
}
