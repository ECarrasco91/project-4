package com.ezequielc.successplanner.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ezequielc.successplanner.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class VisionBoardActivity extends AppCompatActivity implements View.OnTouchListener {
    public static final int IMAGE_REQUEST = 1;

    ImageView mSavedVisionBoard, mNewImageView;
    TextView mNewTextView;
    ViewGroup mViewGroup;

    int mStartX, mStartY;
    boolean mEditingOrDeletingItem, mItemsOnVisionBoard, mVisionBoardExist, mVisionBoardSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_board);

        getSupportActionBar().setTitle("Vision Board");

        // Request Write External Storage permission if not granted
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, IMAGE_REQUEST);
        }

        mViewGroup = findViewById(R.id.activity_vision_board);
        mSavedVisionBoard = findViewById(R.id.saved_vision_board);
        checkVisionBoard();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // View holds position while being chosen to be delete or edited
        if (mEditingOrDeletingItem) {
            return false;
        }

        RelativeLayout.LayoutParams getLayoutParams =
                (RelativeLayout.LayoutParams) view.getLayoutParams();

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mStartX = (int) motionEvent.getX();
                mStartY = (int) motionEvent.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                int delta_x = (int) motionEvent.getX() - mStartX;
                int delta_y = (int) motionEvent.getY() - mStartY;
                getLayoutParams.leftMargin = getLayoutParams.leftMargin + delta_x;
                getLayoutParams.rightMargin = getLayoutParams.rightMargin - delta_x;
                getLayoutParams.topMargin = getLayoutParams.topMargin + delta_y;
                getLayoutParams.bottomMargin = getLayoutParams.bottomMargin - delta_y;
                view.setLayoutParams(getLayoutParams);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                break;
        }
        mViewGroup.invalidate();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mVisionBoardSaved || !mItemsOnVisionBoard) {
            super.onBackPressed();
            return;
        }
        // AlertDialog notifying users Vision Board will not be saved
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Are you sure you want to exit?"+"\nYou will lose your Vision Board")
                .setPositiveButton("Yes", (dialogInterface, i) -> VisionBoardActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    public void screenshotPrompt(){
        // If Write External Storage is not granted, cancels the process
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Do not have Storage Permission", Toast.LENGTH_SHORT).show();
            return;
        }

        // AlertDialog asking if User wants to save the vision board
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Are you sure you want to save Vision Board?"+"\nYou will not be able to edit after!")
                .setPositiveButton("Yes", (dialogInterface, i) -> captureScreenshot())
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    public File visionBoardFile(){
        String visionBoardPath = getFilesDir().toString()+"/VisionBoard.jpg";
        return new File(visionBoardPath);
    }

    public void checkVisionBoard(){
        if (visionBoardFile().exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(visionBoardFile().getAbsolutePath());
            mSavedVisionBoard.setImageBitmap(bitmap);
            mSavedVisionBoard.setVisibility(View.VISIBLE);
            mVisionBoardExist = true;
        } else {
            mVisionBoardExist = false;
            mItemsOnVisionBoard = false;
        }
    }

    public void captureScreenshot(){
        try {
            mViewGroup.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(mViewGroup.getDrawingCache());
            mViewGroup.setDrawingCacheEnabled(false);

            mVisionBoardExist = false;

            if (visionBoardFile().exists()) {
                Toast.makeText(this, "Vision Board has been updated!", Toast.LENGTH_LONG).show();
                mVisionBoardExist = true;
            }

            FileOutputStream outputStream = new FileOutputStream(visionBoardFile());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!mVisionBoardExist) {
            Toast.makeText(VisionBoardActivity.this,
                    "Vision Board has been saved!", Toast.LENGTH_SHORT).show();
        }

        mVisionBoardSaved = true;
    }

    // AlertDialog listing text options
    public void textOptions(){
        CharSequence[] textOptions = {"New", "Edit", "Change Color", "Change Size"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Text Options:")
                .setItems(textOptions, (dialogInterface, i) -> {
                    switch (i) {
                        case 0: // Add New Text
                            addNewText();
                            break;
                        case 1: // Edit Text
                            editTextView();
                            break;
                        case 2: // Change Color of Text
                            changeTextColor();
                            break;
                        case 3: // Change Size of Text
                            changeTextSize();
                            break;
                        default:
                            break;
                    }
                }).create().show();
    }

    // Displays New TextView unto the Vision Board
    public void addNewText(){
        RelativeLayout.LayoutParams wrapContent =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        mNewTextView = new TextView(getApplication());
        mNewTextView.setLayoutParams(wrapContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(VisionBoardActivity.this);
        LayoutInflater inflater = LayoutInflater.from(VisionBoardActivity.this);
        View view = inflater.inflate(R.layout.dialog_add_text, null);
        builder.setView(view);

        final EditText editText = view.findViewById(R.id.new_text_edit_text);

        builder.setPositiveButton("ADD", (dialogInterface, i) -> {
            if (editText.getText().toString().trim().length() == 0) {
                Toast.makeText(VisionBoardActivity.this, "Please fill field!", Toast.LENGTH_SHORT).show();
                return;
            }
            String input = editText.getText().toString();
            mNewTextView.setText(input);
            mViewGroup.addView(mNewTextView);
        })
                .setNegativeButton("Cancel", null)
                .setCancelable(false);
        builder.create().show();

        mNewTextView.setTextSize(24);
        mNewTextView.setOnTouchListener(this);
        mItemsOnVisionBoard = true;
    }

    // Allow TextView to be edited
    public void editTextView(){
        if (mViewGroup.getChildCount() == 1) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditingOrDeletingItem = true;
        Toast.makeText(this, "Choose Text to Edit...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(view -> {
                // Handles Error when picking an ImageView instead of a TextView, ClassCastException
                if (child instanceof ImageView) {
                    Toast.makeText(VisionBoardActivity.this, "This is an Image", Toast.LENGTH_SHORT).show();
                    mEditingOrDeletingItem = false;
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(VisionBoardActivity.this);
                LayoutInflater inflater = LayoutInflater.from(VisionBoardActivity.this);
                View dialogView = inflater.inflate(R.layout.dialog_add_text, null);
                builder.setView(dialogView);

                // Grabs the text in the adapter position and set it to the edit text
                final EditText editText = dialogView.findViewById(R.id.new_text_edit_text);
                editText.setText(((TextView) child).getText().toString());

                builder.setPositiveButton("EDIT", (dialogInterface, i1) -> {
                    String input = editText.getText().toString();
                    ((TextView) child).setText(input);
                })
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false);
                builder.create().show();
                mEditingOrDeletingItem = false;
            });
        }
    }

    // Change color of TextView
    public void changeTextColor(){
        if (mViewGroup.getChildCount() == 1) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditingOrDeletingItem = true;
        Toast.makeText(this, "Choose Text to Change Color...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(view -> {
                // Handles Error when picking an ImageView instead of a TextView, ClassCastException
                if (child instanceof ImageView) {
                    Toast.makeText(VisionBoardActivity.this, "This is an Image", Toast.LENGTH_SHORT).show();
                    mEditingOrDeletingItem = false;
                    return;
                }
                pickColor((TextView) child);
                mEditingOrDeletingItem = false;
            });
        }
    }

    // List of colors to be picked for TextView
    public void pickColor(final TextView textView){
        CharSequence[] colors = {"Green", "Red", "Blue", "Yellow", "Magenta", "Black", "White", "Default"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Pick Color:")
                .setItems(colors, (dialogInterface, i) -> {
                    switch (i) {
                        case 0: // Green
                            textView.setTextColor(Color.GREEN);
                            break;
                        case 1: // Red
                            textView.setTextColor(Color.RED);
                            break;
                        case 2: // Blue
                            textView.setTextColor(Color.BLUE);
                            break;
                        case 3: // Yellow
                            textView.setTextColor(Color.YELLOW);
                            break;
                        case 4: // Magenta
                            textView.setTextColor(Color.MAGENTA);
                            break;
                        case 5: // Black
                            textView.setTextColor(Color.BLACK);
                            break;
                        case 6: // White
                            textView.setTextColor(Color.WHITE);
                            break;
                        case 7: // Default
                            textView.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                    android.R.color.secondary_text_dark));
                        default:
                            break;
                    }
                }).create().show();
    }

    // List of colors to be picked for ViewGroup
    public void pickColor(final ViewGroup viewGroup){
        CharSequence[] colors = {"Green", "Red", "Blue", "Yellow", "Magenta", "White", "Default"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Pick Color:")
                .setItems(colors, (dialogInterface, i) -> {
                    switch (i) {
                        case 0: // Green
                            viewGroup.setBackgroundColor(Color.GREEN);
                            break;
                        case 1: // Red
                            viewGroup.setBackgroundColor(Color.RED);
                            break;
                        case 2: // Blue
                            viewGroup.setBackgroundColor(Color.BLUE);
                            break;
                        case 3: // Yellow
                            viewGroup.setBackgroundColor(Color.YELLOW);
                            break;
                        case 4: // Magenta
                            viewGroup.setBackgroundColor(Color.MAGENTA);
                            break;
                        case 5: // White
                            viewGroup.setBackgroundColor(Color.WHITE);
                            break;
                        case 6: // Default
                            viewGroup.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(),
                                            android.R.color.holo_orange_light));
                            break;
                        default:
                            break;
                    }
                }).create().show();
    }

    // Change the text size of the TextView
    public void changeTextSize(){
        if (mViewGroup.getChildCount() == 1) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditingOrDeletingItem = true;
        Toast.makeText(this, "Choose Text to Change Size...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(view -> {
                // Handles Error when picking an ImageView instead of a TextView, ClassCastException
                if (child instanceof ImageView) {
                    Toast.makeText(VisionBoardActivity.this, "This is an Image", Toast.LENGTH_SHORT).show();
                    mEditingOrDeletingItem = false;
                    return;
                }
                pickSize((TextView) child);
                mEditingOrDeletingItem = false;
            });
        }
    }

    // Displays a Dialog with a NumberPicker to pick a text Size
    public void pickSize(final TextView textView){
        AlertDialog.Builder builder = new AlertDialog.Builder(VisionBoardActivity.this);
        LayoutInflater inflater = LayoutInflater.from(VisionBoardActivity.this);
        View view = inflater.inflate(R.layout.dialog_number_picker, null);
        builder.setView(view);

        final NumberPicker numberPicker = view.findViewById(R.id.number_picker);

        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(10);
        float currentSize = textView.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        numberPicker.setValue((int) currentSize);

        builder.setTitle("Choose Size")
                .setPositiveButton("Set", (dialogInterface, i) -> {
                    int value = numberPicker.getValue();
                    textView.setTextSize(value);
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false);
        builder.create().show();
    }

    // Displays New ImageView unto the Vision Board
    public void addNewImage(Bitmap bitmap){
        RelativeLayout.LayoutParams wrapContent =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        mNewImageView = new ImageView(getApplicationContext());
        mNewImageView.setLayoutParams(wrapContent);
        mViewGroup.addView(mNewImageView);
        mNewImageView.setImageBitmap(bitmap);
        mNewImageView.setAdjustViewBounds(true);
        mNewImageView.setOnTouchListener(this);
        mItemsOnVisionBoard = true;
    }

    // AlertDialog listing delete options
    public void deletionOptions(){
        if (mViewGroup.getChildCount() == 1) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] deleteOptions = {"Delete Item", "Delete All Items", "Delete Saved Vision Board"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Deletion Options:")
                .setItems(deleteOptions, (dialogInterface, i) -> {
                    switch (i) {
                        case 0: // Delete One View
                            deleteView();
                            break;
                        case 1: // Delete All Views
                            deleteAllViews();
                            break;
                        case 2: // Delete Saved Vision Board
                            deleteSavedVisionBoard();
                            break;
                        default:
                            break;
                    }
                }).create().show();
    }

    // Deletes saved vision board from app file directory
    public void deleteSavedVisionBoard(){
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Delete Saved Vision Board?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    mViewGroup.removeAllViews();
                    visionBoardFile().delete();
                    mVisionBoardExist = false;
                    mVisionBoardSaved = false;
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    // Removes all views in ViewGroup
    public void deleteAllViews(){
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Delete All?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    mViewGroup.removeAllViews();
                    mItemsOnVisionBoard = false;
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    // Specific view from ViewGroup to be removed
    public void deleteView(){
        mEditingOrDeletingItem = true;
        Toast.makeText(this, "Choose Item to Delete...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(view -> {
                new AlertDialog.Builder(VisionBoardActivity.this)
                        .setMessage("Delete Item?")
                        .setPositiveButton("Yes", (dialogInterface, i1) -> mViewGroup.removeView(child))
                        .setNegativeButton("No", null)
                        .setCancelable(false)
                        .create().show();
                mEditingOrDeletingItem = false;
            });
        }
    }

    // Change Color of ViewGroup
    public void changeBackgroundColor(){
        pickColor(mViewGroup);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.vision_board_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload_image:
                selectImage();
                return true;

            case R.id.action_text:
                textOptions();
                return true;

            case R.id.action_delete:
                deletionOptions();
                return true;

            case R.id.action_save:
                screenshotPrompt();
                return true;

            case R.id.action_change_background:
                changeBackgroundColor();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && null != data) {
            Uri image = data.getData();
            try {
                Bitmap resizeBitmap = resizedBitmap(getApplicationContext(), image, 2);
                addNewImage(resizeBitmap);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                Toast.makeText(this, "Images too large. Use smaller Images. Restarting...",
                        Toast.LENGTH_LONG).show();
                mViewGroup.removeAllViews();
            }
        }
    }

    // Intent to select an Image
    public void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Select Image"), IMAGE_REQUEST);
    }

    private Bitmap resizedBitmap(Context context, Uri uri, int sampleSize){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
    }
}
