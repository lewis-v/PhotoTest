package com.yw.phototest.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yw.phototest.R;

import java.util.List;

/**
 * Created by yw on 2017-08-09.
 */

public class MytextHomeAdapter extends RecyclerView.Adapter<MytextHomeAdapter.MyViewHolder>{
    private Context context;
    private List<String> stringList;
    private OnItemClickLitener onItemClickLitener;
    private int layout;

    public MytextHomeAdapter(Context context, List<String> strings){
        super();
        this.context = context;
        this.stringList = strings;
        this.layout = R.layout.item_text;
    }

    public MytextHomeAdapter(Context context, List<String> strings, int layout){
        super();
        this.context = context;
        this.stringList = strings;
        this.layout = layout;
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }
    public void setOnItemClikListener(OnItemClickLitener onItemClikListener){
        this.onItemClickLitener = onItemClikListener;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(
                LayoutInflater.from(context).inflate(layout,parent,false));
        return holder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.tv_item.setText(stringList.get(position));

        if (onItemClickLitener!=null){
            holder.ll_item_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickLitener.onItemClick(v,holder.getLayoutPosition());
                }
            });
            holder.ll_item_text.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickLitener.onItemLongClick(v,holder.getLayoutPosition());
                    return false;
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return stringList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_item;
        LinearLayout ll_item_text;
        public MyViewHolder(View itemView) {
            super(itemView);
            tv_item = (TextView)itemView.findViewById(R.id.tv_item);
            ll_item_text = (LinearLayout)itemView.findViewById(R.id.ll_item_text);
        }
    }
}
