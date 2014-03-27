package com.assignment2.takeandsavephoto;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.Toast;

public class TakeAndSavePhoto extends Activity {
	private SurfaceView surfacePreviewPhoto = null;
	private SurfaceHolder surfacePreviewHolder = null;
	private Camera photoCamera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	byte[] photoData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_take_and_save_photo);
		surfacePreviewPhoto = (SurfaceView) findViewById(R.id.surfaceView1);
		surfacePreviewHolder = surfacePreviewPhoto.getHolder();
		surfacePreviewHolder.addCallback(surfaceCallback);
		surfacePreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (photoCamera == null) {
			photoCamera = Camera.open();
		}
		inPreview = true;
		startPreview();
	}

	private void startPreview() {
		if (cameraConfigured && photoCamera != null) {
			photoCamera.startPreview();
			inPreview = true;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (inPreview) {
			photoCamera.stopPreview();
		}
		photoCamera.release();
		photoCamera = null;
		inPreview = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.take_and_save_photo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.item_take_photo) {
			if (inPreview) {
				photoCamera.takePicture(null, null, photoCallback);
				inPreview = false;
			}
		}
		if (item.getItemId() == R.id.item_save_photo) {
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

							File fileMusic = new File(Environment
									.getExternalStoragePublicDirectory(
											Environment.DIRECTORY_PICTURES)
									.getPath()
									+ "/" + input.getText().toString() + ".jpg");
							FileOutputStream outputMusicFileStream;
							try {
								outputMusicFileStream = new FileOutputStream(
										fileMusic, fileMusic.createNewFile());
								outputMusicFileStream.write(photoData);
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

	private Camera.Size getBestPreviewSize(int width, int height,
			Camera.Parameters parameters) {
		Camera.Size result = null;

		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width <= width && size.height <= height) {
				if (result == null) {
					result = size;
				} else {
					int resultArea = result.width * result.height;
					int newArea = size.width * size.height;

					if (newArea > resultArea) {
						result = size;
					}
				}
			}
		}

		return (result);
	}

	private void initPreview(int width, int height) {
		if (photoCamera != null && surfacePreviewHolder.getSurface() != null) {
			try {
				photoCamera.setPreviewDisplay(surfacePreviewHolder);
			} catch (Throwable t) {
				Toast.makeText(TakeAndSavePhoto.this, t.getMessage(),
						Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters = photoCamera.getParameters();
				Camera.Size size = getBestPreviewSize(width, height, parameters);

				if (size != null) {
					parameters.setPreviewSize(size.width, size.height);

					parameters.setPictureFormat(ImageFormat.JPEG);
					photoCamera.setParameters(parameters);
					cameraConfigured = true;
				}
			}
		}
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {
			// wait until surfaceChanged()
		}

		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
			final double B_SIZE = 2;// 1.2
			double targetRatio = (double) w / h;
			if (sizes == null)
				return null;

			Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;

			int targetHeight = h;

			// Try to find an size match aspect ratio and size
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > B_SIZE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}

			// Cannot find the one match the aspect ratio, ignore the
			// requirement
			if (optimalSize == null) {
				minDiff = Double.MAX_VALUE;
				for (Size size : sizes) {
					if (Math.abs(size.height - targetHeight) < minDiff) {
						optimalSize = size;
						minDiff = Math.abs(size.height - targetHeight);
					}
				}
			}
			return optimalSize;
		}

		void setCameraDisplayOrientation() {
			// get degree of Rotation
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			int degrees = 0;
			switch (rotation) {
			case Surface.ROTATION_0:
				degrees = 0;
				break;
			case Surface.ROTATION_90:
				degrees = 90;
				break;
			case Surface.ROTATION_180:
				degrees = 180;
				break;
			case Surface.ROTATION_270:
				degrees = 270;
				break;
			}
			int result = 0;
			// get camera info cameraId
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(0, info); // 0 is back camera
			result = ((360 - degrees) + info.orientation);

			result %= 360;
			photoCamera.setDisplayOrientation(result);
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			if (inPreview) {
			photoCamera.stopPreview();	
			}
			setCameraDisplayOrientation();
			try {
				photoCamera.setPreviewDisplay(holder);
				Toast.makeText(TakeAndSavePhoto.this,
						"Surface size is " + width + "w " + height + "h",
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(TakeAndSavePhoto.this, e.toString(),
						Toast.LENGTH_LONG).show();
			}
			Camera.Parameters parameters = photoCamera.getParameters();
			List<Size> sizes = parameters.getSupportedPreviewSizes();

			Size optimalSize = getOptimalPreviewSize(sizes, width, height);
			parameters.setPreviewSize(optimalSize.width, optimalSize.height);
			photoCamera.setParameters(parameters);
			// camera.startPreview();//delete
			initPreview(width, height);
			if (inPreview) {
				startPreview();
				}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {

		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			new SavePhotoTask().execute(data);
			camera.stopPreview();
		}
	};

	class SavePhotoTask extends AsyncTask<byte[], String, String> {
		@Override
		protected String doInBackground(byte[]... data) {
			photoData = null;
			photoData = Arrays.copyOf(data[0], data[0].length);
			return (null);
		}
	}
}