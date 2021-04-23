package com.example.ListSelectionProject.Main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ListSelectionProject.R;
import com.example.ListSelectionProject.Trials.Trial1_Old;

public class InstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
    }

    public void redirectToTrial1Screen(View view) {
        Intent intent = new Intent(this, Trial1_Old.class);
        startActivity(intent);
    }
}