package com.brucetoo.photopicker.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 相册信息实体类
 * 
 * @author Sam Sun
 * @version $Revision:1.0.0, $Date: 2014年11月6日 下午5:56:01
 */
public class Album implements Serializable {

	private static final long serialVersionUID = 4763830567780194562L;

	private String name; // 相册名称
	private ArrayList<Photo> mPhotoList; // 图片列表

	public Album() {

	}

	public Album(String name, ArrayList<Photo> photoList) {

		this.name = name;
		this.mPhotoList = photoList;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	public ArrayList<Photo> getPhotoList() {

		return mPhotoList;
	}

	public void setPhotoList(ArrayList<Photo> mPhotoList) {

		this.mPhotoList = mPhotoList;
	}

	/**
	 * 获取图片数量
	 * 
	 * @return
	 */
	public int getPhotoCount() {

		return (null == mPhotoList) ? 0 : mPhotoList.size();
	}

	/**
	 * 添加图片
	 * 
	 * @param photo
	 */
	public void addPhoto(Photo photo) {

		if (null == mPhotoList)
			mPhotoList = new ArrayList<Photo>();

		mPhotoList.add(photo);
	}

	/**
	 * 获取相册封面图片
	 * 
	 * @return
	 */
	public Photo getCoverPhoto() {

		return (null == mPhotoList) ? null : mPhotoList.get(0);
	}

	public String toString() {

		return name + "[" + getPhotoCount() + "]";
	}
}
