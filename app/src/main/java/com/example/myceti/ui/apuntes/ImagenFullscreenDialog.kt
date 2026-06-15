
package com.example.myceti.ui.apuntes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.myceti.R

class ImagenFullscreenDialog : DialogFragment() {

    companion object {
        fun newInstance(base64: String): ImagenFullscreenDialog {
            val fragment = ImagenFullscreenDialog()
            fragment.arguments = Bundle().apply {
                putString("base64", base64)
            }
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(0xFF000000.toInt())
            setOnClickListener { dismiss() }
        }

        val base64 = arguments?.getString("base64") ?: return imageView
        val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
        Glide.with(this).load(bytes).into(imageView)

        return imageView
    }
}