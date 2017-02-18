package com.vpaliy.studioq.common.eventBus;

import com.zomato.photofilters.imageprocessors.Filter;


public class ChangeFilter {

    public final Filter filter;

    public ChangeFilter(Filter filter) {
        this.filter=filter;
    }

}
