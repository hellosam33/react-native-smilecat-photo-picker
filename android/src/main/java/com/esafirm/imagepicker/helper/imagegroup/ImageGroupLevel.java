package com.esafirm.imagepicker.helper.imagegroup;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Locale;

public enum ImageGroupLevel {
    UNKNOWN(0, null, 2),
    DAY(1, new SimpleDateFormat("M월 d일 E요일", Locale.KOREA), 3),
    WEEK(2, new SimpleDateFormat("M월 W주", Locale.KOREA), 4),
    MONTH(3, new SimpleDateFormat("yyyy년 M월", Locale.KOREA), 5);

    private final int level;
    private final SimpleDateFormat dateFormat;
    private final int columnSize;

    ImageGroupLevel(int level, SimpleDateFormat dateFormat, int columnSize) {
        this.level = level;
        this.dateFormat = dateFormat;
        this.columnSize = columnSize;
    }

    public int getLevel() {
        return level;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public @Nullable static ImageGroupLevel valueOf(int level) {
        for (ImageGroupLevel value : ImageGroupLevel.values()) {
            if (value.getLevel() == level) {
                return value;
            }
        }
        return null;
    }
}
