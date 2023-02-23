package com.samham.dronecontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText address_container = null;
    TextView power_percentage = null;

    SeekBar all_motors;
    SeekBar back_left_prop;
    SeekBar front_left_prop;
    SeekBar front_right_prop;
    SeekBar back_right_prop;

    int average_value = 0;
    int back_left_value = 0;
    int back_right_value = 0;
    int front_left_value = 0;
    int front_right_value = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("TAG", "begin");

        try {
            DatagramSocket socket = new DatagramSocket();
            sendCommandTask.setSocket(socket);
        } catch (Exception e) {
            Log.e("TAG", "failed to open socket");
        }

        address_container = (EditText) findViewById(R.id.editTextAddress);
        power_percentage = (TextView) findViewById(R.id.powerPercentage);
        back_left_prop = (SeekBar) findViewById(R.id.backLeftSeekBar);
        front_left_prop = (SeekBar) findViewById(R.id.frontLeftSeekBar);
        front_right_prop = (SeekBar) findViewById(R.id.frontRightSeekBar);
        back_right_prop = (SeekBar) findViewById(R.id.backRightSeekBar);
        all_motors = (SeekBar) findViewById(R.id.allMotorsSeekBar);
        Switch power_switch = (Switch) findViewById(R.id.switch1);

        power_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendPacket("ON");
                    Log.d("TAG", "Power On");
                } else {
                    sendPacket("OFF");
                    Log.d("TAG", "Power Off");
                }
            }
        });

        all_motors.setOnSeekBarChangeListener(new SliderListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int delta = progress - average_value;

                back_left_prop.setProgress(back_left_value + delta);
                front_left_prop.setProgress(front_left_value + delta);
                front_right_prop.setProgress(front_right_value + delta);
                back_right_prop.setProgress(back_right_value + delta);

                power_percentage.setText(progress + "%");
            }
        });

        back_left_prop.setOnSeekBarChangeListener(new SliderListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("TAG", "back left: " + progress);
                back_left_value = progress;
                sendPacket("BL" + progress);
                update_average_value();
            }
        });

        front_left_prop.setOnSeekBarChangeListener(new SliderListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("TAG", "front left: " + progress);
                front_left_value = progress;
                sendPacket("FL" + progress);
                update_average_value();
            }
        });

        front_right_prop.setOnSeekBarChangeListener(new SliderListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("TAG", "front right: " + progress);
                front_right_value = progress;
                sendPacket("FR" + progress);
                update_average_value();
            }
        });

        back_right_prop.setOnSeekBarChangeListener(new SliderListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d("TAG", "back right: " + progress);
                back_right_value = progress;
                sendPacket("BR" + progress);
                update_average_value();
            }
        });
    }

    private InetAddress getAddress() {
        String address_string = (String) address_container.getText().toString();
        Log.v("TAG", "Address: " + address_string);
        String[] numbers = address_string.split("\\.");
        byte[] address_bytes = new byte[numbers.length];

        int i = 0;
        byte b = 0;
        for (String num : numbers) {
            try {
                int a = Integer.parseInt(num);
                b = (byte) a;
            } catch (Exception e) {
                Log.e("TAG", "Failed to parse IP address");
            }
            address_bytes[i] = b;
            i++;
        }
        InetAddress address = null;
        try {
            address = InetAddress.getByAddress(address_bytes);
        } catch (Exception e) {
            Log.e("TAG", "Could not resolve host IP address");
        }
        return address;
    }

    private void sendPacket(String msg) {
        Log.v("TAG", "Sending packet");
        msg += '\0';
        byte[] buf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, getAddress(), 8989);
        new sendCommandTask().execute(packet);
    }

    private void update_average_value() {
        average_value = (back_left_value + front_left_value + back_right_value + front_right_value) / 4;
        all_motors.setProgress(average_value);
    }
}
