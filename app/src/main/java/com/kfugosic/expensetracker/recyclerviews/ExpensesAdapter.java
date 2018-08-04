package com.kfugosic.expensetracker.recyclerviews;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;
import com.kfugosic.expensetracker.ui.StatisticsActivity;
import com.kfugosic.expensetracker.widget.ExpensesWidgetService;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpensesViewHolder> implements IDataLoaderListener {

    private Context mContext;
    private Cursor mCursor;
    private HashMap<Integer, Integer> mCategoryIdToColor;

    public ExpensesAdapter(Context context, Cursor c, HashMap<Integer, Integer> idToColorMap) {
        mContext = context;
        mCursor = c;
        mCategoryIdToColor = idToColorMap;
    }

    @NonNull
    @Override
    public ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.expense_layout, viewGroup, false);
        return new ExpensesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpensesViewHolder holder, int position) {
        int idIndex = mCursor.getColumnIndex(ExpensesContract.ExpensesEntry._ID);
        int amountIndex = mCursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT);
        int descIndex = mCursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_DESCRIPTION);
        int categoryIndex = mCursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_CATEGORY);
        int dateIndex = mCursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_DATE);
        int photoIndex = mCursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_PHOTO_LOCATION);

        mCursor.moveToPosition(position);
        final int id = mCursor.getInt(idIndex);
        float amount = mCursor.getFloat(amountIndex);
        String desc = mCursor.getString(descIndex);
        int categoryId = mCursor.getInt(categoryIndex);
        long date = mCursor.getLong(dateIndex);
        Uri photoUri = null;
        if(mCursor.getString(photoIndex) != null) {
            photoUri = Uri.parse(mCursor.getString(photoIndex));
        }

        holder.itemView.setTag(id);
        int color = 0;
        if(mCategoryIdToColor.containsKey(categoryId)) {
            color = mCategoryIdToColor.get(categoryId);
        }
        holder.setValues(amount, desc, color, photoUri);
        Log.d("TAG123", "onBindViewHolder: " + id +  " "  + amount + " " + categoryId + " " + date + " " +photoUri);

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

    class ExpensesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.expense_amount)
        TextView mExpenseAmount;
        @BindView(R.id.expense_desc)
        TextView mExpenseDescription;
        @BindView(R.id.expense_category_color)
        View mExpenseCategoryColor;
        @BindView(R.id.expense_delete)
        View mExpenseDelete;
        @BindView(R.id.expense_image)
        ImageView mExpenseImage;


        public ExpensesViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        public void setValues(float amount, String desc, int color, final Uri uri) {
            mExpenseAmount.setText(String.format(Locale.ENGLISH, "%.2f", amount));
            if( !TextUtils.isEmpty(desc)) {
                mExpenseDescription.setText(desc);
            }
            mExpenseCategoryColor.setBackgroundColor(color);

            if(uri != null) {
                try {
//                    InputStream imageStream = null;
//                    imageStream = mContext.getContentResolver().openInputStream(uri);
//                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                    mExpenseImage.setImageBitmap(selectedImage);
//                    imageStream.close();
                    Picasso.get()
                            .load(uri)
                            .into(mExpenseImage);
                    mExpenseCategoryColor.setBackgroundColor(color);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @OnClick(R.id.expense_delete)
        void onDeleteButtonClick() {
            //https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(mContext, android.R.style.Theme_DeviceDefault_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(mContext);
            }
            builder.setTitle("Delete expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] selectionArgs = new String[]{String.valueOf(itemView.getTag())};
                        mContext.getContentResolver().delete(ExpensesContract.ExpensesEntry.CONTENT_URI, "_id=?", selectionArgs);
                        StatisticsActivity activity = (StatisticsActivity) mContext;
                        activity.getSupportLoaderManager().restartLoader(StatisticsActivity.EXPENSES_LOADER_ID, null, activity.getLoader());
                        ExpensesWidgetService.startActionUpdateExpensesTextviews(mContext);
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
