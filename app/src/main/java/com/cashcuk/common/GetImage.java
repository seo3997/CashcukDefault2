package com.cashcuk.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.NinePatch;
import android.graphics.drawable.NinePatchDrawable;

/**
 * 이미지 load
 */
public class GetImage {
    private Context mContext;

    public  GetImage(Context context) {
        mContext = context;
    }
    /**
     * Ninepatch image 만듬.
     * @param bitmap
     * @return NinePatchDrawable
     */
    public NinePatchDrawable DrawableNinePatch(Bitmap bitmap){
        byte[] chunk = bitmap.getNinePatchChunk();

        if(chunk==null) return null;

        NinePatch npPatch = new NinePatch(bitmap , chunk, null);
        NinePatchDrawable npNineDra = new NinePatchDrawable(npPatch);

        return npNineDra;
    }
}
