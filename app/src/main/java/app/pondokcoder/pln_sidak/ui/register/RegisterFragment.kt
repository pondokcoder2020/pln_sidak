package app.pondokcoder.pln_sidak.ui.register

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.login.LoginFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST


class RegisterFragment : Fragment() {

    private val configuration : Config = Config()
    private lateinit var root: View
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager:SessionManager
    private lateinit var phoneNumber : String


    private lateinit var txt_nik : TextInputEditText
    private lateinit var txt_nama : TextInputEditText
    private lateinit var txt_username : TextInputEditText
    private lateinit var txt_password : TextInputEditText
    private lateinit var txt_password_conf : TextInputEditText
    private lateinit var txt_contact : TextInputEditText

    private lateinit var btn_cancel : MaterialButton
    private lateinit var btn_register : MaterialButton
    private lateinit var txt_kecamatan : Spinner
    private lateinit var txt_kelurahan : Spinner

    private var kecamatanID : ArrayList<String> = arrayListOf()
    private var kecamatanName : ArrayList<String> = arrayListOf()

    private var kelurahanID : ArrayList<String> = arrayListOf()
    private var kelurahanName : ArrayList<String> = arrayListOf()

    private lateinit var selectedKecamatan : String
    private lateinit var selectedKelurahan : String

