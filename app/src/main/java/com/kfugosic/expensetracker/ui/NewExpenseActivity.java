package com.kfugosic.expensetracker.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.DataLoader;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;
import com.kfugosic.expensetracker.util.ToolbarUtil;
import com.kfugosic.expensetracker.widget.ExpensesWidgetService;
import com.squareup.picasso.Picasso;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import static com.kfugosic.expensetracker.ui.CategoriesActivity.CATEGORIES_LOADER_ID;

public class NewExpenseActivity extends AppCompatActivity implements IDataLoaderListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_FROM_GALLERY = 2;
    private static final String SAVED_TEXTVIEW_KEY = "textview";
    private static final String SAVED_CATEGORY_ID_KEY = "category_id";
    private static final String SAVED_CATEGORY_NAME_KEY = "category_name";
    private static final String SAVED_TEMP_IMAGE_URI_KEY = "temp_image_uri";
    private static final String SAVED_IMAGE_URI_KEY = "image_uri";
    private static final int IMAGE_DIMENSIONS = 600;
    private static final String STAY_BUTTON_KEY = "stay_button";
    private static final boolean DEFAULT_STAY_BUTTON_VALUE = false;

    @BindView(R.id.et_amount)
    EditText mAmount;
    @BindView(R.id.et_desc)
    EditText mDescription;
    @BindView(R.id.btn_select_category)
    Button mBtnSelectCategory;
    @BindView(R.id. btn_camera)
    ImageButton mCameraBtn;
    @BindView(R.id. btn_gallery)
    ImageButton mGalleryBtn;
    @BindView(R.id.iv_imagepreview)
    ImageView mImageView;
    @BindView(R.id.tv_selected_image)
    TextView mImageTextView;
    @BindView(R.id.cb_stay)
    CheckBox mStayButton;
    @BindView(R.id.btn_add)
    Button mAddButton;

    private DataLoader mLoader;
    private Cursor mCursor;
    private Uri mTempImageUri;
    private Uri mImageUri;
    private int mSelectedCategory = -1;
    private Bitmap mSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        ToolbarUtil.setupToolbar(this);
        findViewById(R.id.action_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ButterKnife.bind(this);

        mLoader = new DataLoader(this, this, CategoriesContract.CategoriesEntry.CONTENT_URI);
        initOrRestartLoader();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mCameraBtn.setEnabled(false);
        }

        setupCheckBox();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVED_TEXTVIEW_KEY, mImageTextView.getText().toString());
        outState.putInt(SAVED_CATEGORY_ID_KEY, mSelectedCategory);
        outState.putString(SAVED_CATEGORY_NAME_KEY, mBtnSelectCategory.getText().toString());
        outState.putParcelable(SAVED_TEMP_IMAGE_URI_KEY, mTempImageUri);
        outState.putParcelable(SAVED_IMAGE_URI_KEY, mImageUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            return;
        }
        mImageTextView.setText(savedInstanceState.getString(SAVED_TEXTVIEW_KEY));
        mSelectedCategory = savedInstanceState.getInt(SAVED_CATEGORY_ID_KEY);
        mTempImageUri = savedInstanceState.getParcelable(SAVED_TEMP_IMAGE_URI_KEY);
        mBtnSelectCategory.setText(savedInstanceState.getString(SAVED_CATEGORY_NAME_KEY));
        try {
            mImageUri = savedInstanceState.getParcelable(SAVED_IMAGE_URI_KEY);
            InputStream imageStream = getContentResolver().openInputStream(mImageUri);
            mSelectedImage = BitmapFactory.decodeStream(imageStream);
            mImageView.setImageBitmap(mSelectedImage);

            Picasso.get()
                    .load(mImageUri)
                    .resize(IMAGE_DIMENSIONS,IMAGE_DIMENSIONS)
                    .centerInside()
                    .into(mImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String filename = mTempImageUri.getPath();
            int cut = filename.lastIndexOf('/');
            filename = filename.substring(cut + 1);
            getContentResolver().notifyChange(mTempImageUri, null);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mTempImageUri);
                mImageTextView.setText(R.string.default_image_from_camera_textview);
                mSelectedImage = bitmap;
                mImageUri = mTempImageUri;
                Picasso.get()
                        .load(mImageUri)
                        .resize(IMAGE_DIMENSIONS,IMAGE_DIMENSIONS)
                        .centerInside()
                        .into(mImageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mImageTextView.setText(R.string.default_image_from_gallery_textview);
                mSelectedImage = selectedImage;
                mImageUri = imageUri;
                mTempImageUri = null;
                Picasso.get()
                        .load(mImageUri)
                        .resize(IMAGE_DIMENSIONS,IMAGE_DIMENSIONS)
                        .centerInside()
                        .into(mImageView);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.file_not_found_err, Toast.LENGTH_LONG).show();
            }
        }
    }


    private void setupCheckBox() {
        boolean value = getPreferences(MODE_PRIVATE).getBoolean(STAY_BUTTON_KEY, DEFAULT_STAY_BUTTON_VALUE);
        mStayButton.setChecked(value);
        mStayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(STAY_BUTTON_KEY, value);
                editor.apply();
            }
        });
    }


    // developer.android.com
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "tmp_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    static final int REQUEST_TAKE_PHOTO = 1;


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // do nothing
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mTempImageUri = FileProvider.getUriForFile(this,
                        "com.kfugosic.expensetracker.data.images",
                        photoFile);
