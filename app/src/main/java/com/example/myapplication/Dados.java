package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


public class Dados extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    ArrayList<LatLng> pontosLatLng = new ArrayList();

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    FusedLocationProviderClient mFusedLocationClient;


    public Double getLag() {
        return lag;
    }

    public void setLag(Double lag) {
        this.lag = lag;
    }

    public Double getLog() {
        return log;
    }

    public void setLog(Double log) {
        this.log = log;
    }

    Double lag;
    Double log;
    LatLng pontoBlumenau;
    Runnable r;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;

    private void incializarFireBase() {

        FirebaseApp.initializeApp(Dados.this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //PolyLine Initialize
//
        pontosLatLng.add(new LatLng(-26.906440, -49.075242));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        final Button botao = (Button) findViewById(R.id.btn_iniciar);
        final Button botao_finalizar = (Button) findViewById(R.id.btn_finalizar);

        final TextInputEditText etNome = findViewById(R.id.nome_motorista);
        final TextInputEditText etNumeroOnibus = findViewById(R.id.numero_onibus);

        final String nome = etNome.getText().toString().trim();
        final String numerobus = etNumeroOnibus.getText().toString().trim();

        buildGoogleApiClient();

        final Spinner spinner = (Spinner) findViewById(R.id.linha);

        // Spinner click listener
        // Spinner Drop down elements
        final List<String> categories = new ArrayList<String>();
        categories.add("Blumenau - Ilhota");
        categories.add("Ilhota - Blumenau");
        categories.add("Blumenau - Gaspar");
        categories.add("Gaspar - Blumenau");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        setTitle("Motorista");

        incializarFireBase();
        final mot motorista = new mot();

        botao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String linha = spinner.getSelectedItem().toString();


                if (etNome.getText().toString().length()>=2 && etNumeroOnibus.getText().toString().length()>3) {

                    botao.setEnabled(true);

                    r = new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i<=i +1; i++) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                motorista.setNomeMotorista(etNome.getText().toString());
                                motorista.setNumMotorista(etNumeroOnibus.getText().toString());
                                motorista.setLinha(linha);
                                motorista.setLatitude(lag);
                                motorista.setLongitude(log);
                                motorista.setCoordenadas(String.valueOf(pontoBlumenau));

                                ref.child("motorista").child(motorista.getNumMotorista()).setValue(motorista);
                            }
                        }
                    };

                    new Thread(r).start();

                    botao.setVisibility(View.INVISIBLE);
                    botao_finalizar.setVisibility(View.VISIBLE);

                    etNome.setEnabled(false);
                    etNumeroOnibus.setEnabled(false);
                    spinner.setEnabled(false);

                }else if((etNome.getText().toString().length()<=0 && etNumeroOnibus.getText().toString().length()<=0)){

                    etNome.setError("Campo obrigatório");
                    etNumeroOnibus.setError("Campo obrigatório");

                }else if((etNome.getText().toString().length()<=1 && etNumeroOnibus.getText().toString().length()<=3)){

                    etNome.setError("Nome deve conter mais de dois caracteres");
                    etNumeroOnibus.setError("Número do ônibus deve conter mais de três caracteres");

                }else if(etNome.getText().toString().length()<2) {

                    etNome.setError("Nome deve conter mais de dois caracteres");

                }else if(etNome.getText().toString().length()<=0) {

                    etNome.setError("Campo obrigatório");

                }else if (etNumeroOnibus.getText().toString().length()<3){

                    etNumeroOnibus.setError("Campo obrigatório");

                }else if (etNumeroOnibus.getText().toString().length()<3){

                    etNumeroOnibus.setError("Número do ônibus deve conter mais de três caracteres");

                }
            }
        });

        botao_finalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ref.child("motorista").child(motorista.getNumMotorista()).removeValue();

                botao_finalizar.setVisibility(View.INVISIBLE);
                botao.setVisibility(View.VISIBLE);

                etNome.setEnabled(true);
                etNumeroOnibus.setEnabled(true);
                spinner.setEnabled(true);

                etNome.setText("");
                etNumeroOnibus.setText("");


                Toast.makeText(Dados.this,"Finalizando",Toast.LENGTH_SHORT).show();

                new Thread(r).interrupt();

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        //stop location updates when Activity is no longer active
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled( true );
        mGoogleMap.getUiSettings().setZoomControlsEnabled( true );
        mGoogleMap.getUiSettings().setCompassEnabled( true );

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // one minute interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mLastLocation = location;
                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker.isVisible();
                }

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                 pontoBlumenau = new LatLng(location.getLatitude(),location.getLongitude());

                // Cria marcadores para os pontos de onibus e posicao do celular
                MarkerOptions markerPosicao = new MarkerOptions();

                lag = pontoBlumenau.latitude;
                log = pontoBlumenau.longitude;

            }
        }
    };

    private void drawLine() {

//        PolylineOptions options = new PolylineOptions().width(8).color( Color.BLUE);
//            options.add(pontoBlumenau,pontoIlhota);
//        line = mGoogleMap.addPolyline(options); //add Polyline
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(Dados.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                // No argue needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}

