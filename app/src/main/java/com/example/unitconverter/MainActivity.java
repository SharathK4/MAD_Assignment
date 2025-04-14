// MainActivity.java
package com.example.unitconverter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private EditText editTextFrom;
    private EditText editTextTo;

    private String[] units = {"Feet", "Inches", "Centimeters", "Meters", "Yards"};
    private String fromUnit = "Feet";
    private String toUnit = "Meters";
    private boolean isUpdatingFromEditText = false;
    private boolean isUpdatingToEditText = false;
    private DecimalFormat df = new DecimalFormat("#.######");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        editTextFrom = findViewById(R.id.editTextFrom);
        editTextTo = findViewById(R.id.editTextTo);

        setupSpinners();
        setupEditTexts();
    }

    private void setupSpinners() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        // Set default values
        spinnerFrom.setSelection(0); // Feet
        spinnerTo.setSelection(3);   // Meters

        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromUnit = units[position];
                convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toUnit = units[position];
                convert();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupEditTexts() {
        editTextFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingFromEditText) {
                    isUpdatingToEditText = true;
                    convert();
                    isUpdatingToEditText = false;
                }
            }
        });

        editTextTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isUpdatingToEditText) {
                    isUpdatingFromEditText = true;
                    reverseConvert();
                    isUpdatingFromEditText = false;
                }
            }
        });
    }

    private void convert() {
        String fromValueStr = editTextFrom.getText().toString();
        if (fromValueStr.isEmpty()) {
            editTextTo.setText("");
            return;
        }

        try {
            double fromValue = Double.parseDouble(fromValueStr);
            double result = performConversion(fromValue, fromUnit, toUnit);
            editTextTo.setText(df.format(result));
        } catch (NumberFormatException e) {
            editTextTo.setText("");
        }
    }

    private void reverseConvert() {
        String toValueStr = editTextTo.getText().toString();
        if (toValueStr.isEmpty()) {
            editTextFrom.setText("");
            return;
        }

        try {
            double toValue = Double.parseDouble(toValueStr);
            double result = performConversion(toValue, toUnit, fromUnit);
            editTextFrom.setText(df.format(result));
        } catch (NumberFormatException e) {
            editTextFrom.setText("");
        }
    }

    private double performConversion(double value, String from, String to) {
        // First convert to meters (our base unit)
        double valueInMeters;

        switch (from) {
            case "Feet":
                valueInMeters = value * 0.3048;
                break;
            case "Inches":
                valueInMeters = value * 0.0254;
                break;
            case "Centimeters":
                valueInMeters = value * 0.01;
                break;
            case "Meters":
                valueInMeters = value;
                break;
            case "Yards":
                valueInMeters = value * 0.9144;
                break;
            default:
                valueInMeters = value;
                break;
        }

        // Then convert from meters to target unit
        switch (to) {
            case "Feet":
                return valueInMeters / 0.3048;
            case "Inches":
                return valueInMeters / 0.0254;
            case "Centimeters":
                return valueInMeters / 0.01;
            case "Meters":
                return valueInMeters;
            case "Yards":
                return valueInMeters / 0.9144;
            default:
                return valueInMeters;
        }
    }
}