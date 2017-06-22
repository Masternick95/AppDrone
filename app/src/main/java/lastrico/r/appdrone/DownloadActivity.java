package lastrico.r.appdrone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class DownloadActivity extends AppCompatActivity {
    ImageView img;
    Button btn;
    byte[] imgDown = null;
    String imgName = null;
    TextView nameOfPerson = null;

    //BINDING WITH SocketService
    private boolean mIsBound;
    private SocketService mBoundService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mBoundService = binder.getService();

            imgDown = mBoundService.getImgDownload();
            imgName = mBoundService.getImgName();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download);

        if (!mIsBound) {
            Intent intent = new Intent(DownloadActivity.this, SocketService.class);
            mIsBound = getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
        img = (ImageView) findViewById(R.id.imgDownload);
        btn = (Button) findViewById(R.id.display);
        nameOfPerson = (TextView) findViewById(R.id.namePerson);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imgDown != null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgDown,0,imgDown.length);
                    img.setImageBitmap(bitmap);
                    nameOfPerson.setText(imgName);
                }

            }
        });


    }

}
