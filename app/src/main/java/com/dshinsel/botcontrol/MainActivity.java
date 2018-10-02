package com.dshinsel.botcontrol;
// This code uses the amarino library from:
//   http://www.amarino-toolkit.net/index.php/docs.html
// and samples from various sources including:
//   http://cloud101.eu/blog/2012/03/10/android-introduction-to-a-simple-calculator/

  
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
// android:theme="@style/AppTheme" 

public class MainActivity extends Activity implements SensorEventListener {

    // TODO: change the address to match your Bluetooth module
	
	// LOKI
	//private static final String DEVICE_ADDRESS = "00:06:66:4F:90:CD"; // "Firefly 90CD" - BlueSMirf LOKI
	// SHELDON
	private static final String DEVICE_ADDRESS = "00:06:66:4B:E3:EF"; // "Firefly E3EF" - BlueSMiRF Sheldon

	// SPARE MODULES
	// Note: "New" BlueSMiRF modules seem to not always disconnect, so they get hung up...
	//private static final String DEVICE_ADDRESS = "00:06:66:73:E3:EA"; // New BlueSMirf Spare
	//private static final String DEVICE_ADDRESS = "00:06:66:73:E8:DD"; // New BlueSmirf Spare
	//private static final String DEVICE_ADDRESS = "00:A0:96:12:C0:DB"; // BlueSMirf #2 - ROBO NOVA
	//private static final String DEVICE_ADDRESS = "00:12:05:24:95:55";  // EZ Robot module


	Context MyContext = this;
    private TextView textView;

	// Bluetooth Connection stuff
	public enum btConnectionState {
		 DISCONNECTED, CONNECTING, CONNECTED, DISCONNECTING  //; is optional
		}
    private btConnectionState ConnectionState = btConnectionState.DISCONNECTED;
	private btConnectReceiver ConnectReceiver = new btConnectReceiver();
	private btDisconnectReceiver DisconnectReceiver = new btDisconnectReceiver();
	private btConnectFailedReceiver ConnectFailedReceiver = new btConnectFailedReceiver();
	private btPairingRequiredReceiver PairingRequiredReceiver = new btPairingRequiredReceiver();
	private btDataReceiver DataReceiver = new btDataReceiver();

	// Accelerometer and Compass stuff
	private SensorManager sensorManager;
	private long lastUpdateTime;
	private boolean AccelerometerEnabled = false;
	private boolean SensorEventRegistered = false;
	private float[] mMagneticValues = null;
	private int LastAzimuth = 0, LastPitch = 0, LastRoll = 0;
	//PowerManager.WakeLock BotWakeLock;
	
  
    ///////////////////////////////////////////////////////////////////
    // onCreate - Called when the activity is first created
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
        textView = (TextView) findViewById(R.id.entry);
        //resultView = (EditText) findViewById(R.id.result);
        //  final Button button0 = (Button) findViewById(R.id.button0);

