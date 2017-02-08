package com.ezequielc.successplanner.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

    ImageView mNewImage;
    TextView mNewText;
    ViewGroup mViewGroup;

    int mStartX, mStartY;
    boolean mEditingOrDeleting;

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

        mViewGroup = (ViewGroup) findViewById(R.id.activity_vision_board);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        // View holds position while being chosen to be delete or edited
        if (mEditingOrDeleting) {
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
        // AlertDialog notifying users Vision Board will not be saved
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Are you sure you want to exit?" +
                "\nYou will lose your Vision Board")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    VisionBoardActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    public void takeScreenshot(){
        // If Write External Storage is not granted, cancels the process
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Do not have Storage Permission", Toast.LENGTH_SHORT).show();
            return;
        }

        // AlertDialog asking if User wants to save the vision board
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Save Vision Board?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            // Path to the screenshot folder
                            String path = "/Pictures/Screenshots/VisionBoard.jpg";
                            String pathName = Environment.getExternalStorageDirectory().toString() + path;

                            View screenshot = getWindow().getDecorView().getRootView();
                            screenshot.setDrawingCacheEnabled(true);
                            Bitmap bitmap = Bitmap.createBitmap(screenshot.getDrawingCache());
                            screenshot.setDrawingCacheEnabled(false);

                            final File imageFile = new File(pathName);

                            FileOutputStream outputStream = new FileOutputStream(imageFile);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                            outputStream.flush();
                            outputStream.close();

                            // Updates the gallery to see new files instantly
                            MediaScannerConnection.scanFile(getApplicationContext(),
                                    new String[]{imageFile.toString()}, null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        public void onScanCompleted(String path, Uri uri) {

                                        }
                                    });

                            // AlertDialog asking if User wants to open the vision board
                            new AlertDialog.Builder(VisionBoardActivity.this)
                                    .setMessage("Do you want to open Vision Board?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            openScreenShoot(imageFile);
                                        }
                                    })
                                    .setNegativeButton("No", null)
                                    .setCancelable(false)
                                    .create().show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    // Intent to open screenshot
    public void openScreenShoot(File file){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    // AlertDialog listing text options
    public void textOptions(){
        CharSequence[] textOptions = {"New", "Edit", "Change Color", "Change Size"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Text Options:")
                .setItems(textOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                    }
                }).create().show();
    }

    // Displays New TextView unto the Vision Board
    public void addNewText(){
        RelativeLayout.LayoutParams wrapContent =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        mNewText = new TextView(getApplication());
        mNewText.setLayoutParams(wrapContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(VisionBoardActivity.this);
        LayoutInflater inflater = LayoutInflater.from(VisionBoardActivity.this);
        View view = inflater.inflate(R.layout.dialog_add_text, null);
        builder.setView(view);

        final EditText editText = (EditText) view.findViewById(R.id.new_text_edit_text);

        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (editText.getText().toString().trim().length() == 0) {
                            Toast.makeText(VisionBoardActivity.this, "Please fill field!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String input = editText.getText().toString();
                        mNewText.setText(input);
                        mViewGroup.addView(mNewText);
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false);
        builder.create().show();

        mNewText.setTextSize(24);
        mNewText.setOnTouchListener(this);
    }

    // Allow TextView to be edited
    public void editTextView(){
        if (mViewGroup.getChildCount() == 0) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditingOrDeleting = true;
        Toast.makeText(this, "Choose Text to Edit...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handles Error when picking an ImageView instead of a TextView, ClassCastException
                    if (child instanceof ImageView) {
                        Toast.makeText(VisionBoardActivity.this, "This is an Image", Toast.LENGTH_SHORT).show();
                        mEditingOrDeleting = false;
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(VisionBoardActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(VisionBoardActivity.this);
                    View dialogView = inflater.inflate(R.layout.dialog_add_text, null);
                    builder.setView(dialogView);

                    // Grabs the text in the adapter position and set it to the edit text
                    final EditText editText = (EditText) dialogView.findViewById(R.id.new_text_edit_text);
                    editText.setText(((TextView) child).getText().toString());

                    builder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String input = editText.getText().toString();
                            ((TextView) child).setText(input);
                        }
                    })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false);
                    builder.create().show();
                    mEditingOrDeleting = false;
                }
            });
        }
    }

    // Change color of TextView
    public void changeTextColor(){
        if (mViewGroup.getChildCount() == 0) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditingOrDeleting = true;
        Toast.makeText(this, "Choose Text to Change Color...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handles Error when picking an ImageView instead of a TextView, ClassCastException
                    if (child instanceof ImageView) {
                        Toast.makeText(VisionBoardActivity.this, "This is an Image", Toast.LENGTH_SHORT).show();
                        mEditingOrDeleting = false;
                        return;
                    }
                    pickColor((TextView) child);
                    mEditingOrDeleting = false;
                }
            });
        }
    }

    // List of colors to be picked for TextView
    public void pickColor(final TextView textView){
        CharSequence[] colors = {"Green", "Red", "Blue", "Yellow", "Magenta", "Black", "White", "Default"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Pick Color:")
                .setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                    }
                }).create().show();
    }

    // List of colors to be picked for ViewGroup
    public void pickColor(final ViewGroup viewGroup){
        CharSequence[] colors = {"Green", "Red", "Blue", "Yellow", "Magenta", "White", "Default"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Pick Color:")
                .setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
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
                    }
                }).create().show();
    }

    // Change the text size of the TextView
    public void changeTextSize(){
        if (mViewGroup.getChildCount() == 0) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        mEditingOrDeleting = true;
        Toast.makeText(this, "Choose Text to Change Size...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handles Error when picking an ImageView instead of a TextView, ClassCastException
                    if (child instanceof ImageView) {
                        Toast.makeText(VisionBoardActivity.this, "This is an Image", Toast.LENGTH_SHORT).show();
                        mEditingOrDeleting = false;
                        return;
                    }
                    pickSize((TextView) child);
                    mEditingOrDeleting = false;
                }
            });
        }
    }

    // Displays a Dialog with a NumberPicker to pick a text Size
    public void pickSize(final TextView textView){
        AlertDialog.Builder builder = new AlertDialog.Builder(VisionBoardActivity.this);
        LayoutInflater inflater = LayoutInflater.from(VisionBoardActivity.this);
        View view = inflater.inflate(R.layout.dialog_number_picker, null);
        builder.setView(view);

        final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker);

        numberPicker.setMaxValue(100);
        numberPicker.setMinValue(10);
        float currentSize = textView.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        numberPicker.setValue((int) currentSize);

        builder.setTitle("Choose Size")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int value = numberPicker.getValue();
                        textView.setTextSize(value);
                    }
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

        mNewImage = new ImageView(getApplicationContext());
        mNewImage.setLayoutParams(wrapContent);
        mViewGroup.addView(mNewImage);
        mNewImage.setImageBitmap(bitmap);
        mNewImage.setAdjustViewBounds(true);
        mNewImage.setOnTouchListener(this);
    }

    // AlertDialog listing delete options
    public void deletionOptions(){
        if (mViewGroup.getChildCount() == 0) {
            Toast.makeText(this, "Vision Board is Empty", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] deleteOptions = {"Delete Item", "Delete All"};
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setTitle("Deletion Options:")
                .setItems(deleteOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0: // Delete One View
                                deleteView();
                                break;
                            case 1: // Delete All Views
                                deleteAllViews();
                                break;
                            default:
                                break;
                        }
                    }
                }).create().show();
    }

    // Removes all views in ViewGroup
    public void deleteAllViews(){
        new AlertDialog.Builder(VisionBoardActivity.this)
                .setMessage("Delete All?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mViewGroup.removeAllViews();
                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(false)
                .create().show();
    }

    // Specific view from ViewGroup to be removed
    public void deleteView(){
        mEditingOrDeleting = true;
        Toast.makeText(this, "Choose Item to Delete...", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < mViewGroup.getChildCount(); i++) {
            final View child = mViewGroup.getChildAt(i);
            child.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(VisionBoardActivity.this)
                            .setMessage("Delete Item?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mViewGroup.removeView(child);
                                }
                            })
                            .setNegativeButton("No", null)
                            .setCancelable(false)
                            .create().show();
                    mEditingOrDeleting = false;
                }
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
                takeScreenshot();
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
