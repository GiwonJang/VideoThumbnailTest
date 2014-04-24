package com.hcsw.videothumbnailtest.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final int READ_REQUEST_CODE = 42;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	/**
	 * Fires an intent to spin up the “file chooser” UI and select an image.
	 */
	public void openVideo() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			Intent mediaChooser = new Intent(Intent.ACTION_GET_CONTENT);
			mediaChooser.setType("video/*");
			startActivityForResult(mediaChooser, READ_REQUEST_CODE);
		} else {
			// ACTION_OPEN_DOCUMENT is the intent to choose a file via the system’s file
			// browser.
			Intent mediaChooser = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			// Filter to only show results that can be “opened”, such as a
			// file (as opposed to a list of contacts or timezones)
			mediaChooser.addCategory(Intent.CATEGORY_OPENABLE);
			// Filter to show only images, using the image MIME data type.
			// If one wanted to search for ogg vorbis files, the type would be “audio/ogg”.
			// To search for all documents available via installed storage providers,
			// it would be “*/*”.
			mediaChooser.setType("video/*");
			startActivityForResult(mediaChooser, READ_REQUEST_CODE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
		// The ACTION_OPEN_DOCUMENT intent was sent with the request code
		// READ_REQUEST_CODE. If the request code seen here doesn’t match, it’s the
		// response to some other intent, and the code below shouldn’t run at all.
		if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			// The document selected by the user won’t be returned in the intent.
			// Instead, a URI to that document will be contained in the return intent
			// provided to this method as a parameter.
			// Pull that URI using resultData.getData().
			Uri uri = null;
			if (resultData != null) {
				uri = resultData.getData();
				Log.i("MainActivity", "Uri: " + uri.toString());
				Log.i("MainActivity", "FilePath: " + getRealPathFromURI(uri));
				showThumbnail(uri);
			}
		}
	}

	private void showThumbnail(Uri uri) {
		Bitmap thumbnail = CustomThumbnailUtils.createVideoThumbnail(getRealPathFromURI(uri), MediaStore.Video.Thumbnails.MINI_KIND, 5000 * 1000);

		ImageView iv_thumbnail = (ImageView) findViewById(R.id.iv_thumbnail);
		iv_thumbnail.setImageBitmap(thumbnail);
	}

	public String getRealPathFromURI(Uri contentUri) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			Cursor cursor = null;
			try {
				String[] proj = { MediaStore.Video.Media.DATA };
				cursor = getContentResolver().query(contentUri,  proj, null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
				cursor.moveToFirst();
				return cursor.getString(column_index);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		} else {
			// Will return "video:x*"
			String wholeID = DocumentsContract.getDocumentId(contentUri);
			// Split at colon, use second item in the array
			String id = wholeID.split(":")[1];
			String[] column = {MediaStore.Video.Media.DATA};
			// where id is equal to
			String sel = MediaStore.Video.Media._ID + "=?";

			Cursor cursor = null;
			try {
				String filePath = null;
				cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);
				int columnIndex = cursor.getColumnIndex(column[0]);
				if (cursor.moveToFirst()) {
					filePath = cursor.getString(columnIndex);
				}
				return filePath;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}


		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_open :
				openVideo();
				break;
		}
	}
}
