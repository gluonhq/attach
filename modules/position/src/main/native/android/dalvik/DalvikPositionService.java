/*
 * Copyright (c) 2020, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.helloandroid;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import android.util.Log;


/**
 * An implementation of the
 * {@link PositionService PositionService} for the
 * Android platform.
 * 
 * Requires ACCESS_COARSE_LOCATION and/or ACCESS_FINE_LOCATION permission:
 * {@code <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
 *  <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
 * 
 */

public class DalvikPositionService implements LocationListener {

    private static final String TAG = "GluonAttach";
    private Activity activityContext;
public static final Parameters DEFAULT_PARAMETERS = new Parameters(Parameters.Accuracy.MEDIUM, false);

    private static final Logger LOG = Logger.getLogger(DalvikPositionService.class.getName());
    
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ALTITUDE = "altitude";

    private EarthGravitationalModel gh;
    private LocationManager locationManager;
    private String locationProvider;

    private AndroidLooperTask looperTask = null;
    private Parameters parameters;
    private boolean running;
    private boolean debug = true;

    public DalvikPositionService(Activity activity) {
        Log.v(TAG, "DALVIKPOSITIONSERVICE");
        this.activityContext = activity;
        boolean gpsEnabled = Util.verifyPermissions(Manifest.permission.ACCESS_COARSE_LOCATION) || 
                Util.verifyPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!gpsEnabled) {
            Log.v(TAG, "GPS disabled. ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permissions are required");
        }
        gh = new EarthGravitationalModel();
        try {
            gh.load("egm180.nor");
        } catch (IOException e) {
            Log.v(TAG, "Failed to load nor file", e);
e.printStackTrace();
        }
    }    

    public void start() {
        start(DEFAULT_PARAMETERS);
    }

    public void start(Parameters parameters) {
        Log.v(TAG, "DalvikPositionService, START called");
        if (running) {
            stop();
        }
        
        this.parameters = parameters;
        
        initialize();
        
        running = true;
    }

    public void stop() {
        running = false;
        
        quitLooperTask();
    }
    
    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (debug) {
                LOG.log(Level.INFO, String.format("Android location changed: %f / %f / %f",
                        location.getLatitude(), location.getLongitude(), location.getAltitude()));
            }
            updatePosition(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (debug) {
            LOG.log(Level.INFO, String.format("Status for LocationProvider %s changed to %d.", provider, status));
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (debug) {
            LOG.log(Level.INFO, String.format("LocationProvider %s was enabled by the user.", provider));
        }
        if (provider.equals(locationProvider) && looperTask == null) {
            createLooperTask();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (debug) {
            LOG.log(Level.INFO, String.format("LocationProvider %s was disabled by the user, quitting looper task.", provider));
        }
        
        if (provider.equals(locationProvider)) {
            quitLooperTask();
        }
    }
        
    private void initialize() {    
        
        Object systemService = activityContext.getSystemService(Activity.LOCATION_SERVICE);
        locationManager = (LocationManager) systemService;

        List<String> locationProviders = locationManager.getAllProviders();
        if (debug) {
            LOG.log(Level.INFO, String.format("Available location providers on this device: %s.", locationProviders.toString()));
        }
        
        locationProvider = locationManager.getBestProvider(getLocationProvider(), false);
        if (debug) {
            Log.v(TAG, String.format("Picked %s as best location provider.", locationProvider));
        }
        
        boolean locationProviderEnabled = locationManager.isProviderEnabled(locationProvider);
        if (!locationProviderEnabled) {
            if (debug) {
                LOG.log(Level.INFO, String.format("Location provider %s is not enabled, starting intent to ask user to activate the location provider.", locationProvider));
            }
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activityContext.startActivity(intent);
        }

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation != null) {
            if (debug) {
                LOG.log(Level.INFO, String.format("Last known location for provider %s: %f / %f / %f",
                        locationProvider, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), lastKnownLocation.getAltitude()));
            }
            updatePosition(lastKnownLocation);
        }

/*
        final Intent serviceIntent = new Intent(activityContext, AndroidPositionBackgroundService.class);
        final IntentFilter intentFilter = new IntentFilter(AndroidPositionBackgroundService.class.getName());
        
        Services.get(LifecycleService.class).ifPresent(l -> {
            l.addListener(LifecycleEvent.PAUSE, () -> {
                quitLooperTask();
                // if the PositionService is still running and backgroundModeEnabled 
                // then start background service when the app goes to background
                if (running && parameters.isBackgroundModeEnabled()) {
                    activityContext.registerReceiver(broadcastReceiver, intentFilter);
                    activityContext.startService(serviceIntent);
                }
            });
            l.addListener(LifecycleEvent.RESUME, () -> {
                // if backgroundModeEnabled then stop the background service when
                // the app goes to foreground and resume PositionService
                if (parameters.isBackgroundModeEnabled()) {
                    try {
                        activityContext.unregisterReceiver(broadcastReceiver);
                    } catch (IllegalArgumentException e) {}
                    activityContext.stopService(serviceIntent);
                }
                createLooperTask();
            });
        });
*/
        createLooperTask();
    }

    private void createLooperTask() {
        if (locationManager == null) {
            if (debug) {
                LOG.log(Level.INFO, "There is no LocationManager. Can't start LooperTask");
            }
            return;
        }
        
        if (debug) {
            LOG.log(Level.INFO, String.format("Creating LooperTask to request location updates every %d milliseconds or %f meters.", parameters.getTimeInterval(), parameters.getDistanceFilter()));
        }
        
        looperTask = new AndroidLooperTask() {

            @Override
            public void execute() {
                locationManager.requestLocationUpdates(parameters.getTimeInterval(), parameters.getDistanceFilter(), 
                        getLocationProvider(), DalvikPositionService.this, this.getLooper());
            }
        };

        Thread thread = new Thread(looperTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void quitLooperTask() {
        if (debug) {
            LOG.log(Level.INFO, "Cancelling LooperTask");
        }
        if (looperTask != null) {
            looperTask.quit();
            looperTask = null;
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void updatePosition(Location location) {
        updatePosition(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    private void updatePosition(double latitude, double longitude, double altitude) {
        Log.v(TAG, "[DALVIKPOSITION] update to "+latitude+", " + longitude+", "+altitude);
        double altitudeMeanSeaLevel = altitude;
        try {
            double offset = gh.heightOffset(longitude, latitude, altitude);
            altitudeMeanSeaLevel = altitude - offset;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error getting altitude mean sea level", ex);
        }
System.err.println("[DALVIKPOSITION] update to "+latitude+", " + longitude+", "+altitude);
        // Position newPosition = new Position(latitude, longitude, altitudeMeanSeaLevel);
        // Platform.runLater(() -> positionProperty.set(newPosition));
    }

    private Criteria getLocationProvider() {
        final Parameters.Accuracy accuracy = parameters.getAccuracy();
        final Criteria criteria = new Criteria();
        switch (accuracy) {
            case HIGHEST:
            case HIGH:
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);
                break;
            case MEDIUM:
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setBearingAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_MEDIUM);
                criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
                break;
            case LOW:
            case LOWEST:
            default:
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setHorizontalAccuracy(Criteria.ACCURACY_LOW);
                criteria.setVerticalAccuracy(Criteria.ACCURACY_LOW);
                criteria.setBearingAccuracy(Criteria.ACCURACY_LOW);
                criteria.setSpeedAccuracy(Criteria.ACCURACY_LOW);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
        }
        return criteria;
    }
    
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double latitude = Double.valueOf(intent.getStringExtra(LATITUDE));
            Double longitude = Double.valueOf(intent.getStringExtra(LONGITUDE));
            Double altitude = Double.valueOf(intent.getStringExtra(ALTITUDE));
            updatePosition(latitude, longitude, altitude);
        }
    };
}
