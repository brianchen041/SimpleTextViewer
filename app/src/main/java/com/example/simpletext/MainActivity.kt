package com.example.simpletext

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.recycleView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val GET_TEXT = 100
    }

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        initView()
        checkIntent()
    }

    private fun initView() {
        val adapter = MyAdapter()
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
        mainViewModel.title.observe(
            this,
            Observer {
                supportActionBar?.title = it
            }
        )
        mainViewModel.textList.observe(
            this,
            Observer {
                adapter.setItems(it)
                recycleView.scrollToPosition(0)
            }
        )
    }

    private fun checkIntent() {
        if ("text/plain" == intent.type) {
            intent.data?.let {
                mainViewModel.handleTextOpen(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_open) {
            mainViewModel.openTextFile(
                this,
                GET_TEXT
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GET_TEXT) {
            val uri = data?.data ?: return
            mainViewModel.handleTextOpen(uri)
        }
    }
}
