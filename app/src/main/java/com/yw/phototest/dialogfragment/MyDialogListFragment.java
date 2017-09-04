package com.yw.phototest.dialogfragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.foamtrace.photopicker.PhotoPickerActivity;
import com.foamtrace.photopicker.SelectModel;
import com.foamtrace.photopicker.intent.PhotoPickerIntent;
import com.yalantis.ucrop.UCrop;
import com.yw.phototest.recycler.MytextHomeAdapter;
import com.yw.phototest.R;
import com.yw.phototest.recycler.RecyclerViewItemDecoration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by yw on 2017-07-13.
 */

public class MyDialogListFragment extends DialogFragment {
    private final static int RESULT_LOAD_IMAGE = 999;
    private final static int REQUEST_CODE_CAPTURE_CAMEIA = 888;

    private View view;
    private RecyclerView recyclerView;
    private MytextHomeAdapter myHomeAdapter;
    private List<String> stringList;
    private ArrayList<String> imagePaths;
    private List<Uri> uriList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dialog_list, container, false);
        setCancelable(false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        super.onCreateView(inflater,container,savedInstanceState);
        initView();
        return view;
    }

    public void initView() {
        recyclerView = (RecyclerView)view.findViewById(R.id.dialog_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        uriList = new ArrayList<>();
        imagePaths = new ArrayList<>();
        stringList = new ArrayList<>();
        stringList.add("拍照");
        stringList.add("从相册中选择");
        stringList.add("取消");
        myHomeAdapter = new MytextHomeAdapter(getActivity(),stringList);
        recyclerView.addItemDecoration(
                new RecyclerViewItemDecoration(RecyclerViewItemDecoration.MODE_HORIZONTAL, Color.BLUE,1,2,1));
        recyclerView.setAdapter(myHomeAdapter);

        myHomeAdapter.setOnItemClikListener(new MytextHomeAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED
                            || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请WRITE_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        Toast.makeText(getContext(),"请授予读取照片的权限",Toast.LENGTH_SHORT).show();
                        dismiss();
                        ((MyDialogListContract) getActivity()).returnUri(uriList,"0");
                        return;
                    }
                }else {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请WRITE_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        Toast.makeText(getContext(),"请授予读取照片的权限",Toast.LENGTH_SHORT).show();
                        dismiss();
                        ((MyDialogListContract) getActivity()).returnUri(uriList,"0");
                        return;
                    }
                }
                switch (position){
                    case 0:
                        getImageFromCamera();
                        break;
                    case 1:
                        getImageBySys();
                        break;
                    case 2:
                        dismiss();
                        ((MyDialogListContract) getActivity()).returnUri(uriList,"0");
                        break;
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    /**
     * 拍照并获取照片
     */
    protected void getImageFromCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent getImageByCamera = new Intent("android.media.action.IMAGE_CAPTURE");
//            getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoPath);//存入指定路径
            startActivityForResult(getImageByCamera, REQUEST_CODE_CAPTURE_CAMEIA);
        }
        else {
            Toast.makeText(getContext(),"请确认已经插入SD卡",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择照片
     * 根据传入的tag来确定选择的照片数量
     * 1-9为实际数量,10至20为单选,大于20的根据个位数来决定选择数量
     * 选择后会返回传入的tag
     */
    public void getImageBySys() {
        int type = Integer.parseInt(getTag());
        if (type > 1 && type <= 9) {
            /**
             *多选
             */
            PhotoPickerIntent intent = new PhotoPickerIntent(getActivity());
            intent.setSelectModel(SelectModel.MULTI);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            intent.setMaxTotal(type); // 最多选择照片数量，默认为9
            intent.setSelectedPaths(imagePaths); // 已选中的照片地址， 用于回显选中状态
            startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } else if (type > 10 && type < 20) {
            /**
             *单选
             */
            PhotoPickerIntent intent = new PhotoPickerIntent(getActivity());
            intent.setSelectModel(SelectModel.SINGLE);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } else {
            type = type%10;
            /**
             *多选
             */
            PhotoPickerIntent intent = new PhotoPickerIntent(getActivity());
            intent.setSelectModel(SelectModel.MULTI);
            intent.setShowCarema(false); // 是否显示拍照， 默认false
            intent.setMaxTotal(type); // 最多选择照片数量，默认为9
            intent.setSelectedPaths(imagePaths); // 已选中的照片地址， 用于回显选中状态
            startActivityForResult(intent, RESULT_LOAD_IMAGE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("----requestCode----",requestCode+";;"+ UCrop.REQUEST_CROP+";;"+ UCrop.RESULT_ERROR);
        if (resultCode == RESULT_OK ) {
            if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA || requestCode == RESULT_LOAD_IMAGE) {
                //拍照

                if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {

                    Uri uri = data.getData();
                    if (uri == null) {
                        Bundle bundle = data.getExtras();
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        String path = Environment.getExternalStorageDirectory().getPath() + "/gz/";
                        try {
                            File dirFile = new File(path);
                            if (!dirFile.exists()) {
                                dirFile.mkdir();
                            }
                            File myCaptureFile = new File(path +new Date().getTime()+ "photo.jpeg");
                            if (!myCaptureFile.exists()){
                                myCaptureFile.createNewFile();
                            }
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                            bos.flush();
                            bos.close();
                            uri = Uri.fromFile(myCaptureFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("---DFragmentTakePho---", uri.toString());
                    uriList.add(uri);
                    ((MyDialogListContract) getActivity()).returnUri(uriList,getTag());

                } else
                    //从相册筛选
                    if (requestCode == RESULT_LOAD_IMAGE) {
                        if (data != null) {
                            imagePaths = data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT);
                            for (String uri : imagePaths) {
                                uriList.add(Uri.fromFile(new File(uri)));
                            }
                            Log.i("---DFragmentChoose---", imagePaths.toString());
                            ((MyDialogListContract) getActivity()).returnUri(uriList,getTag());
                        }else{
                            Log.e("---dialogChoose---","dataNULL");
                        }
                    }
                dismiss();//选择好照片就更关闭dialog
            }

        }
    }


}
