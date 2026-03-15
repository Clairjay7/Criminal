package com.example.criminalgalorpot;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.criminalgalorpot.model.Crime;
import com.example.criminalgalorpot.model.CrimeRepository;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CrimeDetailActivity extends AppCompatActivity {

    private static final String EXTRA_CRIME_ID = "com.example.criminalgalorpot.crime_id";

    private Crime crime;
    private EditText titleField;
    private TextView dateButton;
    private Switch solvedSwitch;
    private EditText suspectField;
    private Button photoButton;
    private Button shareButton;
    private ImageView photoView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());

    public static Intent newIntent(Context context, UUID crimeId) {
        Intent intent = new Intent(context, CrimeDetailActivity.class);
        intent.putExtra(EXTRA_CRIME_ID, crimeId.toString());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_detail);

        String crimeIdString = getIntent().getStringExtra(EXTRA_CRIME_ID);
        UUID crimeId = UUID.fromString(crimeIdString);
        crime = CrimeRepository.getInstance().getCrime(crimeId);
        if (crime == null) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Crime Details");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        titleField = findViewById(R.id.crime_title);
        titleField.setText(crime.getTitle());
        titleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dateButton = findViewById(R.id.crime_date);
        dateButton.setText(dateFormat.format(crime.getDate()));
        Button changeDateButton = findViewById(R.id.change_date_button);
        changeDateButton.setOnClickListener(v -> showDatePicker());

        solvedSwitch = findViewById(R.id.crime_solved);
        solvedSwitch.setChecked(crime.isSolved());
        solvedSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> crime.setSolved(isChecked));

        suspectField = findViewById(R.id.crime_suspect);
        suspectField.setText(crime.getSuspect() != null ? crime.getSuspect() : "");
        suspectField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                crime.setSuspect(s.toString().trim().isEmpty() ? null : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        photoView = findViewById(R.id.crime_photo);
        photoButton = findViewById(R.id.crime_photo_button);
        updatePhotoButtonAndImage();
        photoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select photo"), 0);
        });

        shareButton = findViewById(R.id.crime_share);
        shareButton.setOnClickListener(v -> shareReport());

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            crime.setPhotoPath(uri.toString());
            updatePhotoButtonAndImage();
        }
    }

    private void updatePhotoButtonAndImage() {
        if (crime.getPhotoPath() != null) {
            photoButton.setText(R.string.change_photo);
            photoView.setVisibility(View.VISIBLE);
            photoView.setImageURI(Uri.parse(crime.getPhotoPath()));
        } else {
            photoButton.setText(R.string.attach_photo);
            photoView.setVisibility(View.GONE);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(crime.getDate());
        new android.app.DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    Date newDate = cal.getTime();
                    crime.setDate(newDate);
                    dateButton.setText(dateFormat.format(newDate));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void shareReport() {
        String title = crime.getTitle().isEmpty() ? "(No title)" : crime.getTitle();
        String solvedString = crime.isSolved() ? "Solved" : "Unsolved";
        String suspectString = crime.getSuspect() != null ? crime.getSuspect() : "No suspect";
        String report = "Crime: " + title + "\n"
                + "Date: " + dateFormat.format(crime.getDate()) + "\n"
                + "Status: " + solvedString + "\n"
                + "Suspect: " + suspectString;

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Crime Report");
        sendIntent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(sendIntent, "Share crime report with"));
    }
}
