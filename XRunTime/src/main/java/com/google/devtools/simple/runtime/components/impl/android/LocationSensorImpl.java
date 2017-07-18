/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.simple.runtime.components.impl.android;

import com.google.devtools.simple.runtime.Log;
import com.google.devtools.simple.runtime.android.ApplicationImpl;
import com.google.devtools.simple.runtime.android.ApplicationImpl.OnResumeListener;
import com.google.devtools.simple.runtime.android.ApplicationImpl.OnStopListener;
import com.google.devtools.simple.runtime.components.ComponentContainer;
import com.google.devtools.simple.runtime.components.LocationSensor;
import com.google.devtools.simple.runtime.components.impl.ComponentImpl;
import com.google.devtools.simple.runtime.events.EventDispatcher;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;

/**
 * Sensor that can provide information on longitude, latitude, and altitude.
 *
 * @author Ellen Spertus
 */
public final class LocationSensorImpl extends ComponentImpl
    implements LocationSensor, OnStopListener, OnResumeListener {

  /**
   * Class that listens for changes in location, raises appropriate events,
   * and provides properties.
   *
   * @author spertus@google.com (Ellen Spertus)
   */
  private class LocationSensorListener implements LocationListener {
    /**
     * {@inheritDoc}
     * 
     * This sets fields longitude, latitude, altitude, hasLocationData, and
     * hasAltitude, then calls LocationSensor.LocationChanged(), all in the
     * enclosing class LocationSensor.
     */
    @Override
    public void onLocationChanged(Location location) {
      lastLocation = location;
      longitude = location.getLongitude();
      latitude = location.getLatitude();
      // If the current location doesn't have altitude information, the prior
      // altitude reading is retained.
      if (location.hasAltitude()) {
        hasAltitude = true;
        altitude = location.getAltitude();
      }
      hasLocationData = true;
      Changed(latitude, longitude, altitude);
    }

    @Override
    public void onProviderDisabled(String provider) {
      stopListening();
      if (enabled) {
        tryToStartListening();
      }
    }

    @Override
    public void onProviderEnabled(String provider) {
      tryToStartListening();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
      switch (status) {
        // Ignore TEMPORARILY_UNAVAILABLE, because service usually returns quickly.
        // case LocationProvider.TEMPORARILY_UNAVAILABLE:
        case LocationProvider.OUT_OF_SERVICE:
          // If the provider we were listening to is no longer available,
          // find another.
          if (provider.equals(providerName)) {
            tryToStartListening();
          }
          break;

        case LocationProvider.AVAILABLE:
          // If another provider becomes available and is one we hadn't known
          // about see if it is better than the one we're currently using.
          if (!provider.equals(providerName) && !allProviders.contains(provider)) {
            tryToStartListening();
          }
          break;
      }
    }
  }

  // Constant returned by Longitude(), Latitude(), and Altitude() if no value could be obtained for
  // them. The client can find this out directly by calling HasLongitudeLatitude() or HasAltitude().
  private static final int UNKNOWN_VALUE = 0;

  // Minimum time in milliseconds between location checks. The documentation for
  // android.location.LocationManager.requestLocationUpdates() does not recommend using a location
  // lower than 60,000 (60 seconds) because of power consumption.
  private static final long MIN_TIME_INTERVAL = 60000;

  // Minimum distance in meters to be reported
  private static final long MIN_DISTANCE_INTERVAL = 5;  // 5 meters

  // These variables contain information related to the LocationProvider.
  private final Criteria locationCriteria;
  private final LocationManager locationManager;

  // These variables are changed together in stopListening() or tryToStartListening().
  private final LocationSensorListener locationSensorListener;
  private List<String> allProviders;  // all providers available when we chose providerName
  private String providerName;
  private LocationProvider locationProvider;
  private boolean listening;

  // These location-related values are set in MyLocationListener.onLocationChanged().
  private double longitude = UNKNOWN_VALUE;
  private double latitude = UNKNOWN_VALUE;
  private double altitude = UNKNOWN_VALUE;
  private Location lastLocation;
  private boolean hasLocationData;
  private boolean hasAltitude;

  // This is used in reverse geocoding.
  private final Geocoder geocoder;

  // This is set to true after Initialize() runs.
  boolean initialized;

  // Backing for properties
  private boolean enabled;

  /**
   * Creates a new LocationSensor component.
   *
   * @param container  container which will hold the component (must not be
   *                   {@code null}, for non-visible component, like this one
   *                   must be the form)
   */
  public LocationSensorImpl(ComponentContainer container) {
    super(container);

    // Set up stop/resume listeners
    ApplicationImpl context = ApplicationImpl.getContext();
    context.addOnResumeListener(this);
    context.addOnStopListener(this);

    // Initialize location-related fields
    geocoder = new Geocoder(context);
    locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    locationCriteria = new Criteria();
    locationSensorListener = new LocationSensorListener();
  }

  // Events

  @Override
  public void Initialize() {
    EventDispatcher.dispatchEvent(this, "Initialize");
    initialized = true;

    // To ensure all listeners are registered
    Enabled(Enabled());
  }

  @Override
  public void Changed(double latitude, double longitude, double altitude) {
    if (enabled) {
      EventDispatcher.dispatchEvent(this, "Changed", latitude, longitude, altitude);
    }
  }

  // Properties

  @Override
  public boolean IsAvailable() {
    return hasLocationData;
  }

  @Override
  public boolean HasAltitude() {
    return hasAltitude;
  }

  @Override
  public boolean HasAccuracy() {
    return Accuracy() != UNKNOWN_VALUE;
  }

  @Override
  public double Longitude() {
    return longitude;
  }

  @Override
  public double Latitude() {
      return latitude;
  }

  @Override
  public double Altitude() {
    return altitude;
  }

  @Override
  public double Accuracy() {
    if (lastLocation != null && lastLocation.hasAccuracy()) {
      return lastLocation.getAccuracy();
    } else if (locationProvider != null) {
      return locationProvider.getAccuracy();
    } else {
      return UNKNOWN_VALUE;
    }
  }

  @Override
  public boolean Enabled() {
    return enabled;
  }

  @Override
  public void Enabled(boolean enabled) {
    this.enabled = enabled;
    if (initialized) {
      if (!enabled) {
        stopListening();
      } else {
        tryToStartListening();
      }
    }
  }

  @Override
  public String CurrentAddress() {
    if (hasLocationData &&
        latitude <= 90 && latitude >= -90 &&
        longitude <= 180 || longitude >= -180) {
      try {
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
        if (addresses != null && addresses.size() == 1) {
          Address address = addresses.get(0);
          if (address != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
              sb.append(address.getAddressLine(i));
              sb.append("\n");
            }
            return sb.toString();
          }
        }
      } catch (IOException e) {
        Log.Warning(Log.MODULE_NAME_RTL, "Exception thrown by getFromLocation() " + e.getMessage());
      }
    }

    return "";
  }

  // Methods to stop and start listening to LocationProviders

  /**
   * This unregisters {@link #myLocationListener} as a listener to location
   * updates.  It is safe to call this even if no listener had been registered,
   * in which case it has no effect.  This also sets the values of
   * {@link #providerName}, {@link #locationProvider}, and {@link #allProviders}
   * to {@code null} and sets {@link #listening} to {@code false}.
   */
  private void stopListening() {
    if (listening) {
      locationManager.removeUpdates(locationSensorListener);
      providerName = null;
      locationProvider = null;
      allProviders = null;
      listening = false;
    }
  }

  /**
   * This tries to find the best available location provider and to
   * register {@link #myLocationListener} as a listener.
   * <p>
   * This method is called in two situations, to choose a provider when either
   * (1) none is currently selected (such as on start/restart or when one
   * becomes available for the first time), or (2) there is a problem with the
   * provider currently in use, such as its having been reported no longer
   * available via onStatusChanged().  To prevent the same poorly
   * performing provider from being selected, this does not select the current
   * provider unless it is the only one.
   *
   * If this can find and listen to a provider, the private field
   * {@link #listening} is set to {@code true}, and the variables
   * {@link #providerName}, {@link #locationProvider}, and {@link
   * #allProviders}  are given the appropriate values.  Otherwise,
   * {@link #available} is set to {@code false}, and {@link #providerName},
   * {@link #locationProvider}, and {@link #allProviders} are set to
   * {@code null}.
   */
  private void tryToStartListening() {
    // We may be able to find a provider better than the previous one,
    // if there was one, so stop listening to it.
    String lastProviderName = providerName;
    stopListening();

    // Find the best current provider.
    providerName = locationManager.getBestProvider(locationCriteria, true);
    if (providerName == null || providerName.length() == 0) {
      // No providers are available
      return;
    }

    allProviders = locationManager.getProviders(true);
    // Don't reuse the last provider unless there is no alternative.
    if (providerName.equals(lastProviderName)) {
      if (allProviders == null || allProviders.size() == 0) {
        // No providers are enabled.  This could occur if a provider was
        // disabled between the above calls to getBestProvider() and
        // getProviders().
        providerName = null;
        return;
      }
      // If a different provider can be found, prefer that.
      // Otherwise, just keep using the old one.
      for (String provider : allProviders) {
        if (!provider.equals(lastProviderName)) {
          providerName = provider;
          break;
        }
      }
    }

    // Find the associated LocationProvider.
    locationProvider = locationManager.getProvider(providerName);
    if (locationProvider == null) {
      providerName = null;
      allProviders = null;
      return;
    }

    // Indicate that we are now listening.
    locationManager.requestLocationUpdates(providerName, MIN_TIME_INTERVAL, MIN_DISTANCE_INTERVAL,
        locationSensorListener);
    listening = true;
  }

  // OnResumeListener implementation

  @Override
  public void onResume() {
    if (enabled) {
      tryToStartListening();
    }
  }

  // OnStopListener implementation

  @Override
  public void onStop() {
    stopListening();
  }
}
