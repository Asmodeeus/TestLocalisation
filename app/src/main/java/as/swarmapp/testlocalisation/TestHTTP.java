package as.swarmapp.testlocalisation;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class TestHTTP extends ActionBarActivity implements GestionHorsUI {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_test_http);
        MAJaffichage(null);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_htt, menu);
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

    @Override
    public void MAJaffichage(final Object o) {
        new Thread(new Runnable() { public void run() {
            // aFaireHorsUI nous dit si l'on doit afficher le layout normal ou passer directement à une autre activité
            Object params = aFaireHorsUI(o);
            aFaireEnUI(params);

        } }).start();
    }

    @Override
    public Object aFaireHorsUI(final Object o){
        String s = "INITIALE";
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://raspi:8000/tests/log")).openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                s = streamToString(in);
            }finally{
                urlConnection.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return s;
    }

    public void aFaireEnUI(final Object o){
        runOnUiThread(new Runnable() { public void run() {

            ((TextView) findViewById(R.id.TreponseHTTP)).setText(o.toString());

        }});
    }

    public static String streamToString(InputStream in){
        int n = 0;
        StringBuffer sb = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(in, "UTF-8");
            while ((n = isr.read()) != -1) {
                sb.append((char)n);
            }
            in.close();
        }catch (IOException e){
            e.printStackTrace();
            return "";
        }
        return sb.toString();
    }
}
