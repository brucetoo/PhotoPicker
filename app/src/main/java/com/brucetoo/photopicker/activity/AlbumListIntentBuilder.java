package com.brucetoo.photopicker.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.brucetoo.photopicker.model.Photo;

import java.util.ArrayList;

/**
 * 相册列表页面Intent构造器类
 */
public class AlbumListIntentBuilder {

	/** 默认最多选中的图片数 */
	public static final int DEFAULT_MAX_PHOTO = 5;

	/** 是否单选模式相册访问键 */
	public static final String KEY_IS_SINGLE = "is_single";
	/** 确认按钮文本访问键 */
	public static final String KEY_DONE_BUTTON_TXT = "done_button_txt";
	/** 选中图片访问键 */
	public static final String KEY_SELECTED_PHOTOS = "selected_photos";
	/** 选中或取消选中单个的图片访问键 */
	public static final String KEY_SELECTED_PHOTO = "selected_photo";
	/** 最多选择多少个图片访问键 */
	public static final String KEY_SELECTED_PHOTOS_MAX = "selected_photos_max";
	/** 是否预览访问键 */
	public static final String KEY_IS_PREVIEW = "is_preview";
	/** 当前选中图片的位置访问键 */
	public static final String KEY_SELECTED_POSITION = "position";

	/** 相册访问键 */
	public static final String KEY_ALBUM = "album";
	/** 相册图片列表访问键 */
	public static final String KEY_ALBUM_PHOTOS = "photos";
	/** 图片选择状态标识访问键 */
	public static final String KEY_PHOTO_SELECTED_FLAG = "flag";
	/** 是否需要返回 */
	public static final String KEY_IS_FORRESULT = "isForResult";

	private boolean isSingle = true; // 是否为单选
	private boolean isPreview = false; // （多选模式）是否预览模式
	private int max = DEFAULT_MAX_PHOTO; // （多选模式）最多选择图片数量
	private int position = 0; // 当前图片的序号（isPreview为true的时候生效）
	private String done = null; // 完成按钮文本
	private Photo mSelectedPhoto; // （单选模式）选中的图片
	private ArrayList<Photo> mSelectedList; // （多选模式）选中的图片列表
	private boolean isForResult = true; // 是否需要返回（默认需要）

	/**
	 * 相册列表页面Intent构造器
	 *
	 * @param isSingle
	 *            是否单选模式
	 */
	public AlbumListIntentBuilder(boolean isSingle) {

		this.isSingle = isSingle;
	}

	/**
	 * 相册列表页面Intent构造器
	 *
	 * @param isSingle
	 *            是否单选模式
	 * @param doneTxt
	 *            完成按钮标题
	 */
	public AlbumListIntentBuilder(boolean isSingle, String doneTxt) {

		this.isSingle = isSingle;
		this.done = doneTxt;
	}

	/**
	 * （多选模式）设置是否编辑模式
	 *
	 * @param isPreview
	 *            是否为编辑模式
	 * @param pos
	 *            当前图片的position（isEidt为true时有效）
	 * @return
	 */
	public AlbumListIntentBuilder setIsEdit(boolean isPreview, int pos) {

		this.isPreview = isPreview;
		this.position = pos;

		return this;
	}

	/**
	 * （多选模式）设置最多选择图片数量
	 *
	 * @param max
	 * @return
	 */
	public AlbumListIntentBuilder setMax(int max) {

		this.max = max;

		return this;
	}

	/**
	 * （单选模式）设置选中的图片
	 *
	 * @param selectedPhoto
	 * @return
	 */
	public AlbumListIntentBuilder setSelectedPhoto(Photo selectedPhoto) {

		this.mSelectedPhoto = selectedPhoto;

		return this;
	}

	/**
	 * （多选模式）设置选中的图片列表
	 *
	 * @param selectedList
	 * @return
	 */
	public AlbumListIntentBuilder setSelectedList(ArrayList<Photo> selectedList) {
		this.mSelectedList = selectedList;
		return this;
	}

	public void setIsForResult(boolean isForResult) {
		this.isForResult = isForResult;
	}

	/**
	 * 获取启动相册Intent对象
	 *
	 * @param cxt
	 * @return
	 */
	public Intent getIntent(Context cxt) {

		Intent intent = new Intent(cxt, AlbumActivity.class);
		intent.putExtra(KEY_IS_SINGLE, isSingle);
		intent.putExtra(KEY_IS_FORRESULT, isForResult);
		if (!TextUtils.isEmpty(done)) {
			intent.putExtra(KEY_DONE_BUTTON_TXT, done);
		} else {
			intent.putExtra(KEY_DONE_BUTTON_TXT,
					"完成");
		}
		if (isSingle) {
			intent.putExtra(KEY_SELECTED_PHOTOS_MAX, 1);
			if (null != mSelectedPhoto)
				intent.putExtra(KEY_SELECTED_PHOTO, mSelectedPhoto);
		} else {
			intent.putExtra(KEY_IS_PREVIEW, isPreview);
			intent.putExtra(KEY_SELECTED_PHOTOS_MAX, max);
			if (isPreview)
				intent.putExtra(KEY_SELECTED_POSITION, position);
			if (null != mSelectedList)
				intent.putExtra(KEY_SELECTED_PHOTOS, mSelectedList);
		}
		return intent;
	}
}
