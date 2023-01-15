// JsonQuery.java
// Does the Json query in order to know the currency rates
// and return those rates


package com.siraapps.raul.tipandsplitreva;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JsonQuery {

    private Double selectedRate;
    private JSONObject jsonObject;
    //private String latestDate;

    private Ready mReady; // Interface

    public Double getSelectedRate(String currency) throws JSONException {  // getter for selectedRate
        if (jsonObject != null){
            selectedRate = jsonObject.getJSONObject(currency).getDouble("rate");
        }
        return selectedRate;
    }


    public JsonQuery() {  // Constructor

        String currencyRatesAddress = "https://www.floatrates.com/daily/usd.json"; // usd based query
        URL url = createUrl(currencyRatesAddress);  // url created

        String dolarTodayAddress = "https://s3.amazonaws.com/dolartoday/data.json";
        URL urlDT = createUrl(dolarTodayAddress);

        // initiate the process to get rates via json coded message
        if (url != null ) {
            GetCurrencyRates getRates = new GetCurrencyRates();
            getRates.execute(url);
        }

         if (urlDT != null ) {
            GetCurrencyRates getDolarTodayRate = new GetCurrencyRates();
            getDolarTodayRate.execute(urlDT);
         }

    }

    public void requestUpdatedData() {

        String currencyRatesAddress = "https://www.floatrates.com/daily/usd.json"; // usd based query
        URL url = createUrl(currencyRatesAddress);  // url created

        /*
        String dolarTodayAddress = "https://s3.amazonaws.com/dolartoday/data.json";
        URL urlDT = createUrl(dolarTodayAddress);
         */

        // initiate the process to get rates via json coded message
        if (url != null ) {
            GetCurrencyRates getRates = new GetCurrencyRates();
            getRates.execute(url);
        }

        /*
         if (urlDT != null ) {
            GetCurrencyRates getDolarTodayRate = new GetCurrencyRates();
            getDolarTodayRate.execute(urlDT);
         }
        */

    }



    // Assemble the Url based on the web address
    public URL createUrl (String address) {
        try {
            return new URL(address); // return address converted into URL
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null; // Url couldn't be created
    }


    private class GetCurrencyRates
            extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... urls) {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) urls[0].openConnection(); // attempt to connect to first url
                int response = connection.getResponseCode();
                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()))) {

                        String line;

                        while ((line = reader.readLine()) != null){
                            builder.append(line);
                        }
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                assert connection != null;
                connection.disconnect();
            }
            return null;
        }

        // process JSON response and print results
        @Override
        protected void onPostExecute(JSONObject currencies) {
            jsonObject = currencies;
            //Double dolarTodayDouble = null;
            if (jsonObject!=null) {
                // Enable only in case of DolarToday
                /*
                try {
                   // dolarTodayDouble = jsonObject.getJSONObject("USD").getDouble("promedio"); // DolarToday
                   dolarTodayDouble = jsonObject.getJSONObject("USD").getDouble("sicad2"); // Bcv
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //
                 */

                if (mReady != null) {
                    //if (dolarTodayDouble == null) { // jsonObject from world rates
                        mReady.goRefresh();
                    //}
                    /*
                    else { // jsonObject from DolarToday
                        mReady.goGetDolarToday(dolarTodayDouble);
                    }
                     */
                }
            }

        }
    }

   public interface Ready {
        void goRefresh();
        void goGetDolarToday(Double dtDouble);
   }

    // Assign the listener implementing events interface that will receive the events
    public void setCustomObjectListener(Ready listener) {
        mReady = listener;
    }


}
