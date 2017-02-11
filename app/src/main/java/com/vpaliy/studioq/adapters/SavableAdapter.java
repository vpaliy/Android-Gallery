package com.vpaliy.studioq.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;


public interface SavableAdapter {

    void saveState(@NonNull Bundle outState);
    void restoreState(@NonNull Bundle savedInstanceState);
}
