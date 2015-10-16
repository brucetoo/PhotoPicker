package com.brucetoo.photopicker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.brucetoo.photopicker.R;
import com.brucetoo.photopicker.model.Album;
import com.brucetoo.photopicker.model.Photo;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * 相册列表适配器类
 * 
 * @author Sam Sun
 * @version $Revision:1.0.0, $Date: 2014年11月6日 下午7:01:09
 */
public class AlbumListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<Album> mAlbumList = new ArrayList<Album>();

	public AlbumListAdapter(Context cxt) {

		this.mInflater = (LayoutInflater) cxt.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * 设置新的相册数据
	 * 
	 * @param albums
	 */
	public void setAlbumsList(List<Album> albums) {

		mAlbumList.clear();
		mAlbumList.addAll(albums);

		notifyDataSetChanged();// 通知刷新listView
	}

	/**
	 * 添加相册数据
	 * 
	 * @param albums
	 */
	public void addAlbums(List<Album> albums) {

		mAlbumList.addAll(albums);

		notifyDataSetChanged();
	}

	/**
	 * 添加相册数据
	 * 
	 * @param album
	 */
	public void addAlbum(Album album) {

		mAlbumList.add(album);

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {

		return (null == mAlbumList) ? 0 : mAlbumList.size();
	}

	@Override
	public Album getItem(int position) {

		return (position >= 0 && position < getCount()) ? mAlbumList.get(position) : null;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View view, ViewGroup viewGroup) {

		ViewHolder holder;
		Album album = getItem(position);
		Photo coverPhoto = album.getCoverPhoto();

		if (null == view) {

			view = mInflater.inflate(R.layout.list_item_album_list, null);
			holder = new ViewHolder(view);
		} else {

			holder = (ViewHolder) view.getTag();
		}

		holder.nameTV.setText(album.getName());
		holder.numTV.setText(holder.numTV.getContext().getString(R.string.text_total_number, String.valueOf(album.getPhotoCount())));


        Glide.with(view.getContext()).load(coverPhoto.getPathWithPrefix()).placeholder(R.drawable.default_image).into(holder.coverImgV);


		return view;
	}

	/**
	 * 列表Item复用类
	 */
	public class ViewHolder {

		public TextView nameTV;
		public TextView numTV;
		public ImageView coverImgV;

		public ViewHolder(View v) {

			nameTV = (TextView) v.findViewById(R.id.tv_album_name);
			numTV = (TextView) v.findViewById(R.id.tv_album_num);
			coverImgV = (ImageView) v.findViewById(R.id.img_album_cover);

			v.setTag(this);
		}
	}


}
