package app.pondokcoder.pln_sidak.ui.home

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.document.DocDetailFragment
import app.pondokcoder.pln_sidak.ui.document.DocFragment
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.single_doc_type.view.*
import kotlinx.android.synthetic.main.single_icon_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class HomeFragment : Fragment(), MainActivity.IOnBackPressed {

    private val configuration:Config = Config()

    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager:SessionManager
    private lateinit var welcome_name: TextView
    private lateinit var welcome_unit: TextView
    private lateinit var btn_view_fpu_all: TextView
    private lateinit var btn_view_kesehatan_all: TextView
    private lateinit var home_profile: ImageView
    var token:String? = null
    private var load_kategori:RecyclerView? = null
    private var  load_kesehatan: RecyclerView? = null
    private lateinit var load_document: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        parentFragment = activity as MainActivity
        sessionManager = parentFragment?.applicationContext?.let { SessionManager(it) }!!

        welcome_name = root.findViewById(R.id.welcome_name)
        welcome_name.setText(sessionManager.nama)

        welcome_unit = root.findViewById(R.id.welcome_unit)
        welcome_unit.setText(sessionManager.unit_name)
        home_profile = root.findViewById(R.id.home_profile)
        Picasso.get().load(configuration.foto_server + "pegawai/" + sessionManager.foto).transform(CircleTransform())
            .into(home_profile, object: com.squareup.picasso.Callback{
                override fun onSuccess() {
                    // Image is loaded successfully...
                }
                override fun onError(e: java.lang.Exception?) {
                    Picasso.get().load(configuration.foto_server + "/impostor.png").transform(CircleTransform())
                        .into(home_profile)
                }
            })

        load_kategori = root.findViewById(R.id.load_kategori)
        load_kesehatan = root.findViewById(R.id.load_kesehatan)
        load_document = root.findViewById(R.id.load_document)
        btn_view_fpu_all = root.findViewById(R.id.btn_view_fpu_all)
        btn_view_fpu_all.setOnClickListener {
            val targetFragment: Fragment = AllSubkategoriFragment()
            val args = Bundle()
            args.putString("id", "1")
            args.putString("nama", "Fire Protection System")
            targetFragment.arguments = args
            //parentFragment.fragments_history.set(0, HomeFragment())
            parentFragment.fragments.set(0, targetFragment)
            parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
            parentFragment.mPager!!.setCurrentItem(0, true)
        }
        btn_view_kesehatan_all = root.findViewById(R.id.btn_view_kesehatan_all)
        btn_view_kesehatan_all.setOnClickListener{
            val targetFragment: Fragment = AllSubkategoriFragment()
            val args = Bundle()
            args.putString("id", "2")
            args.putString("nama", "Tools Kesehatan")
            targetFragment.arguments = args
            //parentFragment.fragments_history.set(0, HomeFragment())
            parentFragment.fragments.set(0, targetFragment)
            parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
            parentFragment.mPager!!.setCurrentItem(0, true)
        }


        refreshKategori("1", "grid", load_kategori!!, "3")
        refreshKategori("2", "list", load_kesehatan!!, "2")
        refreshDocument("", load_document, 4)

        return root
    }









    open fun refreshDocument(type: String, loader: RecyclerView, limit: Int = 0){
        var retInc : DocApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(
            DocApiInterface::class.java)
        val wk_data: Call<DocResponseData> = retInc.getData(limit)
        wk_data.enqueue(object : Callback<DocResponseData> {
            override fun onResponse(call: Call<DocResponseData>, response: Response<DocResponseData>) {
                var dataset: DocResponseData? = response.body()

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

            override fun onFailure(call: Call<DocResponseData>, t: Throwable) {
                Log.e("TANAKA", "Failure : " + t.message)
            }

        })
    }

    class DocResponseData {
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

    interface DocApiInterface {
        @GET("Document/kategori/{limit}")
        fun getData(@Path("limit") limit: Int?): Call<DocResponseData>
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
                    //parentFragment.fragments_history.set(2, DocFragment())
                    parentFragment.fragments.set(2, targetFragment)
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(2, true)
                }
            }
            return this
        }
    }





















    open fun refreshKategori(target: String, type: String, loader: RecyclerView, limit: String){
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(ApiInterface::class.java)
        val kategori_data:Call<ResponseData> = retInc.getData(target, limit)
        kategori_data.enqueue(object : Callback<ResponseData>{
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()

                if(response.code() == 200) {
                    if(type.equals("grid")) {
                        val kategoriAdapter = KategoriAdapter(dataset?.getResponseData())
                        loader.apply {
                            layoutManager = GridLayoutManager(parentFragment, 3)
                            adapter = kategoriAdapter
                        }
                    } else {
                        val kategoriAdapter = KategoriListAdapter(dataset?.getResponseData())
                        loader.apply {
                            layoutManager = LinearLayoutManager(parentFragment)
                            adapter = kategoriAdapter
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
        private var response_data: ArrayList<Kategori>? = null

        fun getResponseData(): ArrayList<Kategori>? {
            return response_data
        }
    }

    class Kategori {
        @SerializedName("id")
        @Expose
        private var response_id: Int? = null

        @SerializedName("nama")
        @Expose
        private var response_nama: String? = null

        @SerializedName("count_asset")
        @Expose
        private var response_count: Int? = null

        @SerializedName("icon")
        @Expose
        private var response_gambar: String? = null

        @SerializedName("kat_id")
        @Expose
        private var response_kat_id: String? = null

        @SerializedName("kat_name")
        @Expose
        private var response_kat_name: String? = null

        fun getResponseGambar(): String? {
            return response_gambar
        }

        fun getResponseNama(): String? {
            return response_nama
        }

        fun getResponseId(): Int? {
            return response_id
        }

        fun getCountItem(): Int? {
            return response_count
        }

        fun getResponseKatId(): String? {
            return response_kat_id
        }

        fun getResponseKatName(): String? {
            return response_kat_name
        }
    }


    interface ApiInterface {
        @GET("Aset/subkategori/{id}/{limit}")
        fun getData(@Path("id") id: String?, @Path("limit") limit: String?): Call<ResponseData>
    }

    class KategoriHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val kat_cap = view.kategori_caption
        private val kat_count = view.jlh_unit
        private val kat_icon = view.icon_subkategori
        private val context = view.context

        fun bindKategori(kategori: Kategori) {
            kat_cap.text = kategori.getResponseNama()
            kat_count.text = kategori.getCountItem().toString()
            var gambar = kategori.getResponseGambar()
            loadSvg(gambar, kat_icon, context)
            /*Picasso.get().load(gambar.toString())
                .resize(50, 50)
                .error(R.drawable.ic_baseline_no_photography_24)
                //.transform(CircleTransform())
                .into(kat_icon, object: com.squareup.picasso.Callback{
                    override fun onSuccess() {
                        // Image is loaded successfully...
                    }
                    override fun onError(e: java.lang.Exception?) {
                        Picasso.get().load(R.drawable.ic_baseline_no_photography_24).transform(CircleTransform())
                            .into(kat_icon)
                    }
                })*/
        }

        fun loadSvg(url: String?, image: ImageView, context: Context) {
            GlideToVectorYou
                .init()
                .with(context)
                .setPlaceHolder(R.drawable.ic_baseline_no_photography_24, R.drawable.ic_baseline_no_photography_24)
                .load(Uri.parse(url), image)
        }
    }



    /*fun ImageView.loadSvg(url: String) {
        val imageLoader = ImageLoader.Builder(this.context)
            .componentRegistry { add(SvgDecoder(this@loadSvg.context)) }
            .build()

        val request = ImageRequest.Builder(parentFragment)
            .crossfade(true)
            .crossfade(500)
            .data(url)
            .target(this)
            .build()

        imageLoader.enqueue(request)
    }*/

    inner class KategoriAdapter(private val kategories: ArrayList<Kategori>?) : RecyclerView.Adapter<KategoriHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): KategoriHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_icon_home, viewGroup, false)

            val height: Int = viewGroup.getMeasuredHeight() / 3
            inflater.minimumHeight = height

            return KategoriHolder(inflater).listen{
                pos, type, context ->
                val item = kategories?.get(pos)
            }

        }

        override fun getItemCount(): Int = kategories?.size!!

        override fun onBindViewHolder(holder: KategoriHolder, position: Int) {
            kategories?.get(position)?.let { holder.bindKategori(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: Int? = kategories?.get(position)?.getResponseId()
                var nameSelected: String? = kategories?.get(position)?.getResponseNama()
                var katIDSelected: String? = kategories?.get(position)?.getResponseKatId()
                var katNameSelected: String? = kategories?.get(position)?.getResponseKatName()
                parentFragment?.runOnUiThread {
                    val targetFragment: Fragment = ListItemSubKatFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("id", it1.toString()) }
                    nameSelected?.let { it1 -> args.putString("name", it1.toString()) }
                    args.putString("subkategori_id", katIDSelected)
                    args.putString("subkategori_nama", katNameSelected)
                    targetFragment.arguments = args
                    //parentFragment.fragments_history.set(0, HomeFragment())
                    parentFragment.fragments.set(0, targetFragment)
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
                }
            }
            return this
        }
    }










    inner class KategoriListAdapter(private val kategories: ArrayList<Kategori>?) : RecyclerView.Adapter<KategoriHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): KategoriHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_icon_home_list, viewGroup, false)

            val height: Int = viewGroup.getMeasuredHeight() / 3
            inflater.minimumHeight = height

            return KategoriHolder(inflater).listen{
                    pos, type, context ->
                val item = kategories?.get(pos)
            }

        }

        override fun getItemCount(): Int = kategories?.size!!

        override fun onBindViewHolder(holder: KategoriHolder, position: Int) {
            kategories?.get(position)?.let { holder.bindKategori(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: Int? = kategories?.get(position)?.getResponseId()
                var nameSelected: String? = kategories?.get(position)?.getResponseNama()
                var katIDSelected: String? = kategories?.get(position)?.getResponseKatId()
                var katNameSelected: String? = kategories?.get(position)?.getResponseKatName()
                parentFragment?.runOnUiThread {
                    val targetFragment: Fragment = ListItemSubKatFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("id", it1.toString()) }
                    nameSelected?.let { it1 -> args.putString("name", it1.toString()) }
                    args.putString("subkategori_id", katIDSelected)
                    args.putString("subkategori_nama", katNameSelected)
                    targetFragment.arguments = args
                    //parentFragment.fragments_history.set(0, HomeFragment())
                    parentFragment.fragments.set(0, targetFragment)
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
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
        transaction.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
        transaction.commit()
        return true
    }

}

