package com.brucetoo.photopicker.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.brucetoo.photopicker.model.Photo;
import com.brucetoo.photopicker.model.PhotoView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 图片浏览器ViewPager适配器类
 */
public class PhotoBrowserAdapter extends PagerAdapter {

    private View.OnClickListener onClickListener;
    private int childCount = 0;
    private ArrayList<Photo> mList; // 图片列表

    public PhotoBrowserAdapter(ArrayList<Photo> list, View.OnClickListener onClickListener) {
        this.mList = list;
        this.onClickListener = onClickListener;
    }

    /**
     * 设置图片列表
     *
     * @param list
     */
    public void setData(ArrayList<Photo> list) {
        this.mList = list;
    }

    public ArrayList<Photo> getData() {
        return this.mList;
    }

    public void remove(int position) {
        this.mList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        childCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
        if (childCount > 0) {
            childCount--;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        String path = mList.get(position).getPathWithPrefix();
        PhotoView photoView = new PhotoView(container.getContext());
        photoView.touchEnable(true);
        photoView.setOnClickListener(onClickListener);
        Glide.with(container.getContext()).load(path).thumbnail(0.2f).into(photoView);
        container.addView(photoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return (null == mList) ? 0 : mList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return (arg0 == arg1);
    }

}