package org.techtown.club.register;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.club.MainActivity;
import org.techtown.club.PreferenceManager;
import org.techtown.club.R;
import org.techtown.club.dto.Club;
import org.techtown.club.dto.Role;
import org.techtown.club.retrofit.RetrofitClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OpenClubActivity extends AppCompatActivity {

    ListView listView1;
    ListViewAdapter_openClub adapter;
    ArrayList<String> listItem;

    EditText groupjob;
    Button jobaddbutton;

    EditText groupName;
    EditText groupNum;
    EditText groupCode;

    Button checkBtn;
    TextView clubInfo;
    Button makeGroupBtn;
    TextView textView;
    private Context context;
    List<Role> roles;
    CheckBox authCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_openclub);
        context = this;
        roles = new ArrayList<>();

        groupjob = findViewById(R.id.groupjob);
        jobaddbutton = findViewById(R.id.jobaddbutton);
        listView1 = (ListView) findViewById(R.id.listView1);

        groupName = findViewById(R.id.groupname);
        groupCode = (EditText)findViewById(R.id.groupcode);
        groupNum = findViewById(R.id.groupnumber);

        clubInfo = (TextView)findViewById(R.id.clubInfo);
        checkBtn = findViewById(R.id.checkBtn);

        textView = findViewById(R.id.textView);
        authCheck = findViewById(R.id.authCheck);

        adapter = new ListViewAdapter_openClub(OpenClubActivity.this);
        listView1.setAdapter(adapter);

        makeGroupBtn = findViewById(R.id.makegroupbutton2);

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeCheck(groupCode.getText().toString());
            }
        });

        jobaddbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.addItem(groupjob.getText().toString(), authCheck.isChecked());
                adapter.notifyDataSetChanged(); // ?????????????????? ???????????? ????????????.
                groupjob.setText("");
                authCheck.setChecked(false);
            }
        });

        Button searchgroupbutton = (Button) findViewById(R.id.searchgroupbutton);

        listView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        searchgroupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(OpenClubActivity.this, JoinClubActivity.class);
                OpenClubActivity.this.startActivity(registerIntent);
            }
        });

        makeGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < ListViewAdapter_openClub.listItems.size();i++) {
                    ListItemDetail_register detail_register = ListViewAdapter_openClub.listItems.get(i);
                    String roleName = detail_register.getWhat();
                    boolean checked = detail_register.isAuth();
                    Log.d("??????", roleName + checked);
                    Role role = new Role(roleName, checked);
                    roles.add(role);
                }
                Role r2 = new Role("??????", true);
                roles.add(r2);
                String name = groupName.getText().toString();
                int generation = Integer.parseInt(groupNum.getText().toString());
                String code = groupCode.getText().toString();
                String info = clubInfo.getText().toString();
                Log.d("?????? ??????",name+code+info+generation);
                Club club = new Club(name, generation, info, code);
                openClub(club);
            }
        });


    }

    public void sendRole() {
        Long clubId = PreferenceManager.getLong(context,"clubId");
        Call<Void> call = RetrofitClient.getApiService().sendRoles(clubId, roles);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? send role", "error code" + response.code());
                    return;
                }
                Log.d("?????? ?????? send role", "??????");
                addLeaderRole();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("?????? ?????? send role", t.getMessage());
            }
        });
    }

    public void openClub(Club club) {
        Call<Long> call = RetrofitClient.getApiService().postClub(club);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("club ?????? ?????????","error code"+response.code());
                    return;
                }
                //Long clubId = PreferenceManager.getLong(context,"clubId");
                PreferenceManager.setLong(context,"clubId",response.body());
                String log = Long.toString(PreferenceManager.getLong(context,"clubId"));
                Log.d("clubId ??????",log);
                sendRole();
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("club ?????? ??????", t.getMessage());
            }
        });
    }

    public void addLeaderRole() {
        Role role = new Role("??????", true);
        Call<Long> call = RetrofitClient.getApiService()
                .addLeaderRole(PreferenceManager.getLong(context,"clubId"), role);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? setReader","error code"+response.code());
                    return;
                }
                Long clubRoleId = response.body();
                PreferenceManager.setLong(context, "clubRoleId",clubRoleId);
                Log.d("???????????? setLeader",response.body().toString());
                setRole(PreferenceManager.getLong(context,"clubRoleId"));
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.d("?????? ?????? setLeader", t.getMessage());
            }
        });
    }

    public void registerUser() { //user??? ???????????? ??????????????? function
        Long userId = PreferenceManager.getLong(context,"userId");
        Long clubId = PreferenceManager.getLong(context, "clubId"); //????????? ?????? ????????? ?????? ???????????? ?????????!!!!
        Call<Long> call = RetrofitClient.getApiService().registerUserToClub(clubId,userId);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? register user","error code"+response.code());
                    return;
                }
                Log.d("?????? ?????? register user",response.body().toString());
                Intent registerIntent = new Intent(OpenClubActivity.this, MainActivity.class);
                OpenClubActivity.this.startActivity(registerIntent);
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("?????? ?????? register user", t.getMessage());
            }
        });
    }

    public void setRole(Long clubRoleId) {
        Long userId = PreferenceManager.getLong(context, "userId");
        Call<Long> call = RetrofitClient.getApiService().setUserRole(userId, clubRoleId);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (!response.isSuccessful()) {
                    Log.e("?????? ????????? set role","error code"+response.code());
                    return;
                }
                Log.d("?????? ?????? set role",response.body().toString());
                registerUser();
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e("?????? ?????? set role", t.getMessage());
            }
        });
    }

    public boolean check;
    public boolean codeCheck(String code){
        Log.d("?????? ??????",code);
        Call<Boolean> call = RetrofitClient.getApiService().checkCode(code);
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (!response.isSuccessful()) {
                    Log.e("code check ?????? ?????????","error code"+response.code());
                    return;
                }
                Log.d("code check??????",response.body().toString());;
                check = response.body();
                Log.d("??????",Boolean.toString(check));
                if (check) {
                    textView.setText("????????? ??? ?????? ?????????????????????.");
                    textView.setTextColor(Color.parseColor("#FF0000"));
                    Log.d("????????????","??????");
                }else {
                    textView.setTextColor(Color.parseColor("#00FF00"));
                    textView.setText("????????? ??? ?????? ?????????????????????.");
                    Log.d("??????","???");
                }
                textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.e("code check ?????? ??????", t.getMessage());
            }
        });
        return check;
    }
}