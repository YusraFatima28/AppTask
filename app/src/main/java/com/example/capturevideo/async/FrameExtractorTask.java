package com.example.capturevideo.async;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.view.Gravity;
import android.widget.ImageView;
import com.example.capturevideo.R;
import com.example.capturevideo.R;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import wseemann.media.FFmpegMediaMetadataRetriever;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class FrameExtractorTask extends AsyncTask<String, Float, Bitmap> {

	private final int time;
	private final WeakReference<ImageView> frameViewReference;
	private ProgressDialog progressDlg;
	
	public FrameExtractorTask(ImageView frameView, int time) {
		this.time = time;
		frameViewReference = new WeakReference<ImageView>(frameView);
		createDialog(frameView.getContext());
	}
	
	@Override
    protected void onPreExecute() {
		showProgress();
    }
	
	@Override
	protected Bitmap doInBackground(String @NotNull ... params) {
		FFmpegMediaMetadataRetriever mmr = new FFmpegMediaMetadataRetriever();
	    mmr.setDataSource(params[0]);

	    return mmr.getFrameAtTime(time, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
	}
	@Override
	protected void onPostExecute(Bitmap result) {
		if (frameViewReference != null) {
			ImageView frameView = frameViewReference.get();
			if (result != null) {
				frameView.setImageBitmap(result);
			}
		}
		hideProgress();
	}
	private Dialog createDialog(Context ctx) {
		progressDlg = new ProgressDialog(ctx);
		progressDlg.setMessage(ctx.getString(R.string.fetchFrame));
		progressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
