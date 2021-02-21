package com.Gonigon.bombi;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Gonigon.bombi.model.ListCheckModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private ArrayList<ListCheckModel> mData;



    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView_userName, textView_area, textView_sex, textView_age, textView_lastMessage;
        ImageView imageView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            textView_userName = itemView.findViewById(R.id.recyclerview_item_chat_textView_userName);
            textView_area = itemView.findViewById(R.id.recyclerview_item_chat_textView_area);
            textView_sex = itemView.findViewById(R.id.recyclerview_item_chat_textView_sex);
            textView_age = itemView.findViewById(R.id.recyclerview_item_chat_textView_age);
            textView_lastMessage = itemView.findViewById(R.id.recyclerview_item_chat_textView_lastMessage);
            imageView = itemView.findViewById(R.id.recyclerview_item_chat_imageView);


            // 아이템 클릭 이벤트 처리.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(v.getContext(), RoomActivity.class);
                    intent.putExtra("uid", mData.get(position).uid);
                    intent.putExtra("token", mData.get(position).token);

                    v.getContext().startActivity(intent);
                    
                }
            });
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    ChatAdapter(ArrayList<ListCheckModel> list) {
        mData = list;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.recyclerview_item_chat, parent, false);
        ChatAdapter.ViewHolder viewHolder = new ChatAdapter.ViewHolder(view);

        return viewHolder;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.textView_userName.setText(mData.get(position).userName);
        holder.textView_area.setText(mData.get(position).area);
        holder.textView_age.setText(mData.get(position).age);
        holder.textView_sex.setText(mData.get(position).sex);
        holder.textView_lastMessage.setText(mData.get(position).lastMessage);
        if(mData.get(position).sex.equals("남성")){
            holder.textView_sex.setTextColor(0xFF2196F3);
        } else {
            holder.textView_sex.setTextColor(0xFFFF96D7);
        }
        if (!mData.get(position).chatBubbleWriter.equals(myUid)) { //상대방이 쓴글일때

            if (mData.get(position).read.equals(true)){ // 내가 읽었으면
                holder.imageView.setVisibility(View.INVISIBLE);
            } else { //내가 안읽었으면
                holder.imageView.setVisibility(View.VISIBLE);
            }
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }

}
