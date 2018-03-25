package com.manish.fingerprint;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;


public class MainActivity extends Activity implements MFS100Event {

    TextView lblMessage;
    EditText txtEventLog;
    ImageView imgFinger;
    EditText editText1;
    Button Capture,Verify;
    FingerData lastCapFingerData;
    User[] user = new User[5];
    MFS100 mfs100 = null;
    int f =-1;
    int timeout = 10000;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText1 = (EditText) findViewById(R.id.editText);
        imgFinger = (ImageView) findViewById(R.id.imageView);
        Capture = (Button) findViewById(R.id.button);
        Verify =(Button) findViewById(R.id.button2);
        txtEventLog = (EditText) findViewById(R.id.editText2);
        lblMessage = (TextView) findViewById(R.id.textView);

    }

    @Override
    protected void onStart() {
        if (mfs100 == null) {
            mfs100 = new MFS100(this);
            mfs100.SetApplicationContext(MainActivity.this);
        } else {
            InitScanner();

        }
        super.onStart();

    }
    protected void onDestroy() {
        if (mfs100 != null) {
            mfs100.Dispose();
        }
        super.onDestroy();
    }
    protected void onStop() {
        UnInitScanner();
        super.onStop();
    }
    private void InitScanner() {
        try {
            int ret = mfs100.Init();
            if (ret != 0) {
                SetTextonuiThread(mfs100.GetErrorMsg(ret));
            } else {
                SetTextonuiThread("Init success");
                String info = "Serial: " + mfs100.GetDeviceInfo().SerialNo()
                        + " Make: " + mfs100.GetDeviceInfo().Make()
                        + " Model: " + mfs100.GetDeviceInfo().Model()
                        + "\nCertificate: " + mfs100.GetCertification();
                SetLogOnUIThread(info);
            }
        } catch (Exception ex) {
            Toast.makeText(this, "Init failed, unhandled exception",
                    Toast.LENGTH_LONG).show();
            SetTextonuiThread("Init failed, unhandled exception");
        }
    }
    private void UnInitScanner() {
        try {
            int ret = mfs100.UnInit();
            if (ret != 0) {
                SetTextonuiThread(mfs100.GetErrorMsg(ret));
            } else {
                SetLogOnUIThread("Uninit Success");
                SetTextonuiThread("Uninit Success");
                lastCapFingerData = null;
            }
        } catch (Exception e) {
            Log.e("UnInitScanner.EX", e.toString());
        }
    }

    @Override
    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        int ret = 0;
        if (!hasPermission) {
            SetTextonuiThread("Permission denied");
            return;
        }
        if (vid == 1204 || pid == 11279) {
            if (pid == 34323) {
                ret = mfs100.LoadFirmware();
                if (ret != 0) {
                    SetTextonuiThread(mfs100.GetErrorMsg(ret));

                } else {
                    SetTextonuiThread("Loadfirmware success");
                }
            } else if (pid == 4101) {
                ret = mfs100.Init();
                if (ret != 0) {
                    SetTextonuiThread(mfs100.GetErrorMsg(ret));
                } else {
                    SetTextonuiThread("Init success");
                    Toast.makeText(this, "Device connected", Toast.LENGTH_LONG).show();
                    String info = "Serial: "
                            + mfs100.GetDeviceInfo().SerialNo() + " Make: "
                            + mfs100.GetDeviceInfo().Make() + " Model: "
                            + mfs100.GetDeviceInfo().Model();
                    SetLogOnUIThread(info);
                }
            }
        }
    }

    private void DisplayFinger(final Bitmap bitmap) {
        imgFinger.post(new Runnable() {
            @Override
            public void run() {
                imgFinger.setImageBitmap(bitmap);
            }
        });
    }
    private void SetTextonuiThread(final String str) {
        lblMessage.post(new Runnable() {
            public void run() {
                lblMessage.setText(str, TextView.BufferType.EDITABLE);
            }
        });
    }
    private void SetLogOnUIThread(final String str) {
        txtEventLog.post(new Runnable() {
            public void run() {

                txtEventLog.setText(txtEventLog.getText().toString() + "\n"
                        + str, TextView.BufferType.EDITABLE);
            }
        });
    }
    public void Capture(View view){

        f++;
        InitScanner();
        if(editText1.getText()!=null){
            user[f]=new User();
        user[f].useraadhaarno = editText1.getText().toString();
        user[f].userfingerdata = StartSyncCapture();}
    }
    private FingerData StartSyncCapture() {



                    //f++;
                    //user[f].aadhaarnumber  = editText1.getText().toString();
                    SetTextonuiThread("");
                    try {
                        FingerData fingerData = new FingerData();
                        int ret = mfs100.AutoCapture(fingerData, timeout,true);
                        Log.e("StartSyncCapture.RET", "" + ret);
                        if (ret != 0) {
                            SetTextonuiThread(mfs100.GetErrorMsg(ret));
                        } else {
                            lastCapFingerData = fingerData;
                            //user[f++].fingerprintdata = fingerData.ISOTemplate();
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0,
                                    fingerData.FingerImage().length);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    imgFinger.setImageBitmap(bitmap);
                                }
                            });

                            SetTextonuiThread("Capture Success");
                            String log = "\nQuality: " + fingerData.Quality()
                                    + "\nNFIQ: " + fingerData.Nfiq()
                                    + "\nWSQ Compress Ratio: "
                                    + fingerData.WSQCompressRatio()
                                    + "\nImage Dimensions (inch): "
                                    + fingerData.InWidth() + "\" X "
                                    + fingerData.InHeight() + "\""
                                    + "\nImage Area (inch): " + fingerData.InArea()
                                    + "\"" + "\nResolution (dpi/ppi): "
                                    + fingerData.Resolution() + "\nGray Scale: "
                                    + fingerData.GrayScale() + "\nBits Per Pixal: "
                                    + fingerData.Bpp() + "\nWSQ Info: "
                                    + fingerData.WSQInfo();
                            SetLogOnUIThread(log);
                            //SetData2(fingerData);
                        }
                    } catch (Exception ex) {
                        SetTextonuiThread("Error");
                    }





        return  lastCapFingerData;
    }
    public void Verify(View view){

        InitScanner();
        User one = new User();
        one.userfingerdata = StartSyncCapture();

            int ret=0;
            for (int i = 0; i <= f; i++) {


                ret = mfs100.MatchISO(one.userfingerdata.ISOTemplate(), user[i].userfingerdata.ISOTemplate());
                if (ret < 0) {
                    SetTextonuiThread("Error: " + ret + "(" + mfs100.GetErrorMsg(ret) + ")");
                } else {
                    if (ret >= 1000) { //1400
                        SetTextonuiThread("Finger matched with score: " + ret);
                        txtEventLog.setText("Fingerprint matched with " + user[i].useraadhaarno);
                        Toast.makeText(this, "Finger matched with user " + user[i].useraadhaarno + " with: "+ ret , Toast.LENGTH_LONG).show();
                        break;
                        //Toast.makeText()
                    } else {
                        SetTextonuiThread("Finger not matched, score: " + ret);
                        //Toast.makeText(this, "Finger not found" , Toast.LENGTH_LONG).show();
                    }
                }

            }
            if (ret<1000){
                Toast.makeText(this, "Finger not found" , Toast.LENGTH_LONG).show();
                txtEventLog.setText("Fingerprint not matched with ");
            }


    }

    @Override
    public void OnHostCheckFailed(String s) {
        try {
            SetLogOnUIThread(s);
            Toast.makeText(this, s, Toast.LENGTH_LONG).show();
        } catch (Exception ignored) {
        }

    }

    @Override
    public void OnDeviceDetached() {
        UnInitScanner();
        SetTextonuiThread("Device removed");
    }
}
