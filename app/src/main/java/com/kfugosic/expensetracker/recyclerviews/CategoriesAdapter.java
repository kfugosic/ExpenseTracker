package com.kfugosic.expensetracker.recyclerviews;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public CategoriesAdapter(Context context, Cursor c) {
        mContext = context;
        mCursor = c;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.category_layout, viewGroup, false);
        return new CategoryViewHolder(view);    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry._ID);
        int nameIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_NAME);
        int colorIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_COLOR);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String name = mCursor.getString(nameIndex);
        Integer color = mCursor.getInt(colorIndex);

        //Set values
        holder.itemView.setTag(id);
        holder.setCategoryName(name);
        holder.setCategoryColor(color);
        Log.d("TAG123", "onBindViewHolder: " +name+color);

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public void swapCursor(Cursor c) {
        if (mCursor == c || c == null) {
            return;
        }
        mCursor = c;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.category_name)
        TextView mCategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mCategoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG123", "onClick: "+getAdapterPosition());
                }
            });
        }

        public void setCategoryName(String categoryName) {
            mCategoryName.setText(categoryName);
        }

        public void setCategoryColor(int color) {
            mCategoryName.setBackgroundColor(color);
        }

    }


}
