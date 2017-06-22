package lastrico.r.appdrone;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

import io.vov.vitamio.utils.Log;

/**
 * Created by Nick on 25/05/2017.
 */

public class WifiHandler {
    //private final String PATH = "tcp://192.168.1.1:5555/";  //Path per acquisizione stream video dal drone
    private final String SSID_DRONE = "ardrone2_044992";    //SSID di default del drone a cui connettersi
    private Context context;
    private String SSID_WiFi;

    public WifiHandler(Context context){
        this.context = context;
    }

    public void setWifiSSID(String SSID_WiFi){
        this.SSID_WiFi = SSID_WiFi;
    }

    public String getWifiSSID(){
        return this.SSID_WiFi;
    }

    public boolean checkDroneConnection(){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            //Wifi Non attivo
            Log.e("Wifi spento");
            return false;
        }else{
            WifiInfo connInfo = wifiManager.getConnectionInfo();
            if(connInfo != null){
                String _SSID = connInfo.getSSID().replace("\"","");
                if(_SSID.equals(SSID_DRONE)){
                    return  true;
                }else{

                    Log.e(" yolo "+connInfo.getSSID());
                    Log.e("Connesso ad altra rete");
                    return false;
                }
            }else{
                Log.e("Nessuna connessione attiva");
                return false;
            }
        }
    }

    public boolean getDroneConnected(){
        Log.e("INIZIO CHECK CONNESSIONE");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            //Wifi non attivo, devo attivare
            if(wifiManager.setWifiEnabled(true) == false){
                //Errore connessione
                Log.e("Errore accensione wifi");
                return false;
            }
        }
        WifiInfo connInfo = wifiManager.getConnectionInfo();
        if(connInfo != null){
            //Telefono connesso ad una rete
            String _SSID = connInfo.getSSID().replace("\"","");
            if(_SSID.toString().equals(SSID_DRONE)){
                //Telefono connesso al drone
                Log.e("Già connesso");
                return true;
            }else{
                //Telefono connesso ad un altra wifi, salvo SSID
                SSID_WiFi = connInfo.getSSID();
                wifiManager.disconnect();
            }
        }
        //Cerco wifi drone
        if(!wifiManager.startScan()){
            //Errore avvio scansione reti wifi
            Log.e("Errore scansione reti wifi");
            return false;
        }else{
            List<ScanResult> networksFound = wifiManager.getScanResults();
            for(ScanResult network : networksFound){
                Log.e("Risultato wifi: " + network.SSID);
                if(network.SSID.equals(SSID_DRONE)){
                    //Rete wifi-drone trovata
                    List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                    int netId = -1;
                    for(WifiConfiguration configured : configuredNetworks){
                        if(configured.SSID.equals(SSID_DRONE)){
                            //Rete drone già configurata
                            netId = configured.networkId;
                            break;
                        }
                    }
                    if(netId == -1){
                        //Rete drone non ancora configurata
                        WifiConfiguration configuration = new WifiConfiguration();
                        configuration.SSID = SSID_DRONE;
                        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        netId = wifiManager.addNetwork(configuration);
                        if(netId == -1){
                            //Errore connessione
                            Log.e("Errore configurazione wifi del drone");
                            return false;
                        }
                    }
                    //Tutto pronto per la connessione
                    if(wifiManager.enableNetwork(netId, true)){
                        //Connesso al drone
                        Log.e("Connesso");
                        return true;
                    }else{
                        //Errore connessione
                        Log.e("Errore connessione al drone");
                        return false;
                    }
                }
            }
            //Rete wifi-drone non disponibile
            Log.e("Rete wifi drone non rilevata");
            return false;
        }
    }

    public boolean reconnectWifi(){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            //Wifi non attivo, devo attivare
            if(wifiManager.setWifiEnabled(true) == false){
                //Errore connessione
                return false;
            }
        }
        WifiInfo connInfo = wifiManager.getConnectionInfo();
        if(connInfo != null){
            //Telefono connesso ad una rete
            if(connInfo.getSSID().toString().equals(SSID_WiFi)){
                //Telefono connesso al drone
                return true;
            }else{
                //Telefono connesso ad un altra wifi
                wifiManager.disconnect();
            }
        }
        //Cerco wifi
        if(!wifiManager.startScan()){
            //Errore avvio scansione reti wifi
            return false;
        }else {
            List<ScanResult> networksFound = wifiManager.getScanResults();
            for (ScanResult network : networksFound) {
                if (network.SSID.equals(SSID_WiFi)) {
                    //Rete wifi trovata
                    List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                    int netId = -1;
                    for (WifiConfiguration configured : configuredNetworks) {
                        if (configured.SSID.equals(SSID_WiFi)) {
                            //Rete già configurata
                            netId = configured.networkId;
                            break;
                        }
                    }
                    if (netId == -1) {
                        //Rete non ancora configurata
                        WifiConfiguration configuration = new WifiConfiguration();
                        configuration.SSID = SSID_WiFi;
                        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        netId = wifiManager.addNetwork(configuration);
                        if (netId == -1) {
                            //Errore connessione
                            return false;
                        }
                    }
                    //Tutto pronto per la connessione
                    if (wifiManager.enableNetwork(netId, true)) {
                        //Connesso al drone
                        return true;
                    } else {
                        //Errore connessione
                        return false;
                    }
                }
            }
            //Rete wifi non disponibile
            return false;
        }
    }
}