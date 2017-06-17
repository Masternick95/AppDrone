package lastrico.r.appdrone;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import io.vov.vitamio.LibsChecker;

import static lastrico.r.appdrone.Interface.ServerInterface.RECOGNITION;
import static lastrico.r.appdrone.Interface.ServerInterface.TRAINING;

public class Recognition extends AppCompatActivity {
    private Button btnTake, btnDownload, btnUpload, btnConnetti, btnDrone, btnConnectDrone;
    private ImageView img;
    private ProgressDialog progress;
    private Bitmap bitmap = null;
    private String bitmapName = null;
    private Calendar time;
    private byte[] imgByte = null;
    ContentValues values;
    Uri imageUri;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    WifiHandler wifiHandler;


    //BINDING WITH SocketService
    private boolean mIsBound;
    private SocketService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mBoundService = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    private void dispatchTakePictureIntent() {
        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //manipulateImage = new ManipulateImage(progress, img, bitmap, 1);
            //manipulateImage.execute("");
            //img.setImageBitmap(bitmap);
            time = Calendar.getInstance();
            bitmapName = "Picture_" + time.get(Calendar.HOUR)+time.get(Calendar.MINUTE)+time.get(Calendar.SECOND)
                    +"_"+time.get(Calendar.DAY_OF_MONTH)+"_"+(time.get(Calendar.MONTH)+1)+"_"+time.get(Calendar.YEAR);

            resizeBitmap(bitmap);
            img.buildDrawingCache();
            Bitmap bmp2=img.getDrawingCache();


            imgByte = getBytesFromBitmap(bitmap);

            img.destroyDrawingCache();
        }
    }

    private void resizeBitmap(Bitmap srcBmp){
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

        bitmap = Bitmap.createScaledBitmap(dstBmp, 360, 360, true);
        img.setImageBitmap(bitmap);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recognition);

        if (!LibsChecker.checkVitamioLibs(this))
            return;

        btnTake = (Button) findViewById(R.id.btnTake);
        btnDownload = (Button) findViewById(R.id.btnDownload);
        btnUpload = (Button) findViewById(R.id.upload);
        btnConnetti = (Button) findViewById(R.id.connect);
        btnConnectDrone = (Button) findViewById(R.id.connect_drone);
        btnDrone = (Button) findViewById(R.id.drone);
        img = (ImageView) findViewById(R.id.img1);
        progress = new ProgressDialog(this);

        btnDrone.setVisibility(View.INVISIBLE);
        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBound && mBoundService != null) {
                    if (imgByte != null) {
                        try {
                            SocketWorker socketWorker =
                                    new SocketWorker(Recognition.this,mBoundService,bitmapName,imgByte,progress,RECOGNITION);
                            socketWorker.execute();

                        } catch (Exception e) {
                            mBoundService.displayToast(e.getMessage());
                        }
                    }
                }
                else
                    Toast.makeText(Recognition.this, "Non sei connesso al server", Toast.LENGTH_SHORT).show();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBound && mBoundService != null) {
                    if (imgByte != null) {
                        try {
                            SocketWorker socketWorker =
                                    new SocketWorker(Recognition.this,mBoundService,bitmapName,imgByte,progress,TRAINING);
                            socketWorker.execute();
                        } catch (Exception e) {
                            mBoundService.displayToast(e.getMessage());
                        }
                    }
                }
                else
                    Toast.makeText(Recognition.this, "Non sei connesso al server", Toast.LENGTH_SHORT).show();
            }
        });

        btnConnetti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsBound){
                    mBoundService.displayToast("Sei gia' connesso al server");
                }
                else{
                    Intent intent = new Intent(Recognition.this, Connect.class);
                    startActivity(intent);

                }

            }
        });

        btnConnectDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiHandler = new WifiHandler(getApplicationContext());
                if(wifiHandler.getDroneConnected()) {
                    btnDrone.setVisibility(View.VISIBLE);
                    btnConnectDrone.setVisibility(View.INVISIBLE);
                    Toast.makeText(Recognition.this, "Connesso al drone", Toast.LENGTH_LONG);
                }else{
                    btnDrone.setVisibility(View.INVISIBLE);
                    Toast.makeText(Recognition.this, "Errore connessione drone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Recognition.this, DroneControlActivity.class);
                intent.putExtra("Activity", "Recognition");
                intent.putExtra("oldNetSSID", wifiHandler.getWifiSSID());
                startActivity(intent);
            }
        });



        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("commandMessage"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            if (message.equals("EXIT")){
                if (mIsBound) {
                    getApplicationContext().unbindService(mConnection);
                    mIsBound = false;
                }
                getApplicationContext().stopService(new Intent(Recognition.this,SocketService.class));
            }

            else if(message.equals("FAILED")){
                if (mIsBound) {
                    getApplicationContext().unbindService(mConnection);
                    mIsBound = false;
                }
            }

            else if(message.equals("SUCCESS")){
                Intent i = new Intent(Recognition.this, SocketService.class);
                mIsBound = getApplicationContext().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
            }

        }
    };

    //Function for saving state of activity (for example when going landscape)
    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("bitmap", bitmap);

    }

    //Function for reloading state of activity
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        bitmap = savedInstanceState.getParcelable("bitmap");
        img.setImageBitmap(bitmap);
        //myImageView = (ImageView) findViewById(R.id.etImage);
        //myImageView.setImageBitmap(myBitmap);
    }

    // convert from bitmap to byte array
    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

}
