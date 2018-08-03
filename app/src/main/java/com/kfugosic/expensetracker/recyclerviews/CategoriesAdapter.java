package com.kfugosic.expensetracker.recyclerviews;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;
import com.kfugosic.expensetracker.ui.CategoriesActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> implements IDataLoaderListener {

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
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry._ID);
        int nameIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_NAME);
        int colorIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_COLOR);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String name = mCursor.getString(nameIndex);
        Integer color = mCursor.getInt(colorIndex);

        holder.itemView.setTag(id);
        holder.setCategoryName(name);
        holder.setCategoryColor(color);
        Log.d("TAG123", "onBindViewHolder: " + id + " " + name + " " + color);

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public void onDataLoaded(int id, Cursor c) {
        if (mCursor == c || c == null) {
            return;
        }
        mCursor = c;
        notifyDataSetChanged();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.category_name)
        TextView mCategoryName;
        @BindView(R.id.category_color)
        View mCategoryColor;
        @BindView(R.id.category_delete)
        View mCategoryDelete;


        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mCategoryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("TAG123", "onClick: " + getAdapterPosition());
                }
            });
        }

        public void setCategoryName(String categoryName) {
            mCategoryName.setText(categoryName);
        }

        public void setCategoryColor(int color) {
            mCategoryColor.setBackgroundColor(color);
        }

        @OnClick(R.id.category_delete)
        void onDeleteButtonClick() {
            //https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(mContext);
            }
            builder.setTitle("Delete category")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] selectionArgs = new String[]{String.valueOf(itemView.getTag())};
                        int res = mContext.getContentResolver().delete(CategoriesContract.CategoriesEntry.CONTENT_URI, "_id=?", selectionArgs);
                        CategoriesActivity activity = (CategoriesActivity) mContext;
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_CATEGORY, -1);
                        res = mContext.getContentResolver().update(
                                ExpensesContract.ExpensesEntry.CONTENT_URI,
                                contentValues,
                                ExpensesContract.ExpensesEntry.COLUMN_CATEGORY+"=?",
                                selectionArgs);
                        activity.getSupportLoaderManager().restartLoader(CategoriesActivity.CATEGORIES_LOADER_ID, null, activity.getLoader());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        }

    }

}
