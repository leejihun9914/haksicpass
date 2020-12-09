package com.ljh.hasicpass_android

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class ViewPagerAdapter(private val context: Context) : PagerAdapter() {
    private var layoutInflator : LayoutInflater? = null

    private val Data= arrayOf(
        R.drawable.bannerimg1,
        R.drawable.bannerimg2
    )

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return Data.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = layoutInflator!!.inflate(R.layout.auto_viewpager, null)
        val data = v.findViewById<View>(R.id.image_container) as ImageView

        data.setImageResource(Data[position])
        val vp = container as ViewPager
        vp.addView(v, 0)
        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val vp = container as ViewPager
        val v = `object` as View
        vp.removeView(v)
    }
}