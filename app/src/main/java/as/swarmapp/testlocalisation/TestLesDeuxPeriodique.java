package as.swarmapp.testlocalisation;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;


public class TestLesDeuxPeriodique extends ActionBarActivity implements GestionHorsUI,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE= "longitude";
    public GoogleApiClient monClient;
    private LocationRequest mLocationRequest = new LocationRequest();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_test_les_deux_periodique);
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
        /*
        Location dernièrePos = LocationServices.FusedLocationApi.getLastLocation(
                monClient);
        String s;
        if (dernièrePos != null) {
            s = locationToURL(dernièrePos);
            MAJaffichage(s);
        }else{
            s = "Aucune position disponible";
            ((TextView) findViewById(R.id.TtestLesDeuxPeriodique)).setText(s);
        }
        Log.w("dernière position : ", s);
        //*/
        //((TextView) findViewById(R.id.TtestLesDeux)).setText(s);
        createLocationRequest();
        startLocationUpdates();
    }


    protected void createLocationRequest() {
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                monClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            String paramPOST = locationToURL(location);
            MAJaffichage(paramPOST);
        }
    }

    @Override
    public void MAJaffichage(final Object paramPOST) {
        new Thread(new Runnable() { public void run() {
            Object reponsePOST = aFaireHorsUI(paramPOST);
            aFaireEnUI(reponsePOST);

        } }).start();
    }

    /**
     *
     * @param paramPOST
     * @return la réponse HTTP POST, ou INITIALE si la requête a échoué
     */
    @Override
    public Object aFaireHorsUI(final Object paramPOST){
        // strPosition est la position sous forme de string formatée
        String s = "INITIALE";
        /*
        // Version GET
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://raspi:8000/tests/log" + "?" + strPosition.toString())).openConnection();
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
            HttpURLConnection urlConnection = (HttpURLConnection) (new URL("http://raspi:8000/tests/log")).openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setFixedLengthStreamingMode(paramPOST.toString().getBytes().length);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            try {
                OutputStream osDeURLconn = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(osDeURLconn, "UTF-8"));
                writer.write(paramPOST.toString());
                writer.flush();
                writer.close();
                osDeURLconn.close();

                InputStream inDeURLconn = new BufferedInputStream(urlConnection.getInputStream());
                s = streamToString(inDeURLconn);

            }catch(Exception e){
                e.printStackTrace();

            }finally{
                urlConnection.disconnect();

            }
        }catch (Exception e){
            e.printStackTrace();

        }
        return s;
    }

    @Override
    public void aFaireEnUI(final Object o){
        runOnUiThread(new Runnable() { public void run() {

            ((TextView) findViewById(R.id.TtestLesDeuxPeriodique)).setText(o.toString());

        }});
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (monClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(monClient, this);
    }

    public static String locationToString(Location l){
        return String.valueOf(l.getLatitude()) + " | " + String.valueOf(l.getLongitude());
    }

    public static String locationToURL(Location l){
        return LATITUDE + "=" + String.valueOf(l.getLatitude()) + "&" + LONGITUDE + "=" + String.valueOf(l.getLongitude());
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
