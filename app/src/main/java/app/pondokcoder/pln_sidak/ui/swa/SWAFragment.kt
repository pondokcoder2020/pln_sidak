package app.pondokcoder.pln_sidak.ui.swa

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_swa.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET

class SWAFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity
    private lateinit var back_page: LinearLayout

    private lateinit var load_wk: RecyclerView

    private lateinit var call_me: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_swa, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity
        back_page = root.findViewById(R.id.back_page)
        back_page.setOnClickListener {
            parentFragment.runOnUiThread {
                val targetFragment: Fragment
                targetFragment = HomeFragment()
                parentFragment.fragments_history[0] = HomeFragment()
                parentFragment.fragments[0] = targetFragment
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(0, true)
            }
        }

        load_wk = root.findViewById(R.id.load_swa)

        refreshWorkingPermit("", load_wk);

        return root
    }










    open fun refreshWorkingPermit(type: String, loader: RecyclerView){
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(
            ApiInterface::class.java)
        val wk_data: Call<ResponseData> = retInc.getData()
        wk_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()

                if(response.code() == 200) {
                    if(type.equals("grid")) {
                        val WKAdapters = WKAdapter(dataset?.getResponseData())
                        loader.apply {
                            layoutManager = GridLayoutManager(parentFragment, 3)
                            adapter = WKAdapters
                        }
                    } else {
                        val WKAdapters = WKAdapter(dataset?.getResponseData())
                        loader.apply {
                            layoutManager = LinearLayoutManager(parentFragment)
                            adapter = WKAdapters
                        }
                    }
                } else if(response.code() == 202) {
                    sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                } else {
                    //refreshKategori()
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Log.e("TANAKA", "Failure : " + t.message)
            }

        })
    }

    class ResponseData {
        @SerializedName("token")
        @Expose
        private var response_token: String? = null

        fun getResponseToken(): String? {
            return response_token
        }

        @SerializedName("response_package")
        @Expose
        private var response_data: ArrayList<WK>? = null

        fun getResponseData(): ArrayList<WK>? {
            return response_data
        }
    }


    class WK {

        @SerializedName("uid_working_permit")
        @Expose
        private var response_uid_working_permit: String? = null

        @SerializedName("uid_jsa")
        @Expose
        private var response_uid_jsa: String? = null

        @SerializedName("pengawas_k3")
        @Expose
        private var response_pengawas_k3: String? = null


        @SerializedName("detail_pekerjaan")
        @Expose
        private var response_job_name: String? = null


        fun getResponseWK(): String? {
            return response_uid_working_permit
        }

        fun getResponsePengawas(): String? {
            return response_pengawas_k3
        }

        fun getResponseJOB(): String? {
            return response_job_name
        }

        fun getResponseJSA(): String? {
            return response_uid_jsa
        }
    }

    interface ApiInterface {
        @GET("SWA/work")
        fun getData(): Call<ResponseData>
    }

    class WKHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val wk_job = view.txt_kode
        private val wk_mandor = view.txt_jsa

        fun bindWK(wkk: WK) {
            wk_job.text = wkk.getResponseJOB().toString()
            if(wkk.getResponsePengawas().toString().equals("null")) {
                wk_mandor.text = "<Belum ada Pengawas>"
            } else {
                wk_mandor.text = wkk.getResponsePengawas().toString()
            }
        }
    }



    inner class WKAdapter(private val wks: ArrayList<WK>?) : RecyclerView.Adapter<WKHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): WKHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_swa, viewGroup, false)

            return WKHolder(inflater).listen{
                    pos, type, context ->
                val item = wks?.get(pos)
            }

        }

        override fun getItemCount(): Int = wks?.size!!

        override fun onBindViewHolder(holder: WKHolder, position: Int) {
            wks?.get(position)?.let { holder.bindWK(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: String? = wks?.get(position)?.getResponseWK()
                parentFragment?.runOnUiThread {
                    val targetFragment: Fragment = SWADetailFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("uid", it1.toString()) }
                    targetFragment.arguments = args
                    parentFragment.fragments_history.set(3, SWAFragment())
                    parentFragment.fragments.set(3, targetFragment)
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(3, true)
                }
            }
            return this
        }
    }







    override  fun onBackPressed() : Boolean {
        //Toast.makeText(parentFragment, "Hello", Toast.LENGTH_LONG).show()
        val targetFragment: Fragment = SWAFragment()
        val args = Bundle()
        targetFragment.arguments = args
        parentFragment.fragments_history[3] = SWAFragment()
        parentFragment.fragments[3] = targetFragment
        parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
        parentFragment.mPager!!.setCurrentItem(0, true)
        return true
    }
}