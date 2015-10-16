package com.brucetoo.photopicker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.brucetoo.photopicker.activity.AlbumListIntentBuilder;
import com.brucetoo.photopicker.model.Photo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp";
    // 头像或icon文件地址
    public static final String AVATAR = "avatar";
    public static final String CACHE_AVATAR = DIRECTORY + "/" + AVATAR;
    private static final String TEMP_PHOTO_FILE_NAME = "temp_photo.png"; // 临时头像名称
    private static final short REQUEST_CODE_PICK_PHOTO = 0x1; // 选择相册图片请求码
    private static final short REQUEST_CODE_TAKE_PICTURE = 0x2;// 拍照请求码
    private static final short REQUEST_CODE_CROP_PICTURE = 0x3; // 裁剪图片请求码

    protected static final short MSG_UPDATE_AVATOR_SUCCEED = 0x20;// 更新个人头像成功消息
    protected static final short MSG_UPDATE_AVATOR_FAILURE = 0x21;// 更新个人头像失败消息
    protected static final short MSG_UPLOAD_PHOTO_SUCCESS = 0x22; // 上传图片成功消息
    protected static final short MSG_UPLOAD_PHOTO_FAILURE = 0x23; // 上传图片失败消息
    protected static final short MSG_START_CROP_PICTURE = 0x24; // 开始裁剪图片消息
    protected static final short MSG_CROP_PICTURE_DONE = 0x25; // 裁剪图片完成消息
    protected static final short MSG_REFRESH_PICTURE = 0x26; // 刷新头像消息

    private PopupWindow popupWindow;
    private File mTmpFile = null; // 临时的图片文件

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
//                case MSG_CROP_PICTURE_DONE: // 图片裁剪完成
//                    uploadAvatorImg(); // 上传头像并保存
//                    break;
//                case MSG_UPLOAD_PHOTO_SUCCESS: // 上传用户头像
//                    onUpdateAvator();
//                    break;
//                case MSG_UPLOAD_PHOTO_FAILURE: // 上传头像失败
//                    uploadAvatorFailure();
//                    break;
//                case MSG_UPDATE_AVATOR_SUCCEED:// 更新用户头像信息成功
//                    updateAvatorSuccess();
//                    break;
//                case MSG_UPDATE_AVATOR_FAILURE:// 更新用户头像信息失败
//                    updateAvatorFailure();
//                    break;
//                case MSG_REFRESH_PICTURE: // 刷新头像
//                    onRefreshAvator((String) msg.obj, msg.arg1);
//                    break;
                default:
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prepareTmpFile();//准备临时文件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        findViewById(R.id.btn_choose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_select_photo, null);
                view.findViewById(R.id.btn_take_photo).setOnClickListener(takePhotoListener);
                view.findViewById(R.id.btn_pick_photo).setOnClickListener(choosePhotoListener);
                popupWindow = createPopWindow(MainActivity.this,view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
            }
        });

    }



    private View.OnClickListener takePhotoListener = new  View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(popupWindow.isShowing()){
                popupWindow.dismiss();
            }
            takePhoto();
        }
    };

    private View.OnClickListener choosePhotoListener = new  View.OnClickListener(){

        @Override
        public void onClick(View v) {
            if(popupWindow.isShowing()){
                popupWindow.dismiss();
            }
            pickPhoto();
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            switch (requestCode) {
                case REQUEST_CODE_PICK_PHOTO:
                    Photo photo = (Photo) data.getSerializableExtra(AlbumListIntentBuilder.KEY_SELECTED_PHOTO);
                    if (null != photo) {
                        copyImage2TmpFile(photo.getUri());// 复制选中的图片到临时文件
                    }
                    break;
                case REQUEST_CODE_TAKE_PICTURE:
                    mHandler.sendEmptyMessage(MSG_START_CROP_PICTURE);
                    break;
                case REQUEST_CODE_CROP_PICTURE:
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_CROP_PICTURE_DONE, BitmapFactory.decodeFile(mTmpFile.getPath())));
                    break;
            }
        }
    }

    /**
     * 选择图片
     */
    private void pickPhoto() {
        AlbumListIntentBuilder builder = new AlbumListIntentBuilder(false);
        builder.setMax(9);
        builder.setIsForResult(false);
        Intent intent = builder.getIntent(this);
        startActivityForResult(intent, REQUEST_CODE_PICK_PHOTO);
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri mImageCaptureUri = Uri.fromFile(mTmpFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //删除临时文件
        if (null != mTmpFile && mTmpFile.exists()) {
            mTmpFile.delete();
            mTmpFile = null;
        }
    }


    /**
     *创建临时文件
     */
    private void prepareTmpFile() {
        String state = Environment.getExternalStorageState();
        String dirPath;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            dirPath = CACHE_AVATAR;
        } else {
            dirPath = getFilesDir().getPath();
        }
        mTmpFile = new File(dirPath + File.separator + TEMP_PHOTO_FILE_NAME);
        if (!mTmpFile.exists()) { // 头像临时文件不存在，则创建它
            mTmpFile = createFile(dirPath, TEMP_PHOTO_FILE_NAME);
        }
    }

    /**
     * 拷贝图片到临时文件
     *
     * @param uri
     */
    private void copyImage2TmpFile(final Uri uri) {

        if (null == uri)
            return;

        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (null != mTmpFile) {

                    String path = mTmpFile.getPath();

                    path = path.substring(0, path.indexOf(TEMP_PHOTO_FILE_NAME));

                    uriToFile(MainActivity.this, path, TEMP_PHOTO_FILE_NAME, uri);

                    mHandler.sendEmptyMessage(MSG_START_CROP_PICTURE);
                }
            }
        });
    }

    /**
     * 创建popwindow
     * @param activity
     * @param contentView
     * @param width
     * @param height
     * @param gravity
     * @return
     */
    public PopupWindow createPopWindow(final Activity activity,View contentView,int width, int height,int gravity) {
        //背景半透明
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        layoutParams.alpha = 0.5f;
        activity.getWindow().setAttributes(layoutParams);
        PopupWindow popupWindow = new PopupWindow(activity);
        popupWindow.setWidth(width);
        popupWindow.setHeight(height);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(R.style.Popupwindow);
        popupWindow.setContentView(contentView);
        ColorDrawable colorDrawable = new ColorDrawable(-00000);
        popupWindow.setBackgroundDrawable(colorDrawable);
        //隐藏的时候背景恢复
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = 1f;
                activity.getWindow().setAttributes(lp);
            }
        });
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), gravity, 0, 0);
        return popupWindow;
    }

    /**
     * Uri转成File
     *
     * @param context
     * @param dirPath
     * @return
     */
    public static File uriToFile(Context context, String dirPath, String fileName, Uri uri) {

        File file = createFile(dirPath, fileName);
        byte[] buffer = new byte[1024];
        int len;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
            output = new FileOutputStream(file);
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
        } finally {
            try {
                if (null != output) {
                    output.close();
                    output = null;
                }

                if (null != input) {
                    input.close();
                    input = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                file = null;
            }
        }

        return file;
    }


    /**
     * 创建文件
     *
     * @param dirPath
     * @param fileName
     * @return
     */
    public static File createFile(String dirPath, String fileName) {

        File dir = new File(dirPath);
        File file = new File(dirPath + "/" + fileName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (file.exists()) {
            return file;
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }
}
