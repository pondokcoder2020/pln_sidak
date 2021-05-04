package app.pondokcoder.pln_sidak.ui.document

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import app.pondokcoder.pln_sidak.ui.home.ListItemSubKatFragment
import app.pondokcoder.pln_sidak.ui.login.LauncherFragment
import app.pondokcoder.pln_sidak.ui.swa.SWADetailFragment
import app.pondokcoder.pln_sidak.ui.swa.SWAFragment
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_doc_type.view.*
import kotlinx.android.synthetic.main.single_swa.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class DocFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity

    private lateinit var load_doc: RecyclerView
    private lateinit var back_page: LinearLayout

    private lateinit var call_me: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_document, container, false)
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

        load_doc = root.findViewById(R.id.load_document)

        refreshDocument("", load_doc)

        return root
    }




    open fun refreshDocument(type: String, loader: RecyclerView, limit: Int = 0){
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(
            ApiInterface::class.java)
        val wk_data: Call<ResponseData> = retInc.getData(limit)
        wk_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()

                if(response.code() == 200) {
                    if(type.equals("grid")) {
                        val DocAdapters = DocAdapter(dataset?.getResponseData())
                        loader.apply {
                            layoutManager = GridLayoutManager(parentFragment, 3)
                            adapter = DocAdapters
                        }
                    } else {
                        val DocAdapters = DocAdapter(dataset?.getResponseData())
                        loader.apply {
                            layoutManager = LinearLayoutManager(parentFragment)
                            adapter = DocAdapters
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
        private var response_data: ArrayList<Doc>? = null

        fun getResponseData(): ArrayList<Doc>? {
            return response_data
        }
    }


    class Doc {

        @SerializedName("id")
        @Expose
        private var response_id_jenis: String? = null

        @SerializedName("nama")
        @Expose
        private var response_nama_jenis: String? = null

        fun getResponseID(): String? {
            return response_id_jenis
        }

        fun getResponseNama(): String? {
            return response_nama_jenis
        }
    }

    interface ApiInterface {
        @GET("Document/kategori/{limit}")
        fun getData(@Path("limit") limit: Int?): Call<ResponseData>
    }

    class DocHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val doc_nama = view.txt_nama_type

        fun bindDoc(doc: Doc) {
            doc_nama.text = doc.getResponseNama().toString()
            if(doc.getResponseNama().toString().equals("null")) {
                doc_nama.text = "<Tidak ada judul>"
            } else {
                doc_nama.text = doc.getResponseNama().toString()
            }
        }
    }



    inner class DocAdapter(private val wks: ArrayList<Doc>?) : RecyclerView.Adapter<DocHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): DocHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_doc_type, viewGroup, false)

            return DocHolder(inflater).listen{
                    pos, type, context ->
                val item = wks?.get(pos)
            }

        }

        override fun getItemCount(): Int = wks?.size!!

        override fun onBindViewHolder(holder: DocHolder, position: Int) {
            wks?.get(position)?.let { holder.bindDoc(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: String? = wks?.get(position)?.getResponseID()
                var nameSelected: String? = wks?.get(position)?.getResponseNama()
                parentFragment?.runOnUiThread {
                    val targetFragment: Fragment = DocDetailFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("id", it1.toString()) }
                    nameSelected?.let { it1 -> args.putString("name", it1.toString()) }
                    targetFragment.arguments = args
                    parentFragment.fragments_history.set(1, DocFragment())
                    parentFragment.fragments.set(1, targetFragment)
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(1, true)
                }
            }
            return this
        }
    }






    override  fun onBackPressed() : Boolean {
        var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
        var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
        parentFragment.targetted = HomeFragment()
        transaction!!.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
        transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
        transaction!!.commit()
        return true
    }
}