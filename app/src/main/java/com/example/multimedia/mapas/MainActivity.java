package com.example.multimedia.mapas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Creo los objetos
    TextView mensaje1;
    TextView mensaje2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creo las referencias con la interfaz
        mensaje1 = (TextView) findViewById(R.id.view_uno);
        mensaje2 = (TextView) findViewById(R.id.view_dos);

        //Valido los permisos
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }else{
            locationStart();
        }
    }

    private void locationStart() {
        //Obtengo los servicios de localizacion del GPS
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Creamos la instancia de Localizacion
        Localizacion local = new Localizacion();
        //Le pasamos la actividad
        local.setMainActivity(this);
        //Almacenamos un booleano si el GPS esta activado
        final boolean gpsEnable = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //Si el GPS no esta activado
        if (!gpsEnable){
            //Mandamos al usuario a la pantalla de configuracion del GPS
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,(LocationListener) local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,(LocationListener) local);
        mensaje1.setText("Localizacion agregada");
        mensaje2.setText("");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationStart();
                return;
            }
        }
    }

    public void setLocation(Location loc){
        //Obtenemos la direccion de la calle a partir de la longitud y la longitud
        //Validamos que la latitud y la longitud sean diferentes de 0
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0){
            try{
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                //Validamos que la lista no este vacia
                if (!list.isEmpty()){
                    Address dirCalle = list.get(0);
                    mensaje2.setText("Mi direccion es: \n" + dirCalle.getAddressLine(0));
                }else {
                    mensaje2.setText("No hay ninguna direccion");
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Aqui empieza la clase Localizacion la cual se encarga de escuchar los cambios de posicion del GPS
     */
    public class Localizacion implements LocationListener{

        MainActivity mainActivity;

        public MainActivity getMainActivity(){
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity){
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            /**
             * Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas debido
             * a la deteccion de un cambio de ubicacion.
             */
            loc.getLatitude();
            loc.getLongitude();
            String text = "Mi ubicacion actual es: " + "\n Lat = " + loc.getLatitude() + "\n Long = " + loc.getLongitude();
            mensaje1.setText(text);
            this.mainActivity.setLocation(loc);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status){
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            mensaje1.setText("GPS activado");
        }

        @Override
        public void onProviderDisabled(String s) {
            mensaje1.setText("GPS desactivado");
        }
    }
}
