package sample;

import java.util.Comparator;


public class SortIgnoreCase implements Comparator<Object>
{
    /**
     * The method overrides the "compare" method of "Comparator" in order to sort two Strings while ignoring all cases
     * @param o1
     * @param o2
     * @return
     */
    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        return s1.compareToIgnoreCase(s2);
    }
}