//                Log.d(TAG, "dispatchTakePictureIntent: "+ mTempImageUri);
//                Log.d(TAG, "dispatchTakePictureIntent: "+ photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @OnClick(R.id.btn_camera)
    void onCameraButtonClick() {
        dispatchTakePictureIntent();
    }

    @OnClick(R.id.btn_gallery)
    void onGalleryButtonClick() {
        Intent chooseFromGalleryIntent = new Intent(Intent.ACTION_PICK);
        chooseFromGalleryIntent.setType("image/*");
        if (chooseFromGalleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooseFromGalleryIntent, REQUEST_IMAGE_FROM_GALLERY);
        }
    }

    @OnTextChanged(R.id.et_amount)
    protected void handleTextChange(Editable editable) {
        if (editable == null || editable.toString().trim().isEmpty()) {
            mAddButton.setEnabled(false);
        } else {
            mAddButton.setEnabled(true);
        }
    }

    @OnClick(R.id.btn_add)
    void onAddButtonClick() {
        float amount = Float.valueOf(mAmount.getText().toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT, amount);
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_DESCRIPTION, mDescription.getText().toString());
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_CATEGORY, mSelectedCategory);
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_DATE, new Date().getTime());

        if(mSelectedImage != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            String imageFileName = "IMG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File file = null;
            try {
                file = new File(storageDir, imageFileName+".jpg");
                OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
                mSelectedImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Uri imageUri = FileProvider.getUriForFile(this,
                    "com.kfugosic.expensetracker.data.images",
                    file);
            contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_PHOTO_LOCATION, imageUri.toString());

            if(mTempImageUri != null) {
                getContentResolver().delete(mTempImageUri, null, null);
            }
        }

        getContentResolver().insert(ExpensesContract.ExpensesEntry.CONTENT_URI, contentValues);

        Intent intent = new Intent();
        intent.putExtra(MainActivity.SHOULD_RESTART_KEY, true);
        setResult(RESULT_OK, intent);

        ExpensesWidgetService.startActionUpdateExpensesTextviews(this);
        Toast.makeText(this, R.string.new_expense_added_info, Toast.LENGTH_SHORT).show();
        if(mStayButton.isChecked()) {
            resetUI();
        } else {
            finish();
        }

    }

    private void resetUI() {
        mAmount.setText("");
        mDescription.setText("");
        mBtnSelectCategory.setText(R.string.select_category_button_default_text);
        mImageTextView.setText(R.string.image_text_view_default_text);
        mImageView.setImageBitmap(null);
        mTempImageUri = null;
        mImageUri = null;
        mSelectedCategory = -1;
        mSelectedImage = null;
    }

    @OnClick(R.id.btn_select_category)
    void onSelectCategoryButtonClick() {
        showCategoryDialog();
    }

    private void initOrRestartLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(CATEGORIES_LOADER_ID);

        if (loader == null) {
            loaderManager.initLoader(CATEGORIES_LOADER_ID, null, mLoader);
        } else {
            loaderManager.restartLoader(CATEGORIES_LOADER_ID, null, mLoader);
        }
    }

    @Override
    public void onDataLoaded(int id, Cursor cursor) {
        mCursor = cursor;
    }

    private void showCategoryDialog() {
        final AtomicInteger clickedPos = new AtomicInteger(-1);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(mCursor == null) {
            // If cursor wasn't set asynchronously by now (loader in onCreate), we must block the thread and load it
            mCursor = getContentResolver().query(CategoriesContract.CategoriesEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        }
        builder.setCursor(mCursor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position) {
                        clickedPos.set(position);
                        int idIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry._ID);
                        int nameIndex = mCursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_NAME);
                        mCursor.moveToPosition(clickedPos.get());
                        mSelectedCategory = mCursor.getInt(idIndex);
                        String name = mCursor.getString(nameIndex);
                        mBtnSelectCategory.setText(name);
                    }
                },
                CategoriesContract.CategoriesEntry.COLUMN_NAME);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
