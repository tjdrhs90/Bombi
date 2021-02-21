package com.Gonigon.bombi;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Gonigon.bombi.model.MessageModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    private ArrayList<MessageModel> mData = null;
    FirebaseAuth firebaseAuth;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView_message;
        TextView textView_time;
        TextView textView_name;
        TextView textView_read;
        LinearLayout linearLayout_main;
        LinearLayout linearLayout_sub;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            textView_message = itemView.findViewById(R.id.recyclerview_item_room_message);
            textView_time = itemView.findViewById(R.id.recyclerview_item_room_time);
            textView_name = itemView.findViewById(R.id.recyclerview_item_room_name);
            textView_read = itemView.findViewById(R.id.recyclerview_item_room_read);
            linearLayout_main = itemView.findViewById(R.id.linearlayout_main);
            linearLayout_sub = itemView.findViewById(R.id.linearlayout_sub);
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    RoomAdapter(ArrayList<MessageModel> list) {
        mData = list;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public RoomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.recyclerview_item_room, parent, false);
        RoomAdapter.ViewHolder viewHolder = new RoomAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RoomAdapter.ViewHolder holder, int position) {
        holder.textView_message.setText(mData.get(position).message);
        holder.textView_time.setText(mData.get(position).formatTime);
        if (mData.get(position).writerUid.equals(firebaseAuth.getInstance().getCurrentUser().getUid())){ //내 글
            holder.linearLayout_main.setGravity(Gravity.RIGHT);
            holder.linearLayout_sub.setGravity(Gravity.RIGHT);
            holder.textView_message.setBackgroundResource(R.drawable.rightbubble);
            holder.textView_name.setVisibility(View.INVISIBLE);

            if (mData.get(position).read.equals(true)){
                holder.textView_read.setVisibility(View.INVISIBLE);
            } else {
                holder.textView_read.setVisibility(View.VISIBLE);
            }

        } else { //상대방 글
            holder.linearLayout_main.setGravity(Gravity.LEFT);
            holder.linearLayout_sub.setGravity(Gravity.LEFT);
            holder.textView_name.setText(mData.get(position).writerName);
            holder.textView_message.setBackgroundResource(R.drawable.leftbubble);
            holder.textView_name.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
