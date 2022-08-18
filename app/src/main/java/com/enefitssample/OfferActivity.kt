package com.enefitssample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.enefitssample.databinding.ActivityEnefitsBinding
import com.enefitssample.databinding.ActivityOffersBinding

class OfferActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOffersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOffersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        val offers  = intent.getStringExtra("offers")
        if (offers != null){
            binding.tvOffers.text = offers
        }
    }
}
