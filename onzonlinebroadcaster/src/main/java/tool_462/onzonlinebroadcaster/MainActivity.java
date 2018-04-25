package tool_462.onzonlinebroadcaster;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    ToneGenerator toneG;
    String strTransaction;
    String strNetHash = "aa14b4d84260e00b6fc033c022a25965629ab0e8a4aafc77e64cad4cf0dc2e00";
    String strURL = "https://tnode11.onzcoin.com/peer/transactions";
    URL urlNetLink;
    Spinner spinnerLiskNet;
    EditText textCustomNode;

    final int RequestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case RequestCameraPermissionID:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        return;
                    }
                    try {
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
            //    @Override
            //public void onClick(View view) {
                //       Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //                .setAction("Action", null).show();
                //    }
        //});

        // custom code
        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        textView = (TextView) findViewById(R.id.code_info);
        textCustomNode = (EditText) findViewById(R.id.txtCustomNode);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        spinnerLiskNet = (Spinner) findViewById(R.id.spinnerLiskNet);
        spinnerLiskNet.setOnItemSelectedListener(this.sliderItemSelectListener);

        if (!barcodeDetector.isOperational()) {
            Log.w("MainActivity", "Detector dependencies are not yet available");
        } else {
            cameraSource = new CameraSource
                    .Builder(getApplicationContext(), barcodeDetector)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(640, 480)  // TODO: set resolution to a better value
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();

            cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.CAMERA},
                                    RequestCameraPermissionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    cameraSource.stop();
                }
            });

            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                    if (barcodes.size() != 0) {
                        textView.post(new Runnable() {    // Use the post method of the TextView
                            public void run() {
                                textView.setText(    // Update the TextView
                                        barcodes.valueAt(0).displayValue
                                );
                            }
                        });
                        strTransaction = "{\"transaction\":" + barcodes.valueAt(0).displayValue + "}";
                        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        final Button btnStart = (Button) findViewById(R.id.buttonStart);
        btnStart.setOnClickListener(this.buttonSendClickListener);

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
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private AdapterView.OnItemSelectedListener sliderItemSelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            switch (spinnerLiskNet.getSelectedItemPosition()) {
                case 3:         // NO net selected
                    textCustomNode.setVisibility(View.VISIBLE);
                    break;
                default:
                    textCustomNode.setVisibility(View.INVISIBLE);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }
    };

    private View.OnClickListener buttonSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            textView.setText("Waiting for response from Server");
            switch (spinnerLiskNet.getSelectedItemPosition()) { // TODO: add custom node
                case 0:         // NO net selected
                    textView.setText("NO NET SELECTED");
                    Toast toast = Toast.makeText(getApplicationContext(), "Please select net.", Toast.LENGTH_LONG);
                    TextView vi = (TextView) toast.getView().findViewById(android.R.id.message);
                    vi.setTextColor(Color.RED);
                    toast.show();
                    return;
                //break;
                case 1:         // TEST NET selected
                    strNetHash = "aa14b4d84260e00b6fc033c022a25965629ab0e8a4aafc77e64cad4cf0dc2e00";
                    strURL = "https://tnode11.onzcoin.com/peer/transactions";
                    break;
                case 2:         // MAIN NET selected
                    strNetHash = "ef56692f7973f7a8e82d6bd5bc68d5f514e4c3ed97d4cfca0345fddd0f421999";
                    strURL = "https://node06.onzcoin.com/peer/transactions";
                    break;
                case 3:         // CUSTOM NODE selected
                    strNetHash = "ef56692f7973f7a8e82d6bd5bc68d5f514e4c3ed97d4cfca0345fddd0f421999";
                    strURL = String.valueOf(textCustomNode.getText()) + "/peer/transactions";
                    break;
                default:
                    strNetHash = "aa14b4d84260e00b6fc033c022a25965629ab0e8a4aafc77e64cad4cf0dc2e00";
                    strURL = "https://tnode11.onzcoin.com/peer/transactions";
                    break;
            }

            try {
                urlNetLink = new URL(strURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            new SendPostRequest().execute();
        }
    };

    private class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {
            try{

                //URL urlNetLink = new URL("https://testnet.lisk.io/peer/transactions");
                //String trans = "{\"transaction\":{\"type\":0,\"amount\":10000000,\"fee\":10000000,\"recipientId\":\"1541786588265098370L\",\"timestamp\":44505392,\"asset\":{},\"senderPublicKey\":\"23be4e11ddcb6bf6d18ad2e4de1141b0ea2b08625767a20ade249b4117276b5f\",\"signature\":\"5c0a40ecb723fecbc2eceb64ac6489a63bbbcb6da59e3dd36de224ee240108f54601a9d725e76a906b5e54a33cd3910d8abb6c251a22ade3fea68d8e757a3e0e\",\"signSignature\":\"b9081c128ddc3453efd9820797f91ba67fc29367bb22cde90b7795e215ff0bcbd0b77e36e88217996e35640accdcd18cddf1137ae9bdc2d0c0353d0bf490870f\",\"id\":\"5630001309045380353\"}}";
                String trans = strTransaction;
                strTransaction = "";
                JSONObject postDataParams;
                try {
                    postDataParams = new JSONObject(trans);
                }
                catch(Exception e){
                    return new String("ERROR: Transaction had no valid JSON format!");
                }
                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) urlNetLink.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("os", "linux4.4.0-78-generic");
                conn.setRequestProperty("version", "0.9.13");
                conn.setRequestProperty("port", "1");
                conn.setRequestProperty("nethash", strNetHash);

                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postDataParams.toString());

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }
                    in.close();
                    return sb.toString();
                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String strResult = "";
            try {
                JSONObject postResult = new JSONObject(result);
                strResult = postResult.getString("success");

            } catch (JSONException e) {
                textView.setText(result);
                Toast toast = Toast.makeText(getApplicationContext(), "FAILED! See textfield for details.", Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                v.setTextColor(Color.RED);
                toast.show();
            }

            if (strResult == "true")
            {
                textView.setText("");
                Toast toast = Toast.makeText(getApplicationContext(), "SUCCESS!", Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                v.setTextColor(Color.GREEN);
                toast.show();
            }
            else
            {
                textView.setText(result);
                Toast toast = Toast.makeText(getApplicationContext(), "FAILED! See textfield for details.", Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                v.setTextColor(Color.RED);
                toast.show();
            }
        }
    }


}
