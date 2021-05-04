package app.pondokcoder.pln_sidak

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import app.pondokcoder.pln_sidak.ui.document.DocFragment
import app.pondokcoder.pln_sidak.ui.history.ScanFragment
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import app.pondokcoder.pln_sidak.ui.login.LauncherFragment
import app.pondokcoder.pln_sidak.ui.profile.ProfileFragment
import app.pondokcoder.pln_sidak.ui.swa.SWAFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*


open class MainActivity : AppCompatActivity() {


    private val selected: MenuItem? = null
    lateinit var sessionManager: SessionManager
    private val config: Config = Config()

    var targetted: Fragment? = null
    var transaction: FragmentTransaction? = null
    var fragmentManager: FragmentManager? = null
    var mPager: ViewPager? = null
    open var currentTarget: Fragment? = null
    open var fragments = ArrayList<Fragment>()
    var fragments_history = ArrayList<Fragment>()
    var items: List<MenuItem> = ArrayList()
    open val NUM_PAGES = 5
    var pagerAdapter: PagerAdapter? = null
    var phoneNumber : String = ""
    var googleToken : String = ""



    lateinit var navView: BottomNavigationView
    private lateinit var navController: NavController


    inner class ScreenSlidePagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm!!) {
        override fun getItem(position: Int): Fragment {
            var b:Int = position
            if(position == fragments.size) {
                b = position - 1
            }
            val currentTarget = fragments.get(b)
            return currentTarget
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }


    fun defineSlider() {
        fragments.clear()
        fragments = ArrayList()
        fragments_history.clear()
        fragments_history = ArrayList()

        runOnUiThread {
            if(sessionManager.jWT.isNullOrEmpty() || sessionManager.jWT.equals("")){
                //fragments.add(LauncherFragment())
            } else {
                fragments.add(HomeFragment())
                fragments.add(DocFragment())
                fragments.add(ScanFragment())
                fragments.add(SWAFragment())
                fragments.add(ProfileFragment())
            }

            fragments_history = fragments
            pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
            mPager!!.adapter = pagerAdapter
        }

        mPager!!.setCurrentItem(0, true)


        mPager!!.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position == 0) {
                    navView.menu.findItem(R.id.navigation_home).isChecked = true
                }
                if (position == 1) {
                    navView.menu.findItem(R.id.navigation_doc).isChecked = true
                }
                if (position == 2) {
                    navView.menu.findItem(R.id.navigation_scan).isChecked = true
                }
                if (position == 3) {
                    navView.menu.findItem(R.id.navigation_swa).isChecked = true
                }
                if (position == 4) {
                    navView.menu.findItem(R.id.navigation_profile).isChecked = true
                }
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    navView.menu.findItem(R.id.navigation_home).isChecked = true
                }
                if (position == 1) {
                    navView.menu.findItem(R.id.navigation_doc).isChecked = true
                }
                if (position == 2) {
                    navView.menu.findItem(R.id.navigation_scan).isChecked = true
                }
                if (position == 3) {
                    navView.menu.findItem(R.id.navigation_swa).isChecked = true
                }
                if (position == 4) {
                    navView.menu.findItem(R.id.navigation_profile).isChecked = true
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                //
            }
        })






        navView.setOnNavigationItemSelectedListener { menuItem ->
            val position:Int = items.indexOf(menuItem)
            defineSlider()
            when(menuItem.itemId){
                R.id.navigation_home -> {
                    fragments[0] = HomeFragment()
                    mPager!!.setCurrentItem(0, true)
                }

                R.id.navigation_doc -> {
                    fragments[1] = DocFragment()
                    mPager!!.setCurrentItem(1, true)
                }

                R.id.navigation_scan -> {
                    fragments[2] = ScanFragment()
                    mPager!!.setCurrentItem(2, true)
                }

                R.id.navigation_swa -> {
                    fragments[3] = SWAFragment()
                    mPager!!.setCurrentItem(3, true)
                }

                R.id.navigation_profile -> {
                    fragments[4] = ProfileFragment()
                    mPager!!.setCurrentItem(4, true)
                }
            }
            if (menuItem !== selected) {
                transaction = supportFragmentManager.beginTransaction()
            }
            true
        }
    }


    private val updates_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, i: Intent) {
            //val message: String = SyncService!!.INFO_UPDATE_FILTER
            Log.e("TANAKA", "message")
        }
    }



    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }
        setContentView(R.layout.activity_main)


        sessionManager = SessionManager(applicationContext)

        val window: Window = window
        window.setFormat(PixelFormat.RGBA_8888)


        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)


        checkSession()



        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
            var mPhoneNumber : TelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            mPhoneNumber = mPhoneNumber.line1Number
            return
        }*/











        if (!checkNetworkPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.INTERNET),
                1
            )
        }

        if (!checkStorageReadPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }

        if (!checkStorageReadPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CALL_PHONE),
                1
            )
        }

        if (!checkStorageWritePermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }


        if (!checkCameraPermission()) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                200
            )
        }

        if (checkPermission()) {
            //
        } else {
            requestPermission();
        }
    }

    fun checkSession() {
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_scan,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        mPager = findViewById(R.id.pager)

        fragmentManager = supportFragmentManager
        transaction = fragmentManager!!.beginTransaction()

        if(sessionManager.jWT.isNullOrEmpty() || sessionManager.jWT.equals("")) {
            navView.visibility = View.INVISIBLE
            mPager!!.visibility = View.INVISIBLE
            targetted = LauncherFragment()
            transaction!!.replace(R.id.nav_host_fragment, targetted!!)
            transaction!!.addToBackStack(targetted.toString())
            transaction!!.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction!!.commit()
        } else {




            mPager!!.visibility = View.VISIBLE
            navView.visibility = View.GONE
            defineSlider()
            Handler().postDelayed(Runnable() {
                runOnUiThread {


                    val menu = navView.menu
                    if (menu != null) {
                        for (i in 0 until menu.size()) {
                            items.plus(menu?.getItem(i))
                        }
                    }


                    navView.visibility = View.VISIBLE
                    mPager!!.setCurrentItem(0, true)
                    targetted = HomeFragment()

                    transaction!!.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                    transaction!!.replace(R.id.nav_host_fragment, targetted!!, "Fragment")
                    transaction!!.addToBackStack(null)
                    transaction!!.commit()
                }
            }, 3000)
        }
    }


















    fun checkNetworkPermission(): Boolean {
        val permission = Manifest.permission.INTERNET
        val res = applicationContext.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    fun checkCameraPermission(): Boolean {
        val permission = Manifest.permission.CAMERA
        val res = applicationContext.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    fun checkStorageReadPermission(): Boolean {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        val res = applicationContext.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }

    fun checkStorageWritePermission(): Boolean {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        val res = applicationContext.checkCallingOrSelfPermission(permission)
        return res == PackageManager.PERMISSION_GRANTED
    }


    open fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    open fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA),
            200
        )
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        showMessageOKCancel("Allow Camera ?",
                            DialogInterface.OnClickListener { dialog, which ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    requestPermission()
                                }
                            })
                    } else {
                        Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                // main logic
            } else {
                Log.e("TANAKA", "Permission Denied")
            }
        }
    }

    open fun showMessageOKCancel(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val window: Window = window
        window.setFormat(PixelFormat.RGBA_8888)
    }


















    interface IOnBackPressed {
        fun onBackPressed(): Boolean
    }

    override fun onBackPressed() {
        val keyMonitor = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (keyMonitor !is IOnBackPressed || !(keyMonitor as? IOnBackPressed)?.onBackPressed()!!) {
            super.onBackPressed()
        }
    }

}
