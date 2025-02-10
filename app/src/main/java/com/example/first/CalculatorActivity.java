package com.example.first;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class CalculatorActivity extends AppCompatActivity{
    private StringBuilder expression = new StringBuilder();
    private TextView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator_activity);
        display = findViewById(R.id.displayText);

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button button = (Button) v;
                String buttonText = button.getText().toString();

                if(buttonText.equals("=")) {
                    evaluateExpression();
                } else if (buttonText.equals("x")) {
                    if (expression.length() > 0) {
                        expression.deleteCharAt(expression.length() - 1);
                    }
                } else {
                    expression.append(buttonText);
                }
                display.setText(expression.length() == 0 ? "CALC" : expression.toString());
            }
        };

        int[] buttonIds = {
                R.id.btn0, R.id.btn1,R.id.btn2, R.id.btn3,R.id.btn4, R.id.btn5,
                R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnPlus, R.id.btnMinus,
                R.id.btnX, R.id.btnEqual,
        };
        for (int id: buttonIds) {
            findViewById(id).setOnClickListener(buttonClickListener);
        }
    }

    private void evaluateExpression() {
        try {
            String exp = expression.toString();
            String[] parts;
            int result;

            if (exp.contains("+")){
                parts = exp.split("\\+");
                if (parts.length != 2) throw new Exception("Invalid expression");
                result = Integer.parseInt(parts[0].trim()) + Integer.parseInt(parts[1].trim());

            } else if (exp.contains("-")){
                parts = exp.split("-");
                if (parts.length != 2) throw new Exception("Invalid expression");
                result = Integer.parseInt(parts[0].trim()) - Integer.parseInt(parts[1].trim());

            } else {    
                throw new Exception("Invalid expression");
            }
            expression.setLength(0);
            expression.append(result);  
        } catch (Exception e) {
            expression.setLength(0);
            expression.append("error");
        }
        display.setText(expression.toString());
    }
}
