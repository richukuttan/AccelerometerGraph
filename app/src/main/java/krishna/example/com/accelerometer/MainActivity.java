package krishna.example.com.accelerometer;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;

import static krishna.example.com.accelerometer.R.styleable.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private TextView h,x,y,z;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Vibrator vibrator;
    private Button start,stop;
    private List<Double> arrayList;
    private Boolean record =false;
    private GraphView graphView;
    private double values[];
    private double well[];
    private double filt[];
    private LineGraphSeries<DataPoint> series,filtered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        //initialize views
        h = (TextView)findViewById(R.id.heading);
        x = (TextView)findViewById(R.id.xdata);
        y = (TextView)findViewById(R.id.ydata);
        z = (TextView)findViewById(R.id.zdata);
        start = (Button)findViewById(R.id.start);
        stop = (Button)findViewById(R.id.stop);
        graphView = (GraphView)findViewById(R.id.graph);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScrollable(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(50);

        //START BUTTON
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList = new ArrayList<Double>();
                v.setClickable(false);
                record = true;
                graphView.removeAllSeries();
            }
        });

        //STOP BUTTON
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record =false;
                start.setClickable(true);
                Double[] arr = arrayList.toArray(new Double[arrayList.size()]);
                values = new double[arr.length];
                for(int j=0;j<arr.length;j++){
                    values[j] = arr[j].doubleValue();
                }
                SGFilter sgFilter = new SGFilter(3,3);
                filt = sgFilter.computeSGCoefficients(3,3,4);
                well = sgFilter.smooth(values,filt);
                series = new LineGraphSeries<DataPoint>();
                filtered = new LineGraphSeries<DataPoint>();
                filtered.setColor(Color.GREEN);
                for(int i=0;i<values.length;i++){
                    series.appendData(new DataPoint(i,values[i]),true,100);
                }
                for(int i=0;i<well.length;i++){
                    filtered.appendData(new DataPoint(i,well[i]),true,100);
                }
                graphView.addSeries(series);
                graphView.addSeries(filtered);
            }
        });


        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x.setText("x - "+event.values[0]);
        y.setText("y - "+event.values[1]);
        z.setText("z - "+event.values[2]);
        if(record) {
            arrayList.add(Math.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
