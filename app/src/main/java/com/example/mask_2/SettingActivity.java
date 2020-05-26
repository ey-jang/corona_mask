package com.example.mask_2;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SettingActivity extends AppCompatActivity{


    Button save;
    EditText name;
    TextView ymBtn, txtDrug;
    TextView time;
    picker Picker;
    public String year, month, day;
    String sfName = "myFile";
    private int cnt = 0;
    private SharedPreferences appData;
    TimePickerDialog timePickerDialog;
    public static Context conYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        //설정값 불러오기
        appData = getSharedPreferences("appData", MODE_PRIVATE);

        time = (TextView) findViewById(R.id.time);
        name = (EditText) findViewById(R.id.name);
        ymBtn = (TextView) findViewById(R.id.ymBtn);
        txtDrug = (TextView) findViewById(R.id.txtDrug);
        save = (Button) findViewById(R.id.save);

        //즐겨찾기
        /*
        Intent resultIntent = getIntent();
        txtDrug.setText(resultIntent.getExtras().getString("drug"));

         */

        SharedPreferences sf = getSharedPreferences(sfName,0);
        if(Picker.check){
            Intent pickerData = getIntent(); /*데이터 수신*/
            if(pickerData.getExtras().getString("yy")!=null && pickerData.getExtras().getString("mm") != null && pickerData.getExtras().getString("dd") != null){
                year = pickerData.getExtras().getString("yy");
                month = pickerData.getExtras().getString("mm");
                day = pickerData.getExtras().getString("dd");
                ymBtn.setText(year+"년 "+month+"월 "+day+"일");
            }
            else{
                Toast.makeText(getApplicationContext(),"년월일을 다시 입력해주세요",Toast.LENGTH_SHORT).show();
            }
        }

        save.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        save();

                                        Intent intent = new Intent(getApplicationContext(), NaverMapActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(getApplicationContext(), "저장되었습니다", Toast.LENGTH_LONG).show();

                                    }

                                });


    }//onCreate






    public void onClick(View v){
        switch(v.getId()){
            case R.id.ymBtn:
                Intent picker = new Intent(getApplicationContext(), picker.class);
                startActivityForResult(picker,1000);
                break;
        }
    }

    private void save(){
        SharedPreferences.Editor editor = appData.edit();

        editor.putString("name", name.getText().toString().trim());

        editor.apply();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent){
        if(requestCode == 100 && resultCode == 1){
            txtDrug.setText(resultIntent.getStringExtra("drug"));
        }
    }

}




