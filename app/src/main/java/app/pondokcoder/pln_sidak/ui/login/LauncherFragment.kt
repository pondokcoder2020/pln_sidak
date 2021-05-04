package app.pondokcoder.pln_sidak.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.pondokcoder.pln_sidak.MainActivity
import app.pondokcoder.pln_sidak.R
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import app.pondokcoder.pln_sidak.ui.home.LaporanNew
import com.google.android.material.card.MaterialCardView

class LauncherFragment : Fragment(){
    private lateinit var root: View
    private lateinit var parentFragment: MainActivity
    private lateinit var portal_k3 : MaterialCardView
    private lateinit var portal_lingkungan : MaterialCardView
    private lateinit var portal_keamanan : MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_login, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        parentFragment = activity as MainActivity

        portal_k3 = root.findViewById(R.id.portal_k3)
        portal_lingkungan = root.findViewById(R.id.portal_lingkungan)
        portal_keamanan = root.findViewById(R.id.portal_keamanan)

        portal_k3.setOnClickListener {
            parentFragment?.runOnUiThread {
                var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
                var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
                parentFragment.targetted = LoginK3Fragment()
                transaction!!.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
                transaction!!.commit()
            }
        }

        portal_lingkungan.setOnClickListener {
            parentFragment?.runOnUiThread {
                var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
                var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
                parentFragment.targetted = LoginLingkunganFragment()
                transaction!!.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
                transaction!!.commit()
            }
        }

        portal_keamanan.setOnClickListener {
            parentFragment?.runOnUiThread {
                var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
                var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
                parentFragment.targetted = LoginKeamananFragment()
                transaction!!.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
                transaction!!.commit()
            }
        }

        return root
    }
}