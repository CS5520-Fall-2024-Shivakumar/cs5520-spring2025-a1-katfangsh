package com.example.first;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PrimeSearchActivity extends AppCompatActivity {
    private TextView currentNumberText;
    private TextView latestPrimeText;
    private CheckBox pacifierSwitch;
    private Button findPrimesButton;
    private Button terminateButton;
    private Thread searchThread;
    private volatile boolean isSearching = false;
    private long currentNumber = 3;
    private long latestPrime = 3;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String KEY_CURRENT_NUMBER = "currentNumber";
    private static final String KEY_LATEST_PRIME = "latestPrime";
    private static final String KEY_IS_SEARCHING = "isSearching";
    private static final String KEY_PACIFIER_STATE = "pacifierState";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prime_search_activity);

        // Initialize UI elements
        initializeViews();

        // Restore state if available
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        // Set up button click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        currentNumberText = findViewById(R.id.currentNumberText);
        latestPrimeText = findViewById(R.id.latestPrimeText);
        pacifierSwitch = findViewById(R.id.pacifierSwitch);
        findPrimesButton = findViewById(R.id.findPrimesButton);
        terminateButton = findViewById(R.id.terminateButton);

        // Initialize text views with default values
        currentNumberText.setText(String.valueOf(currentNumber));
        latestPrimeText.setText(String.valueOf(latestPrime));
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {
        currentNumber = savedInstanceState.getLong(KEY_CURRENT_NUMBER, 3);
        latestPrime = savedInstanceState.getLong(KEY_LATEST_PRIME, 3);
        isSearching = savedInstanceState.getBoolean(KEY_IS_SEARCHING, false);
        pacifierSwitch.setChecked(savedInstanceState.getBoolean(KEY_PACIFIER_STATE, false));

        // Update UI with restored values
        currentNumberText.setText(String.valueOf(currentNumber));
        latestPrimeText.setText(String.valueOf(latestPrime));

        // If search was running, restart it at the saved number
        if (isSearching) {
            startPrimeSearch();
        }
    }

    private void setupClickListeners() {
        findPrimesButton.setOnClickListener(v -> {
            if (!isSearching) {
                // Reset to 3 only when starting a new search
                if (searchThread == null || !searchThread.isAlive()) {
                    currentNumber = 3;
                }
                startPrimeSearch();
                updateButtonStates(true);
            }
        });

        terminateButton.setOnClickListener(v -> {
            stopPrimeSearch();
            updateButtonStates(false);
        });
    }

    private void updateButtonStates(boolean searching) {
        findPrimesButton.setEnabled(!searching);
        terminateButton.setEnabled(searching);
    }

    private void startPrimeSearch() {
        isSearching = true;
        searchThread = new Thread(() -> {
            try {
                while (isSearching) {
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }

                    if (isPrime(currentNumber)) {
                        final long prime = currentNumber;
                        mainHandler.post(() -> {
                            latestPrime = prime;
                            latestPrimeText.setText(String.valueOf(prime));
                        });
                    }

                    final long current = currentNumber;
                    mainHandler.post(() -> currentNumberText.setText(String.valueOf(current)));

                    currentNumber += 2;
                }
            } catch (InterruptedException e) {
                // Search was interrupted, clean up
                isSearching = false;
                mainHandler.post(() -> updateButtonStates(false));
            }
        });
        searchThread.start();
    }
    private void stopPrimeSearch() {
        isSearching = false;
        if (searchThread != null && searchThread.isAlive()) {
            searchThread.interrupt();
            try {
                // Wait for the thread to finish, but not indefinitely
                searchThread.join(1000);
            } catch (InterruptedException e) {
                // Handle interruption if necessary
            }
        }
    }

    private boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0) return false;

        for (long i = 3; i <= Math.sqrt(n); i += 2) {
            if (Thread.interrupted()) {
                return false;
            }
            if (n % i == 0) return false;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_CURRENT_NUMBER, currentNumber);
        outState.putLong(KEY_LATEST_PRIME, latestPrime);
        outState.putBoolean(KEY_IS_SEARCHING, isSearching);
        outState.putBoolean(KEY_PACIFIER_STATE, pacifierSwitch.isChecked());
    }

    @Override
    public void onBackPressed() {
        if (isSearching) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Exit")
                    .setMessage("Are you sure you want to stop the prime search and exit?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        stopPrimeSearch();
                        super.onBackPressed();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPrimeSearch();
    }
}
