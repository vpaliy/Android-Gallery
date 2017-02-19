package com.vpaliy.studioq.adapters;

import java.util.ArrayList;
import java.util.List;

public interface MediaAdapter<T> {
    List<T> getAllChecked();
    List<T> getData();
    void removeAt(int index);
    void setData(ArrayList<T> data);
    int[] getAllCheckedForDeletion();
    boolean isMultiModeActivated();
    boolean isEmpty();
}
