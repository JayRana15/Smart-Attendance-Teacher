package com.example.de_2bteacherapp;

import static java.time.MonthDay.now;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.acl.Permission;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import Controller.controller;
import ResponseModel.responseModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class lectureActivity extends AppCompatActivity {

    TextView emailIDTV,nameTV;
    Button addLectureBtn;
    ImageView dpIV,QRIV;
    EditText subject_nameET;
    TextInputLayout tempLayout2;

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;

    String email_id,name,subject_name = "";

    private static int REQUEST_CODE = 100;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.light_blue));

        emailIDTV = findViewById(R.id.emailIDTV);
        nameTV = findViewById(R.id.name);
        addLectureBtn = findViewById(R.id.addLectureBtn);
        dpIV = findViewById(R.id.dpIV);
        QRIV = findViewById(R.id.QRIV);
        subject_nameET = findViewById(R.id.subject_nameET);
        tempLayout2 = findViewById(R.id.tempLayout2);

        Toolbar toolbar1 = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar1);
        setTitle("Add lecture");


        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        Uri photo = null;
        if (acc != null) {
            email_id = acc.getEmail();
            photo = acc.getPhotoUrl();
            name = acc.getDisplayName();
        }

        Glide.with(this).load(photo).apply(RequestOptions.circleCropTransform()).into(dpIV);
        nameTV.setText(name);
        emailIDTV.setText(email_id);






        addLectureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject_name = subject_nameET.getText().toString().trim();
                if (subject_name.startsWith("TOC") || subject_name.startsWith("MI") || subject_name.startsWith("WP")
                        || subject_name.startsWith("AJ") || subject_name.startsWith("IOT") || subject_name.startsWith("CPDP")) {
                    subject_name.toLowerCase();
                    tempLayout2.setErrorEnabled(false);

                    MultiFormatWriter writer = new MultiFormatWriter();
                    try {
                        BitMatrix bitMatrix = writer.encode(subject_name, BarcodeFormat.QR_CODE,300,300);

                        BarcodeEncoder encoder = new BarcodeEncoder();
                        Bitmap bitmap = encoder.createBitmap(bitMatrix);

                        QRIV.setImageBitmap(bitmap);

                        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(subject_nameET.getApplicationWindowToken(),0 );

                    } catch (WriterException e) {
                        e.printStackTrace();
                    }

                    checkPermission();
                    addLecture();

                } else {
                    subject_nameET.setText("");
                    tempLayout2.setError("Enter proper subject name");
                }
            }
        });



    }

    private void addLecture() {
        Call<responseModel> call = controller.getInstance().getAPI().addLecture(subject_name);
        call.enqueue(new Callback<responseModel>() {
            @Override
            public void onResponse(Call<responseModel> call, Response<responseModel> response) {
                responseModel obj = response.body();
                String  msg = obj.getQRMessage();

                if (msg.equals("added")){
                    Toast.makeText(lectureActivity.this, "Lecture added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(lectureActivity.this, "Lecture not added", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<responseModel> call, Throwable t) {
                Log.d("try6",t.getMessage());
            }
        });
    }

    void checkPermission(){
        if (ContextCompat.checkSelfPermission(lectureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(lectureActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE);
        } else {
            saveImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage();
            } else {
                Toast.makeText(this, "Grant Permissions", Toast.LENGTH_SHORT).show();
            }
        }

    }


    void saveImage(){

        Toast.makeText(this, "Make sure to save QR code.", Toast.LENGTH_SHORT).show();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();

        OutputStream fos;

        BitmapDrawable drawable = (BitmapDrawable) QRIV.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q);{
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME,subject_name+" " + formatter.format(date) + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES + File.separator + "DE-2B QR codes");

                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                Objects.requireNonNull(fos);

                Toast.makeText(this, "QR saved.", Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {
            Log.d("Exception",e.toString());
            Toast.makeText(this, "QR not saved \n"+ e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

}