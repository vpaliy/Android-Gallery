package com.vpaliy.studioq.cases;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.support.annotation.NonNull;

public class SortCase<T> extends Case{


    private Comparator<T> comparator;
    private List<T> dataModel;

    private Callback<T> callback;
    private boolean reverse;

    private SortCase(List<T>  data) {
        this.dataModel=data;
    }

    private SortCase(Comparator<T> comparator, List<T> data ) {
        this.comparator=comparator;
        this.dataModel=data;
    }


    public SortCase<T> comparator(@NonNull Comparator<T> comparator) {
        this.comparator=comparator;
        return this;
    }

    public SortCase<T> reverse() {
        reverse=true;
        return this;
    }

    public SortCase<T> callback(@NonNull Callback<T> callback) {
        this.callback=callback;
        return this;
    }

    @Override
    public void execute() {
        Comparator<T> resultComparator=comparator;
        if(reverse) {
            resultComparator=new Comparator<T>() {
                @Override
                public int compare(T o1, T o2) {
                    int result=comparator.compare(o1,o2);
                    return result!=0?result*(-1):0;
                }
            };
        }

        Collections.sort(dataModel, resultComparator);

        //notify about the change here!
        if(callback!=null) {
            callback.onFinished(dataModel);
        }

        //handle

    }

    public static<T> SortCase<T> startWith(@NonNull List<T>  data) {
        return new SortCase<>(data);
    }

    public static<T> SortCase<T> startWith(Comparator<T> comparator, @NonNull List<T> data) {
        return new SortCase<>(comparator,data);
    }

    public interface Callback<T> {
        void onFinished(List<T> model);
    }
}
