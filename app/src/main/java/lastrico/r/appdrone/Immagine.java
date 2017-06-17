package lastrico.r.appdrone;

/**
 * Created by Nick on 15/05/2017.
 */

//Classe wrapper
public class Immagine {
    public int id;
    public String imgPath;
    public String imgLabel;

    public Immagine(){

    }

    public Immagine(int id, String path, String label){
        this.id = id;
        this.imgPath = path;
        this.imgLabel = label;
    }
}
