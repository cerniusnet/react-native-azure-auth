package com.auth.react;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CustomTabsWrapper {
    private Context context;
    private String packageName;
    private CustomTabsClient client;
    BlockingQueue<Boolean> lock;

    public CustomTabsWrapper(Context context, String packageName) {
        this.context = context;
        this.packageName = packageName;
    }

    public void openUrl(String url) {
        launchUrl(url, null);
    }

    public void openUrl(String url, Runnable onLoad) {
        bindCustomTabsService();

        CustomTabsSession customTabsSession = client.newSession(
                onLoadCallbackFromRunnable(onLoad)
        );

        launchUrl(url, customTabsSession);
    }

    private void launchUrl(String url, CustomTabsSession session) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(session);
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    private CustomTabsCallback onLoadCallbackFromRunnable(final Runnable runnable) {
        return new CustomTabsCallback() {
            @Override
            public void onNavigationEvent(int navigationEvent, Bundle extras) {
                if (navigationEvent == CustomTabsCallback.NAVIGATION_FINISHED) {
                    runnable.run();
                }
            }
        };
    }

    private void bindCustomTabsService() {
        CustomTabsClient.bindCustomTabsService(
                context,
                packageName,
                new CustomTabsServiceConnection() {
                    @Override
                    public void onCustomTabsServiceConnected(
                            ComponentName componentName,
                            CustomTabsClient customTabsClient
                    ) {
                        client = customTabsClient;
                        if (lock != null) {
                            lock.add(true);
                        }
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        client = null;
                    }
                }
        );
        int i = 0;
        while(client == null && i++ < 200){
            try{
                Thread.sleep(25);
            }catch(InterruptedException e){
                
            }
        }
        if(i == 200){
            throw new RuntimeException("it didnt rise");
        }
    }

    private void waitUntilClientIsSet() {
        if (client == null) {
            try {
                lock = new ArrayBlockingQueue<>(1);
                lock.poll(5, TimeUnit.SECONDS);
                lock = null;
            } catch (InterruptedException e) {
                waitUntilClientIsSet();
            }
        }
    }
}
