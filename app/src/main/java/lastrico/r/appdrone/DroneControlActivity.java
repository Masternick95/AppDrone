package lastrico.r.appdrone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class DroneControlActivity extends AppCompatActivity {
    private VideoView videoView;
    private Button takePictureButton;
    private Button endAcquisitionButton;

    private Context context = null;
    WifiHandler gestoreWifi;

    String imgPath = null;
    String mode;    //Modalità di funzionamento activity(training o recognition) fondamentale per determinare numero immagini da acquisire
    String label;   //Necessario solo per il training

    private final String PATH = "tcp://192.168.1.1:5555/";  //Path per acquisizione stream video dal drone
    private final String SSID_DRONE = "ardrone2_044992";    //SSID di default del drone a cui connettersi

    private String SSID_Wifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drone_control);
        context = getApplicationContext();

        Intent intent = getIntent();
        SSID_Wifi = intent.getStringExtra("oldNetSSID");
        mode = intent.getStringExtra("Activity");
        if(mode.equals("Training")){
            label = intent.getStringExtra("label");
        }

        videoView = (VideoView) findViewById(R.id.vitamio_videoView);
        takePictureButton = (Button) findViewById(R.id.takePhotoButton);
        endAcquisitionButton = (Button) findViewById(R.id.endAcquisition);

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturePhoto(null);

            }
        });
        endAcquisitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gestoreWifi = new WifiHandler(getApplicationContext());
                gestoreWifi.setWifiSSID(SSID_Wifi);
                gestoreWifi.reconnectWifi();
                //Passa imgsPaths al server
                //Per il training: nella variabile label è contenuto il nome della persona di cui sono state scattate le foto
                //Per entrambe le modalità nella variabile imgsPaths è contenuto un array list di stringhe che contiene le path di tutte le immagini acquisite

                //Toast.makeText(getApplicationContext(), "INVIO IMMAGINE", Toast.LENGTH_LONG);
            }
        });

        videoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);
        videoView.setBufferSize(4096);
        videoView.setVideoPath(PATH);
        videoView.requestFocus();
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH, 0);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackSpeed(1.0f);
            }
        });

    }

    private void capturePhoto(View view){
        try{
            PhotoSaver photoSaver = new PhotoSaver(context, videoView.getMediaPlayer(), label);
            imgPath = photoSaver.record();
            if(imgPath == null) {
                //errore acquisizione foto
                Toast.makeText(getApplicationContext(), "Errore acquisizione foto", Toast.LENGTH_LONG);
            }
        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Picture error!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}