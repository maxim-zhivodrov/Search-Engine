package sample;

import com.sun.deploy.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SpecialParsing
{
    private String[] monthsFirst={"January","February","March","April","May","June","July","August","September","October","November","December"};
    private String[] monthsSecond={"JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"};
    private String[] monthsThird={"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
    private String[] monthsFourth={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
    protected List<String> monthsListFirst;
    protected List<String> monthsListSecond;
    protected List<String> monthsListThird;
    protected List<String> monthsListFourth;
    public SpecialParsing()
    {
        monthsListFirst = Arrays.asList(monthsFirst);
        monthsListSecond = Arrays.asList(monthsSecond);
        monthsListThird = Arrays.asList(monthsThird);
        monthsListFourth=Arrays.asList(monthsFourth);
    }

    /**
     * checking all combinations of one words
     * @param one
     * @return
     */
    public String SpecialParseOne(String one)
    {
        try {
            String term="";
            if(one.length()==0) return term;
            one=removeCommasAndDotsFromBeggining(one);
            one=removeCommasAndDotsFromEnd(one);
            if(one.length()==0) return term;
            term=checkPhrase(one);
//        --------------------- Phrases --------------------
            if(term.length()>0) return term;
//        ------------------ NUMBER K/M/B ---------------------
            boolean negativeNumber=false;
            if(one.charAt(0)=='-'){
                negativeNumber=true;
                one.substring(1);
            }
            if((one.contains(",") && one.charAt(0)!='$' && isNumeric(one.replace(",",""))) || isNumeric(one))
            {
                one=one.replace(",","");
                double num=Double.parseDouble(one);
                if(num>=1000000000)
                {
                    term=""+num/1000000000+"B";
                    if(term.contains(".0B"))
                        term=term.replaceAll(".0B","B");
                }
                else if(num>=1000000)
                {
                    term=""+num/1000000+"M";
                    if(term.contains(".0M"))
                        term=term.replaceAll(".0M","M");
                }
                else if(num>=1000)
                {
                    term=""+num/1000+"K";
                    if(term.contains(".0K"))
                        term=term.replaceAll(".0K","K");
                }
                if(term.length()>0)
                    one=term;
                term=checkThreeDigits(one);
                if(negativeNumber)
                    term="-"+term;
            }
    //        ----------------------- 6% -----------------------------
            else if(one.charAt(one.length()-1)=='%')
                term=one.substring(0,one.length()-1)+"%";
    //        ------------------------ $price -------------------------
            else if(one.charAt(0)=='$'&&isNumeric(one.substring(1).replace(",","")))
            {
                double num = Double.parseDouble(one.substring(1).replace(",",""));
                if(num<1000000)
                    term=term+one.substring(1)+" Dollars";
                else if(num>=1000000)
                {
                    num=num/1000000;
                    term=term+num;
                    term+=" M"+" Dollars";
                    term=checkDotZeroSpaceM(term);
                }
            }
    //        -------------------------------3/4------------------------------
            else if(one.contains("/")&&one.indexOf('/')==one.lastIndexOf('/')&&
                    one.indexOf('/')!=0&&one.indexOf('/')!=one.length()-1&&
                    isNumeric(one.substring(0,one.indexOf('/')).replace(",",""))
                    &&isNumeric(one.substring(one.indexOf('/')+1).replace(",",""))){
                        term=one;

            }
//        --------------------------------------------------------

            return term;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * checking all combinations of two words
     * @param one
     * @param two
     * @return
     */
    public String SpecialParseTwo(String one,String two)
    {
        try {
            String term="";
            if(one.length()==0||two.length()==0) return term;
            one=removeCommasAndDotsFromBeggining(one);
            two=removeCommasAndDotsFromEnd(two);
            if(one.length()==0||two.length()==0) return term;
//        ------------------------ Date -------------------------
            term=checkDate(one,two);
            if(term.length()>0)
                return term;
//        --------------------------------------------------------

            if(isNumeric(one.replace(",","")))
            {
    //        ---------------- 123 Thousand --------------------
                if(two.equals("Thousand"))
                    term=checkThreeDigits(one)+"K";
    //        ---------------- 55 Million --------------------
                else if(two.equals("Million"))
                    term=checkThreeDigits(one)+"M";
    //        ---------------- 55 Billion --------------------
                else if(two.equals("Billion"))
                    term=checkThreeDigits(one)+"B";
    //        ---------------- 10.6 percent/percentage --------------------
                else if(two.equals("percent") || two.equals("percentage"))
                    term=one+"%";
    //        -------------------------20 kilometers-----------------------
                else if(two.equalsIgnoreCase("kilometers") || two.equalsIgnoreCase("kilometer") || two.equalsIgnoreCase("km"))
                {
                    term=one+" km";
                }
    //        ------------------------------------------------------

                else if(two.equals("Dollars"))
                {
                    double num=Double.parseDouble(one.replace(",",""));
    //        ---------------- 1,000,000 Dollars --------------------
                    if(num>=1000000)
                    {
                        term=""+num/1000000+" M";
                        term=checkDotZeroSpaceM(term);
                    }
    //        ---------------- 1.732 Dollars --------------------
                    else
                        {
                        term=""+num;
                        if(term.contains(".0")&&term.charAt(term.length()-1)=='0'&&term.charAt(term.length()-2)=='.')
                            term=term.replaceAll(".0","");
                    }
    //        ----------------------------------------------------
                    term=term+" Dollars";
                }
    //        ---------------- 35 3/4 --------------------
                else if(two.indexOf('/')>0 && (isNumeric(two.substring(0,two.indexOf('/')).replace(",",""))
                        && isNumeric(two.substring(two.indexOf('/')+1).replace(",",""))))
                {
                    term=one+" "+two;
                }
    //        ---------------------------------------------

            }
            else
            {
                if(one.charAt(0)=='$'&&isNumeric(one.substring(1).replace(",","")))
                {
    //        ---------------- $100 million --------------------
                    if(two.equals("million"))
                        term=one.substring(1)+" M Dollars";
    //        ---------------- $100 billion --------------------
                    else if(two.equals("billion"))
                    {
                        double num=Double.parseDouble(one.substring(1).replace(",",""));
                        term=""+(num*1000)+" M Dollars";
                        term=checkDotZeroSpaceM(term);
                    }
    //        ---------------------------------------------------
                }
                else if(two.equals("Dollars")){
    //        ---------------- 20.6m Dollars --------------------
                    if(isNumeric(one.substring(0,one.length()-1))&&one.charAt(one.length()-1)=='m')
                        term=one.substring(0,one.length()-1)+" M Dollars";
                        //        ---------------- 100bn Dollars --------------------
                    else if(one.length()>1&&isNumeric(one.substring(0,one.length()-2))&&(one.charAt(one.length()-2)=='b'&&one.charAt(one.length()-1)=='n'))
                    {
                        double num=Double.parseDouble(one.substring(0,one.length()-2).replace(",",""));
                        term=""+(num*1000)+" M Dollars";
                        term=checkDotZeroSpaceM(term);
                    }

    //        ---------------------------------------------------

                }
            }

            return term;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * checking all combinations of three words
     * @param one
     * @param two
     * @param three
     * @return
     */
    public String SpecialParseThree(String one,String two,String three)
    {
        try {
            String term="";
            if(one.length()==0||two.length()==0||three.length()==0) return term;
            one=removeCommasAndDotsFromBeggining(one);
            three=removeCommasAndDotsFromEnd(three);
            if(one.length()==0||two.length()==0||three.length()==0) return term;
//        ---------------- 22 3/4 Dollars --------------------
            if(isNumeric(one.replace(",",""))&&three.equals("Dollars")&&two.indexOf('/')>=0
                    &&isNumeric(two.substring(0,two.indexOf('/')).replace(",",""))
                    &&isNumeric(two.substring(two.indexOf('/')+1).replace(",","")))
            {
                term=one+" "+two+" "+three;
            }
//        ---------------------------------------------------
            return term;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * checking all combinations of four words
     * @param one
     * @param two
     * @param three
     * @param four
     * @return
     */
    public String SpecialParseFour(String one,String two,String three,String four)
    {
        try {
            String term="";
            if(one.length()==0||two.length()==0||three.length()==0||four.length()==0) return term;
            one=removeCommasAndDotsFromBeggining(one);
            four=removeCommasAndDotsFromEnd(four);
            if(one.length()==0||two.length()==0||three.length()==0||four.length()==0) return term;
//        ---------------- 100 trillion/billion/million U.S. dollars --------------------
            if(isNumeric(one) && three.equals("U.S.") && ((four.equals("dollars") || four.equals("Dollars"))))
            {
                if(two.equals("million"))
                {
                    term=one+" M Dollars";
                }
                else if(two.equals("billion"))
                {
                    String num=""+Double.parseDouble(one.replace(",",""))*1000;
                    if(num.substring(num.length()-2).equals(".0"))
                        num=num.substring(0,num.length()-2);
                    term=num+" M Dollars";
                }
                else if(two.equals("trillion"))
                {
                    String num=""+Double.parseDouble(one.replace(",",""))*1000000;
                    if(num.substring(num.length()-2).equals(".0"))
                        num=num.substring(0,num.length()-2);
                    term=num+" M Dollars";
                }
            }
    //        ---------------- Between/between number and number --------------------
            else if((one.toLowerCase().equals("between")) &&
                    isNumeric(two.replace(",","")) &&
                    isNumeric(four.replace(",","")) && three.toLowerCase().equals("and"))
            {
                term=two+"-"+four;
            }
//        ------------------------------------------------------------------------
            return term;
        } catch (Exception e) {
            return "";
        }
    }





    /**
     * check if the String is a number
     * @param str
     * @return
     */
    public boolean isNumeric(String str)
    {
        try {
            int countDot=0;
            if(str.length()>0 ){
                for(int i=0;i<str.length();i++)
                {
                    if(!((str.charAt(i)>=48 && str.charAt(i)<=57) || str.charAt(i)=='.'))
                        return false;
                    if(str.charAt(i)=='.')
                    {
                        if(i==0 || countDot>0) return false;
                        countDot++;
                    }
                }
            }
            else
                return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
 * if a String has a comma or a dot in the end, the function removes it.
 * @param s
 * @return
 */
    protected String removeCommasAndDotsFromBeggining(String s){
        try {
            if(s.length()>0 && (s.charAt(0)=='.'||s.charAt(0)==','))
                s=s.substring(1);
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * if a String has a comma or a dot in the beginning, the function removes it.
     * @param s
     * @return
     */
    protected String removeCommasAndDotsFromEnd(String s){
        try {
            if(s.length()>0 && (s.charAt(s.length()-1)=='.'||s.charAt(s.length()-1)==','))
                s=s.substring(0,s.length()-1);
            return s;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * keeps a number accurate to the third number after the decimal point
     * @param one
     * @return
     */
    private String checkThreeDigits(String one) {
        try {
            String s="";
            if(one.contains("."))
            {
                if(one.charAt(one.length()-1)=='B' || one.charAt(one.length()-1)=='K' || one.charAt(one.length()-1)=='M')
                {
                    if((one.length()-1)-one.indexOf('.')>3){
                        s=one.substring(0,one.indexOf('.')+4)+one.charAt(one.length()-1);
                        if(s.charAt(s.length()-2)=='0'&&s.charAt(s.length()-3)=='0'&&s.charAt(s.length()-4)=='0')
                            s=s.substring(0,s.indexOf('.'))+s.charAt(s.length()-1);
                        else if(s.charAt(s.length()-2)=='0'&&s.charAt(s.length()-3)=='0')
                            s=s.substring(0,s.indexOf('.')+2)+s.charAt(s.length()-1);
                        else if(s.charAt(s.length()-2)=='0')
                            s=s.substring(0,s.indexOf('.')+3)+s.charAt(s.length()-1);
                        return s;
                    }

                }
                else if(one.length()-one.indexOf('.')>3){
                    s=one.substring(0,one.indexOf('.')+4);
                    if(s.charAt(s.length()-1)=='0'&&s.charAt(s.length()-2)=='0'&&s.charAt(s.length()-3)=='0')
                        s=s.substring(0,s.indexOf('.'));
                    else if(s.charAt(s.length()-1)=='0'&&s.charAt(s.length()-2)=='0')
                        s=s.substring(0,s.indexOf('.')+2);
                    else if(s.charAt(s.length()-1)=='0')
                        s=s.substring(0,s.indexOf('.')+3);
                    return s;
                }

                else return one;
            }
            return one;
        } catch (Exception e) {
            return one;
        }
    }

    /**
     * replaces ".0 M" with " M"
     * @param term
     * @return
     */
    private String checkDotZeroSpaceM(String term){
        try {
            if(term.contains(".0 M"))
                term=term.replaceAll(".0 M"," M");
            return term;
        } catch (Exception e) {
            return term;
        }
    }

    /**
     * checking if two word are a Date
     * @param one
     * @param two
     * @return
     */
    private String checkDate(String one,String two){
        try {
            String term="";
            String month="";
            String day="";

            if(isNumeric(one)&&(monthsListFirst.contains(two)||monthsListSecond.contains(two)||
                    monthsListThird.contains(two)||monthsListFourth.contains(two)))//14 MAY
            {
                if(monthsListFirst.contains(two))
                    month=""+(monthsListFirst.indexOf(two)+1);
                else if(monthsListSecond.contains(two))
                    month=""+(monthsListSecond.indexOf(two)+1);
                else if(monthsListThird.contains(two))
                    month=""+(monthsListThird.indexOf(two)+1);
                else if(monthsListFourth.contains(two))
                    month=""+(monthsListFourth.indexOf(two)+1);
                if(Double.parseDouble(month)<10)
                    month="0"+month;
                if(Double.parseDouble(one)<10)
                    day="0"+one;
                else
                    day=one;
                term=month+"-"+day;
            }
            else if(isNumeric(two)&&(monthsListFirst.contains(one)||monthsListSecond.contains(one)||
                    monthsListThird.contains(one)||monthsListFourth.contains(one)))//June 4 / May 1994
            {
                if(monthsListFirst.contains(one))
                    month=""+(monthsListFirst.indexOf(one)+1);
                else if(monthsListSecond.contains(one))
                    month=""+(monthsListSecond.indexOf(one)+1);
                else if(monthsListThird.contains(one))
                    month=""+(monthsListThird.indexOf(one)+1);
                else if(monthsListFourth.contains(one))
                    month=""+(monthsListFourth.indexOf(one)+1);
                if(Double.parseDouble(month)<10)
                    month="0"+month;
                if(Double.parseDouble(two)<10)
                    day="0"+two;
                else if(Double.parseDouble(two)<32)
                    day=two;
                if(day.length()==0)
                    term=two+"-"+month;
                else
                    term=month+"-"+day;
            }

            return term;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * checking phrases like Number-word | Word-Number | Number-number
     * @param one
     * @return
     */
    private String checkPhrase(String one){
        try {
            String term="";
            if(one.length()>1&&one.contains("-")&&one.indexOf('-')!=one.length()-1){
                boolean firstNumberNegative=false;
                if(one.charAt(0)=='-'){
                    one=one.substring(1);
                    firstNumberNegative=true;
                }
                int numberOfMinus=0;
                for(int i=0;i<one.length();i++){
                    if(one.charAt(i)=='-')
                        numberOfMinus++;
                }
                boolean twoMinusTogether=false;
                if(numberOfMinus==2){
                    String[] splittedArrayByMinus=one.split("-");
                    if(splittedArrayByMinus.length==3&&splittedArrayByMinus[1].length()==0)
                        twoMinusTogether=true;
                }
                if(numberOfMinus==1){
                    term=one;
                    if(firstNumberNegative&&isNumeric(one.substring(0,one.indexOf('-')).replace(",","")))
                        term="-"+term;
                    else if(firstNumberNegative&&!isNumeric(one.substring(0,one.indexOf('-')).replace(",","")))
                        term="";
                }
                else if(numberOfMinus==2&&!twoMinusTogether&&!firstNumberNegative){
                    String[] minusSplit=one.split("-");
                    boolean checkIfWord=true;
                    for(String s:minusSplit){
                        if(isNumeric(s.replace(",","")))
                            checkIfWord=false;
                    }
                    if(checkIfWord)
                        term=one;
                }
                else if(numberOfMinus==2&&twoMinusTogether&&isNumeric(one.substring(one.lastIndexOf('-')+1).replace(",",""))){
                    term=one;
                    if(firstNumberNegative&&isNumeric(one.substring(0,one.indexOf('-')).replace(",","")))
                        term="-"+term;
                    else if(firstNumberNegative&&!isNumeric(one.substring(0,one.indexOf('-')).replace(",","")))
                        term="";
                }

            }

            return term;
        } catch (Exception e) {
            return "";
        }

    }
}
