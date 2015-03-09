package as.swarmapp.testlocalisation;

import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestLoc extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static int nbPos = 0;
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); // timestamp selon le format de Haggis

    private LocationRequest mLocationRequest = new LocationRequest();
    public GoogleApiClient monClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activite_test_loc);
        buildGoogleApiClient();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_test_loc, menu);
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
        //*
        Location dernièrePos = LocationServices.FusedLocationApi.getLastLocation(
                monClient);
        String s;
        if (dernièrePos != null) {
            s = String.format("position %d : ",nbPos++) +locationToString(dernièrePos);

        }else{
            s = "Aucune position disponible";
        }
        Log.w("onConnected", s);
        ((TextView) findViewById(R.id.Ttest)).setText(s);
        //*/
        createLocationRequest();
        startLocationUpdates();
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
        Toast.makeText(TestLoc.this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    protected void createLocationRequest() {
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static String locationToString(Location l){
        return String.valueOf(l.getLatitude()) + " | " + String.valueOf(l.getLongitude());
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            String s = String.format("position %d : ", nbPos++) + locationToString(location) + " update time : "+sdf.format(new Date());
            ((TextView) findViewById(R.id.Ttest)).setText(s);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                monClient, this);
    }
}
