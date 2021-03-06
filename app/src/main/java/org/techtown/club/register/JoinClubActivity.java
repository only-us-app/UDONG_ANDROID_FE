package org.techtown.club.register;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techtown.club.MainActivity;
import org.techtown.club.PreferenceManager;
import org.techtown.club.R;
import org.techtown.club.retrofit.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JoinClubActivity extends AppCompatActivity {

    static ArrayList<String> job;
    static HashMap<String,Object> hashMap;
    //static Long clubid;
    static Long roleId;
    TextView textView;
    Long clubid;
    Button makeBtn;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joinclub);
        mContext = this;

        Button makegroupbutton = (Button) findViewById(R.id.makegroupbutton);
        EditText searchText = (EditText) findViewById(R.id.search);
        makeBtn = (Button)findViewById(R.id.makeBtn);
        ImageButton searchBtn = (ImageButton) findViewById(R.id.searchBtn);

        textView = findViewById(R.id.textViewJoin);
        job = new ArrayList<>();
        hashMap = new HashMap<>();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchText.getText() != null)
                    searchClubCode(searchText.getText().toString());
            }
        });


        job.add("????????? ???????????????");
        Spinner groupSpinner = (Spinner)findViewById(R.id.spinner_group);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, job);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);
        groupSpinner.setSelection(0, false);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (getKey(hashMap,String.valueOf(job.get(i))) != null)
                    roleId = Long.parseLong(getKey(hashMap,String.valueOf(job.get(i))));
                //Toast.makeText(JoinClubActivity.this,"????????? ????????? : "+roleId.toString(),Toast.LENGTH_SHORT).show();
                getClubRoleId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }

    public void getClubRoleId() {
        Log.d("********",roleId.toString());
        Call<Long> call = RetrofitClient.getApiService().getClubRoleId(
                PreferenceManager.getLong(mContext,"clubId"), roleId);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? get clubroleid","error code"+response.code());
                    return;
                }
                Log.d("?????? ?????? get clubroleid",response.body().toString());
                Long clubRoleId = response.body();
                makeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setRole(clubRoleId);
                        registerUser();
                        Intent intent = new Intent(JoinClubActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("?????? ?????? get clubroleid", t.getMessage());
            }
        });
    }

    public static <K, V> K getKey(Map<K, V>map, V value) {
        for (K key : map.keySet()) {
            if (value.equals(map.get(key))) {
                return key;
            }
        }
        return null;
    }

    public void searchClubCode(String clubCode) {//?????? ????????? ????????? ?????? ????????????
        Call call = RetrofitClient.getApiService().getClubInfo(clubCode);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    Log.e("search club ?????? ?????????","error code"+response.code());
                    return;
                }
                String result = new Gson().toJson(response.body());
                //????????? ??? ??????, ?????? ??????????????? ?????? ???????????? ?????? ???
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    clubid = jsonObject.getLong("id");
                    PreferenceManager.setLong(mContext, "clubId", clubid);
                    String clubName = jsonObject.getString("name");
                    if (!jsonObject.isNull("info")){
                        String clubInfo = jsonObject.getString("info");
                        Log.d("info",clubInfo);
                        textView.setText("          "+clubInfo);
                    }
                    else {
                        textView.setText(null);
                    }
                    int generation = jsonObject.getInt("generation");
                    Log.d("??????", "id"+clubid+"name"+clubName +"/generation"+generation);
                    getRoles();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                //?????? ???????????? ?????? ????????? ?????? ???
                Log.e("search club ?????? ??????", t.getMessage());
            }
        });
    }

    public void registerUser() { //user??? ???????????? ??????????????? function
        Long userId = PreferenceManager.getLong(mContext,"userId");
        Long clubId = PreferenceManager.getLong(mContext, "clubId"); //????????? ?????? ????????? ?????? ???????????? ?????????!!!!
        Call<Long> call = RetrofitClient.getApiService().registerUserToClub(clubId,userId);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? register user","error code"+response.code());
                    return;
                }
                Log.d("?????? ?????? register user",response.body().toString());
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("?????? ?????? register user", t.getMessage());
            }
        });
    }

    public void getRoles() {
        Log.d("??????",clubid.toString());
        Call<ResponseBody> call = RetrofitClient.getApiService().getRoleList(clubid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? get roles","error code"+response.code());
                    return;
                }
                try {
                    String result = response.body().string();
                    Log.d("?????? ?????? get role", result);
                    JSONArray jsonArray = new JSONArray(result);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String name = jsonObject.getString("name");
                        Long id = jsonObject.getLong("id");
                        Log.d("role","name"+name+"id"+id);
                        hashMap.put(id.toString(), name);
                        job.add(name);
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("?????? get role", t.getMessage());
            }
        });
    }

    public void setRole(Long clubRoleId) {
        Long userId = PreferenceManager.getLong(mContext, "userId");
        Call<Long> call = RetrofitClient.getApiService().setUserRole(userId, clubRoleId);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? set role","error code"+response.code());
                    return;
                }
                Log.d("?????? ?????? set role",response.body().toString());

            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("?????? ?????? set role", t.getMessage());
            }
        });
    }


}