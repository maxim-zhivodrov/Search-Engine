package sample;

import java.util.*;

public class SortHashMap
{
    public static HashMap<String, Double> sortHashMapByValueDouble(HashMap<String, Double> hm,String direction)
    {
        List<Map.Entry<String, Double> > list = new LinkedList<>(hm.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if(direction.equals("UP"))
                return (o1.getValue()).compareTo(o2.getValue());
            else
                return (o2.getValue()).compareTo(o1.getValue());
        });

        HashMap<String, Double> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static HashMap<String, Integer> sortHashMapByValueInteger(HashMap<String, Integer> hm, String direction)
    {
        List<Map.Entry<String, Integer> > list = new LinkedList<>(hm.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if(direction.equals("UP"))
                return (o1.getValue()).compareTo(o2.getValue());
            else
                return (o2.getValue()).compareTo(o1.getValue());
        });

        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static HashMap<Integer, String> sortHashMapByKeyInteger(HashMap<Integer, String> hm, String direction)
    {
        List<Map.Entry<Integer, String> > list = new LinkedList<>(hm.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if(direction.equals("UP"))
                return (o1.getKey()).compareTo(o2.getKey());
            else
                return (o2.getKey()).compareTo(o1.getKey());
        });

        HashMap<Integer, String> temp = new LinkedHashMap<Integer, String>();
        for (Map.Entry<Integer, String> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    public static HashMap<Double, String> sortHashMapByKeyDouble(HashMap<Double, String> hm, String direction)
    {
        List<Map.Entry<Double, String> > list = new LinkedList<>(hm.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if(direction.equals("UP"))
                return (o1.getKey()).compareTo(o2.getKey());
            else
                return (o2.getKey()).compareTo(o1.getKey());
        });

        HashMap<Double, String> temp = new LinkedHashMap<Double, String>();
        for (Map.Entry<Double, String> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
