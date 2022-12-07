package com.iothub.azure.microsoft.com.androidsample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{

    private final String connString = "[IOT HUB Connection String]";
    private final String deviceId = "[Device ID]";

    Button btnInvoke;

    EditText editTxtSendMsgsIntVal;

    private String lastException;

    private DirectMethodResponse result;

    private final Handler handler = new Handler();

    private final int responseTimeout = 200;
    private final int connectTimeout = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnInvoke = findViewById(R.id.btnInvoke);
        btnInvoke.setEnabled(true);

        editTxtSendMsgsIntVal = findViewById(R.id.editTxtSendMsgsIntVal);
    }

    final Runnable exceptionRunnable = new Runnable() {
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage(lastException);
            builder.show();
            System.out.println(lastException);
        }
    };

    final Runnable methodResultRunnable = new Runnable() {
        public void run() {
            Context context = getApplicationContext();
            CharSequence text;
            if (result != null)
            {
                text = "Received Status=" + result.getStatus() + " Payload=" + result.getPayloadAsJsonElement();
            }
            else
            {
                text = "Received null result";
            }
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    };

    private void invokeMethod()
    {
        new Thread(new Runnable() {
            public void run()
            {
                result = null;
                Map<String, Object> payload = new HashMap<String, Object>()
                {
                    {
                        put("sendInterval", editTxtSendMsgsIntVal.getText().toString());
                    }
                };

                try
                {
                    DirectMethodsClient methodClient = new DirectMethodsClient(connString);
                    DirectMethodRequestOptions options =
                            DirectMethodRequestOptions.builder()
                                    .payload(payload)
                                    .methodConnectTimeoutSeconds(connectTimeout)
                                    .methodResponseTimeoutSeconds(responseTimeout)
                                    .build();
                    result = methodClient.invoke(deviceId, "setMessagingInterval", options);

                    if(result == null)
                    {
                        throw new IOException("Method invoke returns null");
                    }
                    else
                    {
                        handler.post(methodResultRunnable);
                    }
                }
                catch (Exception e)
                {
                    lastException = "Exception while trying to invoke direct method: " + e.toString();
                    handler.post(exceptionRunnable);
                }
            }
        }).start();
    }

    public void btnInvokeOnClick(View v)
    {
        invokeMethod();
    }
}
