package com.brucetoo.photopicker.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.brucetoo.photopicker.R;
import com.brucetoo.photopicker.activity.AlbumActivity;
import com.brucetoo.photopicker.activity.AlbumPhotoBrowserActivity;
import com.brucetoo.photopicker.model.Photo;
import com.brucetoo.photopicker.model.ViewHolder;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 相册图片网格适配器类
 */
public class AlbumPhotoGridAdapter extends BaseAdapter {
    private int itemWidth;
    private boolean isSingle = true; // 是否单选
    private int max; // 最多选择的图片数
    private String done; // 完成按钮标题
    private Activity mContext; // 上下文
    private ArrayList<Photo> mPhotoList = new ArrayList<Photo>();// 相册图片列表
    private ArrayList<Photo> mSelectedList = new ArrayList<Photo>(); // 选中的图片列表
    private AlbumActivity.PhotoCallBack callBack;

    /**
     * 相册图片网格
     *
     * @param cxt
     * @param isSingle
     * @param max
     * @param done
     * @param selectedList
     */
    public AlbumPhotoGridAdapter(Activity cxt, boolean isSingle, int max,
                                 String done, ArrayList<Photo> selectedList, int itemWidth, AlbumActivity.PhotoCallBack callBack) {
        this.isSingle = isSingle;
        this.max = max;
        this.mContext = cxt;
        this.done = done;
        this.mSelectedList.addAll(selectedList);
        this.itemWidth = itemWidth;
        this.callBack = callBack;
    }

    /**
     * 设置选中图片列表
     *
     * @param selectedList
     */
    public void setSelectedPhotoList(ArrayList<Photo> selectedList) {
        mSelectedList.clear();
        mSelectedList.addAll(selectedList);
        notifyDataSetChanged();// 通知刷新gridview
    }

    /**
     * 设置新的相册图片数据
     *
     * @param photos
     */
    public void setPhotoList(ArrayList<Photo> photos) {
        mPhotoList.clear();
        mPhotoList.addAll(photos);
        notifyDataSetChanged();// 通知刷新gridview
    }

    @Override
    public int getCount() {
        return (null == mPhotoList) ? 0 : mPhotoList.size();
    }

    @Override
    public Photo getItem(int position) {
        return (position >= 0 && position < getCount()) ? mPhotoList
                .get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void onSelectedPhotoChanged(boolean selected, Photo photo) {
        if (selected) {
            mSelectedList.add(photo);
        } else {
            Photo tmp = null;
            for (Photo item : mSelectedList) {
                if (photo.equals(item)) {
                    tmp = item;
                    break;
                }
            }
            if (tmp != null) {
                mSelectedList.remove(tmp);
            }
        }
        notifyDataSetChanged();
        callBack.onSelectedPhotoChanged(mSelectedList);
    }

    /**
     * 是否图片已经被选中
     *
     * @param photo
     * @return
     */
    private boolean isPhotoSelected(Photo photo) {
        if(null == photo || mSelectedList.size()==0){
            return false;
        }
        for (Photo tmpPhoto : mSelectedList) {
            if (photo.equals(tmpPhoto))
                return true;
        }
        return false;
    }

    /**
     * 获取选中的图片数量
     *
     * @return
     */
    private int getSelectedPhotoCount() {
        return (null == mSelectedList) ? 0 : mSelectedList.size();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        ViewHolder holder = ViewHolder.newInstance(mContext, convertView, null, R.layout.list_item_album_photo_grid);
        if (null == convertView) {
            LayoutParams params = new LayoutParams(itemWidth, itemWidth);
            holder.getView().setLayoutParams(params);
        }
        final Photo photo = getItem(position);
        final boolean checked = isPhotoSelected(photo);
        boolean enabled = (isSingle && checked) ? false : true;
        CheckBox selectedCBox = holder.getView(R.id.checkbox_photo_selected);
        selectedCBox.setOnCheckedChangeListener(null);
        selectedCBox.setChecked(checked);
        selectedCBox.setEnabled(enabled);
        if (checked) {
            holder.getView(R.id.v_layer).setVisibility(View.VISIBLE);
        } else {
            holder.getView(R.id.v_layer).setVisibility(View.GONE);
        }

        selectedCBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (photo.isError()) {
                    buttonView.setChecked(false);
                    return;
                }
                if (!isSingle) { // 非单选，才验证上限
                    if (isChecked && getSelectedPhotoCount() == max) { // 选中的图片已经到达最多数量，则提示
                        String tip = mContext.getResources().getString(
                                R.string.tip_photo_num_beyond_max, max);
                        Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
                        buttonView.setChecked(false);
                        return;
                    }
                }
                onSelectedPhotoChanged(isChecked, photo);
            }
        });
        holder.getView(R.id.img_photo).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                AlbumPhotoBrowserActivity.startActivity(mContext, isSingle, false, max, position, done, mPhotoList, mSelectedList);
            }
        });

        Glide.with(mContext).load(photo.getPathWithPrefix()).thumbnail(0.2f).into((ImageView) holder.getView(R.id.img_photo));

        return holder.getView();
    }

}
