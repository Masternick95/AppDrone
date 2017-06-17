package lastrico.r.appdrone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Connect extends Activity {
    Button connect;
    EditText editAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.6));

        connect = (Button) findViewById(R.id.connetti_popup);
        editAddress = (EditText) findViewById(R.id.indirizzo);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Connect.this, SocketService.class);
                intent.putExtra("ADDRESS", editAddress.getText().toString());

                startService(intent);
                finish();
            }
        });


    }
}
