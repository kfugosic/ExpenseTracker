package com.kfugosic.expensetracker.ui;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.DataLoader;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;
import com.kfugosic.expensetracker.widget.ExpensesWidgetService;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kfugosic.expensetracker.ui.CategoriesActivity.CATEGORIES_LOADER_ID;

public class NewExpenseActivity extends AppCompatActivity implements IDataLoaderListener {
    private static final String TAG = "TAG1234";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_FROM_GALLERY = 2;

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

    private DataLoader mLoader;
    private Cursor mCursor;

    private String mCurrentPhotoPath;
    private Uri mImageUri;

    private int mSelectedCategory = -1;
    private Bitmap mSelectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_expense);

        ButterKnife.bind(this);

        mLoader = new DataLoader(this, this, CategoriesContract.CategoriesEntry.CONTENT_URI);
        initOrRestartLoader();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mCameraBtn.setEnabled(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            //Log.d(TAG, "onActivityResult1: "+imageBitmap.getHeight()+" "+ imageBitmap.getWidth());
            String filename = mImageUri.getPath();
            int cut = filename.lastIndexOf('/');
            filename = filename.substring(cut + 1);
            Log.d(TAG, "onActivityResult: "+ filename );
            getContentResolver().notifyChange(mImageUri, null);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                Log.d(TAG, "onActivityResult2: "+bitmap.getHeight()+" "+ bitmap.getWidth());
                mImageView.setImageBitmap(bitmap);
                mSelectedImage = bitmap;
                getContentResolver().delete(mImageUri, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //mImageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                Log.d(TAG, "onActivityResult3: "+selectedImage.getHeight()+" "+ selectedImage.getWidth());
                mImageView.setImageBitmap(selectedImage);
                mSelectedImage = selectedImage;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "ERROR - File not found", Toast.LENGTH_LONG).show();
            }
        }
    }

    // developer.android.com
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "tmp_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.d(TAG, "createImageFile: "+storageDir + " " + imageFileName);
        mCurrentPhotoPath = image.getAbsolutePath();
        //mImageUri = Uri.fromFile(image);
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mImageUri = FileProvider.getUriForFile(this,
                        "com.kfugosic.expensetracker.data.images",
                        photoFile);
                Log.d(TAG, "dispatchTakePictureIntent: "+ mImageUri);
                Log.d(TAG, "dispatchTakePictureIntent: "+ photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @OnClick(R.id.btn_camera)
    void onCameraButtonClick() {
        dispatchTakePictureIntent();
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }*/
    }

    @OnClick(R.id.btn_gallery)
    void onGalleryButtonClick() {
        Intent chooseFromGalleryIntent = new Intent(Intent.ACTION_PICK);
        chooseFromGalleryIntent.setType("image/*");
        if (chooseFromGalleryIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooseFromGalleryIntent, REQUEST_IMAGE_FROM_GALLERY);
        }
    }

    @OnClick(R.id.btn_add)
    void onAddButtonClick() {
        Editable editable = mAmount.getText();
        if(editable == null || editable.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please set the amount.", Toast.LENGTH_SHORT).show();
            return;
        }
        float amount = Float.valueOf(editable.toString());

        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT, amount);
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_DESCRIPTION, mDescription.getText().toString());
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_CATEGORY, mSelectedCategory);
        contentValues.put(ExpensesContract.ExpensesEntry.COLUMN_DATE, new Date().getTime());

        if(mSelectedImage != null) {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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
        }

        Uri uri = getContentResolver().insert(ExpensesContract.ExpensesEntry.CONTENT_URI, contentValues);

        Intent intent = new Intent();
        intent.putExtra(MainActivity.SHOULD_RESTART_KEY, true);
        setResult(RESULT_OK, intent);

        ExpensesWidgetService.startActionUpdateExpensesTextviews(this);
        finish();
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
            // If cursor wasn't set asynchronously by now, we must block thread and load it
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
                    }
                },
                "name");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothing
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
