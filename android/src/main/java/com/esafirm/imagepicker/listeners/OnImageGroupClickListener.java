package com.esafirm.imagepicker.listeners;

import android.view.View;

import com.esafirm.imagepicker.model.ImageGroup;

import java.util.List;

public interface OnImageGroupClickListener extends View.OnClickListener {
    void onClick(ImageGroup imageGroups, int position);
}
