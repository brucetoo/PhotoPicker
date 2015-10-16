package com.brucetoo.photopicker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brucetoo.photopicker.R;
import com.brucetoo.photopicker.adapter.PhotoBrowserAdapter;
import com.brucetoo.photopicker.model.Photo;

import java.util.ArrayList;

/**
 * 相册图片浏览器页面类
 */
public class AlbumPhotoBrowserActivity extends Activity implements View.OnClickListener {
    private RelativeLayout containerNav;
    private RelativeLayout containerToolbar;
    private TextView selectedTV; // 选中图片标签
    private ImageButton backBtn; // 返回按钮
    private TextView doneBtn; // 完成按钮
    private CheckBox selectedCBox; // 选中复选框
    private ViewPager photoViewPager; // 相册图片ViewPager

    private int curPos = 0; // 当前图片的位置
    private boolean isSingle;
    private int max;
    private String done;
    private ArrayList<Photo> photoList = new ArrayList<Photo>();
    private ArrayList<Photo> selectedList = new ArrayList<Photo>();
    private boolean isPreview = false; // 是否图片预览
    private PhotoBrowserAdapter mAdapter;// 相册图片网格适配器

    /**
     * 启动相册浏览器页面
     *
     * @param context      上下文
     * @param isSingle     是否为单选
     * @param isPreview    图片预览
     * @param max          最多能够选择多少张图片
     * @param pos          从第几个图片开始浏览
     * @param done         完成按钮标题
     * @param photoList    相册图片列表
     * @param selectedList 选中的图片列表
     */
    public static void startActivity(Activity context, boolean isSingle,
                                     boolean isPreview, int max, int pos, String done,
                                     ArrayList<Photo> photoList, ArrayList<Photo> selectedList) {
        Intent intent = new Intent(context, AlbumPhotoBrowserActivity.class);
        intent.putExtra(AlbumListIntentBuilder.KEY_IS_SINGLE, isSingle);
        intent.putExtra(AlbumListIntentBuilder.KEY_IS_PREVIEW, isPreview);
        intent.putExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS_MAX, max);
        intent.putExtra(AlbumListIntentBuilder.KEY_SELECTED_POSITION, pos);
        intent.putExtra(AlbumListIntentBuilder.KEY_DONE_BUTTON_TXT, done);
        intent.putExtra(AlbumListIntentBuilder.KEY_ALBUM_PHOTOS, photoList);
        intent.putExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS, selectedList);
        context.startActivityForResult(intent, AlbumActivity.ALBUM_PHOTO_BROWSER_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        setContentView(R.layout.activity_album_photo_picker_browser);
        initData();
        initView();
    }

    private void initData() {
        Intent intent = getIntent();
        isSingle = intent.getBooleanExtra(AlbumListIntentBuilder.KEY_IS_SINGLE, true);
        max = intent.getIntExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS_MAX, AlbumListIntentBuilder.DEFAULT_MAX_PHOTO);
        curPos = intent.getIntExtra(AlbumListIntentBuilder.KEY_SELECTED_POSITION, 0);
        done = intent.getStringExtra(AlbumListIntentBuilder.KEY_DONE_BUTTON_TXT);
        ArrayList<Photo> list = (ArrayList<Photo>) intent.getSerializableExtra(AlbumListIntentBuilder.KEY_ALBUM_PHOTOS);
        if (list != null) {
            photoList.addAll(list);
        }
        ArrayList<Photo> selected = (ArrayList<Photo>) intent.getSerializableExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS);
        if (selected != null) {
            selectedList.addAll(selected);
        }
        isPreview = intent.getBooleanExtra(AlbumListIntentBuilder.KEY_IS_PREVIEW, false);
    }

    private void initView() {
        containerNav = (RelativeLayout) findViewById(R.id.container_nav);
        containerToolbar = (RelativeLayout) findViewById(R.id.container_toolbar);
        selectedTV = (TextView) findViewById(R.id.tv_photo_picked);
        backBtn = (ImageButton) findViewById(R.id.btn_back);
        doneBtn = (TextView) findViewById(R.id.btn_done);
        selectedCBox = (CheckBox) findViewById(R.id.checkbox_photo_selected);
        photoViewPager = (ViewPager) findViewById(R.id.pager);
        doneBtn.setText(done);
        backBtn.setOnClickListener(this);
        doneBtn.setOnClickListener(this);
        selectedCBox.setOnClickListener(this);

        mAdapter = new PhotoBrowserAdapter(photoList,new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBarStatus();
            }
        });
        photoViewPager.setAdapter(mAdapter);
        photoViewPager.setCurrentItem(curPos);
        photoViewPager.setPageTransformer(true, new DepthPageTransformer());
        photoViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                containerNav.setVisibility(View.VISIBLE);
                containerToolbar.setVisibility(View.VISIBLE);
                curPos = arg0;
                refreshCheckBox();
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        refreshButtons();
        refreshCheckBox();
    }

    /**
     * 刷新选中状态
     */
    private void refreshCheckBox() {
        if (curPos >= 0 && curPos < photoList.size()) {
            Photo photo = photoList.get(curPos);
            boolean isChecked = isPhotoSelected(photo);
            selectedCBox.setChecked(isChecked);
        }
    }

    public void changeBarStatus() {
        if (containerNav.getVisibility() == View.GONE) {
            containerNav.setVisibility(View.VISIBLE);
        } else {
            containerNav.setVisibility(View.GONE);
        }
        if (containerToolbar.getVisibility() == View.GONE) {
            containerToolbar.setVisibility(View.VISIBLE);
        } else {
            containerToolbar.setVisibility(View.GONE);
        }
    }

    /**
     * 刷新按钮状态
     */
    private void refreshButtons() {
        int selectedNum = selectedList.size();
        boolean enable = (selectedNum > 0);
        if (isSingle) {
            selectedTV.setVisibility(View.GONE);
        } else {
//            String txt = getString(R.string.text_album_picker_done, selectedNum, max);
            selectedTV.setText(String.valueOf(selectedNum));
            selectedTV.setVisibility(View.VISIBLE);
        }
        doneBtn.setEnabled(enable);
    }

    /**
     * 是否图片已经选中
     *
     * @param photo
     * @return
     */
    private boolean isPhotoSelected(Photo photo) {
        if (null == photo || selectedList.size() == 0) {
            return false;
        }
        for (Photo tmpPhoto : selectedList) {
            if (photo.equals(tmpPhoto))
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                onBack(RESULT_CANCELED);
                break;
            case R.id.btn_done:
                onBack(RESULT_OK);
                break;
            case R.id.checkbox_photo_selected:
                onClickedPhotoCheckBox();
                break;
        }
    }

    private void onClickedPhotoCheckBox() {
        boolean isChecked = selectedCBox.isChecked();
        Photo photo = photoList.get(curPos);
        if (photo.isError()) {
            selectedCBox.setChecked(false);
            return;
        }
        if (!isSingle) { // 非单选，才验证上限
            if (isChecked && selectedList.size() == max) { // 选中的图片已经到达最多数量，则提示
                String tip = getResources().getString(R.string.tip_photo_num_beyond_max, max);
                Toast.makeText(AlbumPhotoBrowserActivity.this, tip, Toast.LENGTH_SHORT).show();
                selectedCBox.setChecked(false);
                return;
            }
        }
        if (isChecked) {
            selectedList.add(photo);
        } else {
            Photo tmp = null;
            for (Photo item : selectedList) {
                if (photo.equals(item)) {
                    tmp = item;
                    break;
                }
            }
            if (tmp != null) {
                selectedList.remove(tmp);
            }
        }
        refreshButtons();
    }

    private void onBack(int resultCode) {
        Intent intent = new Intent();
        intent.putExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTOS, selectedList);
        setResult(resultCode, intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            onBack(RESULT_CANCELED);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ViewPager带深度切换效果
     */
    public class DepthPageTransformer implements ViewPager.PageTransformer {

        private final float MIN_SCALE = 0.75f;

        @SuppressLint("NewApi")
        @Override
        public void transformPage(View view, float position) {

            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when
                // moving to the left page

                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.

                view.setAlpha(1 - position);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
                        * (1 - Math.abs(position));

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.

                view.setAlpha(0);
            }
        }
    }
}
