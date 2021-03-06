package org.techtown.club.post;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.techtown.club.PreferenceManager;
import org.techtown.club.R;
import org.techtown.club.dto.ClubPost;
import org.techtown.club.retrofit.RetrofitClient;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.InputStream;

public class GroupWriteActivity extends AppCompatActivity {

    Button submitButton;
    ImageView imageView;
    //Button addImageButton;
    EditText postTitle;
    EditText postContent;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_write);
        mContext = this;

        submitButton = findViewById(R.id.submitButton);
        imageView = findViewById(R.id.imageView);
        //addImageButton = findViewById(R.id.addImageButton);

        postTitle = findViewById(R.id.postTitle);
        postContent = findViewById(R.id.postContent);


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPost();
                postTitle.setText("");
                postContent.setText("");
                finish();
            }
        });


        /*addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });*/
    }

    public void sendPost() {
        Long userId = PreferenceManager.getLong(mContext, "userId");
        Long clubId = PreferenceManager.getLong(mContext, "clubId");
        int teamNumber = PreferenceManager.getInt(mContext, "teamNumber");
        Log.d("id??? ??????(notice)",userId+"//"+clubId);
        String title = postTitle.getText().toString();
        String content = postContent.getText().toString();
        ClubPost clubPost = new ClubPost(title, content,teamNumber);
        Log.d("?????? ??????",clubPost.getTeamNumber()+clubPost.getTitle());
        Call<Long> call = RetrofitClient.getApiService().sendPost(clubId,userId,clubPost);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? post ","error code"+response.code());
                    return;
                }
                Log.d("?????? ?????? post ",Long.toString(response.body()));
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("?????? ?????? post", t.getMessage());
            }
        });
    }

    /*@Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                try {
                    // ????????? ??????????????? ????????? ??????
                    InputStream in = getContentResolver().openInputStream(data.getData());
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    // ????????? ??????
                    imageView.setImageBitmap(img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
}