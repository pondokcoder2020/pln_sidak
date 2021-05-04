package app.pondokcoder.pln_sidak.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.register.RegisterFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.fragment_login.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

class LoginFragment : Fragment(){

    private val configuration: Config = Config()
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager: SessionManager

    private lateinit var progressBar : ProgressBar

    private val client = OkHttpClient()
    private lateinit var root: View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_login, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity








        return root
    }

    fun disableAllAct(){
        /*btn_login.isEnabled = false
        txt_username.isEnabled = false
        txt_password.isEnabled = false*/
    }


    fun enableAllAct(){
        /*btn_login.isEnabled = true
        txt_username.isEnabled = true
        txt_password.isEnabled = true*/
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }
}
