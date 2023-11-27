package com.picovr.picoplaymanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.picovr.picoplaymanager.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    DatabaseReference database;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getInt("counter", -1)!=-1){
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            RegisterActivity.this.finish();
        }
        binding.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.child("is_adding").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean is_adding = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        if(is_adding){
                            RegisterThisVR();
                        }
                        else{
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.refresh.setVisibility(View.VISIBLE);
                            binding.textView3.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        database = FirebaseDatabase.getInstance().getReference();
        database.child("is_adding").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean is_adding = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if(is_adding){
                    RegisterThisVR();
                }
                else{
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.refresh.setVisibility(View.VISIBLE);
                    binding.textView3.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void RegisterThisVR(){
        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.refresh.setVisibility(View.GONE);
        binding.textView3.setVisibility(View.GONE);
        database.child("devices_counter").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int counter = snapshot.getValue(Integer.class);
                DevicesModel devicesModel = new DevicesModel();
                devicesModel.setDevice_id(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                devicesModel.setId(counter+1);
                devicesModel.setState(1);
                devicesModel.setExit(false);
                devicesModel.setUrl("empty");
                devicesModel.setVideoType(2);
                database.child("is_adding").setValue(false);

                database.child("devices").child((counter+1)+"").setValue(devicesModel);
                database.child("devices_counter").setValue((counter+1)).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        prefs.edit().putInt("counter", counter+1).apply();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                        RegisterActivity.this.finish();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DevicesModel devicesModel = new DevicesModel();
        devicesModel.setDevice_id(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));

    }
}