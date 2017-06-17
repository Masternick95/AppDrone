package lastrico.r.appdrone;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

import static android.R.color.holo_red_dark;
import static lastrico.r.appdrone.Interface.ServerInterface.UPLOAD;


public class Training  extends Activity  implements NavigationView.OnNavigationItemSelectedListener {



    ImageView myImageView = null;
    TextView labelTextView;
    Button bttClickMe = null;
    Button bttClickMe2 = null;
    Button bttClickMe4 = null;
    Button sas;
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
        setContentView(R.layout.training_layout);
        labelTextView = (TextView) findViewById(R.id.labelTextView);

        FloatingActionButton btTakePhoto = (FloatingActionButton) findViewById(R.id.btTakePhoto);
        btTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();


            }
        });
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.bttSaveImage);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText imageName=(EditText)findViewById(R.id.imageName);
                String nome=imageName.getText().toString();
                myImageView.buildDrawingCache();
                Bitmap bmap = myImageView.getDrawingCache();
                myBitmap=bmap;
                ImageSaver.saveToInternalStorage(bmap, nome);
                myImageView.destroyDrawingCache();
                Toast.makeText(Training.this, "Immagine salvata!", Toast.LENGTH_LONG).show();


            }
        });

        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.btLoadImage);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText imageName=(EditText)findViewById(R.id.imageName);
                String nome=imageName.getText().toString();
                myBitmap=ImageSaver.LoadImageFromStorage(nome);
                myImageView.setImageBitmap(myBitmap);


            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("commandMessage"));



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_training);
        navigationView.setNavigationItemSelectedListener(this);


        myImageView = (ImageView) findViewById(R.id.etImage);
        bttClickMe2 = (Button) findViewById(R.id.upload);


        sas = (Button) findViewById(R.id.connectServer);
        progress = new ProgressDialog(this);

        bttConnectDrone = (Button) findViewById(R.id.connect_drone);
        bttDrone = (Button) findViewById(R.id.drone);

        //initialize ImageView content
        myBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        myImageView.setImageBitmap(myBitmap); //set ImageView content

        //------------ BUTTONS & SEEKBAR LISTENERS ------------

        //gray scale button
        bttClickMe2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // _myCV = new ManipulateImage(progress, myImageView, myBitmap, 1);
               // _myCV.execute("");
                if (mIsBound && mBoundService != null) {
                    if (imgByte != null) {
                        SocketWorker socketWorker =
                                new SocketWorker(Training.this,mBoundService,"prova",imgByte,progress,UPLOAD);
                        socketWorker.execute();

                    }
                }
                else
                    Toast.makeText(Training.this, "Non sei connesso al server", Toast.LENGTH_SHORT).show();

            }
        });
        sas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsBound){
                    mBoundService.displayToast("Sei gia' connesso al server");
                }
                else{
                    Intent intent = new Intent(Training.this, Connect.class);
                    startActivity(intent);

            }

            }
        });

        bttConnectDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiHandler = new WifiHandler(getApplicationContext());
                if(wifiHandler.getDroneConnected()) {
                    bttDrone.setVisibility(View.VISIBLE);
                    bttConnectDrone.setVisibility(View.INVISIBLE);
                    Toast.makeText(Training.this, "Connesso al drone", Toast.LENGTH_LONG);
                }else{
                    bttDrone.setVisibility(View.INVISIBLE);
                    Toast.makeText(Training.this, "Errore connessione drone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bttDrone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(labelTextView.getText().toString().isEmpty()){
                    //Label vuoto: errore. Necessario label assegnare per il training
                    Toast.makeText(Training.this, "ERRORE: Specificare label", Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(Training.this, DroneControlActivity.class);
                    intent.putExtra("Activity", "Training");
                    intent.putExtra("oldNetSSID", wifiHandler.getWifiSSID());
                    intent.putExtra("label", labelTextView.getText().toString());
                    startActivity(intent);
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                myBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //manipulateImage = new ManipulateImage(progress, img, bitmap, 1);
            //manipulateImage.execute("");
            myImageView.setImageBitmap(myBitmap);

            ManipulateImage.resizeBitmap(myBitmap);
            myImageView.buildDrawingCache();
            Bitmap bmp2=myImageView.getDrawingCache();


            imgByte = getBytesFromBitmap(myBitmap);

            myImageView.destroyDrawingCache();
        }
    }
    //Function for saving state of activity (for example when going landscape)
    @Override
    public void onSaveInstanceState(Bundle toSave) {
        super.onSaveInstanceState(toSave);
        toSave.putParcelable("bitmap", myBitmap);
    }

    //Function for reloading state of activity
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myBitmap = savedInstanceState.getParcelable("bitmap");
        myImageView.setImageBitmap(myBitmap);
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
        } else if (id == R.id.recognition) {
            Intent manda = new Intent( getApplicationContext(),Recognition.class);

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
                getApplicationContext().stopService(new Intent(Training.this,SocketService.class));
            }

            else if(message.equals("FAILED")){
                if (mIsBound) {
                    getApplicationContext().unbindService(mConnection);
                    mIsBound = false;
                }
            }

            else if(message.equals("SUCCESS")){
                Intent i = new Intent(Training.this, SocketService.class);
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