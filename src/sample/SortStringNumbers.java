package sample;

import java.util.Comparator;

public class SortStringNumbers implements Comparator<Object>
{
    /**
     * The method overrides the "compare" method of "Comparator" in order to sort two numeric Strings
     * @param o1
     * @param o2
     * @return
     */
    public int compare(Object o1, Object o2) {
        try {
            String s1 = (String) o1;
            String s2 = (String) o2;
            int s1Num=-1;
            int s2Num=-1;
            if(s1.contains(" "))
                s1Num=Integer.parseInt(s1.substring(0,s1.indexOf(" ")));
            else
                s1Num=Integer.parseInt(s1);
            if(s2.contains(" "))
                s2Num=Integer.parseInt(s2.substring(0,s2.indexOf(" ")));
            else
                s2Num=Integer.parseInt(s2);

            if(s1Num<s2Num)
                return -1;
            else if(s1Num>s2Num)
                return 1;
            else
                return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
