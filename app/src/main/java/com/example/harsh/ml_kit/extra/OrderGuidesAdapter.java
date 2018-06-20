package com.example.harsh.ml_kit.extra;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.harsh.ml_kit.R;

import java.util.ArrayList;

class OrderGuidesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 0;

    private Context context;
    private OrderGuidesAdapter.OnItemClickListener listener;
    private ArrayList<Integer> items = new ArrayList<>();

    public void setOnItemClickListener(OrderGuidesAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public OrderGuidesAdapter(Context context) {
        this.context = context;
        items.add(0);
        items.add(1);
        items.add(2);


    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case TYPE_ITEM:
            default:
                View itemView = LayoutInflater.from(context).inflate(R.layout.item_order_guide, parent, false);
                return new OrderGuidesAdapter.ItemViewHolder(itemView);

        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof OrderGuidesAdapter.ItemViewHolder) {
            OrderGuidesAdapter.ItemViewHolder itemViewHolder = (OrderGuidesAdapter.ItemViewHolder) holder;
            itemViewHolder.setClickListener(listener);
        }
    }


    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public int getItemViewType(int position) {

        return TYPE_ITEM;
    }


    public static class ItemViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        LinearLayout linearLayoutItem;


        private OrderGuidesAdapter.OnItemClickListener clickListener;

        void setClickListener(OrderGuidesAdapter.OnItemClickListener clickListener) {
            this.clickListener = clickListener;
        }

        ItemViewHolder(View view) {
            super(view);
            linearLayoutItem = view.findViewById(R.id.ll_item_order_guide);
            linearLayoutItem.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                switch (v.getId()) {


                    default:
                        clickListener.onClick(getAdapterPosition(), OrderGuidesAdapter.OnItemClickListener.Type.LIST_CLICK);
                        break;
                }
            }
        }

    }


    public interface OnItemClickListener {

        enum Type {
            LIST_CLICK, NOTES, DELETE, REMOVE_ITEM, INCREMENT, DECREMENT, CHECK_BOX
        }

        void onClick(int position, OrderGuidesAdapter.OnItemClickListener.Type type);


    }

}
