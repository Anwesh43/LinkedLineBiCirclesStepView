package com.anwesh.uiprojects.linkedlinebicirclesstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.linebicirclesstepview.LineBiCirclesStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LineBiCirclesStepView.create(this)
    }
}
