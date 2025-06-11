package com.example.eventorganizer.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eventorganizer.R;
import com.example.eventorganizer.models.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddEditEventActivity extends AppCompatActivity {

    // UI Components
    private EditText etTitle, etDate, etTime, etDescription, etLocation;
    private ImageView imageViewPreview;
    private Button buttonChooseImage, buttonSubmit;
    private TextView textUploadImage, textUploadSubtitle;
    private ProgressBar progressBar;

    // State & Data Variables
    private Uri imageUri;
    private String existingImageUrl;
    private boolean isEditMode = false;
    private String eventId = null;

    // Services
    private FirebaseFirestore db;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // ActivityResultLauncher for picking an image from the gallery
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    showImagePreview(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        db = FirebaseFirestore.getInstance();

        setupViews();
        setupToolbar();
        setupListeners();

        // Check if we are in edit mode and load data if necessary
        if (getIntent().hasExtra("EVENT_ID")) {
            isEditMode = true;
            eventId = getIntent().getStringExtra("EVENT_ID");
            ((MaterialToolbar) findViewById(R.id.toolbar)).setTitle("Edit Event");
            buttonSubmit.setText("Update Event");
            loadEventData();
        } else {
            isEditMode = false;
            ((MaterialToolbar) findViewById(R.id.toolbar)).setTitle("Create Event");
        }
    }

    private void setupViews() {
        etTitle = findViewById(R.id.editTextEventTitle);
        etDate = findViewById(R.id.editTextDate);
        etTime = findViewById(R.id.editTextTime);
        etDescription = findViewById(R.id.editTextDescription);
        etLocation = findViewById(R.id.editTextLocation);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        textUploadImage = findViewById(R.id.textUploadImage);
        textUploadSubtitle = findViewById(R.id.textUploadSubtitle);
        buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        etDate.setOnClickListener(v -> showDatePickerDialog());
        etTime.setOnClickListener(v -> showTimePickerDialog());
        buttonChooseImage.setOnClickListener(v -> mGetContent.launch("image/*"));
        buttonSubmit.setOnClickListener(v -> saveEvent());
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            etDate.setText(selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog() {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String amPm = hourOfDay < 12 ? "AM" : "PM";
            int hour = hourOfDay % 12;
            if (hour == 0) hour = 12; // Adjust for 12 AM/PM
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
            etTime.setText(selectedTime);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show(); // false for 12-hour format
    }

    private void showImagePreview(Object imageData) {
        imageViewPreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(imageData).into(imageViewPreview);
        textUploadImage.setVisibility(View.GONE);
        textUploadSubtitle.setVisibility(View.GONE);
        buttonChooseImage.setText("Change Image");
    }

    private void loadEventData() {
        setLoading(true);
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            etTitle.setText(event.getTitle());
                            etDescription.setText(event.getDescription());
                            etDate.setText(event.getDate());
                            etTime.setText(event.getTime());
                            etLocation.setText(event.getLocation());
                            existingImageUrl = event.getImageUrl();

                            if (existingImageUrl != null && !existingImageUrl.isEmpty()) {
                                showImagePreview(existingImageUrl);
                            }
                        }
                    }
                    setLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void saveEvent() {
        String title = etTitle.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (imageUri != null) {
            uploadToImgbbAndSave(title, description, date, time, location);
        } else {
            saveEventToFirestore(title, description, date, time, location, existingImageUrl);
        }
    }

    private void uploadToImgbbAndSave(String title, String description, String date, String time, String location) {
        executorService.execute(() -> {
            try {
                byte[] imageData = getBytesFromUri(imageUri);
                if (imageData == null) throw new IOException("Could not read image data.");

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("key", "2b00147c9e92a6a985ebdc4496d9c6ca")
                        .addFormDataPart("image", "image.jpg",
                                RequestBody.create(imageData, MediaType.parse("image/jpeg")))
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.imgbb.com/1/upload")
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) throw new IOException("Upload failed: " + response);

                    String responseBody = response.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    if (!json.getBoolean("success")) {
                        throw new IOException("API returned an error: " + responseBody);
                    }
                    String imageUrl = json.getJSONObject("data").getString("url");

                    runOnUiThread(() -> saveEventToFirestore(title, description, date, time, location, imageUrl));
                }
            } catch (Exception e) {
                Log.e("ImgbbUpload", "Upload failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(AddEditEventActivity.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
            }
        });
    }

    private byte[] getBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void saveEventToFirestore(String title, String description, String date, String time, String location, String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        Event event = new Event();
        event.setTitle(title);
        event.setDate(date);
        event.setTime(time);
        event.setDescription(description);
        event.setLocation(location);
        event.setImageUrl(imageUrl);
        event.setCreatorId(currentUser.getUid());
        event.setCreatorEmail(currentUser.getEmail());

        Runnable successAction = () -> {
            Toast.makeText(this, isEditMode ? "Event Updated" : "Event Created", Toast.LENGTH_SHORT).show();
            finish();
        };

        if (isEditMode) {
            db.collection("events").document(eventId).set(event)
                    .addOnSuccessListener(aVoid -> successAction.run())
                    .addOnFailureListener(this::handleFailure);
        } else {
            db.collection("events").add(event)
                    .addOnSuccessListener(documentReference -> successAction.run())
                    .addOnFailureListener(this::handleFailure);
        }
    }

    private void handleFailure(Exception e) {
        Toast.makeText(this, "Operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        setLoading(false);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSubmit.setEnabled(!isLoading);
        buttonChooseImage.setEnabled(!isLoading);
    }
}
