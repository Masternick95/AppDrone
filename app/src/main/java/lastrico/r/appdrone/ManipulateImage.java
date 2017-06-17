package lastrico.r.appdrone;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Created by Gruppo 4 on 27/04/2017.
 */

public class ManipulateImage extends AsyncTask<String, Void, Bitmap> {
    private ImageView img;
    private ProgressDialog progress;
    private Bitmap bm;
    private int manipulationSelected;
    private int threshold;

    public ManipulateImage(ProgressDialog progress, ImageView img, Bitmap bm, int manipulationSelected){
        this.progress = progress;
        this.img = img;
        this.bm = bm;
        this.manipulationSelected = manipulationSelected;
    }

    //constructor with threshold
    public ManipulateImage(ProgressDialog progress, ImageView img, Bitmap bm, int manipulationSelected, int threshold){
        this.progress = progress;
        this.img = img;
        this.bm = bm;
        this.manipulationSelected = manipulationSelected;
        this.threshold = threshold;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progress.setIndeterminate(true);
        progress.setTitle("Please wait");
        progress.setMessage("Manipulating image...");
        progress.show();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        //select manipulation
        switch (manipulationSelected) {
            case 1:
                return convertBlackWhite(bm);
            case 2:
                return convertBinary(bm, threshold);
            default:
                return bm;

        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        progress.dismiss();
        img.setImageBitmap(bitmap);
    }

    private Bitmap convertBlackWhite(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, bitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = bitmap.getPixel(x, y); // get pixel color
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }
        return bmOut;
    }

    private Bitmap convertBinary(Bitmap bitmap, int threshold){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap bmOut = Bitmap.createBitmap(width, height, bitmap.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                pixel = bitmap.getPixel(x, y); // get pixel color
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                // use threshold, above -> white, below -> black
                if (gray > threshold) {
                    gray = 255;
                }
                else{
                    gray = 0;
                }
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }
        return bmOut;
    }

    public static Bitmap resizeBitmap(Bitmap srcBmp){
        Bitmap dstBmp;

        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        return dstBmp;
    }
}