    private val utility : Utility = Utility()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_register, container, false)

        parentFragment = activity as MainActivity
        sessionManager = parentFragment?.applicationContext?.let { SessionManager(it) }!!

        txt_nik = root.findViewById(R.id.txt_nik)
        txt_nama = root.findViewById(R.id.txt_nama)
        txt_username = root.findViewById(R.id.txt_username)
        txt_password = root.findViewById(R.id.txt_password)
        txt_password_conf = root.findViewById(R.id.txt_conf_password)
        txt_contact = root.findViewById(R.id.txt_contact)
        btn_register = root.findViewById(R.id.btn_register)
        txt_kecamatan = root.findViewById(R.id.txt_kecamatan)
        txt_kelurahan = root.findViewById(R.id.txt_kelurahan)
        btn_cancel = root.findViewById(R.id.btn_cancel_register)

        /*val kecamatan_item = ArrayAdapter<String>(parentFragment, android.R.layout.simple_spinner_dropdown_item)
        val supir_items_id = ArrayAdapter<String>(parentFragment, android.R.layout.simple_spinner_dropdown_item)*/


        if (ContextCompat.checkSelfPermission(parentFragment, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(parentFragment, android.Manifest.permission.READ_PHONE_STATE)) {
                var mPhoneNumber : TelephonyManager = parentFragment.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                phoneNumber = mPhoneNumber.line1Number
                txt_contact.setText(phoneNumber)
            } else {
                ActivityCompat.requestPermissions(parentFragment, arrayOf(android.Manifest.permission.READ_PHONE_STATE), 2)
            }
        }

        utility.generate_kecamatan(sessionManager, ::kecamatanCallBack)

        txt_kecamatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedKecamatan = kecamatanID.get(position)
                Log.e("TANAKA", selectedKecamatan)
                utility.generate_kelurahan(sessionManager, kecamatanID.get(position), ::kelurahanCallBack)
            }
        }

        txt_kelurahan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedKelurahan = kelurahanID.get(position)
            }
        }

        btn_register.setOnClickListener{
            var nik = txt_nik.text.toString()
            var nama = txt_nama.text.toString()
            var email = txt_username.text.toString()
            var pass = txt_password.text.toString()
            var conf_pass = txt_password_conf.text.toString()
            var contact = txt_contact.text.toString()

            /*Log.e("TANAKA", nik)
            Log.e("TANAKA", nama)
            Log.e("TANAKA", email)
            Log.e("TANAKA", pass)
            Log.e("TANAKA", conf_pass)
            Log.e("TANAKA", contact)
            Log.e("TANAKA", selectedKecamatan)
            Log.e("TANAKA", selectedKelurahan)*/

            if(
                (!email.equals("") && !email.isEmpty()) &&
                (!pass.equals("") && !pass.isEmpty()) &&
                (pass.equals(conf_pass)) &&
                (!contact.equals("") && !contact.isEmpty())
            ) {
                Log.e("TANAKA", "Logged??")
                btn_register.isEnabled = false
                daftar(nik, nama, email, pass, contact, selectedKelurahan, selectedKecamatan)
            } else {
                if (!email.equals("") && !email.isEmpty()) {
                    //
                }

                if (!pass.equals("") && !pass.isEmpty()) {
                    //
                }

                if (pass.equals(conf_pass)) {
                    //
                }

                if (!contact.equals("") && !contact.isEmpty()) {
                    //
                }
            }
        }


        btn_cancel.setOnClickListener{
            if (parentFragment.targetted != null) {
                var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
                var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
                parentFragment.targetted = LoginFragment()
                transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
                transaction!!.commit()
            }
        }

        return  root
    }

    private fun kecamatanCallBack(dataSet: String?) {
        kecamatanID.clear()
        kecamatanName.clear()
        var kecamatanList : JSONArray
        kecamatanList = JSONArray(dataSet)
        for(a in 0 until kecamatanList.length()) {
            var dataCol : JSONObject = kecamatanList.get(a) as JSONObject
            kecamatanID.add(dataCol.get("id").toString())
            kecamatanName.add(dataCol.get("nama").toString())
        }
        val kecamatan_adapter: ArrayAdapter<String> = ArrayAdapter<String>(parentFragment,android.R.layout.simple_spinner_item, kecamatanName)
        txt_kecamatan.adapter = kecamatan_adapter
    }


    private fun kelurahanCallBack(dataSet: String?) {
        kelurahanID.clear()
        kelurahanName.clear()
        var kelurahanList : JSONArray
        kelurahanList = JSONArray(dataSet)
        for(a in 0 until kelurahanList.length()) {
            var dataCol : JSONObject = kelurahanList.get(a) as JSONObject
            kelurahanID.add(dataCol.get("id").toString())
            kelurahanName.add(dataCol.get("nama").toString())
        }
        val kelurahan_adapter: ArrayAdapter<String> = ArrayAdapter<String>(parentFragment,android.R.layout.simple_spinner_item, kelurahanName)
        txt_kelurahan.adapter = kelurahan_adapter
    }





    private fun daftar(nik : String, nama : String, email : String, password: String, contact:String, desa : String, kecamatan : String){

        val retPost = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(SubmitInterface::class.java)
        retPost.regis("register", nik, nama, email, password, contact, desa, kecamatan, "0").enqueue(object :
            Callback<Hasil> {
            override fun onFailure(call: Call<Hasil>, t: Throwable) {
                /*parentFragment.runOnUiThread {
                    val targetFragment: Fragment = HomeFragment()
                    parentFragment.fragments_history[0] = HomeFragment()
                    parentFragment.fragments[0] = targetFragment
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
                }*/
                Log.e("TANAKA", "Failed")
            }

            override fun onResponse(call: Call<Hasil>, response: Response<Hasil>) {
                var dataset: Hasil? = response.body()
                if(dataset?.getResponseQuery().toString().toInt() == 1) {
                    if (parentFragment.targetted != null) {
                        btn_register.isEnabled = true
                        var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
                        var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
                        parentFragment.targetted = SuccessFragment()
                        transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
                        transaction!!.commit()
                    }
                } else {
                    Log.e("TANAKA", response.body().toString())
                }
            }

        })
    }

    class Hasil {
        @SerializedName("response_query")
        @Expose
        private var response_query: String? = null

        @SerializedName("response")
        @Expose
        private var response: String? = null

        fun getResponseQuery(): String? {
            return response_query
        }

        fun getResponse(): String? {
            return response
        }
    }


    interface SubmitInterface {
        @Headers("Accept: application/json")
        @POST("User")
        @FormUrlEncoded
        fun regis(
            @Field("request") request: String,
            @Field("nik") nik: String,
            @Field("nama") nama: String,
            @Field("email") email: String,
            @Field("password") password: String,
            @Field("no_handphone") contact: String,
            @Field("desa") desa: String,
            @Field("kecamatan") kecamatan: String,
            @Field("google_login") gLog: String
        ): Call<Hasil>
    }
}


