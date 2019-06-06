package jusacco.TPFinal.Worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class Test {

    public static void main(String... args) {
        ArrayList<String> arrA = new ArrayList<String>();
        ArrayList<String> arrB = new ArrayList<String>();
        /*arrA.add("");
        arrA.add("");
        arrA.add("");
        */
        arrB.add("blend1");
        arrB.add("blend2");
        arrB.add("blend3");

        Set<String> a = new HashSet<>(arrA);
        Set<String> b = new HashSet<>(arrB);

        Set<String> result = new HashSet<>();
        for (String el: a) {
          if (!b.contains(el)) {
            result.add(el);
            break;
          }
        }/*
        for (String el: b) {
          if (!a.contains(el)) {
            result.add(el);
          }
        }*/
        System.out.println("Uncommon elements of set a and set b is : "
            + result);
        System.out.println("Result is null:"+result.isEmpty());
    }

    private void findUnCommon(ArrayList<String> arrA, ArrayList<String> arrB){

        Set<String> a = new HashSet<>(arrA);
        Set<String> b = new HashSet<>(arrB);

        Set<String> result = new HashSet<>();
        for (String el: a) {
          if (!b.contains(el)) {
            result.add(el);
          }
        }
        for (String el: b) {
          if (!a.contains(el)) {
            result.add(el);
          }
        }
        System.out.println("Uncommon elements of set a and set b is : "
            + result);
    }
    
    public static String[] differences(String[] first, String[] second) {
        String[] sortedFirst = Arrays.copyOf(first, first.length); // O(n)
        String[] sortedSecond = Arrays.copyOf(second, second.length); // O(m)
        Arrays.sort(sortedFirst); // O(n log n)
        Arrays.sort(sortedSecond); // O(m log m)

        int firstIndex = 0;
        int secondIndex = 0;

        LinkedList<String> diffs = new LinkedList<String>();  

        while (firstIndex < sortedFirst.length && secondIndex < sortedSecond.length) { // O(n + m)
            int compare = (int) Math.signum(sortedFirst[firstIndex].compareTo(sortedSecond[secondIndex]));

            switch(compare) {
            case -1:
                diffs.add(sortedFirst[firstIndex]);
                firstIndex++;
                break;
            case 1:
                diffs.add(sortedSecond[secondIndex]);
                secondIndex++;
                break;
            default:
                firstIndex++;
                secondIndex++;
            }
        }

        if(firstIndex < sortedFirst.length) {
            append(diffs, sortedFirst, firstIndex);
        } else if (secondIndex < sortedSecond.length) {
            append(diffs, sortedSecond, secondIndex);
        }

        String[] strDups = new String[diffs.size()];

        return diffs.toArray(strDups);
    }

    private static void append(LinkedList<String> diffs, String[] sortedArray, int index) {
        while(index < sortedArray.length) {
            diffs.add(sortedArray[index]);
            index++;
        }
    }


}
