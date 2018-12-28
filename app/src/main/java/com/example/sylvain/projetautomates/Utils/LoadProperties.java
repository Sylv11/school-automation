package com.example.sylvain.projetautomates.Utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// Load properties file class
public class LoadProperties
{
    public static String getProperty(String key,Context context) throws IOException {
        Properties properties = new Properties();
        AssetManager assetManager = context.getAssets();
        // Load the config.properties file in the assets directory
        InputStream inputStream = assetManager.open("config.properties");
        properties.load(inputStream);
        return properties.getProperty(key);
    }
}
