package com.truemi.loftview

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var loftView = findViewById<LoftView>(R.id.loft)
        var listView = ListView(this)

        loftView.addRefreshView(listView)
        listView.adapter = MyAdapter(this)
        loftView.addHeadView(this, R.layout.home_item_top)
        loftView.buildView()
    }


    inner class MyAdapter(internal var context: Context) : BaseAdapter() {

        override fun getCount(): Int {
            return 30
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            val holder: ViewHolder
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.home_item, null)
                holder = ViewHolder(convertView!!)
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder

            }
            holder.textView.text = position.toString() + ""
            return convertView
        }

        internal inner class ViewHolder(var view: View) {
            var textView: TextView

            init {
                textView = view.findViewById(R.id.textView)
            }
        }
    }
}
