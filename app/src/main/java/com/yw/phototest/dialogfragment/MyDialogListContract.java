package com.yw.phototest.dialogfragment;

import android.net.Uri;

import java.util.List;

/**
 * Created by yw on 2017-08-09.
 */

public interface MyDialogListContract {
    void returnUri(List<Uri> uriList, String tag);
}
