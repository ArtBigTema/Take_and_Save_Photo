package com.assignment2.takeandsavephoto;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class TakeAndSavePhoto extends Activity {
	private Button btnTakePhoto;
	private SurfaceView photoView;
	private Bitmap bmp;
	private static final String FILE_PATH = Environment
			.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
			.getPath();
	private static String FILE_NAME = "/k.jpg";
	private Camera camera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_take_and_save_photo);
		btnTakePhoto = (Button) findViewById(R.id.button1);
		photoView = (SurfaceView) findViewById(R.id.surfaceView1);

		btnTakePhoto.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				onClickPicture(photoView);
			}
		});

		SurfaceHolder holder = photoView.getHolder();
		holder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					camera.setPreviewDisplay(holder);
					camera.startPreview();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera = Camera.open();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	public void onClickPicture(View view) {

		camera.takePicture(null, null, new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				try {
					bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), e.toString(),
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.take_and_save_photo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.save_file) {

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter the file name");
			alert.setMessage("The file name:");

			final EditText input = new EditText(this);
			alert.setView(input);
			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// onCanceled
						}
					});
			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							FILE_NAME = "/" + input.getText().toString()
									+ ".jpg";

							File fileMusic = new File(FILE_PATH + FILE_NAME);
							FileOutputStream outputMusicFileStream;
							try {
								outputMusicFileStream = new FileOutputStream(
										fileMusic, fileMusic.createNewFile());

								bmp.compress(Bitmap.CompressFormat.JPEG, 75,
										outputMusicFileStream);

								outputMusicFileStream.flush();
								outputMusicFileStream.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
			alert.show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
