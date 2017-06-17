package lastrico.r.appdrone;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static lastrico.r.appdrone.Interface.ServerInterface.DOWNLOAD;
import static lastrico.r.appdrone.Interface.ServerInterface.EXIT;
import static lastrico.r.appdrone.Interface.ServerInterface.UPLOAD;

public class SocketService extends Service {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Socket socket;
    public String address;
    private ExecutorService executor = Executors.newFixedThreadPool(1);

    public int port = 9000;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Future<Object> inputData;
    private ExecutorService inputListener = Executors.newFixedThreadPool(2);
    public boolean isClosed;
    private HashMap<String,byte[]> imgDownload;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent == null) stopSelf();
        else address = intent.getStringExtra("ADDRESS");

        if (address == null) stopSelf();
        else new Thread(new ConnectSocket()).start();

        return START_STICKY;
    }

    public void displayToast(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendBroadcastMessage(String type, String msg) {
        Intent intent = new Intent(type);
        intent.putExtra("message",msg);
        LocalBroadcastManager.getInstance(SocketService.this).sendBroadcast(intent);
    }


    public Future<Boolean> uploadImage(final String imgName, final byte[] imgByte){
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Object obj = null;

                try {
                    output.writeObject(UPLOAD);
                    output.writeObject(imgName);
                    output.writeObject(imgByte);
                    obj = inputData.get();

                    if (obj instanceof Exception)
                        throw (Exception) obj;

                    if (obj == null)
                        throw new NullPointerException("Null pointer exception");
                    if (!(obj instanceof Boolean))
                        throw new IllegalArgumentException("You received an invalid response!");
                } catch (IOException | ExecutionException | InterruptedException e) {
                    displayToast(e.getMessage());
                    SocketService.this.stopSelf();
                } catch (Exception e) {
                    displayToast(e.getMessage());
                    return false;
                }

                return (Boolean) obj;
            }
        };

        return executor.submit(callable);
    }

    public Future<Boolean> downloadImage(){
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Object obj = null;

                try {

                    output.writeObject(DOWNLOAD);

                    obj = inputData.get();
                    if (obj == null)
                        throw new NullPointerException("Null pointer exception");

                    SocketService.this.imgDownload = (HashMap<String, byte[]>) obj;

                } catch (IOException | ExecutionException | InterruptedException e) {
                    displayToast(e.getMessage());
                    SocketService.this.stopSelf();
                } catch (Exception e) {
                    displayToast(e.getMessage());
                    return false;
                }

                return true;
            }
        };

        return executor.submit(callable);
    }

    public HashMap<String, byte[]> getImgDownload(){
        return imgDownload;
    }




    private class ConnectSocket implements Runnable {
        @Override
        public void run() {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(address,port),6000);
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                isClosed = false;
            } catch (IOException e) {
                displayToast("Connessione fallita. "+e.getMessage());
                sendBroadcastMessage("commandMessage","FAILED");
                SocketService.this.stopSelf();
                return;
            }

            displayToast("Connesso al server!");
            sendBroadcastMessage("commandMessage","SUCCESS");
            //Intent intentTakePhoto = new Intent(SocketService.this, MainActivity.class);
            //intentTakePhoto.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(intentTakePhoto);

            inputListener.submit(new ListenInput());


        }
    }

    private class ListenInput implements Runnable {
        @Override
        public void run() {
            Callable<Object> callable;
            final Object[] obj = {null};
            while (!socket.isClosed()) {
                callable = new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {

                        if(!isClosed){
                            obj[0] = input.readObject();
                        }

                        synchronized (ListenInput.this) {
                            ListenInput.this.notify();
                        }

                        if (obj[0] instanceof Integer) {
                            if ((Integer) obj[0] == EXIT) {
                                if (socket != null) {
                                    sendBroadcastMessage("commandMessage","EXIT");
                                    displayToast("Connesione con l'host interrotta");
                                    if (socket.isConnected()) {
                                        exit();
                                    }
                                    if (!socket.isClosed()) {
                                        try {
                                            socket.close();
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                socket = null;
                                return null;
                            }
                        }

                        return obj[0];
                    }
                };

                inputData = inputListener.submit(callable);

                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        displayToast(e.getMessage());
                        return;
                    }
                }
            }
        }
    }

    public void exit() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    output.close();
                } catch (IOException e) {
                    displayToast(e.getMessage());
                    SocketService.this.stopSelf();
                } catch(Exception e) {
                    displayToast(e.getMessage());
                    return;
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        displayToast(e.getMessage());
                        SocketService.this.stopSelf();
                    }
                }
                isClosed = true;
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            displayToast(e.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        if (socket != null) {
            if (socket.isConnected())
                exit();
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception e) { }
            }
        }

        socket = null;

        super.onDestroy();
    }
}
