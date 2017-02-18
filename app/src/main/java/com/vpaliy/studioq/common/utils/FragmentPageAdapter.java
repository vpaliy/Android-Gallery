package com.vpaliy.studioq.common.utils;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public  class FragmentPageAdapter extends FragmentStatePagerAdapter {


    private List<Page<?>> mFragmentPageList=new ArrayList<>();
    private SparseArray<Fragment> registeredFragment=new SparseArray<>();

    public FragmentPageAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public interface FragmentInstanceProvider<T> {
        Fragment createInstance(ArrayList<T> mDataModel);
    }

    public static class Page<T> {
        protected FragmentInstanceProvider<T> provider;
        protected ArrayList<T> mDataModel;

        public Page(FragmentInstanceProvider<T> provider, ArrayList<T> mDataModel) {
            this.provider = provider;
            this.mDataModel = mDataModel;
        }

        public Fragment getInstance() {
            return provider.createInstance(mDataModel);
        }

        public void setDataModel(ArrayList<T> mDataModel) {
            this.mDataModel = mDataModel;
        }

    }

    @Override
    public int getCount() {
        return mFragmentPageList.size();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment=mFragmentPageList.get(position).getInstance();
        registeredFragment.put(position,fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        registeredFragment.remove(position);
    }

    //Builder approach, it's possible to add pages like this:
    // FragmentPagerAdapter adapter=new FragmentPagerAdapter(manager).addPage(page).addPage(page2).addPage(page3);
    public FragmentPageAdapter addPage(Page<?> adapterPageModel) {
        mFragmentPageList.add(adapterPageModel);
        return this;
    }

    public FragmentPageAdapter addAllPage(List<Page<?>> adapterPageList) {
        mFragmentPageList.addAll(adapterPageList);
        return this;
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragment.get(position);
    }

    public void removePage(int position) {
        mFragmentPageList.remove(position);

        notifyDataSetChanged();
    }
}
