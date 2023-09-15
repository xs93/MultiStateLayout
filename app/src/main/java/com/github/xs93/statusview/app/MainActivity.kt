package com.github.xs93.statusview.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.xs93.statuslayout.MultiStatusLayout
import com.github.xs93.statusview.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private val mHandler = Handler(Looper.getMainLooper())

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContext.setOnClickListener {
            binding.statusLayout.showContent()
        }

        binding.btnLoading.setOnClickListener {
            binding.statusLayout.showLoading()
        }

        binding.btnEmpty.setOnClickListener {
            binding.statusLayout.showEmpty()
        }

        binding.btnError.setOnClickListener {
            binding.statusLayout.showError()
        }


        binding.btnNoNetwork.setOnClickListener {
            binding.statusLayout.showNoNetwork()
        }

        binding.btnOther.setOnClickListener {
            binding.statusLayout.showViewByStatus(20)
        }

        binding.statusLayout.setRetryClickListener {
            binding.statusLayout.showLoading()
        }

        binding.statusLayout.setViewClick(20, R.id.btn_test) {
            binding.statusLayout.showLoading()
        }

        val other = layoutInflater.inflate(R.layout.msl_other, null, false)
        binding.statusLayout.setViewByStatus(20, other)

        binding.statusLayout.setOnViewStatusChangeListener(object :
            MultiStatusLayout.OnViewStatusChangeListener {
            override fun onStatusChange(
                oldViewStatus: Int,
                oldView: View?,
                newViewStatus: Int,
                newView: View?
            ) {
                Log.d(
                    "MultiStatusLayout onStatusChange",
                    "${oldViewStatus},${oldView},${newViewStatus},${newView}"
                )
            }
        })
    }

    override fun onStart() {
        super.onStart()
        binding.statusLayout.showViewByStatus(20)
    }
}
