package com.hcsw.videothumbnailtest.app;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

/**
 * Created by gwjang on 2014. 4. 24..
 */
public class CustomThumbnailUtils extends ThumbnailUtils {

	/**
	 * Constant used to indicate the dimension of micro thumbnail.
	 * @hide Only used by media framework and media provider internally.
	 */
	public static final int TARGET_SIZE_MICRO_THUMBNAIL = 96;

	/**
	 * Create a video thumbnail for a video. May return null if the video is
	 * corrupt or the format is not supported.
	 *
	 * @param filePath the path of video file
	 * @param kind could be MINI_KIND or MICRO_KIND
	 */
	public static Bitmap createVideoThumbnail(String filePath, int kind, long timeUs) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			bitmap = retriever.getFrameAtTime(timeUs);
		} catch (IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (RuntimeException ex) {
			// Assume this is a corrupt video file.
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
				// Ignore failures while cleaning up.
			}
		}

		if (bitmap == null) return null;

		if (kind == MediaStore.Images.Thumbnails.MINI_KIND) {
			// Scale down the bitmap if it's too large.
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int max = Math.max(width, height);
			if (max > 512) {
				float scale = 512f / max;
				int w = Math.round(scale * width);
				int h = Math.round(scale * height);
				bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
			}
		} else if (kind == MediaStore.Images.Thumbnails.MICRO_KIND) {
			bitmap = extractThumbnail(bitmap,
					TARGET_SIZE_MICRO_THUMBNAIL,
					TARGET_SIZE_MICRO_THUMBNAIL,
					OPTIONS_RECYCLE_INPUT);
		}
		return bitmap;
	}

}
