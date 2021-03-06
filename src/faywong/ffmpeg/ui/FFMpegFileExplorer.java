package faywong.ffmpeg.ui;

import android.app.ListActivity;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.text.Editable;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.media.ffmpeg.FFMpeg;

import faywong.ffmpeg.ui.R;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.util.Arrays;
import java.util.Comparator;

public class FFMpegFileExplorer extends ListActivity {
    private static final String  TAG             = "FFMpegFileExplorer";
    private static final boolean DEBUG           = true;
    private String[]             mRoot           = { "/sdcard", "/extsdcard", };
    private int                  curDirectoryInx = 0;
    private TextView             mTextViewLocation;
    private File[]               mFiles;
    private EditText             mUrlEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ffmpeg_file_explorer);
        mTextViewLocation = (TextView) findViewById(R.id.textview_path);
        getDirectory(mRoot[curDirectoryInx]);
        mUrlEditText = (EditText) findViewById(R.id.NetURL);

        Button playBtn = (Button) findViewById(R.id.startBtn);

        playBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub
                Editable editContent = mUrlEditText.getText();

                if (editContent.toString().equals("")) {

                    /* Todo: Protocol support check??? */
                    Toast.makeText(FFMpegFileExplorer.this, "Your have not set a valid URL!",
                                   Toast.LENGTH_SHORT).show();
                } else {
                    if (DEBUG) {
                        Log.e(TAG, "The input URL is " + editContent);
                    }

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(mUrlEditText.getWindowToken(), 0);
                    startPlayer(editContent.toString());
                }
            }
        });
    }

    protected static boolean checkExtension(File file) {
        String[] exts = FFMpeg.EXTENSIONS;

        for (int i = 0; i < exts.length; i++) {
            if (file.getName().indexOf(exts[i]) > 0) {
                return true;
            }
        }

        return false;
    }

    private void sortFilesByDirectory(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f1.length()).compareTo(f2.length());
            }
        });
    }

    private void getDirectory(String dirPath) {
        try {
            curDirectoryInx++;
            mTextViewLocation.setText("Location: " + dirPath);

            File   f    = new File(dirPath);
            File[] temp = f.listFiles();

            sortFilesByDirectory(temp);

            File[] files = null;

            if (!dirPath.equals(mRoot)) {
                files = new File[temp.length + 1];
                System.arraycopy(temp, 0, files, 1, temp.length);
                files[0] = new File(f.getParent());
            } else {
                files = temp;
            }

            mFiles = files;
            setListAdapter(new FileExplorerAdapter(this, files, temp.length == files.length));
        } catch (Exception ex) {
            if (curDirectoryInx < mRoot.length) {
                getDirectory(mRoot[curDirectoryInx]);
            } else {
                FFMpegMessageBox.show(this, "Error", ex.getMessage());
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = mFiles[position];

        if (file.isDirectory()) {
            if (file.canRead()) {
                getDirectory(file.getAbsolutePath());
            } else {
                FFMpegMessageBox.show(this, "Error", "[" + file.getName() + "] folder can't be read!");
            }
        } else {
            if (!checkExtension(file)) {
                StringBuilder strBuilder = new StringBuilder();

                for (int i = 0; i < FFMpeg.EXTENSIONS.length; i++) {
                    strBuilder.append(FFMpeg.EXTENSIONS[i] + " ");
                }

                FFMpegMessageBox.show(this, "Error", "File must have this extensions: " + strBuilder.toString());

                return;
            }

            startPlayer(file.getAbsolutePath());
        }
    }

    private void startPlayer(String filePath) {
        Intent i = new Intent(this, FFMpegPlayerActivity.class);

        i.putExtra(getResources().getString(R.string.input_file), filePath);
        startActivity(i);
    }
}