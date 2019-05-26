package com.example.coffepitstop;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;

public class Util {

    static void storeSharedPreferences(String key, String value, Context applicationContext){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static String getSharedPreferences(String key, Context applicationContext){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return sharedPreferences.getString(key,null);

    }

    static void deleteSharedPreferences(String key, Context applicationContext){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        sharedPreferences.edit().remove(key).apply();
    }


    static boolean subscribeTopic(String topicName, AmazonSNSClient snsClient, Context applicationContext){
        String topicArnPrefix = "arn:aws:sns:us-east-1:341434091225:";

        try {

            //Tries to subscribe to the topic with the given name
            //If the topic does not exists the NotFoundException is thrown

            final SubscribeRequest subscribeRequest = new SubscribeRequest(topicArnPrefix + topicName, "application", Util.getSharedPreferences("endpointArn",applicationContext));

            SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
            Log.d("SubARN",subscribeResult.getSubscriptionArn());
            Util.storeSharedPreferences("subscriptionArn",subscribeResult.getSubscriptionArn(),applicationContext);
            Log.d("SUBSCRIBE RESULT", "Subscribe done");
            return true;


        } catch (com.amazonaws.services.sns.model.NotFoundException e){

            //In this case, the topic with the initial name is created
            //createTopic(topicName);
            Log.d("SUBSCRIBE RESULT", "CreateTopic done");

            return false;
        }
    }
}
