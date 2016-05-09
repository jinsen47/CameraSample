/**
 * Copyright (c) 2015 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Represents a picture taken by the camera, to be passed through
 * the ImageProcessor chain.
 *
 * The ImageContext should always hold the byte[] representing
 * the JPEG image. If an ImageProcessor needs a Bitmap, it can
 * call getBitmap(true) to force creation of a Bitmap for those
 * JPEG bytes, but this is memory-intensive and should be avoided
 * where possible.
 */
public class ImageContext {
  private static final double LOG_2=Math.log(2.0d);
  private Context ctxt;
  private byte[] jpeg;
  private Bitmap bmp;
  private Bitmap thumbnail;

  ImageContext(Context ctxt, byte[] jpeg) {
    this.ctxt=ctxt.getApplicationContext();
    setJpeg(jpeg);
  }

  /**
   * @return an Android Context suitable for use in cases where
   * you need filesystem paths and the like
   */
  public Context getContext() {
    return(ctxt);
  }

  /**
   * @return the byte[] of JPEG-encoded data for the picture
   */
  public byte[] getJpeg() {
    return(jpeg);
  }

  /**
   * Updates the JPEG data, invalidating any previous Bitmap.
   *
   * @param jpeg the new JPEG data
   */
  public void setJpeg(byte[] jpeg) {
    this.jpeg=jpeg;
    this.bmp=null;
    this.thumbnail=null;
  }

  /**
   * Retrieve a Bitmap rendition of the picture. Try to avoid
   * this where possible, as it is memory-intensive.
   *
   * @param force true if you want to force creation of a Bitmap
   *              if there is none, false if you want the Bitmap
   *              but can live without it if it is unavailable
   * @return the Bitmap rendition of the picture
   */
  public Bitmap getBitmap(boolean force) {
    if (bmp==null && force) {
      updateBitmap();
    }

    return(bmp);
  }

  public Bitmap buildPreviewThumbnail(Context ctxt, Float quality) {
    // TODO: move this into PictureTransaction work somewhere, so done
    // on a background thread

    if (thumbnail==null) {
      int limit=2000000;

      if (quality!=null && quality>0.0f && quality<1.0f) {
        ActivityManager am=(ActivityManager)ctxt.getSystemService(Context.ACTIVITY_SERVICE);
        int flags=ctxt.getApplicationInfo().flags;
        int memoryClass=am.getMemoryClass();

        if ((flags & ApplicationInfo.FLAG_LARGE_HEAP)!=0) {
          memoryClass=am.getLargeMemoryClass();
        }

        limit=(int)(1024*1024*memoryClass*quality);
      }

      thumbnail=createBitmap(null, limit);
    }

    return(thumbnail);
  }

  public Bitmap buildResultThumbnail() {
    // TODO: move this onto background thread

    return(createBitmap(null, 750000));
  }

  private Bitmap createBitmap(Bitmap inBitmap, int limit) {
    double ratio=(double)jpeg.length * 10.0d / (double)limit;
    int inSampleSize;

    if (ratio > 1.0d) {
      inSampleSize=1 << (int)(Math.ceil(Math.log(ratio) / LOG_2));
    } else {
      inSampleSize=1;
    }

    return(createBitmap(inSampleSize, inBitmap, limit));
  }

  private Bitmap createBitmap(int inSampleSize, Bitmap inBitmap,
                              int limit) {
    BitmapFactory.Options opts=new BitmapFactory.Options();

    opts.inSampleSize=inSampleSize;
    opts.inBitmap=inBitmap;

    Bitmap result;

    try {
      result=
        BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length, opts);

      if (limit>0 && result.getByteCount()>limit) {
        return(createBitmap(inSampleSize+1, inBitmap,
          limit));
      }
    }
    catch (OutOfMemoryError e) {
      return(createBitmap(inSampleSize+1, inBitmap,
        limit));
    }

    return(result);
  }

  private void updateBitmap() {
    bmp=createBitmap(1, bmp, -1); // no limit other than OOM
  }
}
