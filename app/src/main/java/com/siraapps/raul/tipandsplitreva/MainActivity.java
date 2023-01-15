package com.siraapps.raul.tipandsplitreva;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String SAVED_CURRENCY = "SAVED CURRENCY";
    private final String SAVED_FLAG = "SAVED FLAG";

    Integer imgidSelectedCurrency;
    String textSelectedCurrency = "usd";

    double numero = 0.00;

    private String billAmountString = "0";

    private static final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    private static DecimalFormat formato;

    double tipFromTheBar = 0.15;
    private double billAmount = 0.0;
    private int numOfPeople = 1;
    private TextView tipAmountTextView;
    private TextView totalTextView;
    private TextView perPersonTextView;
    private TextView tipSeekerTextView;
    private TextView splitSeekerTextView;
    private TextView currencyLabelTip, currencyLabelTotal, currencyLabelPerPerson;
    private EditText billEditText;

    private ImageView currencyLogo;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         progressDialog = new ProgressDialog(MainActivity.this, R.style.AppCompatAlertDialogStyle);
         progressDialog.setMessage(getResources().getString(R.string.progress_dialog_loading));
         progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         progressDialog.setIndeterminate(true);

        imgidSelectedCurrency = R.drawable.usa;

        currencyLabelTip = findViewById(R.id.currencyLabelTip);
        currencyLabelTotal = findViewById(R.id.currencyLabelTotal);
        currencyLabelPerPerson = findViewById(R.id.currencyLabelPerPerson);

        restoreSelectedCurrency();

        formato = new DecimalFormat("#,###,###.##");

        billEditText = findViewById(R.id.billEditText);
        billEditText.addTextChangedListener(billAmountEditTextWatcher);

        splitSeekerTextView = findViewById(R.id.splitOutputTextView);
        tipSeekerTextView = findViewById(R.id.tipOutputTextView);

        tipAmountTextView = findViewById(R.id.tipAmountTextView);
        perPersonTextView = findViewById(R.id.perPersonTextView);

        totalTextView = findViewById(R.id.totalTextView);

        currencyLogo = findViewById(R.id.currencyLogo);
        currencyLogo.setImageResource(imgidSelectedCurrency);
        currencyLogo.setOnClickListener(v -> {
            progressDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(this::jumpToList, 500);
        });

        tipAmountTextView.setText(" 0.0" );
        perPersonTextView.setText(" 0.0" );
        totalTextView.setText(" 0.0" );

        splitSeekerTextView.setText(formato.format(numOfPeople));

        SeekBar tipSeekBar =
                findViewById(R.id.tipSeekBar);
        tipSeekBar.setOnSeekBarChangeListener(tipSeekBarListener);

        SeekBar splitSeekBar =
                findViewById(R.id.splitSeekBar);
        splitSeekBar.setOnSeekBarChangeListener(splitSeekBarListener);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8 or above actions
            startForeService();
        } else {
            startService(); // Start service, if not already started
        }

        // Initially hide soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }


    private void jumpToList() {
        Intent i = new Intent(this, CurrencyList.class);
        i.putExtra("currencyOld", textSelectedCurrency);
        startActivityForResult(i, 1);
    }

    private void jumpToCalculator() {
        String billAmount = "0";
        if (billEditText.getText().toString().length() > 0){
            billAmount = billEditText.getText().toString();
        }
        String newBillAmount = transformInsertNumber(billAmount);
        Intent i = new Intent(this, MainActivityCal.class);
        i.putExtra("currentBillAmount", newBillAmount);
        startActivityForResult(i, 2);
    }

    private String transformInsertNumber(String numberToFormat) {
        Double doubleNumber;
        String formattedNumber;

        // remove commas from display number
        doubleNumber = Double.valueOf(numberToFormat.replaceAll(",",""));
        DecimalFormat formato = new DecimalFormat("#,###,###.##");
        formattedNumber = formato.format(doubleNumber);
        return formattedNumber;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                showInfoPopup();
                break;
            case R.id.action_delete:
                billEditText.setText("");
                break;
            default:
                return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_info) {
            showInfoPopup();
        } else if (id == R.id.nav_currencies) {
            progressDialog.show();
            Handler handler = new Handler();
            handler.postDelayed(this::jumpToList, 500);

        } else if (id == R.id.nav_calculator) {
            jumpToCalculator();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void calculate (Double currencyFactor, Double bill){

        billAmount = bill * currencyFactor;

        double tip = (billAmount * tipFromTheBar) ;
        double total = (billAmount  + tip) ;
        double perPerson = (total / numOfPeople) ;

        String formatoTip = formato.format(tip);
        String formatoTotal = formato.format(total);
        String formatoPerPerson = formato.format(perPerson);


        if (formatoTip.contains("."))
            tipAmountTextView.setText(" " + formatoTip);
        else
            tipAmountTextView.setText(" " + formatoTip + ".0");

        if (formatoTotal.contains("."))
            totalTextView.setText(" " + formatoTotal);
        else
            totalTextView.setText(" " + formatoTotal + ".0");

        if (formatoPerPerson.contains("."))
            perPersonTextView.setText(" " + formatoPerPerson);
        else
            perPersonTextView.setText(" " + formatoPerPerson + ".0");

    }

    private final SeekBar.OnSeekBarChangeListener splitSeekBarListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    numOfPeople = progress + 1;
                    splitSeekerTextView.setText(formato.format(numOfPeople));
                    if (billEditText.getText().toString().length() >0) {
                        billAmount = Double.parseDouble(billEditText.getText().toString().replaceAll(",", ""));
                        calculate(1.0, billAmount);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };

    private final SeekBar.OnSeekBarChangeListener tipSeekBarListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    tipFromTheBar = progress / 100.0;
                    tipSeekerTextView.setText(percentFormat.format(tipFromTheBar));
                    if (billEditText.getText().toString().length() >0) {
                        billAmount = Double.parseDouble(billEditText.getText().toString().replaceAll(",", ""));
                        calculate(1.0, billAmount);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };


    private final TextWatcher billAmountEditTextWatcher =

            new TextWatcher() {

                boolean _ignore = false;
                int newCharPosition;
                int originalLength;
                String originalString;

                @Override
                public void beforeTextChanged (
                        CharSequence s, int start, int count, int after) {

                    if (_ignore){
                        return;
                    }

                    originalString = s.toString();
                    originalLength = s.length();

                    newCharPosition = start;
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {

                    if (_ignore){
                        return;
                    }

                    _ignore = true;

                    if (s.toString().length() > 13) {
                        billEditText.setText(originalString);
                        billEditText.setSelection(billEditText.getText().length());
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.max_length_reached_input),
                                Toast.LENGTH_SHORT).show();
                        _ignore = false;
                        return;
                    }

                    switch (s.toString()){
                        case "" :
                            billEditText.setSelection(0);
                            calculate(1.0, 0.0);
                            _ignore = false;
                            return;
                        case ".":
                            billEditText.setText("0.");
                            billEditText.setSelection(billEditText.getText().length());
                            calculate(1.0, 0.0);
                            _ignore = false;
                            return;
                        case "0.":
                            billEditText.setText("0.");
                            billEditText.setSelection(billEditText.getText().length());
                            calculate(1.0, 0.0);
                            _ignore = false;
                            return;
                        case "0.0":
                            billEditText.setText("0.0");
                            billEditText.setSelection(billEditText.getText().length());
                            calculate(1.0, 0.0);
                            _ignore = false;
                            return;
                        default:
                            break;
                    }

                    String newCharacter = s.toString().replaceAll(",","");

                    int dotCount = 0;

                    for (int i = 0; i < newCharacter.length(); i++){
                        if (newCharacter.charAt(i) == '.'){
                            dotCount++;
                        }
                    }
                    if (dotCount > 1){

                        billEditText.setText(originalString);
                        billEditText.setSelection(billEditText.getText().length());
                        _ignore = false;
                        return;
                    }

                    numero = Double.parseDouble(newCharacter);

                    if (s.toString().charAt(s.length() -1) == '.')
                    {
                        billEditText.setText(s.toString());
                        billEditText.setSelection(billEditText.getText().length());
                        calculate(1.0, numero);
                        _ignore = false;
                        return;
                    }


                    DecimalFormat formato = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formato.applyPattern("#,###,###.##");

                    billEditText.setText(formato.format(numero));
                    billEditText.setSelection(billEditText.getText().length());
                    calculate(1.0, numero);

                    _ignore = false;
                }

            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (progressDialog.isShowing())
            progressDialog.dismiss();

        double factor;

        if (requestCode == 1){
            if (resultCode == RESULT_OK) {

                assert data != null;
                imgidSelectedCurrency = data.getIntExtra("responseLogo", 0);
                currencyLogo.setImageResource(imgidSelectedCurrency);
                textSelectedCurrency = data.getStringExtra("responseCurrency");
                savedSelectedCurrency();

                currencyLabelTip.setText(textSelectedCurrency);
                currencyLabelTotal.setText(textSelectedCurrency);
                currencyLabelPerPerson.setText(textSelectedCurrency);

                factor = data.getDoubleExtra("responseFactor", 1.0);
                if (billEditText.getText().length() > 0) {
                    billAmount = Double.parseDouble(billEditText.getText().toString().replaceAll(",", ""));
                    billAmount = billAmount * factor;

                    Log.i("ETIQUETA", "billAmount result is: " + billAmount);
                     if (billAmount > 9.999999999E9) { // if exceeds 13 digit number
                        billAmount = 9.999999999E9;
                        Toast.makeText(this, getResources().getString(R.string.result_exceeds_max_13),
                                Toast.LENGTH_SHORT).show();
                    }


                    DecimalFormat formato = (DecimalFormat) NumberFormat.getInstance(Locale.US);
                    formato.applyPattern("#,###,###.##");
                    String formatedBillAmount = formato.format(billAmount);
                    if (formatedBillAmount.length()<=13) {
                        billEditText.setText(formatedBillAmount);
                    } else {
                        formato.setMaximumFractionDigits(0);
                        formatedBillAmount = formato.format(billAmount);
                        billEditText.setText(formatedBillAmount);
                    }
                }
                // else {
                //   billAmount = 0.0;
                // }

            }
        }
        else if (requestCode == 2){
            if (resultCode == RESULT_OK){
                assert data != null;
                billAmountString = data.getStringExtra("responseCalculator");
                assert billAmountString != null;
                if (billAmountString.length() > 0)
                    if (!billAmountString.equals("0"))
                billEditText.setText(billAmountString);
            }
        }
    }


    private void restoreSelectedCurrency(){
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String savedCurrency = sharedPreferences.getString(SAVED_CURRENCY, null);
        if (savedCurrency != null) {
            textSelectedCurrency = savedCurrency;
            currencyLabelTip.setText(textSelectedCurrency);
            currencyLabelTotal.setText(textSelectedCurrency);
            currencyLabelPerPerson.setText(textSelectedCurrency);
        }
        int savedLogo = sharedPreferences.getInt(SAVED_FLAG, 0);
        if (savedLogo != 0){
            imgidSelectedCurrency = savedLogo;
        }
    }


    private void savedSelectedCurrency(){
        // Save to SharedPrefs
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putString(SAVED_CURRENCY, textSelectedCurrency);
        editor.putInt(SAVED_FLAG, imgidSelectedCurrency);
        editor.apply();

    }

    private void showInfoPopup (){
        LayoutInflater inflater = (LayoutInflater)MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.layout_info_popup, null);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screnHeight = getResources().getDisplayMetrics().heightPixels;

        final PopupWindow pw;

        pw = new PopupWindow(layout, (int)(screenWidth*0.7), (int)(screnHeight*0.5), true);

        pw.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        pw.setTouchInterceptor((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
                pw.dismiss();
                return true;
            }
            return false;
        });
        pw.setOutsideTouchable(true);
        pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
    }


    // Service Handling  ************************************************************

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startService() {
        if (!isMyServiceRunning(MyService.class)) { // if MyService is not running already
            Toast.makeText(this, getResources().getString(R.string.service_starting), Toast.LENGTH_SHORT).show();
            startService(new Intent(this, MyService.class));
        } else {
            Log.i("ETIQUETA", "Service was already running");
        }
    }

    public void startForeService(){
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Exchange Rates Update Active");

        ContextCompat.startForegroundService(this, serviceIntent);

        Log.i("ETIQUETA", "Foreground Service Starting");
    }







}
