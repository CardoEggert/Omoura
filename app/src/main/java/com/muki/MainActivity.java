package com.muki;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.muki.core.MukiCupApi;
import com.muki.core.MukiCupCallback;
import com.muki.core.model.Action;
import com.muki.core.model.DeviceInfo;
import com.muki.core.model.ErrorCode;
import com.muki.core.model.ImageProperties;
import com.muki.core.util.ImageUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
//Muki code is taken from github, REFRENCE : https://github.com/gustavpaulig/Paulig-Muki
public class MainActivity extends AppCompatActivity {

    private EditText mSerialNumberEdit;
    private TextView mCupIdText;
    private TextView mDeviceInfoText;
    private ImageView mCupImage;
    private SeekBar mContrastSeekBar;
    private ProgressDialog mProgressDialog;
    private Calendar userCalendar;

    private User user;

    private Bitmap mImage;
    private int mContrast = ImageProperties.DEFAULT_CONTRACT;

    private String mCupId;
    private MukiCupApi mMukiCupApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Loading. Please wait...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mMukiCupApi = new MukiCupApi(getApplicationContext(), new MukiCupCallback() {
            @Override
            public void onCupConnected() {
                showToast("Cup connected");
            }

            @Override
            public void onCupDisconnected() {
                showToast("Cup disconnected");
            }

            @Override
            public void onDeviceInfo(DeviceInfo deviceInfo) {
                hideProgress();
                mDeviceInfoText.setText(deviceInfo.toString());
            }

            @Override
            public void onImageCleared() {
                showToast("Image cleared");
            }

            @Override
            public void onImageSent() {
                showToast("Image sent");
            }

            @Override
            public void onError(Action action, ErrorCode errorCode) {
                showToast("Error:" + errorCode + " on action:" + action);
            }
        });

        mSerialNumberEdit = (EditText) findViewById(R.id.serailNumberText);
        mCupIdText = (TextView) findViewById(R.id.cupIdText);
        mDeviceInfoText = (TextView) findViewById(R.id.deviceInfoText);
        mCupImage = (ImageView) findViewById(R.id.imageSrc);
        mContrastSeekBar = (SeekBar) findViewById(R.id.contrastSeekBar);
        mContrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mContrast = i - 100;
                showProgress();
                setupImage();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        user = new User("Jeff", 25, 80.5, 75);
        mCupImage.setImageBitmap(textAsBitmap(chooseNewsAccordingToUser(user), 12F));
        userCalendar = new Calendar();

