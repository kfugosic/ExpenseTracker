package com.kfugosic.expensetracker.ui;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.recyclerviews.CategoriesAdapter;

import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kfugosic.expensetracker.ui.CategoriesActivity.CATEGORIES_LOADER_ID;

public class NewExpenseActivity extends AppCompatActivity {


    @BindView(R.id.et_amount)
    EditText mAmount;
    @BindView(R.id.btn_select_category)
    Button mBtnSelectCategory;

    private int mSelectedCategory = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        ButterKnife.bind(this);

    }


    @OnClick(R.id.btn_select_category)
    void onSelectCategoryButtonClick() {
        final AtomicInteger clickedPos = new AtomicInteger(-1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Cursor cursor = getContentResolver().query(CategoriesContract.CategoriesEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        builder.setCursor(cursor, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                clickedPos.set(position);
                Log.d("TAG123", "onClick: "+position);
                int idIndex = cursor.getColumnIndex(CategoriesContract.CategoriesEntry._ID);
                int nameIndex = cursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_NAME);

                cursor.moveToPosition(clickedPos.get());

                int categoryId = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                Log.d("TAG123", name + categoryId);
            }
        },
        "name");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @OnClick(R.id.btn_add)
    void onAddButtonClick() {
        float amount = Float.valueOf(mAmount.getText().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT, amount);

        Uri uri = getContentResolver().insert(ExpensesContract.ExpensesEntry.CONTENT_URI, contentValues);

    }
}
