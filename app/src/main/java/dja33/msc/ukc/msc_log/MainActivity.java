package dja33.msc.ukc.msc_log;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import dja33.msc.ukc.msc_log.sample.SampleHandler;

public class MainActivity extends AppCompatActivity {

    private FileHandler fileHandler;
    private SoundMeter soundMeter;
    private Thread smRun;

    private AccelerometerSensor accelerometerSensor;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Spinner spinner;

    private static boolean sampling;
    private static String activeChar = "Q";

    public static final String[] keyboardKeys = {"SPACE", "Q", "ENTER", "H", ";"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileHandler = new FileHandler(Environment.getExternalStorageDirectory().getAbsolutePath(), "data.csv", getApplicationContext());
        soundMeter = new SoundMeter();
        spinner = (Spinner) findViewById(R.id.select);

        accelerometerSensor = new AccelerometerSensor();

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(accelerometerSensor, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        inputCharactersFromAlphabet();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Button button = (Button) findViewById(R.id.write);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.yolo);
                tv.setText("Item: " +
                        spinner.getSelectedItem().toString());
                fileHandler.openOutput();
                System.out.println("Writing out data...");
                for(String keys : keyboardKeys){
                    try {
                        if(SampleHandler.getHandler().getSample(keys) != null) {
                            System.out.println("Writing: " + keys);
                            fileHandler.writeOut(SampleHandler.getHandler().getSample(keys));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                fileHandler.closeOut();

            }
        });

        button = (Button) findViewById(R.id.read);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.yolo);
                tv.setText("Reading in... " + fileHandler.getFilePath());

                try {
                    fileHandler.readFile();
                    tv.setText("Done reading! " + fileHandler.getFileElements().size());

                    System.out.println("Start reading.");
                    for(String s : fileHandler.getFileElements()){
                        System.out.println(s);
                    }
                    System.out.println("Stop reading.");

                }catch(Exception e){
                    e.printStackTrace();
                    tv.setText("Error reading!");
                }
            }
        });

        final int view = 4;
        button = (Button) findViewById(R.id.viewbtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.yolo);
                tv.setText("Viewing " + view);

                try {
                    TextView tv2 = (TextView) findViewById(R.id.view);

                    fileHandler.read();

                    ArrayList<String> str = fileHandler.getFileElements();

                    for(String u : str)
                        tv2.append(u);


                }catch(Exception e){
                    e.printStackTrace();
                    tv.setText("Error!");
                }
                tv.setText("Done!");
            }
        });

        button = (Button) findViewById(R.id.delete);
        button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v,  MotionEvent event) {
                TextView tv = (TextView) findViewById(R.id.yolo);
                tv.setText("Deleted file.");
                System.out.println("Deleted file.");
                fileHandler.delete();
                return true;
            }
        });

        button = (Button) findViewById(R.id.sample);
        button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v,  MotionEvent event) {
                TextView tv = (TextView) findViewById(R.id.yolo);

                UpdateProgress up = new UpdateProgress(soundMeter, spinner.getSelectedItem().toString());

                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                    activeChar = spinner.getSelectedItem().toString();

                    tv.setText("Sampling now...");

                    smRun = new Thread(up);
                    smRun.start();

                    sampling = true;


                }else if (event.getAction() == MotionEvent.ACTION_UP){

                    tv.setText("Stopping now...");

                    smRun.interrupt();

                    smRun = null;

                    sampling = false;

                }
                return true;
            }
        });

    }

    private class UpdateProgress implements Runnable {

        private SoundMeter sm;
        private int index;
        private String key;
        private double[] samples;
        private double average;

        public UpdateProgress(SoundMeter sm, String key) {
            this.sm = sm;
            this.samples = new double[1024];
            this.key = key;
        }

        public void run() {
            ProgressBar pr = (ProgressBar) findViewById(R.id.progress);
            try {
                sm.start();
                while (true) {
                    double amp = sm.getAmplitude();
//                    System.out.println("Amplitude: " + amp);
//                    System.out.println("Amplitude accelerometerSensor %: " + ((amp/32768)*100));
                    pr.setProgress((int)((amp/32768)*100));

                    samples[(index+1) % 1024] = amp;

//                    System.out.print(">");
//                    for(double s : soundMeter.getFrequency()) {
//                        if(s < 0)
//                            continue;
//                        DecimalFormat df = new DecimalFormat("#.##");
//                        s = Double.valueOf(df.format(s));
//                        System.out.print(s + "|");
//                    }
//                    System.out.println("<");

                    Thread.sleep(50);
                }
            }catch(InterruptedException e) {
                pr.setProgress(0);
                System.out.println("Interruption!");
                sm.stop();
            }

            for(double res : samples){
                if(res > 0) {
                    average += res;
                }
            }

            average /= samples.length;

            System.out.println("Average: " + average + " | Over " + samples.length + " samples.");

            SampleHandler.getHandler().addAmplitudeSample(getActiveCharacter(), average);

            System.out.println("Logging: " + key + " -> " + average);

        }
    }


    private void inputCharactersFromAlphabet() {
        try {

            ArrayAdapter<String> spinnerItems = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keyboardKeys);
            spinner.setAdapter(spinnerItems);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(accelerometerSensor);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(accelerometerSensor, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // accelerometerSensor you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static boolean isSampling() { return sampling; }
    public static String getActiveCharacter(){ return activeChar; }
}
