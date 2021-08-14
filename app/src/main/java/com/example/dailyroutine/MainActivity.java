package com.example.dailyroutine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout hourListLayout;
    private RelativeLayout dailyTasksLayout;
    private ArrayList<Task> tasksList;

    private File rootFolder, tasksListFile;
    private int startingHour = 0, startingMin = 0, endingHour = 0, endingMin = 0;
    private final int MAX_TASK = 100;
    private final int[] posFromLeft = new int[MAX_TASK];
    private final int DELETE = 1, ERASE = 2, DO_NOT_DELETE = 0;
    private int hourHeight = -1;
    private static final String EXTENSION = ".txt", FILE_SCHEDULE_LIST = "Task List" + EXTENSION;

    private void getSDCardStoragePermission(){

        if(Build.VERSION.SDK_INT >= 23){
            if(checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            initialize();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();
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
        if (id == R.id.action_erase) {
            String s = "Do you want to erase all schedule? It is irreversible process";
            PermissionToDeleteOrErase permissionToDeleteInner =
                    new PermissionToDeleteOrErase(MainActivity.this, ERASE, s, -1);
            permissionToDeleteInner.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initialize(){
        hourListLayout = findViewById(R.id.hourListLayout);
        dailyTasksLayout = findViewById(R.id.dailyTasksLayout);
        tasksList = new ArrayList<>();
//        rootFolder = "/storage/emulated/0/Daily Routine";
        rootFolder = getFilesDir();
        getSDCardStoragePermission();
        createNotificationChannel();
        makePrimaryFileAndFolder();
        getHourHeight(new TypedValue());
        setHourList();
        readDataFromFile();
        findViewById(R.id.btnNewTask).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnNewTask){

            PickTime pickTime = new PickTime(MainActivity.this);
            pickTime.show();

            /*
            TimePickerFragment d = new TimePickerFragment();
            d.show(getSupportFragmentManager(), "timePicker");

             */
        }
    }

    private void makePrimaryFileAndFolder(){
        tasksListFile = new File(rootFolder, FILE_SCHEDULE_LIST);
        if(!rootFolder.exists()){
            if(rootFolder.mkdir()) {

                try {
                    if(!tasksListFile.createNewFile()){
                        Toast.makeText(this, "TaskListFile can't be created",
                                Toast.LENGTH_SHORT).show();
                    }
                }catch (Exception e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Root folder can't be created!",
                        Toast.LENGTH_SHORT).show();
            }

        }
        else{
            try {
                if(!tasksListFile.exists()){
                    if(!tasksListFile.createNewFile()){
                        Toast.makeText(this, "Task file can't be created",
                                Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readDataFromFile(){
        tasksList.clear();
        File rootFolderFile = new File(rootFolder, "Task List.txt");
        if(rootFolderFile.exists()){

            try {
                Scanner sc = new Scanner(rootFolderFile);
                while (sc.hasNextLine()){
                    String title = sc.nextLine();

                    File ithFile = new File(rootFolder, title + ".txt");

                    Scanner sc2 = new Scanner(ithFile);



                    int strtHour = sc2.nextInt(),
                            strtMin = sc2.nextInt(),
                            endHour = sc2.nextInt(),
                            endMin = sc2.nextInt();

                    StringBuilder details = new StringBuilder();
                    while (sc2.hasNextLine()){
                        details.append(sc2.nextLine());
                    }

                    sc2.close();

                    Task task = new Task(strtHour,strtMin,endHour,
                            endMin, title, details.toString());

                    tasksList.add(task);

                }
                sc.close();

                setTaskList();

            }catch (Exception e){
                Toast.makeText(MainActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveDataToFile(){

        try {
            PrintWriter pr1 = new PrintWriter(tasksListFile);

            for(int i = 0; i < tasksList.size(); i++){
                String title = tasksList.get(i).getTitle();
                File titleFile = new File(rootFolder,  title +  EXTENSION);

                if(!titleFile.exists()){
                    if(!titleFile.createNewFile()){
                        Toast.makeText(this, "titleFile can't be created",
                                Toast.LENGTH_SHORT).show();
                    }
                }

                PrintWriter pr2 = new PrintWriter(titleFile);

                pr1.println(title);

                pr2.println(tasksList.get(i).getStartingHour());
                pr2.println(tasksList.get(i).getStartingMin());
                pr2.println(tasksList.get(i).getEndingHour());
                pr2.println(tasksList.get(i).getEndingMin());
                pr2.println(tasksList.get(i).getDetails());
                pr2.close();


            }
            pr1.close();

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private String getTime(int hour, int min){
        String time = "";

        if(hour < 12){
            if(hour < 10){
                if(min < 10) time = "0" + hour + ":0" + min + " AM";
                else time = "0" + hour + ":" + min + " AM";
            }
            else{
                if(min < 10) time = hour + ":0" + min + " AM";
                else time = hour + ":" + min + " AM";
            }
        }
        else if(hour == 12){
            if(min < 10) time = 12 + ":0" + min + " PM";
            else time = 12 + ":" + min + " PM";
        }
        else if(hour < 24){
            if(min < 10) time = (hour - 12) + ":0" + min + " PM";
            else time = (hour - 12) + ":" + min + " PM";
        }
        else if(hour >= 24){
            if(hour == 24){
                if(min < 10) time = 12 + ":0" + min + " AM";
                else time = 12 + ":" + min + " AM";
            }
            else{
                if(min < 10) time = "0" + (hour - 24) + ":0" + min + " AM";
                else time = "0" + (hour - 24) + ":" + min + " AM";
            }
        }

        return time;
    }

    private int getScheduleHeight(int sHour, int sMin, int eHour, int eMin){
        /*
        TODO
            Actual equation is:
        float height = ((eHour + (eMin/(float)60)) -
                (sHour + (sMin/(float)60)))*getHourHeight()*2;

        TODO
            After simplification:
        return  (eHour - sHour)*getHourHeight()*2 + (eMin - sMin)*2;
        */
        return  (eHour - sHour)*getHourHeight()*2 + (eMin - sMin)*2;
    }

    private int getScheduleWidth(){
        return RelativeLayout.LayoutParams.MATCH_PARENT;
    }

    private int getStartingPositionFromTop(int sHour, int sMin){
        /*
        TODO
            Actual equation is:
        positionFromTop = (sHour + (sMin/(float)60))*getHourHeight()*2 +
                getHourHeight()/(float)2;

        TODO
            After simplification:
        return sHour*getHourHeight()*2 + sMin*2 + (int)(getHourHeight()/(float)2);

         */
        return sHour*getHourHeight()*2 + sMin*2 + (int)(getHourHeight()/(float)2);

    }

    private int getEndingPositionFromTop(int eHour, int eMin){
        /*
        TODO
            Actual equation is:
        positionFromTop2 = (eHour + (eMin/(float)60))*getHourHeight()*2 +
                getHourHeight()/(float)2 - 5;
        TODO
            And after simplification:
        return eHour*getHourHeight()*2 + eMin*2 + (int)(getHourHeight()/(float)2) - 5;
         */
        return eHour*getHourHeight()*2 + eMin*2 + (int)(getHourHeight()/(float)2) - 5;
    }

    private void setTaskList(){

        getColorAndPositionFromLeft();

        for(int i = 0; i < tasksList.size(); i++){
            final TextView tv = new TextView(MainActivity.this),
                    txtLine1 = new TextView(this),
                    txtLine2 = new TextView(this);

            int strtngHr = tasksList.get(i).getStartingHour(),
                    strtngMn = tasksList.get(i).getStartingMin(),
                    endngHr = tasksList.get(i).getEndingHour(),
                    endngMn = tasksList.get(i).getEndingMin();

            if(strtngHr > endngHr) endngHr += 24;


            String s = "(" + (i + 1) + "). " + tasksList.get(i).getTitle() + ": " +
                    getTime(strtngHr, strtngMn)+ "--" + getTime(endngHr, endngMn);
            tv.setText(s);
            tv.setTextSize(20);

            int x = posFromLeft[i] + 192, y = posFromLeft[i] + 100, z = posFromLeft[i] + 150,
                    positionFromLeft = posFromLeft[i];

            int color = Color.rgb(x, y, z);
            tv.setBackgroundColor(color);

            int height = getScheduleHeight(strtngHr, strtngMn, endngHr, endngMn),
                    width = getScheduleWidth(),
                    positionFromTop = getStartingPositionFromTop(strtngHr, strtngMn),
                    positionFromTop2 = getEndingPositionFromTop(endngHr, endngMn);

            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(width, height);
            layoutParams.topMargin = (positionFromTop);
            layoutParams.leftMargin = positionFromLeft;
            tv.setLayoutParams(layoutParams);

            RelativeLayout.LayoutParams lineParam = new RelativeLayout.LayoutParams(positionFromLeft, 5);
            lineParam.topMargin = positionFromTop;
            lineParam.leftMargin = 0;
            txtLine1.setLayoutParams(lineParam);
            txtLine1.setBackgroundColor(color);

            RelativeLayout.LayoutParams lineParam2 = new RelativeLayout.LayoutParams(positionFromLeft, 5);
            lineParam2.topMargin = positionFromTop2;
            lineParam2.leftMargin = 0;
            txtLine2.setLayoutParams(lineParam2);
            txtLine2.setBackgroundColor(color);

            final int finalI = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String title, time, details;

                    time = getTime(tasksList.get(finalI).getStartingHour(),
                            tasksList.get(finalI).getStartingMin()) + "--" +
                            getTime(tasksList.get(finalI).getEndingHour(),
                            tasksList.get(finalI).getEndingMin());
                    title = tasksList.get(finalI).getTitle();
                    details = tasksList.get(finalI).getDetails();

                    int strD = tasksList.get(finalI).getStartingHour()*60 + tasksList.get(finalI)
                            .getStartingMin(),
                          endD = tasksList.get(finalI).getEndingHour()*60 + tasksList.get(finalI).getEndingMin(),
                            dr = strD > endD? strD - endD: endD - strD;

                    double duration = ((double)dr/60.0);
                    DecimalFormat df =  new DecimalFormat("0.##");

                    String s = "Schedule No: " + (finalI + 1) + "\nTitle: " + title + "\nTime: " + time +
                            "\nDuration: " + df.format(duration) + " Hour" + "\nDetails: " + details;

                    PermissionToDeleteOrErase permissionToDeleteOrErase =
                            new PermissionToDeleteOrErase(MainActivity.this, DO_NOT_DELETE, s, -1);
                    permissionToDeleteOrErase.show();
                }
            });

            tv.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    String s = "Do you want to delete the schedule " + tv.getText().toString() + "?";

                    PermissionToDeleteOrErase permissionToDeleteOrErase =
                            new PermissionToDeleteOrErase(MainActivity.this, DELETE, s, finalI);

                    permissionToDeleteOrErase.show();

                    return true;
                }
            });
            dailyTasksLayout.addView(txtLine1);
            dailyTasksLayout.addView(txtLine2);
            dailyTasksLayout.addView(tv);
        }

        startingHour = startingMin = endingMin = endingHour = 0;
    }

    private void setHourList(){
        for(int i = 0; i <= 36; i++){
            TextView txtHour = new TextView(MainActivity.this),
                    txtGap = new TextView(MainActivity.this);

            hourListLayout.addView(txtHour);
            if(i < 36) hourListLayout.addView(txtGap);


            txtHour.setGravity(Gravity.CENTER);
            txtHour.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int)getHourHeight()));
            txtGap.setGravity(Gravity.END);
            txtGap.setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getHourHeight()));

            String s;
            if(i == 0) {
                s = "12.00 AM-----";
                txtHour.setText(s);
            }
            else if(i <= 9) {
                s = "0" + i + ".00 AM-----";
                txtHour.setText(s);
            }
            else if(i < 12) {
                s = i + ".00 AM-----";
                txtHour.setText(s);
            }
            else if(i == 12) {
                s = "12" + ".00 PM-----";
                txtHour.setText(s);
            }
            else if(i - 12 <= 9) {
                s = "0" + (i - 12) + ".00 PM-----";
                txtHour.setText(s);
            }
            else if(i - 12 < 12) {
                s = (i - 12) + ".00 PM-----";
                txtHour.setText(s);
            }
            else if(i == 24) {
                s = "12.00 AM-----";
                txtHour.setText(s);
            }

            else if(i - 24 <= 9) {
                s = "0" + (i - 24) + ".00 AM-----";
                txtHour.setText(s);
            }
            else if(i - 24 < 12) {
                s = (i - 24) + ".00 AM-----";
                txtHour.setText(s);
            }
            else if(i == 36) {
                s = "12.00 PM-----";
                txtHour.setText(s);
            }

            s = "-----";
            txtGap.setText(s);
        }

    }

    private void getColorAndPositionFromLeft(){

        for(int i = 0; i <= tasksList.size(); i++){
            posFromLeft[i] = 0;
        }

        for(int j = 0; j < tasksList.size(); j++){

            int startingTime = tasksList.get(j).getStartingHour()*60 + tasksList.get(j).getStartingMin();
            for(int i = 0; i < j; i++){
                int endingTime = tasksList.get(i).getEndingHour()*60 + tasksList.get(i).getEndingMin();

                if(startingTime <= endingTime){
                    posFromLeft[j] = posFromLeft[i] + 50;
                }
            }
        }
    }

    private void getHourHeight(TypedValue outValue){

        getResources().getValue(R.dimen.hour_size, outValue, true);
        hourHeight = (int)outValue.getFloat();

    }

    private int getHourHeight(){
        return hourHeight;
    }

    private class PermissionToDeleteOrErase extends Dialog implements View.OnClickListener {

        int command, index; String commandString;
        public PermissionToDeleteOrErase(Context context, int command, String commandString, int index){
            super(context);
            this.command = command;
            this.commandString = commandString;
            this.index = index;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            setContentView(R.layout.permission_delete);
            initialize();

        }

        public void initialize(){
            Button btnYes = findViewById(R.id.btnYes);
            Button btnNo = findViewById(R.id.btnNo);
            TextView txtPermToDelete = findViewById(R.id.txtPermToDelete);
            LinearLayout permLayout = findViewById(R.id.permLayout);

            String s = "";
            if(command == ERASE) s = "Erase";
            else if(command == DELETE) s = "Delete";
            else if(command == DO_NOT_DELETE){
                btnYes.setVisibility(View.GONE);
                btnNo.setVisibility(View.GONE);
                permLayout.setVisibility(View.GONE);
            }

            btnYes.setText(s);
            txtPermToDelete.setText(commandString);

            btnYes.setOnClickListener(this);
            btnNo.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.btnNo){

                dismiss();
            }
            else if(v.getId() == R.id.btnYes){

                if(command == ERASE){
                    deleteAllFiles();
                    tasksList.clear();
                    dailyTasksLayout.removeAllViews();
                }
                else if(command == DELETE){

                    tasksList.remove(index);
                    deleteAllFiles();
                    dailyTasksLayout.removeAllViews();
                    makePrimaryFileAndFolder();
                    saveDataToFile();
                    getColorAndPositionFromLeft();
                    setTaskList();
                }
                dismiss();
            }

        }
    }

    private void deleteAllFiles(){

        if(rootFolder.exists()){
            String[]entries = rootFolder.list();
            for(String s: entries){
                File currentFile = new File(rootFolder.getPath(), s);
                if(!currentFile.delete()) {
                    Toast.makeText(this, "File deletion failed", Toast.LENGTH_SHORT).show();
                }
            }
            if(rootFolder.delete()) {
                Toast.makeText(this, "Folder deletion failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class PickTime extends Dialog implements View.OnClickListener {

        private Button btnStartingTimeOkay, btnEndingTimeOkay, btnStartingTimeReset, btnEndingTimeReset;
        private TimePicker startingTimePicker, endingTimePicker;
        private TextView selectedTime, selectedTime1;
        private boolean click1 = false, click2 = false;

        public PickTime(Context context){
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_ACTION_BAR);
            setContentView(R.layout.pick_time_dialog);
            setCancelable(false);
            initialize();
        }

        public void initialize(){
            btnStartingTimeOkay = findViewById(R.id.btnStartingTimeOkay);
            btnEndingTimeOkay = findViewById(R.id.btnEndingTimeOkay);
            startingTimePicker = findViewById(R.id.startingTimePicker);
            endingTimePicker = findViewById(R.id.endingTimePicker);
            selectedTime = findViewById(R.id.selectedTime);
            selectedTime1 = findViewById(R.id.selectedTime1);
            btnStartingTimeReset = findViewById(R.id.btnStartingTimeReset);
            btnEndingTimeReset = findViewById(R.id.btnEndingTimeReset1);
            btnStartingTimeOkay.setOnClickListener(this);
            btnEndingTimeOkay.setOnClickListener(this);
            findViewById(R.id.btnOk).setOnClickListener(this);
            findViewById(R.id.btnCancel).setOnClickListener(this);
            btnStartingTimeReset.setOnClickListener(this);
            btnEndingTimeReset.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnStartingTimeOkay){

                startingHour = startingTimePicker.getHour();
                startingMin = startingTimePicker.getMinute();

                btnStartingTimeOkay.setVisibility(View.GONE);
                btnStartingTimeReset.setVisibility(View.VISIBLE);
                startingTimePicker.setVisibility(View.GONE);
                selectedTime.setVisibility(View.VISIBLE);
                String s = "Time- " + getTime(startingHour, startingMin);
                selectedTime.setText(s);
                click1 = true;

            }
            if(v.getId() == R.id.btnEndingTimeOkay){

                endingHour = endingTimePicker.getHour();
                endingMin = endingTimePicker.getMinute();
                btnEndingTimeOkay.setVisibility(View.GONE);
                btnEndingTimeReset.setVisibility(View.VISIBLE);
                selectedTime1.setVisibility(View.VISIBLE);
                endingTimePicker.setVisibility(View.GONE);
                String s = "Time- " + getTime(endingHour, endingMin);
                selectedTime1.setText(s);
                click2 = true;
            }

            if(v.getId() == R.id.btnStartingTimeReset){
                click1 = false;
                btnStartingTimeReset.setVisibility(View.GONE);
                btnStartingTimeOkay.setVisibility(View.VISIBLE);
                selectedTime.setVisibility(View.GONE);
                startingTimePicker.setVisibility(View.VISIBLE);
            }

            if(v.getId() == R.id.btnEndingTimeReset1){
                btnEndingTimeOkay.setVisibility(View.VISIBLE);
                btnEndingTimeReset.setVisibility(View.GONE);
                selectedTime1.setVisibility(View.GONE);
                endingTimePicker.setVisibility(View.VISIBLE);
                click2 = false;
            }

            if(v.getId() == R.id.btnOk){
                if(click2 && click1){

                    if(startingHour > endingHour) endingHour += 24;
                    click1 = false;
                    click2 = false;

                    WriteScheduleInfo d = new WriteScheduleInfo(MainActivity.this);
                    d.show();

                    dismiss();
                }
                else{
                    if(!click1 && !click2){
                        Toast.makeText(MainActivity.this, "Select starting and ending time!",
                                Toast.LENGTH_LONG).show();
                    }
                    else if(!click1){
                        Toast.makeText(MainActivity.this, "Select starting time!",
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Select ending time!",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            if(v.getId() == R.id.btnCancel){
                click1 = false;
                click2 = false;
                dismiss();
            }
        }
    }

    private class WriteScheduleInfo extends Dialog implements View.OnClickListener{

        public WriteScheduleInfo(@NonNull Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.schedule_info);
            initialize();
        }

        private void initialize(){
            findViewById(R.id.btnCancel).setOnClickListener(this);
            findViewById(R.id.btnSave).setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if(v.getId() == R.id.btnSave){
                EditText edtTitle = findViewById(R.id.edtTitle),
                        edtDetails = findViewById(R.id.edtDetails);
                String strTitle = edtTitle.getText().toString().trim(),
                        strDetails = edtDetails.getText().toString().trim();
                if(strTitle.isEmpty()) strTitle = "N/A";
                if(strDetails.isEmpty()) strDetails = "N/A";

                int isExist = isExists(strTitle);

                if(isExist >= 0){
                    edtTitle.setError("Title exists!\nat schedule: " +
                            isExist + "\nChange it slightly");
                    edtTitle.requestFocus();
                    return;
                }

                int isOvrlp = isOverLapTask(startingHour, startingMin,
                        endingHour, endingMin, strTitle);

                if(isOvrlp >= 0){
                    Toast.makeText(MainActivity.this,
                            "Added but\nOverlap with\n" + (isOvrlp + 1) +
                                    "th Schedule",Toast.LENGTH_LONG).show();
                }

                Task task = new Task(startingHour,startingMin,
                        endingHour, endingMin, strTitle, strDetails);

                //setAlarm(startingHour,startingMin, strTitle, strDetails);

                startingHour = startingMin = endingMin = endingHour = 0;

                tasksList.add(task);
                Collections.sort(tasksList, new SortTasksList());

                saveDataToFile();

                dailyTasksLayout.removeAllViews();

                setTaskList();

                dismiss();
            }
            else if(v.getId() == R.id.btnCancel){
                dismiss();
            }
        }

        private int isOverLapTask(int startingHour, int startingMin,
                                  int endingHour, int endingMin, String taskTitleString){

            for(int i = 0; i < tasksList.size(); i++){
                if(!tasksList.get(i).getTitle().equals(taskTitleString)){
                    int currStartingTime =
                            tasksList.get(i).getStartingHour()*60 + tasksList.get(i).getStartingMin(),
                            currEndingTime = tasksList.get(i).getEndingHour()*60 + tasksList.get(i).getEndingMin(),
                            inputStartTime = startingHour*60 + startingMin,
                            inputEndTime = endingHour*60 + endingMin;
                    if((currStartingTime <= inputStartTime && currEndingTime >= inputEndTime) ||
                            (inputStartTime <= currStartingTime && inputEndTime >= currEndingTime)){
                        return i;
                    }

                }
            }

            return -1;
        }

        private int isExists(String taskTitleString){
            for(int i = 0; i < tasksList.size(); i++){
                if(tasksList.get(i).getTitle().equals(taskTitleString)) return i + 1;
            }
            return -1;
        }

    }

    private void setAlarm(int startingHour, int startingMin, String title, String details) {
        AlarmManager aManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        Intent i = new Intent();
        String action = "com.example.dailyroutine" + "." + title; getPackageName();
        i.setAction(action);
        i.addCategory("android.intent.category.DEFAULT");
        i.putExtra("TITLE", title);
        i.putExtra("MESSAGE", details);
        i.putExtra("LONG_TEXT", details);
        
        i.putExtra("CHANEL_ID", getResources().getString(R.string.not_id_start));

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);

        long tim = SystemClock.elapsedRealtime(), tim1 = new MyTime(startingHour, startingMin).toMills();

        Log.i("test", tim + " " + tim1 + " " + (tim1 - tim)/1000/60);

        aManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, tim, tim1 - tim, pi);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    AlertReceiver.CH_START_ID, AlertReceiver.CH_NAME, importance);
            channel.setDescription(AlertReceiver.CH_DESC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }


        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Log.i("test", hourOfDay + " " + minute);
        }
    }

}