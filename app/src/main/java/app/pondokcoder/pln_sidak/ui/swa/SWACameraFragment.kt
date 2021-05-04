package app.pondokcoder.pln_sidak.ui.swa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.pondokcoder.pln_sidak.Config
import app.pondokcoder.pln_sidak.MainActivity
import app.pondokcoder.pln_sidak.R
import app.pondokcoder.pln_sidak.SessionManager

class SWACameraFragment : Fragment(), MainActivity.IOnBackPressed {

    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_swa_detail, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity
        return root
    }

    override  fun onBackPressed() : Boolean {
        var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
        var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
        parentFragment.targetted = SWAFragment()
        transaction!!.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
        transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
        transaction!!.commit()
        return true
    }
}