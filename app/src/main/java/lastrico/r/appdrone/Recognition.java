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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import static lastrico.r.appdrone.Interface.ServerInterface.RECOGNITION;
import static lastrico.r.appdrone.Interface.ServerInterface.TRAINING;

/**
 * Created by Gianluca on 18/06/2017.
 */

public class Recognition extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar toolbar=null;
    ImageView myImageView = null;
    private Calendar time;
    private String bitmapName = null;
    TextView labelTextView;
    Button bttClickMe = null;
    Button bttClickMe2 = null;
    Button bttClickMe4 = null;
    Button connect = null;
    Bitmap myBitmap = null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ProgressDialog progress;
    SeekBar mySeekBar;
    ManipulateImage _myCV;
    Bitmap actualPhoto=null;
    Uri imageUri=null;
    ContentValues values;
    private byte[] imgByte = null;
    ManipulateImage manipulateImage;
    //int manipulationSelected = 1;

    Button bttConnectDrone;
    Button bttDrone;

    WifiHandler wifiHandler;

    //Function to launch camera application
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Take Photo
        FloatingActionButton btTakePhoto = (FloatingActionButton) findViewById(R.id.btTakePhoto);
        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();

            }
        });

        myImageView = (ImageView) findViewById(R.id.etImage);
        bttClickMe2 = (Button) findViewById(R.id.upload);
        connect = (Button) findViewById(R.id.connectServer);
        progress = new ProgressDialog(this);
        bttConnectDrone = (Button) findViewById(R.id.connect_drone);
        bttDrone = (Button) findViewById(R.id.drone);

        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.btLoadImage);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myBitmap=ImageSaver.LoadImageFromStorage("recognition");
                resizeBitmap(myBitmap);
                myImageView.buildDrawingCache();
                Bitmap bmp2=myImageView.getDrawingCache();
                imgByte = getBytesFromBitmap(myBitmap);
                bitmapName = "recognition";
                myImageView.destroyDrawingCache();

            }
        });

        bttClickMe2.setOnClickListener(new View.OnClickListener() {
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

        connect.setOnClickListener(new View.OnClickListener() {
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

        bttConnectDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBound) {
                    getApplicationContext().unbindService(mConnection);
                    mIsBound = false;
                }
                getApplicationContext().stopService(new Intent(Recognition.this,SocketService.class));

                wifiHandler = new WifiHandler(getApplicationContext());
                if(wifiHandler.getDroneConnected()) {
                    bttDrone.setVisibility(View.VISIBLE);
                    bttConnectDrone.setVisibility(View.INVISIBLE);
                    Toast.makeText(Recognition.this, "Connesso al drone", Toast.LENGTH_LONG);
                }else{
                    bttDrone.setVisibility(View.INVISIBLE);
                    Toast.makeText(Recognition.this, "Errore connessione drone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bttDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBound) {
                    getApplicationContext().unbindService(mConnection);
                    mIsBound = false;
                }
                getApplicationContext().stopService(new Intent(Recognition.this,SocketService.class));

                Intent intent = new Intent(Recognition.this, DroneControlActivity.class);
                intent.putExtra("Activity", "Training");
                intent.putExtra("oldNetSSID", wifiHandler.getWifiSSID());
                intent.putExtra("label", "recognition");
                startActivity(intent);

            }
        });




        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("commandMessage"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }

            time = Calendar.getInstance();
            bitmapName = "Picture_" + time.get(Calendar.HOUR)+time.get(Calendar.MINUTE)+time.get(Calendar.SECOND)
                    +"_"+time.get(Calendar.DAY_OF_MONTH)+"_"+(time.get(Calendar.MONTH)+1)+"_"+time.get(Calendar.YEAR);

            resizeBitmap(myBitmap);
            myImageView.buildDrawingCache();
            Bitmap bmp2=myImageView.getDrawingCache();

            imgByte = getBytesFromBitmap(myBitmap);

            myImageView.destroyDrawingCache();
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

        myBitmap = Bitmap.createScaledBitmap(dstBmp, 360, 360, true);
        myImageView.setImageBitmap(myBitmap);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.training) {

            Intent manda = new Intent( getApplicationContext(),Training.class);
            manda.putExtra("isConnected", mIsBound);
            startActivity(manda);

        }
        /*else if (id == R.id.recognition) {

            Intent manda = new Intent( getApplicationContext(),Recognition.class);

            startActivity(manda);

        }*/
        else if (id == R.id.download) {
            Intent manda = new Intent( getApplicationContext(),DownloadActivity.class);

            startActivity(manda);

        }



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
