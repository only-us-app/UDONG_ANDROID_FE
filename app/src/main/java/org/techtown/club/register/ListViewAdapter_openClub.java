package org.techtown.club.register;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.gson.Gson;

import org.techtown.club.PreferenceManager;
import org.techtown.club.dto.Role;
import org.techtown.club.post.ListItemDetail2;
import org.techtown.club.R;

import java.util.ArrayList;

public class ListViewAdapter_openClub extends BaseAdapter {

    private Context mContext;
    public static ArrayList<ListItemDetail_register> listItems = new ArrayList<>();

    public ListViewAdapter_openClub(Context context){
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int i) {
        return listItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // item.xml 레이아웃을 inflate해서 참조획득
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_registeractivity1, parent, false);
        }

        // item.xml 의 참조 획득
        TextView role_name = (TextView)convertView.findViewById(R.id.role_name);
        CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
        Button btn_delete = (Button)convertView.findViewById(R.id.btn_delete);


        ListItemDetail_register listItemDetail2 = listItems.get(position);
        // 가져온 데이터를 텍스트뷰에 입력
        role_name.setText(listItemDetail2.getWhat());
        checkBox.setChecked(listItemDetail2.isAuth());
        checkBox.setEnabled(false);

        String name = listItemDetail2.getWhat();
        boolean notice_auth = checkBox.isChecked();


        // 리스트 아이템 삭제
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItems.remove(position);
                PreferenceManager.setInt(parent.getContext(),"position",position);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    public void addItem(String what, boolean auth){
        ListItemDetail_register listItemDetail_register = new ListItemDetail_register();

        listItemDetail_register.setWhat(what);

        listItemDetail_register.setAuth(auth);

        listItems.add(listItemDetail_register);

    }
}