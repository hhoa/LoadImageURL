package com.example.hhoa.loadimageurl;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Stack;

public class MainActivity extends AppCompatActivity
                            implements View.OnClickListener{

    private static final String TAG = "APP_PERMISSION";
    //widget
    EditText mEditText;
    ImageView img;
    private ProgressDialog mProgressDialog;
    //bitmap
    Bitmap myPicture;
    Bitmap scaledBitmap;
    Bitmap rotatedBitmap;
    float degree = 180;
    final int REQUEST_PERMISSION = 0;
    //use for back button
    Stack<Bitmap> tracer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myRequestPermissions();

        if (!checkInternetConnection())
        {
            NoInternetDialog noInternetDialog = new NoInternetDialog(MainActivity.this);
            noInternetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            noInternetDialog.show();
        }

        mEditText = findViewById(R.id.edt_url);
        img = findViewById(R.id.imgview_picture);
        tracer = new Stack<>();
        Button okButton = findViewById(R.id.btn_ok);
        okButton.setOnClickListener(this);
        img.setOnClickListener(this);
    }

    private void myRequestPermissions() {
        // Here, thisActivity is the current activity
        int checkRequest = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (checkRequest != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    private boolean checkInternetConnection() {
        Connectivity cn = new Connectivity();
        if (cn.isConnected(this))
        {
            if (cn.isConnectedMobile(this))
            {
                Toast.makeText(this, getString(R.string.mobile), Toast.LENGTH_SHORT).show();
            }
            else if (cn.isConnectedWifi(this))
                Toast.makeText(this, getString(R.string.wifi), Toast.LENGTH_SHORT).show();
            return true;
        }
        else
        {
            Toast.makeText(this, getString(R.string.err_connectivity), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_save:
                if (tracer.isEmpty())
                {
                    Toast.makeText(this, R.string.err_save, Toast.LENGTH_SHORT).show();
                    return false;
                }
                save();
                return true;
            case R.id.menu_back:
                if (tracer.size() > 0)
                {
                    degree -= 90;
                    if (degree < 0)
                        degree = 270;
                    img.setImageBitmap(tracer.pop());
                    return true;
                }
                else
                {
                    Toast.makeText(this, getString(R.string.err_back), Toast.LENGTH_SHORT).show();
                    return false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void save()
    {
        String appName = "MyAsyncTask";
        myPicture = tracer.peek();
        if (myPicture == null)
        {
            Toast.makeText(this, "Nothing to save. Please check again", Toast.LENGTH_LONG).show();
        }
        //create path
        String currDir = Environment.getExternalStorageDirectory().toString();
        File path = new File (currDir,appName);
        //if file does not exist
        if (!path.exists())
            path.mkdir();
        String  timeStamp =  new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).
                format(System.currentTimeMillis());
        String fileName = appName + "_" + timeStamp + ".jpg";

        // Assume block needs to be inside a Try/Catch block.
        OutputStream fOut = null;
        File file = new File(path, fileName); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        try
        {
            fOut = new FileOutputStream(file);
            myPicture.compress(Bitmap.CompressFormat.JPEG, 100, fOut); // saving the Bitmap to a file compressed as a JPEG with 100% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
            Toast.makeText(this, getString(R.string.save_image_successully, appName), Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Cannot save!!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_ok:
                clickOkButton();
                break;
            case R.id.imgview_picture:
                clickImage();
                break;
        }
    }

    private void clickImage() {
        Matrix matrix = new Matrix();
        degree += 90;
        if (degree == 360)
            degree = 0;
        matrix.postRotate(degree);
        scaledBitmap = Bitmap.createScaledBitmap
                (myPicture, myPicture.getWidth(),myPicture.getHeight(),true);

        rotatedBitmap = Bitmap.createBitmap
                (scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
        tracer.push(rotatedBitmap);
        img.setImageBitmap(rotatedBitmap);
    }

    private void clickOkButton() {
        String url = mEditText.getText().toString();
        if (url.equals(""))
        {
            Toast.makeText(this, getString(R.string.err_input), Toast.LENGTH_SHORT).show();
            return;
        }

        new DownloadImage().execute(url);
    }

    // DownloadImage AsyncTask
    @SuppressLint("StaticFieldLeak")
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            // Set progressdialog title
            mProgressDialog.setTitle("Downloading Image");
            // Set progressdialog message
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            myPicture = result;

            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            scaledBitmap = Bitmap.createScaledBitmap
                    (result, result.getWidth(), result.getHeight(), true);
            rotatedBitmap = Bitmap.createBitmap
                    (scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            tracer.push(rotatedBitmap);
            // Set the bitmap into ImageView
            img.setImageBitmap(rotatedBitmap);
            Toast.makeText(MainActivity.this, getString(R.string.touch_to_rotate), Toast.LENGTH_SHORT).show();
            // Close progressdialog
            mProgressDialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(MainActivity.this, getString(R.string.err_input), Toast.LENGTH_SHORT).show();
            mEditText.setText("");
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "Permission has now been granted");
            } else {
                Log.i(TAG, "Permission was NOT granted.");
                Toast.makeText(this, R.string.permission_save_image, Toast.LENGTH_SHORT).show();

            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
