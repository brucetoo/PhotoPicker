package com.brucetoo.photopicker.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.brucetoo.photopicker.R;
import com.brucetoo.photopicker.adapter.AlbumPhotoGridAdapter;
import com.brucetoo.photopicker.model.Album;
import com.brucetoo.photopicker.model.Photo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * 图片选择
 */
public class AlbumActivity extends Activity implements View.OnClickListener{
    public static int ALBUM_PHOTO_BROWSER_REQUEST_CODE = 7894;
    private ImageView btnBack;
    private TextView tvTitle;
    private Button btnDone;
    private GridView gvPhoto;
    private AlbumPhotoGridAdapter mAdapter;// 相册图片网格适配器
    private PhotoCallBack callBack;

    private boolean isSingle;
    private int max;
    private String done;
    private Album album;
    private List<Album> albums = new ArrayList<Album>();
    private ArrayList<Photo> selectedList = new ArrayList<Photo>();
    private boolean isForResult = true;

    public interface PhotoCallBack {
        public void onSelectedPhotoChanged(ArrayList<Photo> selected);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        initView();
        initData();
        callBack = new PhotoCallBack() {

            @Override
            public void onSelectedPhotoChanged(ArrayList<Photo> selected) {
                selectedList.clear();
                selectedList.addAll(selected);
                btnDone.setText(done + "(" + selectedList.size() + "/" + max + ")");
                if (selectedList.size() > 0) {
                    btnDone.setEnabled(true);
                } else {
                    btnDone.setEnabled(false);
                }
            }
        };
    }

    private void initView() {
        btnBack = (ImageView) findViewById(R.id.btn_back);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        btnDone = (Button) findViewById(R.id.btn_done);
        gvPhoto = (GridView) findViewById(R.id.gridview_photo);
        btnBack.setOnClickListener(this);
        btnDone.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_done:
                onPickPhotoCompleted();
                break;
        }
    }

    private void initData() {
        Intent intent = getIntent();
        isSingle = intent.getBooleanExtra(AlbumListIntentBuilder.KEY_IS_SINGLE, true);
        isForResult = intent.getBooleanExtra(AlbumListIntentBuilder.KEY_IS_FORRESULT, true);
        max = intent.getIntExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS_MAX, AlbumListIntentBuilder.DEFAULT_MAX_PHOTO);
        done = intent.getStringExtra(AlbumListIntentBuilder.KEY_DONE_BUTTON_TXT);
        album = (Album) intent.getSerializableExtra(AlbumListIntentBuilder.KEY_ALBUM);
        ArrayList<Photo> list = (ArrayList<Photo>) intent.getSerializableExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS);
        if (list != null) {
            selectedList.addAll(list);
        }
        btnDone.setText(done + "(" + selectedList.size() + "/" + max + ")");
        if (selectedList.size() > 0) {
            btnDone.setEnabled(true);
        } else {
            btnDone.setEnabled(false);
        }
        new AlbumsLoaderTask().execute();
    }

    private void show(ArrayList<Album> list) {
        albums.clear();
        albums.addAll(list);
        setAlbum(albums.get(0));
    }

    private void setAlbum(final Album album) {
        this.album = album;
        tvTitle.setText(album.getName());
        if (mAdapter == null) {
            gvPhoto.post(new Runnable() {
                @Override
                public void run() {
                    int itemWidth = (int) ((gvPhoto.getWidth() - (gvPhoto.getNumColumns() - 1) * getResources().getDisplayMetrics().density*6) / gvPhoto.getNumColumns());
                    mAdapter = new AlbumPhotoGridAdapter(AlbumActivity.this, isSingle, max, done, selectedList, itemWidth, callBack);
                    mAdapter.setPhotoList(album.getPhotoList());
                    gvPhoto.setAdapter(mAdapter);
//                    gvPhoto.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
                }
            });
        } else {
            mAdapter.setPhotoList(album.getPhotoList());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, R.anim.slide_out_to_bottom_short);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ArrayList<Photo> selected = (ArrayList<Photo>) data.getSerializableExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS);
        callBack.onSelectedPhotoChanged(selected);
        mAdapter.setSelectedPhotoList(selected);
        if (resultCode == RESULT_OK) {
            onPickPhotoCompleted();
        }
    }

    /**
     * 完成照片选择并返回
     */
    private void onPickPhotoCompleted() {
        Intent data = new Intent();
        if (isSingle) { // 单选
            data.putExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTO, selectedList.get(0));
        } else { // 多选
            data.putExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS, selectedList);
        }
        if (isForResult) {
            setResult(Activity.RESULT_OK, data);
            finish();
        } else {
            Toast.makeText(this,"selectedList:"+selectedList.size(),Toast.LENGTH_SHORT).show();
//            //跳转到发布页面
//            Intent intent = new Intent(this, ReleaseActivity.class);
//            intent.putExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS, selectedList);
//            startActivity(intent);
//            overridePendingTransition(R.anim.slide_in_from_bottom_short, 0);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    finish();
//                }
//            },500);
        }
    }

    /**
     * 获取文件所在文件夹名称
     *
     * @param path
     *            文件路径
     * @return 文件夹名称
     */
    public static String getDir(String path) {

        if (TextUtils.isEmpty(path))
            return "";

        String subString = path.substring(0, path.lastIndexOf('/'));

        return subString.substring(subString.lastIndexOf('/') + 1, subString.length());
    }

    public class AlbumsLoaderTask extends AsyncTask<Void, Void, ArrayList<Album>> {

        @SuppressWarnings("unchecked")
        @Override
        protected ArrayList<Album> doInBackground(Void... params) {
            HashMap<String, Album> map = new HashMap<String, Album>();
            ContentResolver resolver = getContentResolver();
            String[] projection = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE};
            Cursor c = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            //所有图片
            Album all = new Album();
            all.setName("所有图片");
            if (null != c) {
                int imgCount; // 图片文件数量
                long size; // 图片文件大小
                String path; // 图片文件路径
                String dir; // 图片文件路径
                Album album; // 相册
                Photo photo; // 图片
                c.moveToFirst();
                imgCount = c.getCount();
                for (int i = 0; i < imgCount; i++) {
                    path = c.getString(c.getColumnIndex(MediaStore.Video.Media.DATA));
                    size = c.getLong(c.getColumnIndex(MediaStore.Images.Media.SIZE));
                    dir = getDir(path); // 获取路径中文件的目录
                    photo = new Photo(dir, path, size);
                    all.addPhoto(photo);
                    if (map.containsKey(dir)) { // 图片文件存在于已加载的相册中
                        album = map.get(dir);
                        album.addPhoto(photo);
                    } else {
                        album = new Album();
                        album.setName(dir);
                        album.addPhoto(photo);
                        map.put(dir, album);
                    }
                    c.moveToNext();
                }
                c.close();
            }
            ArrayList<Album> list = new ArrayList<Album>();
            list.add(all);
            list.addAll(map2List(map));
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<Album> list) {
            show(list);
        }

        /**
         * 相册map转成相册列表
         *
         * @param map
         * @return
         */
        private ArrayList<Album> map2List(HashMap<String, Album> map) {
            ArrayList<Album> albums = new ArrayList<Album>();
            if (null != map) {
                String key;
                Iterator<String> iterator = map.keySet().iterator();
                while (iterator.hasNext()) {
                    key = iterator.next();
                    albums.add(map.get(key));
                }
            }
            return albums;
        }
    }
}
