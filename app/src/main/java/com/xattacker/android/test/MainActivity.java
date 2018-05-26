package com.xattacker.android.test;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xattacker.android.view.circular.CircularMenuLayout;
import com.xattacker.android.view.circular.CircularMenuListener;
import com.xattacker.android.view.circular.CircularMenuMode;

public class MainActivity extends AppCompatActivity implements CircularMenuListener
{
    private CircularMenuLayout _circularLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _circularLayout = findViewById(R.id.view_circle);
        _circularLayout.setListener(this);
    }

    @Override
    public void onCircularMenuClicked(View aMenuView)
    {
        android.util.Log.d("aaa", aMenuView.toString());
    }

    public void onModeClick(View aView)
    {
        if (_circularLayout != null)
        {
            _circularLayout.setMode(CircularMenuMode.AUTO);
        }
    }

    public void onMode2Click(View aView)
    {
        if (_circularLayout != null)
        {
            _circularLayout.setMode(CircularMenuMode.MANUAL);
        }
    }
}
