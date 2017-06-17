package lastrico.r.appdrone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rikyg on 09/05/2017.
 */

public class ImageSaver {
    public static String saveToInternalStorage(Bitmap bitmapImage, String name){
        String directory="/sdcard/download";
        File mypath=new File(directory, name+".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory;
    }

    public static Bitmap LoadImageFromStorage(String name)
    {
        Bitmap cane=null;
        try {
            File f=new File("/sdcard/download", name+".png");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            cane=b;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return cane;
    }
}
