package org.mackristof.followme;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

import kotlin.android.system.services.R;

/**
 * Created by mackristof on 30/06/2016.
 */
public class GeoGrid {

    public boolean IsAvailable = false;
    public boolean IsLoadingFromFile = false;

    public static final int GRID_SIZE = 8;  // This value MUST Be even;
    short[][] LoadingGridValues = new short[GRID_SIZE][1440];

    private final Semaphore SemaphoreParms = new Semaphore(1, false);
    // This semaphore controls the access to the following values:
    short[][] GridValues = new short[GRID_SIZE][1440];
    // End Semaphore;

    double StartLatitude;
    double LoadCenterToLatitude;

    Context context;

    public GeoGrid(Context context) {
        this.context = context;
    }

    class LoadEGM96Grid implements Runnable {

        private Context context;

        public LoadEGM96Grid(Context context) {
            this.context = context;
        }
        // Thread: Load grid sector

        @Override
        public void run() {


            InputStream inputStream = context.getResources().openRawResource(R.raw.ww15mgh);
            BufferedInputStream bin = new BufferedInputStream(inputStream);
            DataInputStream din = new DataInputStream(bin);
            int i;
            int ilon;
            int ilat = 0;

            int istartlat;
            istartlat = (int) ((((90.0 - LoadCenterToLatitude)) / 0.25f) - ((GRID_SIZE / 2) - 1));
            if (istartlat < 0) istartlat = 0;
            if (istartlat > (721 - GRID_SIZE)) istartlat = 721 - GRID_SIZE;


            int count = (int) ((2076480 / 2) / 1440);
            //int[] values = new int[1440];

            for (i = 0; (i < count) && (ilat < GRID_SIZE); i++) {
                try {
                    for (ilon = 0; ilon < 1440; ilon++) {
                        LoadingGridValues[ilat][ilon] = din.readShort();
                        //if (ilon == 0) tv.append((short)GridValues[ilat][ilon] + " ");
                    }
                    if (i >= istartlat) ilat++;
                } catch (IOException e) {
                    //Toast.makeText(getApplicationContext(), "Oops", Toast.LENGTH_SHORT).show();
                    //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            SemaphoreParms.acquireUninterruptibly();     // Acquire Semaphore
            StartLatitude = 90.0 - (double) istartlat * 0.25f;
            //tv.setText("Read " + i + " arrays \nGrid starts at Latitude: " + StartLatitude + "\n");

            // Update array    ON SEMAPHORE!!!
            for (int ii = 0; ii < GridValues.length; ii++)
                for (int jj = 0; jj < GridValues[ii].length; jj++)
                    GridValues[ii][jj] = LoadingGridValues[ii][jj];
            //GridValues[ii][jj] = 0;
            // End Semaphore
            SemaphoreParms.release();                    // Release Semaphore

            IsAvailable = true;
            IsLoadingFromFile = false;
            //Toast.makeText(getApplicationContext(), "EGM96 correction grid loaded", Toast.LENGTH_SHORT).show();


        }
    }


    private void Load(double CenterToLatitude, double CenterToLongitude) {

        LoadCenterToLatitude = CenterToLatitude;
        new Thread(new LoadEGM96Grid(this.context)).start();
    }

    public double GetAltitudeCorrection(double Latitude, double Longitude) {
        if (IsAvailable) {

            SemaphoreParms.acquireUninterruptibly();     // Acquire Semaphore

            double Lat = 90.0 - Latitude;
            double Lon = Longitude;
            if (Lon < 0) Lon += 360.0;

            int ilon = (int) (Lon / 0.25);
            int ilat = (int) ((Lat - (-StartLatitude + 90.0)) / 0.25);

            if ((ilat < 0) || (ilat > GRID_SIZE - 2)) {
                IsAvailable = false;
                IsLoadingFromFile = true;
                SemaphoreParms.release();                    // Release Semaphore
                Load(Latitude, Longitude);
                return (0);
            }

            int istartlat;
            istartlat = (int) ((((90.0 - Latitude)) / 0.25f) - ((GRID_SIZE / 2) - 1));
            //if (istartlat < 0) istartlat = 0;
            //if (istartlat > (721 - GRID_SIZE)) istartlat = 721-GRID_SIZE;


            // Creating points for interpolation

            short hc11 = GridValues[ilat][ilon];
            short hc12 = GridValues[ilat + 1][ilon];

            ilon++;
            if (ilon > 1439) ilon -= 1440;

            short hc21 = GridValues[ilat][ilon];
            short hc22 = GridValues[ilat + 1][ilon];

            SemaphoreParms.release();                    // Release Semaphore


            // Interpolation
            // Latitude
            double hc1 = hc11 + (hc12 - hc11) * (Lat % 0.25) / 0.25;
            double hc2 = hc21 + (hc22 - hc21) * (Lat % 0.25) / 0.25;
            // Longitude
            double hc = hc1 + (hc2 - hc1) * (Lon % 0.25) / 0.25;


            if (((ilat < 1) && (istartlat > 0)) || ((ilat > GRID_SIZE - 3) && (istartlat < (721 - GRID_SIZE))))      // Near grid border
            {
                if (!IsLoadingFromFile) {
                    IsLoadingFromFile = true;
                    //Toast.makeText(getApplicationContext(), "Loading EGM96 grid", Toast.LENGTH_SHORT).show();
                    Load(Latitude, Longitude);
                }
            }


            return (hc / 100);
        } else {
            if (!IsLoadingFromFile) {
                IsLoadingFromFile = true;
                //Toast.makeText(getApplicationContext(), "Loading EGM96 grid", Toast.LENGTH_SHORT).show();
                Load(Latitude, Longitude);
            }
            return (0);
        }
    }
}