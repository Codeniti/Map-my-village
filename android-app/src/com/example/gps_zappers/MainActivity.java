package com.example.gps_zappers;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONObject;

import com.example.gps_zappers.RestClient.RequestMethod;

import android.util.Log;

public class MainActivity extends Activity {
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Context context;
	TextView txtLat;
	Button	 startTrackingButton;
	Button	 stopTrackingButton;
	String lat;
	String provider;
	protected String latitude, longitude;
	protected boolean gps_enabled, network_enabled;
	JSONObject 		  jsonObj;
	protected boolean startSendingLatLongToServer;
	HttpClient	      httpClient;
	RestClient 	      httpRestClient;
	String 			  httpResponse;
	String 			  httpResponseLat;

	boolean			  stopThread;
	boolean			  requestToSendAuthHttp;
	boolean stopPerThread;
	boolean requestToSendLatHttp;
	
	Location currLocation;
	boolean	 firstTimeSendThread = true;
	boolean	 firstTimeSendAuthThread;

	Thread setupThread = new Thread(){
        public void run(){
        	
        	while(false == stopThread)
        	{
        		if(true == requestToSendAuthHttp)
        		{
        			// send HTTP request here
					// Send Auth request to server here
					{
						Log.d("Anoop", "Connecting to HTTP server");
						
						// TODO: Add HTTP URL here
						try
						{
							httpRestClient = new RestClient("http://192.241.136.52:5000/project/create");
						}
						catch (Exception e) {
							e.printStackTrace();
							Log.d("Anoop", "Unable to connect to HTTP server");
							return;
						}
						
						Log.d("Anoop", "httpRestClient object " + httpRestClient);
						
						httpRestClient.AddParam("name", "Anoop");
	
						try {
							httpRestClient.Execute(RequestMethod.POST);
						} catch (Exception e) {
							
							Log.d("Anoop", "Fatal Unable to send HTTP POST Request " + httpRestClient);
							
							e.printStackTrace();
						}

						Log.d("Anoop", "Done... with connection to HTTP server");
						
					}
	
					// Obtain ID from the server
					{
						httpResponse = httpRestClient.getResponse();
						
						Log.d("Anoop", "Response from HTTP server " + httpResponse);
					}

					requestToSendAuthHttp = false;
					
        		}
        		
            	try {
                    sleep(100); 

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Anoop", "Sleeping for 100 ms ");
                }
        	};
            
        }
    };

    Thread periodicSendThread = new Thread(){
        public void run(){
        	
			while(false == stopPerThread)
        	{
				if(true == requestToSendLatHttp)
        		{
        			// send HTTP request here
					// Send Auth request to server here
					{
						Log.d("Anoop", "Sending Latlong to HTTP server");
						
						httpRestClient = new RestClient("http://192.241.136.52:5000/location/create");
						
						if(null == httpRestClient)
						{
//							txtLat.setText("Error in connecting to server");
//							Log.d("Anoop", "Error in connecting to server");
//							return;
						}
						
						Log.d("Anoop", "Adding parameters to the HTTP Request");
						
						httpRestClient.AddParam("project_id", httpResponse);
						httpRestClient.AddParam("latitude", Double.toString(currLocation.getLatitude()));
						httpRestClient.AddParam("longitude", Double.toString(currLocation.getLongitude()));

						try {
//							txtLat.setText("Sending HTTP request");
							Log.d("Anoop", "Sending HTTP request");
							httpRestClient.Execute(RequestMethod.POST);
						} catch (Exception e) {
							e.printStackTrace();
						}
	        			
					}
	
					// Obtain junk from the server
					{
						httpResponseLat = httpRestClient.getResponse();
						Log.d("Anoop", "Response from HTTP server " + httpResponseLat);
					}

					requestToSendLatHttp = false;
        		}
        		
            	try {
                    sleep(100); 

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("Anoop", "Sleeping for 100 ms ");
                }
        	};
            
        }
    };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		txtLat = (TextView) findViewById(R.id.textview1);
		startTrackingButton = (Button) findViewById(R.id.Start_tracking);
//		stopTrackingButton  = (Button) findViewById(R.id.Stop_tracking);

		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Log.d("Anoop","Obtaining location Manager");
		System.out.println("Coming here ?");

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				System.out.println("On location changed ?");
				// Called when a new location is found by the network location provider.
				// makeUseOfNewLocation(location);

				currLocation = location;
				
				Log.d("Anoop","Latitude " +location.getLatitude());
				Log.d("Anoop", "Longitude " +location.getLongitude());
				System.out.println( "Latitude " +location.getLatitude());

				txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:"
						+ location.getLongitude());

				if(true == startSendingLatLongToServer)
				{
					txtLat.setText("Creating HTTP request");
					Log.d("Anoop", "Creating HTTP request");

					if(true == firstTimeSendThread)
					{
						periodicSendThread.start();
						
						firstTimeSendThread = false;
					}
					
					// Flag indicates to periodic thread to Send LatLong Details
					requestToSendLatHttp = true;
					
					txtLat.setText("Done sending HTTP request");
					Log.d("Anoop", "Done sending HTTP request");

					txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:"
							+ location.getLongitude());
				}
				else
				{
					txtLat.setText("Stopped tracking");
					
					txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:"
							+ location.getLongitude());
				}
			}

			public void onStatusChanged(String provider, int status, Bundle extras) {
				System.out.println("On status changed ");
				txtLat.setText("On status changed ");
			}

			public void onProviderEnabled(String provider) {
				System.out.println("Enabled ");
				txtLat.setText("Enabled");
			}

			public void onProviderDisabled(String provider) {
				System.out.println("Disabled");
				txtLat.setText("Disabled");
			}
		};

		/* Start tracking button listener */
		startTrackingButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				/* Handle START mode of button */
				if(false == startSendingLatLongToServer)
				{
					Toast.makeText(getApplicationContext(), "Started tracking", Toast.LENGTH_SHORT).show();
					Log.d("Anoop", "Started Tracking now" );
	
					stopThread = false;
					requestToSendAuthHttp = true;
					
//					if(true == firstTimeSendAuthThread)
//					{
						setupThread.start();
					
					startSendingLatLongToServer = true;
					
					while(true == requestToSendAuthHttp)
					{
		            	try {
		                    Thread.sleep(100); 

		                } catch (Exception e) {
		                    e.printStackTrace();
		                    Log.d("Anoop", "Sleeping for 100 ms ");
		                }

					}
					
//					stopThread 		= true;
					stopPerThread	= false;
					startTrackingButton.setText("Stop Tracking");

				}
				
				/* Handle STOP mode of button */
				else
				{
					startTrackingButton.setText("Start Tracking");
					
					Toast.makeText(getApplicationContext(), "Stopped tracking", Toast.LENGTH_SHORT).show();
					Log.d("Anoop", "Stopped Tracking now" );
	
					stopPerThread				= true;
					startSendingLatLongToServer = false;

				}
			}
		});

		/* Stop tracking button listener */
//		stopTrackingButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//
//				Toast.makeText(getApplicationContext(), "Stopped tracking", Toast.LENGTH_SHORT).show();
//				Log.d("Anoop", "Stopped Tracking now" );
//
//				startSendingLatLongToServer = false;
//
//				// Send Stop request to server here
//			}
//		});


		//		  
		//			// Define a listener that responds to location updates
		//			LocationListener locationListener_gps = new LocationListener() {
		//			    public void onLocationChanged(Location location) {
		//			    	System.out.println("GPS >> On location changed ?");
		//			      // Called when a new location is found by the network location provider.
		//			     // makeUseOfNewLocation(location);
		//			    	
		//			    	Log.d("Anoop GPS >> ","Latitude "+location.getLatitude());
		//			    	Log.d("Anoop GPS >> ", "Longitude "+location.getLongitude());
		//			    	System.out.println( " GPS >> Latitude "+location.getLatitude());
		//			    }
		//
		//			    public void onStatusChanged(String provider, int status, Bundle extras) {System.out.println(" GPS >> On status changed ");}
		//
		//			    public void onProviderEnabled(String provider) {System.out.println(" GPS >> Enabled ");}
		//
		//			    public void onProviderDisabled(String provider) {System.out.println(" GPS >> Disabled");}
		//			  };
		//
		System.out.println("Coming here again ");
		// Register the listener with the Location Manager to receive location updates

		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		//         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener_gps);
	}

	//	@Override
	//	public void onLocationChanged(Location location) {
	//		txtLat = (TextView) findViewById(R.id.textview1);
	//		Log.d("Anoop", " Coming here ?");
	//		txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:"
	//				+ location.getLongitude());
	//	}
	//
	//	@Override
	//	public void onProviderDisabled(String provider) {
	//		Log.d("Latitude", "disable");
	//	}
	//
	//	@Override
	//	public void onProviderEnabled(String provider) {
	//		Log.d("Latitude", "enable");
	//	}

	//	@Override
	//	public void onStatusChanged(String provider, int status, Bundle extras) {
	//		Log.d("Latitude", "status");
	//	}
}