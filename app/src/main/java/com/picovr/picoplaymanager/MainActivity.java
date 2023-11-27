package com.picovr.picoplaymanager;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.picovr.picoplaymanager.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    EditText editText, editText2;
    private static final int STORAGE_PERMISSION_CODE = 23;
    int REQUEST_TAKE_GALLERY_VIDEO = 123421;
    String filemanagerstring;
    String selectedImagePath;

    DatabaseReference database;
    ActivityMainBinding binding;
    SharedPreferences prefs;
    int state_of_video = 0;
    int video_type = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        binding.myId.setText("#"+prefs.getInt("counter", -1));

        database = FirebaseDatabase.getInstance().getReference();
        database.child("devices").child(prefs.getInt("counter", -1)+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    boolean exit = Boolean.TRUE.equals(snapshot.child("exit").getValue(Boolean.class));
                    video_type = snapshot.child("videoType").getValue(Integer.class);
                    if (exit) {
                        exitPlayerClick();
                    }
                    

                }
                else{
                    prefs.edit().putInt("counter", -1).apply();
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(
                            intent
                    );
                    MainActivity.this.finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        database.child("devices").child(prefs.getInt("counter", -1)+"").child("url").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String url = snapshot.getValue(String.class);

                    if (!url.equals("empty")) {
                        if (url.startsWith("http")) {
                            openPlayerClick(url, video_type);
                        } else {
                            openPlayerClick("/storage/emulated/0/DCIM/" + url, video_type);
                        }
                    }
                }
                else{
                    prefs.edit().putInt("counter", -1).apply();
                    Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(
                            intent
                    );
                    MainActivity.this.finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        /*
        if(!checkStoragePermissions()){
            requestForStoragePermissions();
        }

         */


        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.container.getVisibility()==View.VISIBLE){
                    binding.container.setVisibility(View.GONE);
                }
                else{
                    binding.container.setVisibility(View.VISIBLE);
                }
            }
        });



        init();



    }
    public void init(){
        editText = (EditText) findViewById(R.id.edittext);
        editText2 = (EditText) findViewById(R.id.edittext2);
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlayerClick(editText.getText().toString(), 2);
            }
        });
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPlayerClick(editText2.getText().toString(), 2);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
            }
        });
    }

    private ActivityResultLauncher<Intent> storageActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                //Android is 11 (R) or above
                                if (Environment.isExternalStorageManager()) {
                                    //Manage External Storage Permissions Granted
                                    Toast.makeText(MainActivity.this, "Разрешения одобрены!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                //Below android 11

                            }
                        }
                    });

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        } else {
            //Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }

    }

    public void openPlayerClick(String s, int video_type) {
        Toast.makeText(getApplicationContext(), "open "+s, Toast.LENGTH_SHORT).show();
        state_of_video = 1;
        database.child("devices").child(prefs.getInt("counter", -1)+"").child("url").setValue("empty");
        database.child("devices").child(prefs.getInt("counter", -1)+"").child("state").setValue(0);

         new PicovrLaunchPlayer().uri(s, false).title("test2D")
//                .play_list("[{\"index\":0,\"name\":\"test2D.mp4\",\"playMode\":0,\"url\":\"%2Fsdcard%2Ftest2D.mp4\"},
//                {\"index\":1,\"name\":\"test.mp4\",\"playMode\":2,\"url\":\"%2Fsdcard%2Ftest.mp4\"},
//                {\"index\":2,\"name\":\"test2D2.mp4\",\"playMode\":0,\"url\":\"%2Fsdcard%2Ftest2D.mp4\"}]")
//                .shouldPlayIndex(2)
                .loop(false)
//                .position(100.0f)
//                .videoSource("0")
                .isControl(false)
                .videoType(video_type+"")
                .launchVideoPlayer(this);


    }



    public void exitPlayerClick() {
        Toast.makeText(getApplicationContext(), "exit", Toast.LENGTH_SHORT).show();
        new PicovrLaunchPlayer().exitVideoPlayer(this);
        database.child("devices").child(prefs.getInt("counter", -1)+"").child("exit").setValue(false);
        database.child("devices").child(prefs.getInt("counter", -1)+"").child("state").setValue(1);
    }




    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedImageUri = data.getData();

                // OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();
                // MEDIA GALLERY
                selectedImagePath = getPathFromUri(this, selectedImageUri);
                Log.d("planeta", "onActivityResult: " + selectedImagePath);
                if (selectedImagePath != null) {
                    editText2.setText(selectedImagePath);
                }
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (read && write) {
                    Toast.makeText(MainActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }





    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        database.child("devices").child(prefs.getInt("counter", -1)+"").child("state").setValue(1);
        super.onResume();
    }
}

