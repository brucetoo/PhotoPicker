package com.brucetoo.photopicker.model;

import android.net.Uri;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * 相册图片信息实体类
 * 
 * @author Sam Sun
 * @version $Revision:1.0.0, $Date: 2014年11月7日 下午3:22:31
 */
public class Photo implements Serializable {

	private static final long serialVersionUID = 7697294162622575877L;

	private long size; // 图片尺寸
	private String album; // 图片相册名称
	private String path;// 图片路径
	private boolean error;// 是否是错误图片

	public Photo(String album, String path, long size) {

		this.album = album;
		this.path = path;
		this.size = size;
	}

	public long getSize() {

		return size;
	}

	public void setSize(long size) {

		this.size = size;
	}

	public String getAlbum() {

		return album;
	}

	public void setAlbum(String album) {

		this.album = album;
	}

	public String getPath() {

		return path;
	}

	public void setPath(String path) {

		this.path = path;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	/**
	 * 获取“file://xxxx”格式路径
	 * 
	 * @return
	 */
	public String getPathWithPrefix() {

		if (TextUtils.isEmpty(path))
			return path;

		return "file://" + path;
	}

	/**
	 * 获取图片Uri
	 * 
	 * @return
	 */
	public Uri getUri() {

		String uriStr = getPathWithPrefix();

		if (TextUtils.isEmpty(uriStr))
			return null;

		return Uri.parse(uriStr);
	}

	/**
	 * 判断两个图片是否一样
	 * 
	 * @param photo
	 * @return
	 */
	public boolean equals(Photo photo) {
		if (null == photo)
			return false;
		return (null != photo.path && photo.path.equals(path));
	}

	public String toString() {

		return album + "==>" + path + " (size:" + size + ")";
	}
}
