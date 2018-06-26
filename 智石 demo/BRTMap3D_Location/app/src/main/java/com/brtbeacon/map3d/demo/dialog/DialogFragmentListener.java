package com.brtbeacon.map3d.demo.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public interface DialogFragmentListener {
    void onDialogCancel(DialogFragment fragment);
    void onDialogDismiss(DialogFragment fragment);
    void onDialogResult(DialogFragment fragment, Bundle result);
}
