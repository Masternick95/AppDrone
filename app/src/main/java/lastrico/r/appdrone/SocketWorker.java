package lastrico.r.appdrone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.concurrent.ExecutionException;

public class SocketWorker extends AsyncTask<Void, Void, Boolean> {
    Context context;
    SocketService socket;
    String imgName;
    byte[] imgByte;
    ProgressDialog progress;
    int action;

    public SocketWorker(Context context, SocketService socket, ProgressDialog progress, int action){
        this.context = context;
        this.socket = socket;
        this.progress = progress;
        this.action = action;
    }

    public SocketWorker(Context context, SocketService socket, String imgName, byte[] imgByte, ProgressDialog progress, int action){
        this.context = context;
        this.socket = socket;
        this.imgName = imgName;
        this.imgByte = imgByte;
        this.progress = progress;
        this.action = action;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        progress.setIndeterminate(true);
        progress.setTitle("Please wait");
        if(action==1)
            progress.setMessage("Uploading...");
        else if(action==2)
            progress.setMessage("Downloading...");
        progress.show();
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if(action == 1)
                return socket.uploadImage(imgName,imgByte).get();
            else if(action == 2)
                return socket.downloadImage().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        progress.dismiss();

        if(aBoolean && action == 1)
            socket.displayToast("Upload effettuato");
        else if((aBoolean && action == 2)){
            socket.displayToast("Download effettuato");
            Intent intent = new Intent(context, DownloadActivity.class);
            context.startActivity(intent);
        }

    }
}
