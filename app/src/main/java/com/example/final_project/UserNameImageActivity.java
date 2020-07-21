package com.example.final_project;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.example.final_project.Database.useradate.BlankContract;
import com.example.final_project.Database.useradate.UserDatadbHelper;
import com.example.final_project.firebaseConnection.ConnectionFireBase;
import com.example.final_project.firebaseConnection.UserData;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class UserNameImageActivity extends AppCompatActivity {

    /**
     * @Variable uname ,Is use to store the value of user name enter by user
     * @Variable  email ,Store the user email
     * @Variable connect ,use to upload data in firebase
     * @Variable bitmap ,store the data of image*/
    private EditText u_name;
    private String email,name ;
    static final int REQUEST_IMAGE_CAPTURE=1;
    private CardView cardView;
    private ImageView imageView;
    private final String TAG = "UserNameImageActivity";
    private Bitmap bitmap;
    private  Button button ;
    static final int SELECT_FILE=0;
    private Uri mImageUrl;
    private  ConnectionFireBase connect;
    private  UserData data;
    private ProgressBar progressBar;
    private final com.example.final_project.Database.useradate.UserDatadbHelper mdbHelper = new UserDatadbHelper(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_name_image);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        //Initiating the variables
        data = new UserData();
        connect = new ConnectionFireBase();
        email = getIntent().getStringExtra("Email");
        name = getIntent().getStringExtra("name");
        u_name = findViewById(R.id.text_name);
        u_name.setText(name);
        imageView=findViewById(R.id.imageView);
        String url = getIntent().getStringExtra("image");
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        Log.e(TAG,url+":image url");
        Glide.with(getApplicationContext())
                    .load(getIntent().getStringExtra("image"))
                    .placeholder(R.drawable.account_pic)
                    .into(imageView);
        progressBar.setVisibility(View.GONE);


        cardView=findViewById(R.id.image_card_view);
        button = findViewById(R.id.next_button);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu=new PopupMenu(getApplicationContext(),cardView);
                popupMenu.getMenuInflater().inflate(R.menu.choose_image,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()){
                        case R.id.camera:
                            Intent camera =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (camera.resolveActivity(getPackageManager())!=null){
                                startActivityForResult(Intent.createChooser(camera,"Select Source"),REQUEST_IMAGE_CAPTURE);
                        }
                            break;
                        case R.id.gallery:
                            Intent gallery =new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            gallery.setType("image/*");
                            if (gallery.resolveActivity(getPackageManager())!=null){
                            startActivityForResult(Intent.createChooser(gallery,"Select Source"),SELECT_FILE);
                            break;}
                    }
                    return true;
                });
                popupMenu.show();

            }
        });
        //check if the name is empty then can't go to next Activity
        button.setOnClickListener(v -> {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            if(!u_name.getText().toString().isEmpty()){
                Snackbar.make(findViewById(R.id.ll),"Thankyou!!",Snackbar.LENGTH_LONG).show();

                if(dataEnterToDatabase(u_name.getText().toString(),bitmap)){
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    startActivity(new Intent(UserNameImageActivity.this,RootActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }

                else {
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    new AlertDialog.Builder(UserNameImageActivity.this)
                            .setMessage("Your information don't save in the app's database ,which cause bad behavior of app.\n " +
                                    "To store your information in the app go to setting ")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(UserNameImageActivity.this,RootActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            })
                            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(UserNameImageActivity.this,RootActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            })
                            .setCancelable(true)
                            .setTitle("Alert")
                            .show();
                }}else
                Snackbar.make(findViewById(R.id.ll),"Name Can't be empty",Snackbar.LENGTH_LONG)
                        .show();
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null && data.getData() != null){
            Bundle extras=data.getExtras();
            bitmap= (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            bitmap = imageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imagedata = baos.toByteArray();
            connect.uploadImageToStorage("prof_image",email+""+ MimeTypeMap.getFileExtensionFromUrl(String.valueOf(mImageUrl)),imagedata);
        }
        else if(requestCode==SELECT_FILE && resultCode == RESULT_OK && data != null && data.getData() != null){
            Uri selectedImage = data.getData();
            connect.uploadImageToStorage("prof_image",email+""+ MimeTypeMap.getFileExtensionFromUrl(String.valueOf(mImageUrl)),selectedImage);
            Glide.with(this).load(selectedImage).into(imageView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean dataEnterToDatabase(String name, Bitmap image){
        SQLiteDatabase sqLiteDatabase = null;
        ContentValues values = new ContentValues();
        try {
            sqLiteDatabase = mdbHelper.getWritableDatabase();
            Log.e(TAG,""+getIntent().getStringExtra("phone")+" "+getIntent().getStringExtra("email"));
            values.put(BlankContract.BlankEnter._ID ,getIntent().getStringExtra("id"));
            values.put(BlankContract.BlankEnter.COLUMNS_USER_EMAIL,email);
            values.put(BlankContract.BlankEnter.COLUMNS_USER_NAME,u_name.getText().toString());
            if(image != null) {
                //Getting the image file turn that image(Bitmap) file in to ByteArrayOutputStream and then
                // compress that into byte array(image) and store the file into the database named by image
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG,100,stream);
                byte[] imageFile = stream.toByteArray();
                values.put(BlankContract.BlankEnter.COLUMNS_USER_IMAGE,imageFile);
            }

        }catch(Exception ignored){

        }
        long result = -1;
        if(sqLiteDatabase != null) result = sqLiteDatabase.insert(BlankContract.BlankEnter.LOGIN_TABLE_NAME, BlankContract.BlankEnter._ID,values);
        Log.e(TAG,result+"");
        if(result != -1){
            data.setId(email);

            data.setName(u_name.getText().toString().trim());

            if(getIntent().getStringExtra("phone") != null) data.setPhone(Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("phone"))));

            //Image data set at the time of selection
            if(connect.downloadImageUri != null){
                data.setImage(mImageUrl);
            }

            data.setPassword("");

            connect.setName(data);
            return true;
        }

        return false;
    }
}