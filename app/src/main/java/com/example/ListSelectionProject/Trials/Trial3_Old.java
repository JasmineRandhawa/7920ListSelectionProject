package com.example.ListSelectionProject.Trials;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.ListSelectionProject.Main.Common;
import com.example.ListSelectionProject.Main.Data;
import com.example.ListSelectionProject.Main.Trial;
import com.example.ListSelectionProject.Main.TrialAttempt;
import com.example.ListSelectionProject.NewDesign.Cities;
import com.example.ListSelectionProject.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
/*
.List Selection Old Design
*/

//This is trial 3 screen for combination large list : old design
public class Trial3_Old extends AppCompatActivity {

    //trial specific variables
    final static int trialNumber = 3; // trial number
    final static int partNumber = 1; // part number
    final static boolean isFirstTrialActivity = false; //is its first trial activity to launch
    final static boolean isLastTrialActivity = false; // is it last trial activity
    final static String trialType = "LargeOld"; // trial type
    final static String listEra = "Large";
    final static String designType = "Old Design"; // is it old design or new design
    final static String trialTask = " - List Selection"; // trial task description
    final static String popupMessage = "You are about to use " + designType + " for " + trialTask + ".";
    final static String instructionsPopUpTitle = "Instructions - Trial "; // trial instructions popup title
    final static String trialCompletePopUpTitle = "Success"; // trial success popup titile
    final static String trialCompletionPopupMessage = "You have successfully completed the Trial " + trialNumber +
            (partNumber == 1 ? (" Part " + partNumber) : "");
    final static String popUButtonText = (isLastTrialActivity ? "EMAIL DATA" : ("GO TO " +
            (partNumber == 1 ? "PART 2" : "NEXT TRIAL"))); // popup button text
    @LayoutRes
    final static int layout = R.layout.activity_old_list_selection; // trial layout

    //carry over data
    static Data data;
    static List<Trial> trials = new ArrayList<>();
    static List<TrialAttempt> listAttempts;
    static String listOptionToSelect = ""; // randomly generated list option to select
    //computed variables
    final Context context = Trial3_Old.this;  //context of current screen
    Intent nextScreenIntent;  //intent of next screen when trial ends
    String listOptionSelectedByUser = ""; // list option selected by user
    long startTimeInMillis; // start Time is time in millisec when user taps list picker
    long endTimeInMillis; // end Time when user selects correct list
    long timeTaken = 0; // time taken by user to successfully complete each attempt in  trial.
    int successAttempts = 0; // number of successful attempts when list user selected matches given list
    int failedAttempts = 0; // number of failed attempts when list selected by user does not match given list
    int totalAttempts = (Common.getTotalSuccessfulAttemptsNeededPerTrial()); // number of successful atemapts needed to complete trial
    int totalAttemptsMadeByUser = 0; // number of attempts to select given list
    int noOfTaps = 0;
    int errorCount;

