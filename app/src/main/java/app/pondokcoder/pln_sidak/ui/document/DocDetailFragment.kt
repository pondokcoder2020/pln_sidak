package app.pondokcoder.pln_sidak.ui.document

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_doc_type.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File


class DocDetailFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity
    private lateinit var back_page: LinearLayout

    private lateinit var load_doc: RecyclerView

    private lateinit var call_me: LinearLayout

    private var readID: String ? = ""
    private var readName: String ? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_document_detail, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity

        back_page = root.findViewById(R.id.back_page)
        back_page.setOnClickListener {
            parentFragment.runOnUiThread {
                val targetFragment: Fragment
                targetFragment = HomeFragment()
                parentFragment.fragments_history[1] = DocFragment()
                parentFragment.fragments[1] = DocFragment()
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(1, true)
            }
        }
        load_doc = root.findViewById(R.id.load_document_detail)

        val getUID = arguments
        readID = getUID!!.getString("id", "")
        readName = getUID!!.getString("name", "")

        refreshDocument("", load_doc, readID.toString())

        return root
    }




    open fun refreshDocument(type: String, loader: RecyclerView, jenis: String){
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(
            configuration.server,
            sessionManager.jWT
        ).create(
            ApiInterface::class.java
        )
        val wk_data: Call<ResponseData> = retInc.getData(jenis)
        wk_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()

                if (response.code() == 200) {
                    if (type.equals("grid")) {
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
                } else if (response.code() == 202) {
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

        @SerializedName("id_dokumen")
        @Expose
        private var response_id_jenis: String? = null

        @SerializedName("nama_dokumen")
        @Expose
        private var response_nama_jenis: String? = null

        @SerializedName("file")
        @Expose
        private var response_file: String? = null

        fun getResponseID(): String? {
            return response_id_jenis
        }

        fun getResponseNama(): String? {
            return response_nama_jenis
        }

        fun getResponseFile(): String? {
            return response_file
        }
    }

    interface ApiInterface {
        @GET("Document/kategori_item/{jenis}")
        fun getData(@Path("jenis") uid: String?): Call<ResponseData>
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
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_doc_type,
                viewGroup,
                false
            )

            return DocHolder(inflater).listen{ pos, type, context ->
                val item = wks?.get(pos)
            }

        }

        override fun getItemCount(): Int = wks?.size!!

        override fun onBindViewHolder(holder: DocHolder, position: Int) {
            wks?.get(position)?.let { holder.bindDoc(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: String? = wks?.get(position)?.getResponseID()
                var nameSelected: String? = wks?.get(position)?.getResponseNama()
                var fileSelected: String? = wks?.get(position)?.getResponseFile()
                parentFragment?.runOnUiThread {
                    var file: File
                    file = File(parentFragment.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path.toString(), fileSelected.toString())

                    if(file.exists()) {
                        val install = Intent(Intent.ACTION_VIEW)
                        install.setDataAndType(
                            FileProvider.getUriForFile(parentFragment, parentFragment.packageName + ".provider", file),
                            "application/pdf"
                        )
                        parentFragment.startActivity(install)
                    } else {
                        download_file(
                            parentFragment,
                            configuration.document_server + fileSelected,
                            fileSelected
                        )
                    }
                }
            }
            return this
        }
    }

    fun download_file(baseActivity: Context, url: String?, title: String?): Long {
        val direct = File(parentFragment.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path.toString())

        if (!direct.exists()) {
            direct.mkdirs()
        }
        val extension = url?.substring(url.lastIndexOf("."))
        val downloadReference: Long
        var  dm: DownloadManager
        dm= baseActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOCUMENTS,
            /*"pdf" + System.currentTimeMillis() + extension*/
            title
        )

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle(title)
        Toast.makeText(baseActivity, "Downloading..", Toast.LENGTH_SHORT).show()

        downloadReference = dm?.enqueue(request) ?: 0

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadReference === reference) {
                    val install = Intent(Intent.ACTION_VIEW)
                    install.setDataAndType(
                        Uri.fromFile(File(Environment.DIRECTORY_DOWNLOADS + "/" + title)),
                        "application/pdf"
                    )
                    startActivity(install)
                }
            }
        }

        LocalBroadcastManager.getInstance(parentFragment).registerReceiver(receiver, filter)


        return downloadReference

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