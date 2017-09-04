package com.yw.phototest;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;
import com.yw.phototest.dialogfragment.MyDialogListContract;
import com.yw.phototest.dialogfragment.MyDialogListFragment;

import java.io.File;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class CutActivity extends AppCompatActivity implements MyDialogListContract{
    private PhotoView img;
    private Button bt_choose;
    private static Uri uri = Uri.fromFile(
            new File(Environment.getExternalStorageDirectory().getPath()+"/data/"+"photo.jpg"));//剪裁后存储的地方

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cut);
        initView();
    }

    /**
     * 初始化布局
     */
    public void initView(){
        img = (PhotoView) findViewById(R.id.img);
        bt_choose = (Button) findViewById(R.id.bt_choose);
        bt_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyDialogListFragment().show(getSupportFragmentManager(),"1");
            }
        });
    }

    /**
     * 选择照片后的返回
     * @param uriList 选择的照片的uri
     * @param tag 标记
     */
    @Override
    public void returnUri(List<Uri> uriList, String tag) {
        if (uriList.size()>0) {
            setCrop(uriList.get(0));
        }
    }

    /**
     * 剪裁图片
     * @param photoUri
     */
    public void setCrop(Uri photoUri){
        UCrop uCrop = UCrop.of(photoUri, uri);
        uCrop.withAspectRatio(1,1);//裁剪比例
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(Color.parseColor("#008CEE"));//状态栏颜色
        options.setStatusBarColor(Color.parseColor("#008CEE"));//通知栏颜色
        //开始设置
        //一共三个参数，分别对应裁剪功能页面的“缩放”，“旋转”，“裁剪”界面，对应的传入NONE，就表示关闭了其手势操作，比如这里我关闭了缩放和旋转界面的手势，只留了裁剪页面的手势操作
        options.setAllowedGestures(UCropActivity.NONE, UCropActivity.ALL, UCropActivity.ALL);
        //设置是否展示矩形裁剪框
        options.setShowCropFrame(true);
        //结束设置
        uCrop.withOptions(options);
        uCrop.start(this);
    }

    /**
     * 剪裁后会返回触发这里
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri;
            try {
                resultUri = UCrop.getOutput(data);//获取剪裁的结果,在剪裁时点击返回时会再此触发Null...
            }catch (NullPointerException e){
                return;
            }
            Glide.with(this).load(resultUri).into(img);
        }else
        if (resultCode == UCrop.RESULT_ERROR) {//剪裁出错
            Log.e("---cropERR---", data.toString());
        }
    }
}