    TextView selectedItemTextView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    // called on load of trial screen
    protected void onCreate(Bundle savedInstanceState) {

        // map Screen to UI
        super.onCreate(savedInstanceState);
        setContentView(layout);

        nextScreenIntent = new Intent(this, Trial3_New.class);
        TextView textViewTrialNumber = findViewById(R.id.textViewTrialNumber);
        textViewTrialNumber.setText("Trial " + trialNumber + " : Part " + partNumber);
        TextView textViewTrialName = findViewById(R.id.textViewTrialName);
        textViewTrialName.setText(trialTask + " : " + designType);
        selectedItemTextView = findViewById(R.id.selectedItemTextView);

        //initialize data
        if (isFirstTrialActivity) {
            data = new Data();
            data.Trials = new ArrayList<>();
            listAttempts = new ArrayList<>();
        } else {
            data = (Data) (getIntent().getSerializableExtra("Data"));
            trials.addAll(data.getTrials());
            listAttempts = new ArrayList<>();
        }

        ShowTrialBeginningPopUp();
        StartNextTrialAttempt(false);
        TextView textViewAttempts = findViewById(R.id.textViewAttempts);
        textViewAttempts.setText(successAttempts + " of " + totalAttempts + " completed");
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void BindlistPicker() {
        AtomicBoolean isListClicked = new AtomicBoolean(false);
        selectedItemTextView.setText("");
        ListView citiesList = findViewById(R.id.cities_list);
        ArrayAdapter<String> citiesListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Cities.GetListData());
        citiesList.setAdapter(citiesListAdapter);
        citiesList.setOnItemClickListener((adapter, v, position, arg3) -> {
            listOptionSelectedByUser = Cities.GetListData()[position];
            selectedItemTextView.setText(listOptionSelectedByUser);
        });
        citiesList.setOnTouchListener((v, event) -> {
            if(!isListClicked.get()) {
                startTimeInMillis = Calendar.getInstance().getTimeInMillis();
                isListClicked.set(true);
            }
            if(event.getAction() == MotionEvent.ACTION_UP)
                noOfTaps++;
            return false;
        });
        Button okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> {
            listOptionSelectedByUser = selectedItemTextView.getText().toString();
            selectedItemTextView.setText("");
            if (listOptionSelectedByUser.equals("")) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popUpView = layoutInflater.inflate(R.layout.activity_no_selection, null);
                ShowNoSelectionAlert(popUpView);
                StartNextTrialAttempt(true);
                timeTaken = 0;
                noOfTaps = 0;
                listOptionSelectedByUser = "";
                errorCount = 0;
            } else if (listOptionToSelect.equals(listOptionSelectedByUser)) {
                endTimeInMillis = Calendar.getInstance().getTimeInMillis();
                timeTaken = endTimeInMillis - startTimeInMillis;
                SaveData(true);
            }
            //on failure
            else {
                SaveData(false);
            }
        });
    }

    // Save Data on success or  increment error count info on failure
    private void SaveData(Boolean hasSuceeded) {
        totalAttemptsMadeByUser++;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        TextView title = new TextView(getApplicationContext());
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Black));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView;
        if (hasSuceeded) {
            successAttempts++;
            listOptionSelectedByUser = "";

            //successful attempt not equal to total attempts
            if (successAttempts < totalAttempts) {
                listAttempts.add(new TrialAttempt(successAttempts, timeTaken, noOfTaps, errorCount));
                StartNextTrialAttempt(false);
                LayoutInflater l = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View successPopUpView = layoutInflater.inflate(R.layout.activity_success, null);
                ShowSuccessFailureAlert(successPopUpView);
            } else if (successAttempts == totalAttempts) {
                popupView = layoutInflater.inflate(R.layout.activity_trial_popup, null);
                TextView textViewAttempts = findViewById(R.id.textViewAttempts);
                textViewAttempts.setText(successAttempts + " of " + totalAttempts + " completed");
                TextView textViewSuccessAttempts = popupView.findViewById(R.id.textViewSuccessAttempts);
                title.setText(trialCompletePopUpTitle);
                textViewSuccessAttempts.setText(trialCompletionPopupMessage);

                builder.setPositiveButton(popUButtonText, (dialog, which) -> {
                    // save data in list
                    listAttempts.add(new TrialAttempt(successAttempts, timeTaken, noOfTaps, errorCount));
                    if (!isFirstTrialActivity) {
                        Trial trial = new Trial(trialNumber, trialType, new ArrayList<>(),
                                totalAttemptsMadeByUser, successAttempts, failedAttempts);
                        trial.setTrialAttempts(listAttempts);
                        trials.add(trial);
                        data.setTrials(trials);
                        nextScreenIntent.putExtra("Data", data);
                        trials = new ArrayList<>();
                    } else {
                        List<Trial> trials = new ArrayList<>();
                        Trial trial = new Trial(trialNumber, trialType, new ArrayList<>(),
                                totalAttemptsMadeByUser, successAttempts, failedAttempts);
                        trial.setTrialAttempts(listAttempts);
                        trials.add(trial);
                        data.setTrials(trials);
                        nextScreenIntent.putExtra("Data", data);
                    }
                    data = new Data();
                    listAttempts = new ArrayList<>();
                    startActivity(nextScreenIntent);
                });
                builder.setCustomTitle(title);
                builder.setView(popupView);
                AlertDialog alert = builder.create();
                alert.getWindow().setLayout(400, 800);
                builder.show();
            }
        } else {
            errorCount++;
            listOptionSelectedByUser = "";
            failedAttempts++;
            StartNextTrialAttempt(true);
            View failurePopUpView = layoutInflater.inflate(R.layout.activity_failure, null);
            ShowSuccessFailureAlert(failurePopUpView);
        }
    }

    private void ShowSuccessFailureAlert(View view) {
        TextView textViewAttempts = findViewById(R.id.textViewAttempts);
        textViewAttempts.setText(successAttempts + " of " + totalAttempts + " completed");
        int toastDurationInMilliSeconds = 400;
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 400) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }

            public void onFinish() {
                toast.cancel();
            }
        };
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        toastCountDown.start();
    }

    private void ShowNoSelectionAlert(View view) {
        int toastDurationInMilliSeconds = 400;
        Toast toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        CountDownTimer toastCountDown;
        toastCountDown = new CountDownTimer(toastDurationInMilliSeconds, 400) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }

            public void onFinish() {
                toast.cancel();
            }
        };
        toast.setView(view);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        toastCountDown.start();
    }

    //show trial beginning popup
    private void ShowTrialBeginningPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        TextView title = new TextView(getApplicationContext());
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Black));
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.activity_begin_trial, null);
        TextView textViewTrial = popupView.findViewById(R.id.textViewTrial);
        textViewTrial.setText(popupMessage);
        title.setText(instructionsPopUpTitle + trialNumber + " Part " + partNumber);
        builder.setPositiveButton("START", (dialog, which) -> {

        });
        builder.setCustomTitle(title);
        builder.setView(popupView);
        AlertDialog alert = builder.create();
        alert.getWindow().setLayout(400, 800);
        builder.show();
    }

    //start new trial attempt
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void StartNextTrialAttempt(Boolean isFailure) {
        timeTaken = 0;
        noOfTaps = 0;
        BindlistPicker();
        listOptionSelectedByUser = "";
        if (!isFailure) {
            errorCount = 0;
            GenerateRandomlist();
        }
    }

    // initializes the variables to be computed later.
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void InitializeVariables() {
        timeTaken = 0;
        GenerateRandomlist();
        BindlistPicker();
        listOptionSelectedByUser = "";
        listOptionToSelect = "";
        errorCount = 0;
        successAttempts = 0;
        failedAttempts = 0;
        totalAttemptsMadeByUser = 0;
        noOfTaps = 0;
    }

    //generate random lists
    private void GenerateRandomlist() {
        if (listEra.equals("Small"))
            listOptionToSelect = Common.GenerateRandomMiddleTopListOption(listOptionToSelect);
        else if (listEra.equals("Medium"))
            listOptionToSelect = Common.GenerateRandomMiddleListOption(listOptionToSelect);
        else if (listEra.equals("Large"))
            listOptionToSelect = Common.GenerateRandomMiddleDownListOption(listOptionToSelect);
        TextView textViewGivenListOption = findViewById(R.id.textViewGivenListOption);
        textViewGivenListOption.setText(listOptionToSelect);
    }
}