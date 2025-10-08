package com.example.myrajouney;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SymptomLogActivity extends AppCompatActivity {

    private SeekBar vasSeekbar, fatigueSeekbar;
    private TextView vasValue, fatigueValue;
    private EditText jointsInput, otherSymptoms, notes;
    private RadioGroup stiffnessGroup;
    private Button submitButton;

    private int vasScore = 0, fatigueScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_log);

        vasSeekbar = findViewById(R.id.vasSeekbar);
        vasValue = findViewById(R.id.vasValue);
        fatigueSeekbar = findViewById(R.id.fatigueSeekbar);
        fatigueValue = findViewById(R.id.fatigueValue);
        jointsInput = findViewById(R.id.jointsInput);
        otherSymptoms = findViewById(R.id.otherSymptoms);
        notes = findViewById(R.id.notes);
        stiffnessGroup = findViewById(R.id.stiffnessGroup);
        submitButton = findViewById(R.id.submitButton);

        // Update VAS Score
        vasSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vasScore = progress;
                vasValue.setText("VAS: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update Fatigue
        fatigueSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fatigueScore = progress;
                fatigueValue.setText("Fatigue: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Handle Submit
        submitButton.setOnClickListener(v -> {
            String joints = jointsInput.getText().toString().trim();
            String other = otherSymptoms.getText().toString().trim();
            String note = notes.getText().toString().trim();

            String stiffness = "";
            int selectedId = stiffnessGroup.getCheckedRadioButtonId();
            if (selectedId != -1) {
                RadioButton selected = findViewById(selectedId);
                stiffness = selected.getText().toString();
            }

            String result = "VAS: " + vasScore +
                    "\nJoints: " + joints +
                    "\nStiffness: " + stiffness +
                    "\nFatigue: " + fatigueScore +
                    "\nOther: " + other +
                    "\nNotes: " + note;

            Toast.makeText(SymptomLogActivity.this, result, Toast.LENGTH_LONG).show();
        });
    }
}
