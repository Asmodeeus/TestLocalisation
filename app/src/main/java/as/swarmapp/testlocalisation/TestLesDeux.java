package as.swarmapp.testlocalisation;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TestLesDeux extends ActionBarActivity implements GestionHorsUI,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener   {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); // timestamp selon le format de Haggis
    public static final String FORMAT_PARAM_ = "%s=%s&";
    public static final String LISTE_ = "liste[%s]";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE= "longitude";
    public static final String TOKEN = "token";
    public static final String TRACKER_ID = "tracker_id";
    public static final String DATETIME = "datetime";

    public GoogleApiClient monClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_test_les_deux);
        buildGoogleApiClient();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_les_deux, menu);
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
    public void MAJaffichage(final Object parametresRequete) {
        new Thread(new Runnable() { public void run() {
            Object params = aFaireHorsUI(parametresRequete);
            aFaireEnUI(params);

        } }).start();
    }

    @Override
    public Object aFaireHorsUI(final Object parametresRequete){
        // parametresRequete est la position sous forme de string formatée
        Boolean ok = Boolean.valueOf(false);
        String params = parametresRequete.toString();
        int longueurParams = params.getBytes().length;
        /*
        // Version GET
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://raspi:8000/tests/log" + "?" + parametresRequete.toString())).openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                s = streamToString(in);
            }finally{
                urlConnection.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //*/
        // Version POST
        try {
            //HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://raspi:8000/tests/log")).openConnection();
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://haggis.ensta-bretagne.fr:3000/listes")).openConnection();
            preparerPourPOST(urlConnection, longueurParams);

            try {
                paramsPOST(urlConnection, params);
                ok = reponseRequete(urlConnection);

            }finally{
                urlConnection.disconnect();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return ok;
    }

    @Override
    public void aFaireEnUI(final Object o){
        runOnUiThread(new Runnable() { public void run() {

            //((WebView) findViewById(R.id.WVlesDeux)).loadData(o.toString(), "text/html", null);
            ((TextView) findViewById(R.id.TtestLesDeux)).setText(o.toString());

        }});
    }

    protected synchronized void buildGoogleApiClient() {
        monClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        monClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location dernièrePos = LocationServices.FusedLocationApi.getLastLocation(
                monClient);
        String strPosition;

        if (dernièrePos != null) {
            strPosition = locationToURL(dernièrePos);
            MAJaffichage(strPosition);

        }else{
            strPosition = "Aucune position disponible";
        }

        Log.w("dernière position ", strPosition); //!
        //((TextView) findViewById(R.id.TtestLesDeux)).setText(strPosition);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static String locationToString(Location l){
        return String.valueOf(l.getLatitude()) + " | " + String.valueOf(l.getLongitude());
    }

    public static String locationToURL(Location l){
        Map<String, String> paramsKV = new HashMap<>(5);
        paramsKV.put(String.format(LISTE_,TOKEN), "249737703537f0de5c007e30f7b009b4");
        paramsKV.put(String.format(LISTE_,TRACKER_ID), "1");
        paramsKV.put(String.format(LISTE_, DATETIME), sdf.format(new Date()));
        paramsKV.put(String.format(LISTE_,LATITUDE), String.valueOf(l.getLatitude()));
        paramsKV.put(String.format(LISTE_,LONGITUDE), String.valueOf(l.getLongitude()));


        return mapToParams(paramsKV);
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

    public static HttpURLConnection preparerPourPOST(HttpURLConnection u, int taille_requete) throws ProtocolException {
        u.setReadTimeout(10000);
        u.setConnectTimeout(15000);
        u.setRequestMethod("POST");
        u.setFixedLengthStreamingMode(taille_requete);
        u.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        u.setRequestProperty("User-Agent", "Mozilla");
        u.setRequestProperty("Accept", "*/*");
        u.setInstanceFollowRedirects(false);
        u.setDoInput(true);
        u.setDoOutput(true);
        return u;
    }

    public static HttpURLConnection paramsPOST(HttpURLConnection u, String params) throws Exception{
        OutputStream osDeURLconn = u.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(osDeURLconn, "UTF-8"));
        writer.write(params);
        writer.flush();
        writer.close();
        osDeURLconn.close();
        return u;
    }

    public static Boolean reponseRequete(HttpURLConnection u) throws Exception{
        InputStream inDeURLconn;
        try {
            inDeURLconn = new BufferedInputStream(u.getInputStream());
            inDeURLconn.close();
            return Boolean.TRUE;

        }catch(Exception e){
            inDeURLconn = new BufferedInputStream(u.getErrorStream());
            inDeURLconn.close();
            return Boolean.FALSE;
        }
        /*
        if (u.getResponseCode() < 400){
            inDeURLconn = new BufferedInputStream(u.getInputStream());
        }else{
            Log.w("code : ", String.valueOf(u.getResponseCode()));
            inDeURLconn = new BufferedInputStream(u.getErrorStream());
        }//*/
    }

    /*
    public static String reponseRequete(HttpURLConnection u) throws Exception{
        InputStream inDeURLconn;
        try {
            inDeURLconn = new BufferedInputStream(u.getInputStream());
            Log.w("bla", "In");

        }catch(Exception e){
            e.printStackTrace();
            inDeURLconn = new BufferedInputStream(u.getErrorStream());
            Log.w("bla", "Err");
        }

        String q = streamToString(inDeURLconn);
        Log.w("bla", q);
        return q;
    }
    //*/

    public static String mapToParams(Map<String, String> laMap){
        String s = "";

        for (String k:laMap.keySet()) {
            s += String.format(FORMAT_PARAM_,k, laMap.get(k));
        }
        if (s.length()-1 >= 0) {
            return s.substring(0, s.length() - 1);
        }
        else
            return "";
    }
}
