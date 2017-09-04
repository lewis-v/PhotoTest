package com.yw.phototest;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yw.phototest.dialogfragment.MyDialogListContract;
import com.yw.phototest.dialogfragment.MyDialogListFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import id.zelory.compressor.Compressor;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import uk.co.senab.photoview.PhotoView;

public class CompressActivity extends AppCompatActivity implements View.OnClickListener,MyDialogListContract{
    private PhotoView img,img_luban,img_compress;
    private TextView tv,tv_luban,tv_compress;
    private Button bt_choose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);
        initView();
    }

    /**
     * 初始化布局
     */
    public void initView(){
        img = (PhotoView)findViewById(R.id.img);
        img_compress = (PhotoView)findViewById(R.id.img_compress);
        img_luban = (PhotoView)findViewById(R.id.img_luban);

        tv_compress = (TextView)findViewById(R.id.tv_compress);
        tv_luban = (TextView)findViewById(R.id.tv_luban);
        tv = (TextView)findViewById(R.id.tv);

        bt_choose = (Button)findViewById(R.id.bt_choose);
        bt_choose.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_choose:
                new MyDialogListFragment().show(getSupportFragmentManager(),"1");//选择照片1张
                break;
        }
    }

    /**
     * 选择照片后的返回
     * @param uriList 选择的照片的uri
     * @param tag 标记
     */
    @Override
    public void returnUri(List<Uri> uriList, String tag) {
        if (uriList.size() == 1){//正常情况下返回1个选中的照片uri
            File file = new File(UriToFileUtil.getPath(this,uriList.get(0)));

            //加载原图
            Glide.with(this).load(uriList.get(0)).into(img);//使用glide框架加载图片到imageview
            Log.i("---length---",(int)getFileSize(file)+"kb");
            tv.setText((int)getFileSize(file)+"kb");
            //加载compress压缩后的图片
            Compressor
                    .getDefault(this)//使用默认的设置
                    .compressToFileAsObservable(file)//需要压缩的文件
                    .subscribeOn(Schedulers.io())//压缩时在非主线程中执行
                    .observeOn(AndroidSchedulers.mainThread())//执行完成后的异步在主线程中执行
                    .subscribe(new Action1<File>() {//压缩成功是调用
                        @Override
                        public void call(File file) {
                            Glide.with(CompressActivity.this).load(file).into(img_compress);
                            Log.i("---Compresslength---",getFileSize(file)+"kb");
                            tv_compress.setText((int)getFileSize(file)+"kb");
                        }
                    }, new Action1<Throwable>() {//压缩时出错调用
                        @Override
                        public void call(Throwable throwable) {
                            Log.e("---Compressor---",throwable.getMessage());
                        }
                    });
            //加载luban压缩后的图片
            Luban.get(this).load(file)//设置要压缩的文件
                    .putGear(Luban.THIRD_GEAR)//压缩的档次
                    .setCompressListener(new OnCompressListener() {//压缩回调
                        @Override
                        public void onStart() {//压缩开始

                        }

                        @Override
                        public void onSuccess(File file) {//压缩成功
                            Glide.with(CompressActivity.this).load(file).into(img_luban);
                            Log.i("---Lubanlength---",getFileSize(file)+"kb");
                            tv_luban.setText((int)getFileSize(file)+"kb");
                        }

                        @Override
                        public void onError(Throwable e) {//压缩失败
                            Log.e("---Luban---",e.getMessage());
                        }
                    }).launch();//开始压缩
        }
    }

    /**
     * 获取文件大小(KB)
     * @param file
     * @return
     */
    public double getFileSize(File file){
        double size = 0.0;
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (fileInputStream != null) {
                size = fileInputStream.available();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size/1024;
    }
}
