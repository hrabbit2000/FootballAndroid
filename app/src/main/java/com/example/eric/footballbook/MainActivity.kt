package com.example.eric.footballbook

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.text.Editable

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

import java.util.TimerTask
import java.util.Timer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

import android.view.View
import java.io.File
import android.widget.ArrayAdapter


class BookAccount(id: String = "0", str: String = "") {
    var user: String = id
    var pwd: String = str
}

class BookInfos {
    var year = 0
    var month = 0
    var day = 0
    var s_time = 0
    var account = BookAccount()
    var my_path = "bookfootball"
    var my_file = "user_info"
    var my_pic = ""
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Example of a call to a native method
        time_text.text = stringFromJNI()

        initResource()

        task = object : TimerTask() {
            override fun run() {
                mainLoop()
            }
        }

        timer.schedule(task, 100, 50);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun initResource() {

        readAccount()

        date_spinner.setSelection(12)
        select_hours.setSelection(1)
        //build user selected spinner
        var account_list: MutableList<String>? = ArrayList<String>()
        user_accounts?.forEachIndexed { index, bookAccount ->
            account_list?.add(bookAccount.user)
        }
        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, account_list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selected_user.adapter = adapter
        selected_user.setSelection(0)
        //
        val c = Calendar.getInstance()
        c.set(Calendar.DATE, c.get(Calendar.DATE) + 2);
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH) + 1
        val day = c.get(Calendar.DAY_OF_MONTH)
        date_text.setText("日期: " + month  + "/" + day + "/" + year)

        //fill init user infos
        book_info.year = year
        book_info.month = month
        book_info.day = day
        book_info.s_time = 19

        network = NetworkStuffs()
    }

    fun getCurrentTimeString(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return current.format(formatter)
    }

    fun mainLoop() {
        time_text.text = getCurrentTimeString()
    }

    fun selectDateClicked(target: View) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            // Display Selected date in textbox
            date_text.setText("日期: " + (monthOfYear + 1)  + "/" + dayOfMonth + "/" + year)
            book_info.year = year
            book_info.month = monthOfYear + 1
            book_info.day = dayOfMonth

            writeAccount()
        }, year, month, day)
        dpd.show()
    }

    fun onAddClicked(target: View) {
        var user = user_id.text
        var pwd = user_pwd.text

        var need_write = true
        var need_add = true
        if (user.isNotEmpty() && pwd.isNotEmpty()) {
            user_accounts?.forEachIndexed { index, bookAccount ->
                if (bookAccount.user == user.toString()) {
                    if (bookAccount.pwd == pwd.toString())
                        need_write = false
                    else {
                        bookAccount.pwd = pwd.toString()
                    }
                    need_add = false
                }
            }
        }

        if (need_add == true) {
            user_accounts?.add(BookAccount(user.toString(), pwd.toString()))
        }

        if (need_write == true) {
            writeAccount()
        }
    }

    fun onTestClicked(target: View) {
        network?.login(user_accounts?.get(0)?.user, user_accounts?.get(0)?.pwd)
    }

    fun  onPrepareClicked(target: View) {
//        network?.visitBookCenter()
    }

    fun readAccount() {
        val file_handler = File(filesDir, account_file)
        if (file_handler.exists()) {
            var one_account = BookAccount()
            var content = file_handler.readText()
            openFileInput(account_file).bufferedReader().readLines().forEachIndexed { index, s ->
                if (index % 2 == 0) {
                    one_account.user = s
                } else {
                    one_account.pwd = s
                    user_accounts?.add(BookAccount(one_account.user, one_account.pwd))
                }
            }
        }

        if (user_accounts?.size == 0) {
            user_accounts?.add(BookAccount("26733", "135792468fb"))
        }
    }

    fun writeAccount() {
        openFileOutput(account_file, Context.MODE_PRIVATE).use {
            user_accounts?.forEachIndexed { index, bookAccount ->
                it.write((bookAccount.user + "\n").toByteArray())
                it.write((bookAccount.pwd + "\n").toByteArray())
            }
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }


    private val timer = Timer()
    private var task: TimerTask? = null
    private var book_info = BookInfos()
    private var account_file = "myaccount"
    private var user_accounts: MutableList<BookAccount>? = ArrayList<BookAccount>()
    private var network: NetworkStuffs? = null
}