        // Create a wake lock to keep the CPU (and accelerometer) running when controlling the robot
    	// PARTIAL_WAKE_LOCK allows screen to turn off, but keep CPU running (saves power)
    	//PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
    	//BotWakeLock = pm.newWakeLock(PowerManager. PARTIAL_WAKE_LOCK, "Bot Control");    		
    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdateTime = System.currentTimeMillis();
	}	
	
	
    ///////////////////////////////////////////////////////////////////
    private void updateStatus(String strStatus) {
        textView.setText(strStatus);
    }

    ///////////////////////////////////////////////////////////////////
    private void enableAccelerometer() {
		// turn on the accelerometer - register for events
	    if( !SensorEventRegistered ) {
		    sensorManager.registerListener(this,
		        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		        SensorManager.SENSOR_DELAY_NORMAL); // try SENSOR_DELAY_UI
		    sensorManager.registerListener(this,
			        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
			        SensorManager.SENSOR_DELAY_NORMAL);
		    SensorEventRegistered = true;
    		Log.d("BOT DEBUG", "Accelerometer enabled");

    		
	    }
		AccelerometerEnabled = true;
   		Button myButton  = (Button) findViewById(R.id.buttonAccelerometerEnable);
        myButton.setText("TILT");  // Indicate that Accelerometer is enabled
    }
   

    ///////////////////////////////////////////////////////////////////
    private void disableAccelerometer() {
		// turn off the accelerometer - unregister for events
	    if( SensorEventRegistered ) {
		    sensorManager.unregisterListener(this);
		    SensorEventRegistered = false;	    	
    		Log.d("BOT DEBUG", "Accelerometer disabled");
	    }
		AccelerometerEnabled = false;
   		Button myButton  = (Button) findViewById(R.id.buttonAccelerometerEnable);
        myButton.setText("tilt");
        // Send one last message to the Arduino, clearing out the last accelerometer event
        int iValues[] = {0,0,0};
        Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'A', iValues);    // Accelerometer
        //if( BotWakeLock.isHeld() ) {
        //    BotWakeLock.release();
        //}
    }

    ///////////////////////////////////////////////////////////////////
    public void handleButtonEvent(View v)
    {
    	  
    	
    	switch( v.getId() ) {
    	
    	case R.id.button1:
    		Log.d("ButtonHandler", "Button 1 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 1); // eXecute Command number            
            }   		
    		break;
    		
    		
    	case R.id.button2:
    		Log.d("ButtonHandler", "Button 2 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 2);            
            }
    		break;    		

    	case R.id.button3:
    		Log.d("ButtonHandler", "Button 3 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 3);            
            }
    		break;    		
    	case R.id.button4:
    		Log.d("ButtonHandler", "Button 4 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 4);            
            }
    		break;    		
    		
    	case R.id.button5:
    		Log.d("ButtonHandler", "Button 5 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 5);            
            }
    		break;    		
    		
    	case R.id.button6:
    		Log.d("ButtonHandler", "Button 6 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 6);            
            }
    		break;    		
    		
    	case R.id.button7:
    		Log.d("ButtonHandler", "Button 7 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 7);            
            }
    		break;    		
    		
    	case R.id.button8:
    		Log.d("ButtonHandler", "Button 8 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 8);            
            }
    		break;    		
    		
    	case R.id.button9:
    		Log.d("ButtonHandler", "Button 9 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 9);            
            }
    		break;    		
    		
    	case R.id.button10:
    		Log.d("ButtonHandler", "Button 10 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 10);            
            }
    		break;    		
    		
    	case R.id.button11:
    		Log.d("ButtonHandler", "Button 11 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 11);            
            }
    		break;    		
    		
    	case R.id.button12:
    		Log.d("ButtonHandler", "Button 12 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 12);            
            }
    		break;    		
    		
    	case R.id.button13:
    		Log.d("ButtonHandler", "Button 13 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 13);            
            }
    		break;    		
    		
    	case R.id.button14:
    		Log.d("ButtonHandler", "Button 14 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 14);            
            }
    		break;    		
    		
    	case R.id.button15:
    		Log.d("ButtonHandler", "Button 15 pressed");
            if(btConnectionState.CONNECTED == ConnectionState) {
                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'X', 15);            
            }
    		break; 
    		
    	case R.id.buttonAccelerometerEnable:
    		Log.d("ButtonHandler", "Button Accelerometer pressed");
    		if( !AccelerometerEnabled ) {
    			if(btConnectionState.CONNECTED == ConnectionState) {
                	Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'E', 1); // EnableAccelerometer message to Arduino        
    				enableAccelerometer();
    			}
    		}
    		else {
    			disableAccelerometer();
    			if(btConnectionState.CONNECTED == ConnectionState) {
                	Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'E', 0); // EnableAccelerometer message to Arduino        
    			}
    		}
    		break;      		
    		
    	case R.id.buttonDisconnect:
    		Log.d("ButtonHandler", "Button Disconnect pressed");
            if(btConnectionState.CONNECTED != ConnectionState) {
                Log.d("BOT DEBUG", "Disconnect:  BT Not connected");
    			updateStatus("Disconnect:  BT Not connected");
            }
			// Disconnect Bluetooth
			ConnectionState = btConnectionState.DISCONNECTING;
			Log.d("BOT DEBUG", "Calling Amarino Disconnect: ");
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'C', 0);  // Send disconnection message
			Amarino.disconnect(this, DEVICE_ADDRESS);
			Log.d("BOT DEBUG", "DONE Calling Amarino Disconnect ");
    		break;
    		
    		
    	case R.id.buttonConnect:
       		Log.d("ButtonHandler", "Button Connect pressed");  		
            if(btConnectionState.CONNECTED == ConnectionState) {
                Log.d("BOT DEBUG", "Connect: Already Connected");        
    			updateStatus("Connect: BT Already Connected");
            }
            //else if(btConnectionState.DISCONNECTED != ConnectionState) {
            //    Log.d("BOT DEBUG", "Connect: BT Stack Busy, Try Again");
    		//	updateStatus("Connect: BT Stack Busy, Try Again");
            //}
            else
            {
	            // Toggle button text?
	       		// Button myButton  = (Button) findViewById(R.id.buttonConnect);
	       		
	            // Connect to Bluetooth device
	            ConnectionState = btConnectionState.CONNECTING;
	            updateStatus("Connecting...");
	            Log.d("BOT DEBUG", "Calling Amarino Connect: ");
	            Amarino.connect(this, DEVICE_ADDRESS);
	            //myButton.setText("Disconnect");
	            Log.d("BOT DEBUG", "DONE Calling Amarino Connect ");
            }
     		break;
    		
    	default:
    		Log.d("ButtonHandler", "ERROR! Unhandled Button!");    		
    		break;
    	}
  
    }
    
    ///////////////////////////////////////////////////////////////////
    // ACCELEROMETER AND COMPASS
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] mAccelerometerValues = null;
		float mAzimuth=0, mPitch=0, mRoll=0;
		int MATRIX_SIZE = 16;
		int iAzmimuth=0, iPitch=0, iRoll = 0;
		int iValues[] = {0,0,0};
	    float Ax=0,Ay=0,Az=0;
	
		switch (event.sensor.getType()) {
		
			case Sensor.TYPE_MAGNETIC_FIELD:
				mMagneticValues = event.values.clone(); // Just save for later
				break;
				
			case Sensor.TYPE_ACCELEROMETER:
				mAccelerometerValues = event.values.clone(); // causes new calculation
			    Ax = event.values[0];
			    Ay = event.values[1];
			    Az = event.values[2];
				break;
		}
		
		if (mMagneticValues != null && mAccelerometerValues != null) {
			float[] R = new float[MATRIX_SIZE];
			SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticValues);
			float[] orientation = new float[3];
			SensorManager.getOrientation(R, orientation);
			mAzimuth = orientation[0];
			mPitch = orientation[1];
			mRoll = orientation[2];
		    
			// Convert to degrees
			iAzmimuth = (int)(mAzimuth * 100.0);	// compass direction in Degrees.  0=Magnetic North (not true north)
			iPitch = (int)(mPitch * -60.0);		// Pitch in Degrees.  Positive values = top of phone up
			iRoll = (int)(mRoll * 56.0);			// Roll in Degrees.   Positive values = tilt right

			iValues[0] = iAzmimuth;
			iValues[1] = iPitch;
			iValues[2] = iRoll;
		    long currentTime = System.currentTimeMillis();
		    long timeSinceLastUpdate = currentTime - lastUpdateTime;
            if( (btConnectionState.CONNECTED == ConnectionState) &&  (timeSinceLastUpdate > 100) ) { // don't flood with updates
				int DeltaAzmiuth = Math.abs(iAzmimuth - LastAzimuth);
				int DeltaPitch = Math.abs(iPitch - LastPitch);
				int DeltaRoll = Math.abs(iRoll - LastRoll);
			    boolean HeartbeatNeeded = false;

				// See if phone is flat on a surface (if so, don't send updates, so robot will do it's own thing)
				if( (iPitch < -5) || (iPitch > 8) || (iRoll < -10) || (iRoll > 10) || (DeltaPitch > 2) || (DeltaRoll > 2) )	{ // degrees
	
					// Not flat on the desk
	            	
			    
				    // update every so often as a heart beat, even if no changes detected, unless the phone is flat on a desk
				    if( timeSinceLastUpdate > 500) { // Milliseconds
				    	HeartbeatNeeded = true;
				    }
			    
					// Threshold test
					if( (DeltaAzmiuth > 5) || (DeltaPitch > 1) || (DeltaRoll > 1) ||  HeartbeatNeeded ) { // degrees
						LastAzimuth = iAzmimuth;
						LastPitch = iPitch;
						LastRoll = iRoll;
		            	
		                // Send Accelerometer data to the Arduino
						Log.d("BOT DEBUG", "SENDING Az,Pitch,Roll: " + iAzmimuth + "\t "+ iPitch + "\t "+ iRoll);				
		                Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'A', iValues);
				        lastUpdateTime = currentTime;
		            }
	            }				
					
				// Check for Shake (Not currently used)
				    /*
			    float accelationSquareRoot = (Ax * Ax + Ay * Ay + Az * Az)
				        / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
			    if (accelationSquareRoot >= 3) //
			    {
			      if (actualTime - lastUpdate < 200) {
			        return;
			      }
			      Log.d("ACCELEROMETER", "SHAKE = " + accelationSquareRoot);  
			    }
			    */
            }    
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Required funciton, but not used
	}	    

	
    ///////////////////////////////////////////////////////////////////
	// onResume - Do all startup work, including
    // Register event listeners for Accelerometer and Compass
    // Connect bluetooth to robot
	@Override
	protected void onResume() {
	    super.onResume();
	    // register this class as a listener for the orientation and
	    // accelerometer sensors
        Log.d("BOT DEBUG", "OnResume called");

        ConnectionState = btConnectionState.DISCONNECTED;
        updateStatus("Disconnected");

	    if( AccelerometerEnabled ) {
		    sensorManager.registerListener(this,
		        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		        SensorManager.SENSOR_DELAY_NORMAL);
		    sensorManager.registerListener(this,
			        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
			        SensorManager.SENSOR_DELAY_NORMAL);
		    SensorEventRegistered = true;
    		Log.d("BOT DEBUG", "Accelerometer enabled");
	    }

        Log.d("BOT DEBUG", "Registering Broadcast Receivers");
        // in order to receive broadcasted intents we need to register our receivers
        registerReceiver(ConnectReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));
        registerReceiver(DisconnectReceiver, new IntentFilter(AmarinoIntent.ACTION_DISCONNECTED));
        registerReceiver(ConnectFailedReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTION_FAILED));
        registerReceiver(PairingRequiredReceiver, new IntentFilter(AmarinoIntent.ACTION_PAIRING_REQUESTED));
        registerReceiver(DataReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));

        if(btConnectionState.CONNECTED != ConnectionState) {
            // Auto-Connect to Bluetooth device when app starts or resumes
            ConnectionState = btConnectionState.CONNECTING;
            updateStatus("Connecting...");
            Log.d("BOT DEBUG", "Calling Amarino Connect: ");
            Amarino.connect(this, DEVICE_ADDRESS);
            //myButton.setText("Disconnect");
            Log.d("BOT DEBUG", "DONE Calling Amarino Connect ");
        }



    }
	
    ///////////////////////////////////////////////////////////////////
    // onPause - Do all shutdown work, including
	// UnRegister event listeners for Accelerometer and Compass onPause
    // Disconnect bluetooth from Robot
	@Override
	protected void onPause() {

        Log.d("BOT DEBUG", "OnPause called");

	    // unregister sensor listener
	    super.onPause();
	    if( SensorEventRegistered ) {
		    sensorManager.unregisterListener(this);
		    SensorEventRegistered = false;	    	
    		Log.d("BOT DEBUG", "Accelerometer disabled");
	    }

        // stop Amarino's background service when exiting or hidden
        Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'C', 0);  // Send disconnection message
        Amarino.disconnect(this, DEVICE_ADDRESS);
        // don't forget to unregister all registered receivers
        unregisterReceiver(ConnectReceiver);
        unregisterReceiver(DisconnectReceiver);
        unregisterReceiver(ConnectFailedReceiver);
        unregisterReceiver(PairingRequiredReceiver);
        unregisterReceiver(DataReceiver);
        ConnectionState = btConnectionState.DISCONNECTED; // can't wait for event during Stop
        disableAccelerometer();	// Disable the accelerometer whenever we lose Bluetooth connection


	}
      
	    
	    
    ///////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
    ///////////////////////////////////////////////////////////////////
	// Register broadcast event receivers for Bluetooth onStart
	@Override
	protected void onStart() {
		super.onStart();
        // Note - all startup stuff moved to onResume

		/*
		// load last state, if desired in the future
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        red = prefs.getInt("red", 0);
        green = prefs.getInt("green", 0);
        blue = prefs.getInt("blue", 0);
		*/
		
	}

	
    ///////////////////////////////////////////////////////////////////
	// Unregister broadcast event receivers for Bluetooth onStop
	@Override
	protected void onStop() {
		super.onStop();
        // Note - all shutdown stuff moved to onPause

		// save state
		/*
		PreferenceManager.getDefaultSharedPreferences(this)
			.edit()
				.putInt("red", red)
				.putInt("green", green)
				.putInt("blue", blue)
			.commit();
		*/

	}

    ///////////////////////////////////////////////////////////////////
	// bt_xx__Receivers are responsible for catching broadcasted Bluetooth events from the Amarino service
	
	// Connection event receivers
	public class btConnectReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BT BROADCAST RECEIVER", "CONNECT EVENT RECEIVED");
			// Send message to the Arduino, so it knows we are connected:
			Amarino.sendDataToArduino(MyContext,  DEVICE_ADDRESS,  'C',  1);
			ConnectionState = btConnectionState.CONNECTED;
			updateStatus("Connected");
		}
	}

	public class btDisconnectReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BT BROADCAST RECEIVER", "DISCONNECT EVENT RECEIVED");
			ConnectionState = btConnectionState.DISCONNECTED;
			updateStatus("Disconnected");
			disableAccelerometer();	// Disable the accelerometer whenever we lose Bluetooth connection
		}
	}

	public class btConnectFailedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BT BROADCAST RECEIVER", "CONNECT FAILED EVENT RECEIVED");
			ConnectionState = btConnectionState.DISCONNECTED;
			updateStatus("Connect Failed!");
			disableAccelerometer();	// Disable the accelerometer whenever we lose Bluetooth connection
		}
	}
	
	public class btPairingRequiredReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("BT BROADCAST RECEIVER", "PAIRING REQUIRED EVENT RECEIVED");
			ConnectionState = btConnectionState.DISCONNECTED;
			updateStatus("Pairing Required!");
			disableAccelerometer();	// Disable the accelerometer whenever we lose Bluetooth connection
		}
	}
	

    ///////////////////////////////////////////////////////////////////
	// Bluetooth DATA receiver - receives data from the Arduino device via the Amarino service
	public class btDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			
			// Example of how to get the bluetooth device address from which the data was sent:
			//final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			
			// the type of data which is added to the intent
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			// we only expect String data though, but it is better to check if really string was sent
			// Amarino is supposed to support different data types (some day?), but now comes always as string and
			// you have to parse the data to the type you have sent from Arduino, as shown below
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				
				if (data != null){
					
					// Display the data received in the status area
					updateStatus(data); 
					
					/*
					try {
						// If we know that our string value is an int number we can parse it to an integer
						final int sensorReading = Integer.parseInt(data);

					} 
					catch (NumberFormatException e) { } // data was not an integer
					*/
				}
			}
		}
	}	
	
	
	
}
