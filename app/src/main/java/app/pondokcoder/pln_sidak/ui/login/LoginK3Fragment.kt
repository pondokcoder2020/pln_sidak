package app.pondokcoder.pln_sidak.ui.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.pondokcoder.pln_sidak.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

class LoginK3Fragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager: SessionManager
    private val configuration : Config = Config()

    private lateinit var username : TextInputEditText
    private lateinit var password : TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_login_k3, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity

        val btn_login = root.findViewById<Button>(R.id.btn_login)
        var txt_email: TextInputEditText? = root.findViewById(R.id.txt_username)
        var txt_password: TextInputEditText? = root.findViewById(R.id.txt_password)
        btn_login.setOnClickListener {

            //parentFragment.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            login(txt_email!!.text.toString(),txt_password!!.text.toString(), "login")

        }

        return root
    }


    interface ApiInterface {
        @Headers("Accept: application/json")
        @POST("Pegawai")
        @FormUrlEncoded
        fun signin(
            @Field("request") request: String,
            @Field("nip") email: String,
            @Field("password") password: String
        ): Call<Login>
    }


    class UserData {
        @SerializedName("uid")
        @Expose
        private val uid: String? = null

        @SerializedName("nip")
        @Expose
        private val nip: String? = null

        @SerializedName("nama")
        @Expose
        private val nama: String? = null

        @SerializedName("foto")
        @Expose
        private val foto: String? = null

        @SerializedName("uid_unit")
        @Expose
        private val uid_unit: String? = null

        @SerializedName("unit_name")
        @Expose
        private val unit_name: String? = null


        fun getUID(): String? {
            return uid
        }

        fun getNIP(): String? {
            return nip
        }


        fun getNama(): String? {
            return nama
        }

        fun getUnit(): String? {
            return uid_unit
        }

        fun getFoto(): String? {
            return foto
        }

        fun getUnitName(): String? {
            return unit_name
        }
    }





    private fun login(email: String, password:String, signType : String) {
        var retIn: ApiInterface = RetroInstance.getRetrofitInstance(configuration.server).create(ApiInterface::class.java)
        retIn.signin(signType, email, password).enqueue(object : Callback<Login> {
            override fun onFailure(call: Call<Login>, t: Throwable) {
                Toast.makeText(parentFragment, "Failed " + configuration.server, Toast.LENGTH_LONG).show()
                call.cancel()
            }
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                //Toast.makeText(parentFragment, "Kode : " + response.code().toString(), Toast.LENGTH_LONG).show()
                if (response.code() == 200) {
                    //parentFragment.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    var dataset: Login? = response.body()
                    if(dataset?.getResponseCode() == 200) {
                        sessionManager.saveSPString("__TOKEN__", dataset.getJWT())
                        sessionManager.saveSPString("__LOGIN_TYPE__", signType)
                        sessionManager.saveSPString("__UID__",dataset.getResponseData()?.getUID())
                        sessionManager.saveSPString("__NAME__",dataset.getResponseData()?.getNama())
                        sessionManager.saveSPString("__NIP__",dataset.getResponseData()?.getNIP())
                        sessionManager.saveSPString("__UNIT__",dataset.getResponseData()?.getUnit())
                        sessionManager.saveSPString("__UNIT_NAME__",dataset.getResponseData()?.getUnitName())
                        sessionManager.saveSPString("__FOTO__",dataset.getResponseData()?.getFoto())

                        if(!sessionManager.jWT.equals("") || sessionManager.jWT?.isEmpty()!!) {
                            parentFragment.checkSession()
                            parentFragment.defineSlider()
                            parentFragment.runOnUiThread {

                                //parentFragment.navView!!.visibility = VISIBLE
                            }
                        } else {
                            Toast.makeText(parentFragment, "Failed to create session", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(activity?.applicationContext,"Email / password salah", Toast.LENGTH_LONG).show()
                    }
                } else if(response.code() == 400) {
                    Log.e("TANAKA", "Bad Request. " + response.code())
                } else {
                    Log.e("TANAKA", "Bad Request. " + response.code())
                }
                call.cancel()
            }
        })
    }


    override  fun onBackPressed() : Boolean {
        var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
        var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
        parentFragment.targetted = LauncherFragment()
        transaction!!.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
        transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
        transaction!!.commit()
        return false
    }

    class Login {
        @SerializedName("response_jwt")
        @Expose
        private var response_jwt: String? = null

        @SerializedName("response")
        @Expose
        private var response: String? = null

        @SerializedName("response_code")
        @Expose
        private var response_code: Int? = 0

        @SerializedName("response_data")
        @Expose
        private var response_data: UserData? = null

        fun getJWT(): String? {
            return response_jwt
        }

        fun getResponse(): String? {
            return response
        }

        fun getResponseCode(): Int? {
            return response_code
        }

        fun getResponseData(): UserData? {
            return response_data
        }
    }
}