        reset(null);
    }

    private String chooseNewsAccordingToUser(User user) {
        //List<String> news = readNewsData();
        //TODO: Filter

        Integer userAge = user.getAge();
        Double userWeight = user.getWeight();
        return filterUserDependingUser(userAge, userWeight);
//        if (userWeight <= 100 && userAge < 30){
//            return news.get(0);
//        }else{
//
//        }
//        return news.get(2);

    }

    private String filterUserDependingUser(Integer userAge, Double userWeight) {
        Integer tempSwitch = null;
        if (userAge < 18) {
            tempSwitch = 0;
        } else if (userAge >= 18 && userAge <= 24) {
            tempSwitch = 1;
        } else if (userAge >= 25 && userAge <= 34) {
            tempSwitch = 2;
        } else if (userAge >= 35 && userAge <= 49) {
            tempSwitch = 3;
        } else if (userAge >= 50 && userAge <= 64) {
            tempSwitch = 4;
        } else if (userAge >= 65) {
            tempSwitch = 5;
        }

        List<String> news;
        switch (tempSwitch) {
            //underage
            case 0:
                news = readNewsData(1, 10);
                if (user.getReadyNess() < 85) return news.get(0);
                else return news.get(5);
                //young adult
            case 1:
                news = readNewsData(11, 20);
                if (user.getReadyNess() < 85) return news.get(0);
                else return news.get(5);
                //adult
            case 2:
                news = readNewsData(21, 30);
                if (user.getReadyNess() < 85) return news.get(0);
                else return news.get(5);
                //mature adult
            case 3:
                news = readNewsData(31, 40);
                if (user.getReadyNess() < 85) return news.get(0);
                else return news.get(5);
                //young elderly
            case 4:
                news = readNewsData(41, 50);
                if (user.getReadyNess() < 85) return news.get(0);
                else return news.get(5);
                //elderly
            case 5:
                news = readNewsData(51, 60);
                if (user.getReadyNess() < 85) return news.get(0);
                else return news.get(5);
        }

        //TODO: filter data from BMI
        return null;
    }

    private String readMockData() {
        List<String> relaxIndex = new ArrayList<>();
        List<String> date = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.mockweekdata)));
            String str;
            str = in.readLine();
            String[] splitted = str.split("\t");
            Integer summaryIndex = 0;
            Integer dateIndex = 0;
            for (int i = 0; i < splitted.length; i++) {
                if (splitted[i].equals("summary_date")) dateIndex = i;
                if (splitted[i].equals("score_recovery_index")) summaryIndex = i;
            }
            while ((str = in.readLine()) != null) {
                String[] toBeAdded = str.split("\t");
                relaxIndex.add(toBeAdded[summaryIndex]);
                date.add(toBeAdded[dateIndex]);
            }

            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("File is not found!");
        } catch (IOException e) {
            System.out.println("IOExpection");
        }
        return filterData(relaxIndex, date);
    }

    private String filterData(List<String> relaxIndex, List<String> date) {
        //If under 85 last night then, check if relaxindex is under 70
        if (Integer.parseInt(relaxIndex.get(relaxIndex.size()-1))<85){
            //If relaxindes is under 70, check weekly average relaxindex
            if(Integer.parseInt(relaxIndex.get(relaxIndex.size()-1))<70){

            //Calculate weekly relax index
            Integer sum = 0;
            for (String index:relaxIndex) {
                sum += Integer.parseInt(index);
            }
            Integer weeklyRelaxIndex = sum/7;
            //If weekly relaxindex is under 60, then check the calendar
            if (weeklyRelaxIndex <60){
                //If he doesn't have something to do the same day, then tell him to take a day off
                if (userCalendar.getTodo() == null){
                    //return "Take a day off and do relaxing breathing exercises";
                    return suggestResting();
                }else{
                    return suggestTakingTimeOffDuringTheWeekend();
                }
            }else{
                if (userCalendar.getTodo() == null){
                    //return "Take a day off and do relaxing breathing exercises";
                    return suggestToRestWell();
                }else{
                    return suggestMotivatingQuotes();
                }
            }
            }else{
                if (userCalendar.getTodo() == null){
                    //return "Take a day off and do relaxing breathing exercises";
                    return suggestToRestWell();
                }else{
                    return suggestMotivatingQuotes();
                }
            }
        }
        return suggestMotivatingQuotes();
    }

    private Random random = new Random();

    private String suggestResting() {
        Integer randomNr = random.nextInt(4)+1;
        List<String> restingSuggestions = new ArrayList<>(5);
        restingSuggestions.add("You've had a hard time recently, have some rest :)");
        restingSuggestions.add("You don't have anything ahead of you today, get some rest :)");
        restingSuggestions.add("You've had some bad sleep recently, have some rest :)");
        restingSuggestions.add("Take some stress off and rest :)");
        restingSuggestions.add("Resting doesn't hurt today, rest well :)");
        return restingSuggestions.get(randomNr);
    }
    private String suggestMotivatingQuotes() {
        Integer randomNr = random.nextInt(4)+1;
        List<String> motivatingQuotes = new ArrayList<>(5);
        motivatingQuotes.add("Today is gonna be an awesome day!");
        motivatingQuotes.add("Greatness is just around the corner");
        motivatingQuotes.add("Big changes are happening, take what you can! :D");
        motivatingQuotes.add("You are awesome!");
        motivatingQuotes.add("You will be LEGEN-wait-for-it-DARY!");
        return motivatingQuotes.get(randomNr);

    }
    private String suggestToRestWell() {
        Integer randomNr = random.nextInt(4)+1;
        List<String> restWellSuggestions = new ArrayList<>(5);
        restWellSuggestions.add("Get enough sleep and eat well :)");
        restWellSuggestions.add("Remember to get enough rest! :)");
        restWellSuggestions.add("Being well fed is almost as good as being well rested");
        restWellSuggestions.add("Remember to keep yourself hydrated!");
        restWellSuggestions.add("Remember to work out and eat well :)");
        return restWellSuggestions.get(randomNr);
    }
    private String suggestTakingTimeOffDuringTheWeekend() {
        Integer randomNr = random.nextInt(4)+1;
        List<String> takeTimeOffSuggestions = new ArrayList<>(5);
        takeTimeOffSuggestions.add("Take some time off the weekend, you deserve it! :)");
        takeTimeOffSuggestions.add("You've had a rough week, remember to take some time off this weekend");
        takeTimeOffSuggestions.add("You've had bad sleep this week, remember to take some time off! :)");
        takeTimeOffSuggestions.add("When you get a chance, take some time off :)");
        takeTimeOffSuggestions.add("Try to get rest as soon as you can :)");
        return takeTimeOffSuggestions.get(randomNr);
    }



    private List<String> readNewsData(Integer from, Integer to) {
        List<String> toBeReturned = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.news)));
            String str;
            while ((str = in.readLine()) != null)
                toBeReturned.add(str);
            in.close();
        } catch (FileNotFoundException e) {
            System.out.println("File is not found!");
        } catch (IOException e) {
            System.out.println("IOExpection");
        }
        return toBeReturned.subList(from, to);
    }

    private void setupImage() {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... voids) {
                Bitmap result = Bitmap.createBitmap(mImage);
                ImageUtils.convertImageToCupImage(result, mContrast);
                return result;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mCupImage.setImageBitmap(bitmap);
                hideProgress();
            }
        }.execute();
    }

    public Bitmap textAsBitmap(String text, float textSize) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = 176; // round
        int height = 264;
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public void getData(View view) {
        String printThis = readMockData();
        mCupIdText.setText(printThis);

    }

    public void crop(View view) {
        showProgress();
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        mImage = ImageUtils.cropImage(image, new Point(100, 0));
        image.recycle();
        setupImage();
    }

    public void reset(View view) {
        showProgress();
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.test_image);
        mImage = ImageUtils.scaleBitmapToCupSize(image);
        mContrast = ImageProperties.DEFAULT_CONTRACT;
        mContrastSeekBar.setProgress(100);
        setupImage();
        image.recycle();
    }

    public void send(View view) {
        showProgress();
        mMukiCupApi.sendImage(mImage, new ImageProperties(mContrast), mCupId);
    }

    public void clear(View view) {
        showProgress();
        mMukiCupApi.clearImage(mCupId);
    }

    public void request(View view) {
        String serialNumber = mSerialNumberEdit.getText().toString();
        showProgress();
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    String serialNumber = strings[0];
                    return MukiCupApi.cupIdentifierFromSerialNumber(serialNumber);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                mCupId = s;
                mCupIdText.setText(mCupId);
                hideProgress();
            }
        }.execute(serialNumber);
    }

    public void deviceInfo(View view) {
        showProgress();
        mMukiCupApi.getDeviceInfo(mCupId);
    }

    private void showToast(final String text) {
        hideProgress();
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    private void showProgress() {
        mProgressDialog.show();
    }

    private void hideProgress() {
        mProgressDialog.dismiss();
    }
}
