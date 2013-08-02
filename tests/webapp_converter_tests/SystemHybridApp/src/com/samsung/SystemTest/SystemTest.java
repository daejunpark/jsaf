package com.samsung.SystemTest;


import android.samsung.wrtwidgets.activity.BaseActivity;
import android.os.Bundle;

public class SystemTest extends BaseActivity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		super.launchWidget();
        
    }
}
