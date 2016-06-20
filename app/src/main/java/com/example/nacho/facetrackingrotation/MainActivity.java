package com.example.nacho.facetrackingrotation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

//import android.hardware.Camera.Face;
//import com.google.android.gms.vision.face.Face;


/// For the new Face Listener

//import android.media.FaceDetector.Face;
//import android.media.FaceDetector;


@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, SensorEventListener{

    // For the new face detector
    private static final String TAG = "PPSM";
    private CameraSource mCameraSource = null;
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int RC_HANDLE_GMS = 9001;

    float MinZoom = 1;


    //Variables that are relevant for the calibration-testing process
    int MaxSizeQueue = 22;
    int MaxSizeQueueZoom = 13;
    int ComonWait = 10;
    int BufferCalibrationZoom = ComonWait;
    int BufferCalibrationX = ComonWait;
    int BufferCalibrationY = ComonWait;
    int BufferCalibrationZ = ComonWait;
    int LimitNotFaceCountDown = 800;
    float NoiseFilterLimitTranslation = 0.05f;
    //float NoiseFilterLimitZoom = 0.05f;

    float PreviewSizeRate = 0.28f;
    float PaddingSizeRate = 0.02f;

    int BigRect = 23;
    int SmallRect = 61;

    int RestartPosition = 30;

    private TextView t1;
    private TextView t2;

    float BiggestRectangleFace;// = 62000;//700000f;
    float SmallestRectangleFace;// = 14000;//75000f;


    //It is a ctn taking into account the size of the screen in px and the density of px
    float CompensationCtn = 8.56f;
    float DensityCtn = 861120f;
    float StandardDensity = 100550f;
    float PixDensityCompensation;

    boolean DeviceIsPhone = true;

    // This matrices will be used to move and zoom image
    Matrix matrix = new Matrix();

    Handler mHandler;

    float CurrentScale;

    boolean Starting;
    boolean RestartFlag = true;

    private SensorManager mSensorManager = null;

    Rect Boundary;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;


    //Centre of the screen
    int CentreScreenXCoor;
    int CentreScreenYCoor;

    //private SurfaceView surfaceView2;
    //private SurfaceHolder surfaceHolder2;
	    /*private TextView t1;
	    private TextView t2;
	    private TextView t3;
	    private TextView t4;
	    private TextView t5;*/

    //Decimal format to make sensor's value less sensitive
    DecimalFormat d = new DecimalFormat("#.##");

    boolean SettingOrigin;

    //To filter out noise
    float PrevXRot;
    float PrevYRot;
    float PrevZRot;
    float PrevSize;
    float AuxSwap;

    private LayoutInflater layoutInflater = null;

    int CenterX;
    int CenterY;

    int NotFaceCountDown;

    //Limits in degrees
    //float HorizontalLimit = 17;
    //float VerticalLimit = 12;

    //For tablets
    float HorizontalLimit;// = 12;
    float VerticalLimit;// = 17;

    boolean ImproveCalibration = true;
    double CurrentDistance = 500;


    float ScalingRate;
    boolean FlagHeightRate;
    float Gap;

    //Queues
    LinkedList<Float> ListAverageAccMagOrientationX = new LinkedList<>();
    LinkedList<Float> ListAverageAccMagOrientationY = new LinkedList<>();
    LinkedList<Float> ListAverageAccMagOrientationZ = new LinkedList<>();
    LinkedList<Float> ListAverageSize = new LinkedList<>();

    Iterator ListX;
    Iterator ListY;
    Iterator ListZ;
    Iterator ListSize;

    float AverageAccMagOrientationX;
    float AverageAccMagOrientationY;
    float AverageAccMagOrientationZ;
    float AverageSize;

    float CurrentAccMagOrientationX;
    float CurrentAccMagOrientationY;
    float CurrentAccMagOrientationZ;



    private Camera camera;

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientation = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientationAbsolute = new float[3];

    // orientation angles from accel and magnet
    private float[] accMagOrientationOriginal = new float[3];


    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    //OriginalaccMagOrientation
    //StatisticsMatrix OriginalaccMagOrientation = new StatisticsMatrix();
    private float[] OriginalaccMagOrientation = new float[9];

    //Auxiliar RotationMatrix
    //StatisticsMatrix AuxiliarRotationMatrix = new StatisticsMatrix();
    private float[] AuxiliarRotationMatrix = new float[9];


    boolean preview = false;


    ImageView mview;

    Context context = this;

    //Dimension of the ImageView
    int ImageViewWidth;
    int ImageViewHeight;

    //Dimension of the Image
    int ImageWidth;
    int ImageHeight;

    int PreviewWidth;
    int PreviewHeight;

    RectF RectImage;
    RectF RectScreen;

    int Padding;

    //Rectangles that define the Imageview and the View

    public static final int TIME_CONSTANT = 30;
    private Timer fuseTimer = new Timer();



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Tryint to avoid the axis change for tablets
		    /*if ((getResources().getConfiguration().screenLayout &  Configuration.SCREENLAYOUT_SIZE_MASK) != Configuration.SCREENLAYOUT_SIZE_LARGE) {
		        //Toast.makeText(this, "Large screen",Toast.LENGTH_LONG).show();
		    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		    }*/

        //Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        CentreScreenXCoor = width/2;
        CentreScreenYCoor = height/2;

        System.out.println("*****  Size Screen "+(width*height));
        System.out.println("*****  Density x "+metrics.xdpi+" Density y"+metrics.ydpi);

        //For debugging porpose
        t1 = (TextView)findViewById(R.id.text1);
        t2 = (TextView)findViewById(R.id.text2);



        PixDensityCompensation = (CompensationCtn * (metrics.xdpi * metrics.ydpi)) /(width*height) ;

        System.out.println("Pixel density Rate:"+ PixDensityCompensation );

        BiggestRectangleFace = ((width*height) ) /(BigRect);// * (metrics.xdpi * metrics.ydpi / StandardDensity) );
        SmallestRectangleFace = ((width*height)) / (SmallRect);// * (metrics.xdpi * metrics.ydpi / StandardDensity) );

        BiggestRectangleFace *= DensityCtn / (width*height);
        SmallestRectangleFace *= DensityCtn / (width*height);
        //DensityCtn = 861.120f;
        //float PixDensityCompensation;

        System.out.println("Rectanles BigOne: " + BiggestRectangleFace + " SmallOne " + SmallestRectangleFace + " Preview Rate " + PreviewSizeRate);


        if(getDeviceDefaultOrientation() == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        /*else if(getDeviceDefaultOrientation() == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);*/

        /*int Rotation = getDeviceDefaultOrientation();

        System.out.println("Before Rotation is: "+Rotation);

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Rotation = getDeviceDefaultOrientation();

        System.out.println("Now Rotation is: " + Rotation);
        */

        /*
        //To check if the device is a phone or a tablet
        if (getResources().getBoolean(R.bool.isTablet)) {
            DeviceIsPhone = false;
            HorizontalLimit = 12;
            VerticalLimit = 17;
        } else {
            DeviceIsPhone = true;
            HorizontalLimit = 17;
            VerticalLimit = 12;
        }*/

        HorizontalLimit = 17;
        VerticalLimit = 12;
        DeviceIsPhone = true;

        System.out.println("DeviceIsPhone: "+DeviceIsPhone);

        setContentView(R.layout.activity_main);
        mview = (ImageView) findViewById(R.id.imageView);
        //mview.setOnTouchListener(this);

        CurrentScale = 2f;


        Starting = true;

        SettingOrigin = true;


        NotFaceCountDown = LimitNotFaceCountDown;

        //For the overlay button
        layoutInflater = LayoutInflater.from(getBaseContext());
        View viewControl = layoutInflater.inflate(R.layout.picture_control, null);

        LayoutParams layoutParamsControl
                = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);

        this.addContentView(viewControl, layoutParamsControl);



		      /*
		      t1 = (TextView)findViewById(R.id.text1);
			  t2 = (TextView)findViewById(R.id.text2);
			  t3 = (TextView)findViewById(R.id.text3);
			  t4 = (TextView)findViewById(R.id.text4);
			  t5 = (TextView)findViewById(R.id.text5);*/

        //Attributes of Decimal format
        d.setRoundingMode(RoundingMode.HALF_UP);
        d.setMaximumFractionDigits(2);
        d.setMinimumFractionDigits(2);

        //For the surface which will desplay the previo of the camera
        surfaceView = (SurfaceView)findViewById(R.id.camPreview);
        //android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(160, 100);
        //surfaceView.setLayoutParams(params);
        surfaceHolder = surfaceView.getHolder();
        //surfaceHolder.setFixedSize(100, 100);
        surfaceHolder.addCallback(this);

        PreviewWidth = (int) (PreviewSizeRate*width);
        PreviewHeight = (int) (PreviewSizeRate*height);

        Padding = (int) (PaddingSizeRate * width);

        System.out.println("Sizes prev"+PreviewWidth+" "+PreviewHeight);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(PreviewWidth,PreviewHeight );
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        layoutParams.setMargins(Padding, Padding, Padding, Padding);
        surfaceView.setLayoutParams(layoutParams);

        // For the sensors
        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initBuffers();
        initListeners();
        initGooglePlayFaceTracker();

        // wait for one second until gyroscope and magnetometer/accelerometer
        // data is initialised then scedule the complementary filter task
        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);
        // GUI stuff
        mHandler = new Handler();


    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        // restore the sensor listeners when user resumes the application.
        initListeners();
        InitializeImageDimensions();
        //RestartPosition();
        TranslateMap(0f, 0f);

    }


    public int getDeviceDefaultOrientation() {

        WindowManager windowManager =  (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////


    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================




    //This function is used to smooth the intial movements
    void initBuffers()
    {


        while(ListAverageAccMagOrientationX.size() < MaxSizeQueue) {
            ListAverageAccMagOrientationX.add(0f);
        }

        while(ListAverageAccMagOrientationY.size() < MaxSizeQueue) {
            ListAverageAccMagOrientationY.add(0f);
        }

        while(ListAverageAccMagOrientationZ.size() < MaxSizeQueue) {
            ListAverageAccMagOrientationZ.add(0f);
        }


        /* System.out.println("Initial Sizes");
        System.out.println("ListAverageAccMagOrientationX.size() "+ListAverageAccMagOrientationX.size());
        System.out.println("ListAverageAccMagOrientationY.size() "+ListAverageAccMagOrientationY.size());
        System.out.println("ListAverageAccMagOrientationZ.size() "+ListAverageAccMagOrientationZ.size());*/
    }

    // This function registers sensor listeners for the accelerometer, magnetometer and gyroscope.
    public void initListeners(){
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        //The Gyroscope should not influence
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }


    //Matrix Multiplication: https://en.wikipedia.org/wiki/Matrix_multiplication

    float[] MultiplyMatrix(float[] A, float[] B)
    {
        float[] Output = new float[9];

        Output[0] =	A[0]*B[0] + A[1]*B[3] + A[2]*B[6] ;
        Output[1] = A[0]*B[1] + A[1]*B[4] + A[2]*B[7] ;
        Output[2] = A[0]*B[2] + A[1]*B[5] + A[2]*B[8] ;
        Output[3] = A[3]*B[0] + A[4]*B[3] + A[5]*B[6] ;
        Output[4] = A[3]*B[1] + A[4]*B[4] + A[5]*B[7] ;
        Output[5] = A[3]*B[2] + A[4]*B[5] + A[5]*B[8] ;
        Output[6] = A[6]*B[0] + A[7]*B[3] + A[8]*B[6] ;
        Output[7] = A[6]*B[1] + A[7]*B[4] + A[8]*B[7] ;
        Output[8] = A[6]*B[2] + A[7]*B[5] + A[8]*B[8] ;

        return Output;
    }


    //Code based on: https://en.wikipedia.org/wiki/Invertible_matrix

    float[] InverseMatrix(float[] Input)
    {
        float[] Output = new float[9];

        float Det = DetMatrix3(Input);
        Det = 1/Det;

        Output[0] = Det * ((Input[4]*Input[8])-(Input[5]*Input[7]));
        Output[1] = -1 * Det * ((Input[1]*Input[8])-(Input[2]*Input[7]));
        Output[2] = Det * ((Input[1]*Input[5])-(Input[2]*Input[4]));
        Output[3] = -1 * Det * ((Input[3]*Input[8])-(Input[5]*Input[6]));
        Output[4] = Det * ((Input[0]*Input[8])-(Input[2]*Input[6]));
        Output[5] = -1 * Det * ((Input[0]*Input[5])-(Input[2]*Input[3]));
        Output[6] = Det * ((Input[3]*Input[7])-(Input[4]*Input[6]));
        Output[7] =-1 *  Det * ((Input[0]*Input[7])-(Input[1]*Input[6]));
        Output[8] = Det * ((Input[0]*Input[4])-(Input[1]*Input[3]));

        return Output;
    }

    float DetMatrix3(float [] Matrix)
    {
        return (((Matrix[0]*Matrix[4]*Matrix[8])+(Matrix[1]*Matrix[5]*Matrix[6])+(Matrix[3]*Matrix[7]*Matrix[2]))-((Matrix[2]*Matrix[4]*Matrix[6])+(Matrix[3]*Matrix[1]*Matrix[8])+(Matrix[7]*Matrix[5]*Matrix[0])));
    }


    // calculates orientation angles from accelerometer and magnetometer output
    public void calculateAccMagOrientation() {

        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {

            if(ImproveCalibration)
            {
                ImproveCalibration = false;

                OriginalaccMagOrientation = InverseMatrix(rotationMatrix);

                SensorManager.getOrientation(rotationMatrix, accMagOrientationOriginal);


            }else{

                AuxiliarRotationMatrix = MultiplyMatrix(OriginalaccMagOrientation, rotationMatrix);


                SensorManager.getOrientation(rotationMatrix, accMagOrientationAbsolute);

                SensorManager.getOrientation(AuxiliarRotationMatrix, accMagOrientation);

            }

        }
    }



    // UNIMPLEMENTED METHODS FOR SENSORS

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch(event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                // copy new accelerometer data into accel array and calculate orientation
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;

        }

    }

    //////////////// NEW VERSION FOR SURFACE METHODS

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.equals(surfaceView.getHolder())) { //For the surface that displays the preview of the camera
            if (mCameraSource != null) {
                mCameraSource.stop();
            }
            startCameraSource();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder.equals(surfaceView.getHolder())) {
            startCameraSource();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (holder.equals(surfaceView.getHolder())) {
            if (mCameraSource != null) {
                mCameraSource.release();
            }
        }
    }


    // METHOD TO MAKE ZOOM

    void FaceZoom(float Size)
    {
        //Determine new Scale
        //We consider 250K px^2 min size and 800K px^2 Max size

       // System.out.println("Size: "+Size+ " BiggestRectangleFace "+BiggestRectangleFace+" SmallestRectangleFace "+SmallestRectangleFace);

        if(Size > BiggestRectangleFace)
            Size = BiggestRectangleFace;

        if(Size < SmallestRectangleFace)
            Size = SmallestRectangleFace;


        //Reverse Zooming
        //float SizeMinusOffset = Size - SmallestRectangleFace;
        //Direct Zooming
        float SizeMinusOffset = BiggestRectangleFace -Size;

        //System.out.println("SizeMinusOffset: "+SizeMinusOffset);

        CurrentScale = 4f-((SizeMinusOffset * (4f-MinZoom))/(BiggestRectangleFace-SmallestRectangleFace));

        //System.out.println("CurrentScale: "+CurrentScale);

        //RestartPosition();


        if(DeviceIsPhone)
        {
            TranslateMap(AverageAccMagOrientationX, AverageAccMagOrientationY);
        }else{ //We consider it is a tablet
            TranslateMap(AverageAccMagOrientationY, AverageAccMagOrientationX);
        }
    }


    //Methods to update the GUI with the values of the acelerometer.

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            // update sensor output in GUI
            mHandler.post(updateOreintationDisplayTask);
        }
    }

    private Runnable updateOreintationDisplayTask = new Runnable() {
        public void run() {
            updateOreintationDisplay();
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen for landscape and portrait and set portrait mode always
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            System.out.println("LANDSCAPE");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            System.out.println("PORTRAIT");
        }
    }

    public void updateOreintationDisplay() {

        //int MaxSizeQueue = 22;

        //Prepear Y and Z values to have a [0...360] range
        CurrentAccMagOrientationY = (float) (accMagOrientation[2] * 180 / Math.PI);
        CurrentAccMagOrientationZ = (float) (accMagOrientation[0] * 180 / Math.PI);


        //Prepear X values to have a [0...360] range - We need to take into account
        // the screen orientation
        CurrentAccMagOrientationX = (float) (accMagOrientation[1] * 180 / Math.PI);


        //Avergaing the rotation for X axis
        if (ListAverageAccMagOrientationX.size() < MaxSizeQueue) {

            ListAverageAccMagOrientationX.add(CurrentAccMagOrientationX);

        }else if(BufferCalibrationX > 0){ //Step to calibrate the buffers
            BufferCalibrationX--;

            //Add new element
            ListAverageAccMagOrientationX.add(CurrentAccMagOrientationX);
            //Removes the oldest one
            ListAverageAccMagOrientationX.poll();

        } else {

            //Add new element
            ListAverageAccMagOrientationX.add(CurrentAccMagOrientationX);
            //Removes the oldest one
            ListAverageAccMagOrientationX.poll();

            ListX = ListAverageAccMagOrientationX.iterator();

            AverageAccMagOrientationX = 0;

            while (ListX.hasNext()) {
                AverageAccMagOrientationX += (float) ListX.next();
            }

            AverageAccMagOrientationX /= MaxSizeQueue;

            AverageAccMagOrientationX =Float.valueOf(d.format(AverageAccMagOrientationX).replace(",", "."));

            //System.out.println("Current Average X: "+AverageAccMagOrientationX);

            if(PrevXRot != 0)
            {
                //Not a relevant variation
                if(Math.abs(AverageAccMagOrientationX - PrevXRot) < (Math.abs(AverageAccMagOrientationX*NoiseFilterLimitTranslation)))
                {

                    //AuxSwap = PrevXRot;
                    //PrevXRot = AverageAccMagOrientationX;
                    AverageAccMagOrientationX = PrevXRot;//AuxSwap; //No movement this time

                }
            }

            PrevXRot = AverageAccMagOrientationX;

            //System.out.println("Current Average x: "+AverageAccMagOrientationX);

        }

        if (ListAverageAccMagOrientationY.size() < MaxSizeQueue) {

            ListAverageAccMagOrientationY.add(CurrentAccMagOrientationY);

        }else if(BufferCalibrationY > 0){ //Step to calibrate the buffers
            BufferCalibrationY--;

            //Add new element
            ListAverageAccMagOrientationY.add(CurrentAccMagOrientationY);
            //Removes the oldest one
            ListAverageAccMagOrientationY.poll();

        } else {

            //Add new element
            ListAverageAccMagOrientationY.add(CurrentAccMagOrientationY);
            //Removes the oldest one
            ListAverageAccMagOrientationY.poll();

            ListY = ListAverageAccMagOrientationY.iterator();

            AverageAccMagOrientationY = 0;

            while (ListY.hasNext()) {
                AverageAccMagOrientationY += (float) ListY.next();
            }

            AverageAccMagOrientationY /= MaxSizeQueue;

            AverageAccMagOrientationY =Float.valueOf(d.format(AverageAccMagOrientationY).replace(",", "."));

            if(PrevYRot != 0)
            {
                //Not a relevant variation
                if(Math.abs(AverageAccMagOrientationY - PrevYRot) < (Math.abs(AverageAccMagOrientationY*NoiseFilterLimitTranslation)))
                {
                    /*AuxSwap = PrevYRot;
                    PrevYRot = AverageAccMagOrientationY;*/
                    AverageAccMagOrientationY = PrevYRot;//AuxSwap; //No movement this time

                }
            }

            PrevYRot = AverageAccMagOrientationY;
        }

        //So Far this one will remaing disable
        if (ListAverageAccMagOrientationZ.size() < MaxSizeQueue) {

            ListAverageAccMagOrientationZ.add(CurrentAccMagOrientationZ);

        } else if(BufferCalibrationZ > 0){ //Step to calibrate the buffers
            BufferCalibrationZ--;

            //Add new element
            ListAverageAccMagOrientationZ.add(CurrentAccMagOrientationZ);
            //Removes the oldest one
            ListAverageAccMagOrientationZ.poll();

        } else {

            //Add new element
            ListAverageAccMagOrientationZ.add(CurrentAccMagOrientationZ);
            //Removes the oldest one
            ListAverageAccMagOrientationZ.poll();

            ListZ = ListAverageAccMagOrientationZ.iterator();

            AverageAccMagOrientationZ = 0;

            while (ListZ.hasNext()) {
                AverageAccMagOrientationZ += (float) ListZ.next();
            }

            AverageAccMagOrientationZ /= MaxSizeQueue;

            AverageAccMagOrientationZ =Float.valueOf(d.format(AverageAccMagOrientationZ).replace(",","."));

            if(PrevZRot != 0)
            {
                //Not a relevant variation
                if(Math.abs(AverageAccMagOrientationZ - PrevZRot) < Math.abs((AverageAccMagOrientationZ*NoiseFilterLimitTranslation)))
                {
                    //System.out.println("Irrelevant");

                    AverageAccMagOrientationZ = PrevZRot;

                }
            }

            PrevZRot = AverageAccMagOrientationZ;

            //System.out.println("Current Average Z: "+AverageAccMagOrientationZ);

        }

        //Convert the rotation position in movement
        if(DeviceIsPhone)
        {
            TranslateMap(AverageAccMagOrientationX, AverageAccMagOrientationY);
        }else{ //We consider it is a tablet
            TranslateMap(AverageAccMagOrientationY, AverageAccMagOrientationX);
        }



    }


    public void TranslateMap(float dx, float dy)//(float dx, float dy)
    {

        int SignDx , SignDy;

        if(DeviceIsPhone)
        {
             SignDx = 1;
             SignDy = 1;
        }else{ //We consider it is a tablet
            SignDx = -1;
            SignDy = 1;
        }


        /*if(dx== 0 && dy==0)
            System.out.println("Restarting");*/

        //t4.setText(" "+CurrentScale);

        //Find out direction of translation
        if(dx < 0)
        {
            if(DeviceIsPhone)
            {
                SignDx = -1;
            }else{ //We consider it is a tablet
                SignDx = 1;
            }
        }



        if(dy < 0)
            SignDy = -1;

        //Set maximun limit
        if(Math.abs(dx) > HorizontalLimit)
            dx = HorizontalLimit;

        if(Math.abs(dy) > VerticalLimit)
            dy = VerticalLimit;

        ImageView view = (ImageView) mview;

        //RestartPosition();

        ImageViewWidth = mview.getWidth();
        ImageViewHeight = mview.getHeight();


        InitializeImageDimensions();

        int CurrentImageViewWidth = (int) (ImageViewWidth * CurrentScale);
        int CurrentImageViewHeight = (int) (ImageViewHeight * CurrentScale);

        int MovementHorizontal = (CurrentImageViewWidth - ImageViewWidth) / 2;
        int MovementVertical = (CurrentImageViewHeight - ImageViewHeight) / 2;
        int offset = (int) (Gap * CurrentScale);

        if(FlagHeightRate)
        {
            MovementVertical -= offset;
        }
        else
        {
            MovementHorizontal -= offset;
        }

        float ActualMovementHorizontal = ( MovementHorizontal * Math.abs(dx)) / HorizontalLimit;
        float ActualMovementVertical = (MovementVertical * Math.abs(dy)) / VerticalLimit;


        ActualMovementHorizontal *= SignDx;
        ActualMovementVertical *= SignDy;


        /// Set firstly the Matrix in the center of the screen

        RectScreen = new RectF(0, 0, ImageViewWidth, ImageViewHeight);

        matrix.setRectToRect(RectImage, RectScreen,  Matrix.ScaleToFit.CENTER);

        CenterX = ImageViewWidth/2;
        CenterY = ImageViewHeight/2;

        //To apply a scale factor
        matrix.postScale(CurrentScale, CurrentScale, CenterX, CenterY);


        //view.setImageMatrix(matrix);


        matrix.postTranslate(ActualMovementHorizontal, ActualMovementVertical);

        //view.setImageMatrix(matrix);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mview.setImageMatrix(matrix);
            }
        });


    }


    void InitializeImageDimensions()
    {
        // Definig variables to set the map in the middle of the screen

        ImageViewWidth = mview.getWidth();
        ImageViewHeight = mview.getHeight();

        Drawable d = getResources().getDrawable(R.drawable.map3);
        ImageHeight = d.getIntrinsicHeight();
        ImageWidth = d.getIntrinsicWidth();

        float Aux1 = ImageHeight;
        float Aux2 = ImageWidth;

        float Hrate = (float)(ImageViewHeight/Aux1);
        float Wrate = (float)(ImageViewWidth/Aux2);

        if(Wrate > Hrate)
        {
            ScalingRate = Hrate;
            //To estimate the gap in the shortest side
            Gap = (ImageViewWidth - (ImageWidth * ScalingRate)) / 2;
            FlagHeightRate = false;
            MinZoom = ImageViewWidth /((ImageViewHeight * ImageWidth) /(float)ImageHeight );
        }
        else
        {
            ScalingRate = Wrate;
            //To estimate the gap in the shortest side
            Gap = (ImageViewHeight - (ImageHeight * ScalingRate)) / 2;
            FlagHeightRate = true;
            MinZoom = ImageViewHeight /((ImageViewWidth * ImageHeight) /(float)ImageWidth );
        }

        RectImage = new RectF(0, 0, ImageWidth, ImageHeight);

    }

    ////////////////////     METHODS IMPLEMENTED BY KOSTYA      ///////////////////7

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.FAST_MODE + FaceDetector.NO_CLASSIFICATIONS + FaceDetector.NO_LANDMARKS)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        FaceTracker tracker = new FaceTracker();
        detector.setProcessor(new LargestFaceFocusingProcessor(detector, tracker));

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(320, 240)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(15.0f)
                .build();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(surfaceView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    // Init camera source and google play face tracker
    public void initGooglePlayFaceTracker() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mCameraSource.start(surfaceHolder);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }





    private class FaceTracker extends Tracker<Face> {
        /**
         * Start tracking the detected face instance within the face overlay.
         */
        Drawable d = null;
        RectF drawableRect;
        RectF AnotherviewRect;
        @Override
        public void onNewItem(int faceId, Face item) {

            //System.out.println("New face");

            /*
            d = getResources().getDrawable(R.drawable.map3);
            int h = d.getIntrinsicHeight();
            int w = d.getIntrinsicWidth();
            ImageViewWidth = mview.getWidth();
            ImageViewHeight = mview.getHeight();
            CenterX = ImageViewWidth/2;
            CenterY = ImageViewHeight/2;
            drawableRect = new RectF(0, 0, w, h);
            AnotherviewRect = new RectF(0, 0, ImageViewWidth, ImageViewHeight);*/

        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            //Proper way to center the image

            if(RestartFlag)
            {
                RestartFlag = false;
                CurrentScale = 2;
                //TranslateMap(0,0);
            }


            float FaceCentreXCoor = face.getPosition().x + (face.getWidth()/2);
            float FaceCentreYCoor = face.getPosition().y + (face.getHeight()/2);

            //With respect of the display
            FaceCentreXCoor /= PreviewSizeRate;
            FaceCentreYCoor /= PreviewSizeRate;

            double Distance = DistanceCentre(FaceCentreXCoor, FaceCentreYCoor);


            //System.out.println("EulerRotation Y: " + face.getEulerY()+" and Z: "+face.getEulerZ());// + face.getEulerY ());
            //t2.setText("EulerRotation Z: " + face.getEulerZ ());

            if(Distance < (CurrentDistance*0.1))
            {
                CurrentDistance = Distance;
                //ImproveCalibration = true; not ready yet
                //F("Calibrate ");
                //System.out.println("Distance "+Distance);
            }

            //System.out.println("Distance "+Distance);

            //System.out.println("Centre Face: w "+FaceCentreXCoor+" h "+FaceCentreYCoor);



            float Size = (face.getWidth() * face.getHeight());

            //System.out.println("Size detected: "+Size);

            if (Size > 1) {
                //Size = 1000000000 / Size;
                StoringSize(Size);
            }
            RestartPosition = 30;



        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {

            //System.out.println("No face");

            if(RestartPosition-- <0)
            {
                //System.out.println("That would be a restart");
                RestartFlag = true;
            }
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() { //It is called after 3 consecutive onMissing... not so much margin so useless at the moment

            //System.out.println("The face is gone...");


        }
    }

    void StoringSize(float FaceSize)
    {

        //t2.setText("EulerRotation Z......: ");

        //It crash
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                t2.setText("EulerRotation Z......: ");
        }
        });*/


        if (ListAverageSize.size() < MaxSizeQueueZoom) { //Just for the first time

            while(ListAverageSize.size() < MaxSizeQueueZoom)
            {
                ListAverageSize.add((float) FaceSize);
            }


        }else {

            //Add new element
            ListAverageSize.add((float) FaceSize);
            //Removes the oldest one
            ListAverageSize.poll();

            ListSize = ListAverageSize.iterator();

            AverageSize = 0;

            while (ListSize.hasNext()) {
                AverageSize += (float) ListSize.next();
            }

            AverageSize /= MaxSizeQueueZoom;

            AverageSize =Float.valueOf(d.format(AverageSize).replace(",","."));

            // To supress the noise
            //if(PrevSize != 0)
            //{
            //    //In case there was a relevant change
            //    if(Math.abs(PrevSize-AverageSize) > AverageSize*NoiseFilterLimitZoom)
            //        FaceZoom(AverageSize);
            //}

            FaceZoom(AverageSize);

            PrevSize = AverageSize;

        }

    }

    double DistanceCentre(float CurrentWidth, float CurrentHeight)
    {
        float auxDiffW = Math.abs(CurrentWidth - CentreScreenXCoor);
        float auxDiffH = Math.abs(CurrentHeight - CentreScreenYCoor);
        double Distance = Math.sqrt((auxDiffW * auxDiffW) + (auxDiffH * auxDiffH));

        return Distance;
    }

}
