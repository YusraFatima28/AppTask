package com.example.capturevideo.async;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import wseemann.media.FFmpegMediaMetadataRetriever;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.capturevideo.Frame;
import com.example.capturevideo.OnFrameClickListener;
import com.example.capturevideo.R;
import com.example.capturevideo.utils.ImageUtils;
import com.example.capturevideo.utils.SharedPrefsUtils;
import com.example.capturevideo.secondmain;
import org.jetbrains.annotations.NotNull;
import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.startActivityForResult;
import static java.security.AccessController.getContext;
public class FramesExtractorTask extends AsyncTask<String, Float, ArrayList<Frame>> {


	private final int delta_time; //in microsecs

	private final WeakReference<LinearLayout> framesViewReference;
	private final WeakReference<OnFrameClickListener> listenerReference;
	private ProgressDialog progressDlg;
	File fileUri;

	/*Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
	fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

	startActivityForResult(intent,
						   CAMERA_CAPTURE_VIDEO_REQUEST_CODE);


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Uri selectedImageUri = data.getData();
		imagePath = getPath(selectedImageUri);

	}*/

	/*public String getPath(Uri uri) {
		String[] projection = { MediaStore.Video.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null) {

			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}
*/

	public FramesExtractorTask(@NotNull LinearLayout framesView, OnFrameClickListener listener) {
		this.delta_time = SharedPrefsUtils.getFramesFrequency(framesView.getContext())*1000000; //in microsecs
		System.out.println(delta_time);
		framesViewReference = new WeakReference<LinearLayout>(framesView);
		listenerReference = new WeakReference<OnFrameClickListener>(listener);
		createDialog(framesView.getContext());
	}
	@Override
	protected void onPreExecute() {
		showProgress();
	}
	@Override
	protected ArrayList<Frame> doInBackground(String @NotNull ... params) {
		File sdcard = Environment.getExternalStorageDirectory();
		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);


		//secondmain.getPath(this,secondm);
		FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
		mmr.setDataSource(params[0]);
		String s_duration = mmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
		int duration = getVideoDuration(s_duration);
		ArrayList<Frame> frames = new ArrayList<Frame>();
		for (int i=0; i<=duration; i+=delta_time) {
			Bitmap frame_orig = mmr.getFrameAtTime(i, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
			if (frame_orig == null) {
				setProgress(i, duration);
				continue;
			}
			Frame frame = new Frame();
			frame.setBm(ImageUtils.getScaledBitmap(frame_orig));
			frame.setTime(i);
			frames.add(frame);
			setProgress(i, duration);
		}
		return frames;
	}

	private int getVideoDuration(String s_duration) {
		int duration = 0;
		try {
			duration = Integer.parseInt(s_duration); //in millisecs
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		duration *= 1000; //in microsecs
		return duration;
	}

	@Override
	protected void onPostExecute(ArrayList<Frame> result) {
		if (isCancelled() && result != null) {
			for (int i=0, len=result.size(); i<len; i++) {
				result.get(i).getBm().recycle();
			}
			result.clear();
			result = null;
		}
		if (result == null || result.size() <= 0) {
			hideProgress();
			return;
		}

		OnFrameClickListener listener = null;
		if (listenerReference != null) {
			listener = listenerReference.get();
		}

		if (framesViewReference != null) {
			LinearLayout framesView = framesViewReference.get();
			for (int i=0, len=result.size(); i<len; i++) {
				framesView.addView(ImageUtils.createFrameImage(framesView.getContext(),
						result.get(i).getBm(),
						result.get(i).getTime(),
						listener));
			}
		}

		hideProgress();
	}
	private void setProgress(int cur, int duration) {
		publishProgress((float)(cur + delta_time) / duration * 100);
	}
	@Override
	protected void onProgressUpdate(Float... values) {
		if (progressDlg != null) {
			progressDlg.setProgress(values[0].intValue());
		}
	}

	private Dialog createDialog(Context ctx) {
		progressDlg = new ProgressDialog(ctx);
		progressDlg.setMessage("Fetching video frames..");
		progressDlg.setMax(100);
		progressDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDlg.setCancelable(false);
		progressDlg.getWindow().setGravity(Gravity.CENTER);
		return progressDlg;
	}
	private void showProgress() {
		if (progressDlg != null) {
			progressDlg.show();
		}
	}
	private void hideProgress() {
		if (progressDlg != null && progressDlg.isShowing()) {
			progressDlg.dismiss();
		}
	}

}