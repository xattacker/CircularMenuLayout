package com.xattacker.android.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.xattacker.android.circularmenulayout.R
import com.xattacker.android.view.circular.CircularMenuLayout
import com.xattacker.android.view.circular.CircularMenuListener
import com.xattacker.android.view.circular.CircularMenuMode

class MainActivity : AppCompatActivity(), CircularMenuListener
{
    private var circularLayout: CircularMenuLayout? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        circularLayout = findViewById(R.id.view_circle)
        circularLayout?.listener = this
    }

    override fun onCircularMenuClicked(aMenuView: View)
    {
        android.util.Log.d("aaa", aMenuView.toString())
    }

    fun onModeClick(aView: View)
    {
        circularLayout?.mode = CircularMenuMode.AUTO
    }

    fun onMode2Click(aView: View)
    {
        circularLayout?.mode = CircularMenuMode.MANUAL
    }
}
