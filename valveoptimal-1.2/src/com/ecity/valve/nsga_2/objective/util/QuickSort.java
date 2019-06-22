package com.ecity.valve.nsga_2.objective.util;

import com.ecity.valve.nsga_2.datastruct.Individual;

public class QuickSort {
    public static void sort(Individual[] array){
        recQuickSort(0,array.length-1,array);
    }
    private static int partition(int left,int right,Individual pivot,Individual[] array){
        int leftPtr=left;
        int rightPtr=right-1;
        while (true){
            while (array[++leftPtr].cost<pivot.cost);
            while (array[--rightPtr].cost>pivot.cost);
            if (leftPtr>=rightPtr) break;
            else swap(leftPtr,rightPtr,array);
        }
        swap(leftPtr,right-1,array);
        return leftPtr;
    }
    private static void swap(int a,int b,Individual[] array){
        Individual temp=array[a];
        array[a]=array[b];
        array[b]=temp;
    }
    private static void recQuickSort(int left,int right,Individual[] array){
        int size=right-left+1;
        if (size<=3){
            manualSort(left,right,array);
        }else {
            Individual median=medianOf3(left,right,array);
            int partition= partition(left,right,median,array);
            recQuickSort(left,partition-1,array);
            recQuickSort(partition+1,right,array);
        }
    }
    private static Individual medianOf3(int left,int right,Individual[] array){
        int center=(left+right)/2;
        if (array[left].cost>array[center].cost){
            swap(left,center,array);
        }
        if (array[left].cost>array[right].cost){
            swap(left,right,array);
        }
        if (array[center].cost>array[right].cost)
            swap(center,right,array);
        swap(center,right-1,array);
        return array[right-1];
    }
    private static void manualSort(int left,int right,Individual array[]){
        int size=right-left+1;
        if (size<=1){
            return;
        }
        if (size==2){
            if (array[left].cost>array[right].cost)
                swap(left,right,array);
        }
        else {
            if (array[left].cost>array[right-1].cost){
                swap(left,right-1,array);
            }
            if (array[left].cost>array[right].cost){
                swap(left,right,array);
            }
            if (array[right-1].cost>array[right].cost){
                swap(right-1,right,array);
            }
        }
    }
}
