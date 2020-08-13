package com.example.android;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TableLayout main;
    int N = 5;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main = findViewById(R.id.LL);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < N; i++) {
            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(Color.parseColor("#eeeeee"));
            for (int j = 0; j < N; j++) {
                EditText edit = new EditText(this);
                edit.setGravity(Gravity.CENTER);
                edit.setTextSize(30);
                edit.setText("");
                edit.setFilters(new InputFilter[] {new InputFilter.LengthFilter(1)});
                tr.addView(edit);
            }
            main.addView(tr, params);
        }
    }
}