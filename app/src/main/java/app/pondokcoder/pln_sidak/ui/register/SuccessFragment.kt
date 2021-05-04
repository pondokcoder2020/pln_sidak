package app.pondokcoder.pln_sidak.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.pondokcoder.pln_sidak.Config
import app.pondokcoder.pln_sidak.MainActivity
import app.pondokcoder.pln_sidak.R
import app.pondokcoder.pln_sidak.SessionManager
import app.pondokcoder.pln_sidak.ui.login.LoginFragment
import com.google.android.material.button.MaterialButton

class SuccessFragment : Fragment() {

    private val configuration : Config = Config()
    private lateinit var root: View
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager: SessionManager

    private lateinit var message_confirm: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_register_success, container, false)

        parentFragment = activity as MainActivity
        sessionManager = parentFragment?.applicationContext?.let { SessionManager(it) }!!
        message_confirm = root.findViewById(R.id.btn_back_login)
        message_confirm.setOnClickListener {
            if (parentFragment.targetted != null) {
                var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
                var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
                parentFragment.targetted = LoginFragment()
                transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
                transaction!!.commit()
            }
        }
        return root
    }
}