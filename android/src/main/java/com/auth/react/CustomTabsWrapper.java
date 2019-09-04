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

import java.util.List;
import java.util.Arrays;

public class CustomTabsWrapper {
    private Context context;
    private String packageName;

    private static List<String> packages = Arrays.asList("com.android.chrome", "com.chrome.beta", "com.chrome.dev");

    public CustomTabsWrapper(Context context) {
        this.context = context;
        this.packageName = CustomTabsClient.getPackageName(context, packages);
    }

    public void openUrl(String url) {
        launchUrl(url, null);
    }

    public void openUrl(final String url, final Runnable onLoad) {
        CustomTabsClient.bindCustomTabsService(
                context,
                packageName,
                new CustomTabsServiceConnection() {
                    @Override
                    public void onCustomTabsServiceConnected(
                            ComponentName componentName,
                            CustomTabsClient customTabsClient
                    ) {
                        CustomTabsSession customTabsSession = customTabsClient.newSession(
                                onLoadCallbackFromRunnable(onLoad)
                        );

                        launchUrl(url, customTabsSession);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                    }
                }
        );
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
}
