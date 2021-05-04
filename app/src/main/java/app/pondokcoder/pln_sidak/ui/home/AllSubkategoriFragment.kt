package app.pondokcoder.pln_sidak.ui.home

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.history.ScanFragment
import app.pondokcoder.pln_sidak.ui.login.LauncherFragment
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_icon_home.view.*
import kotlinx.android.synthetic.main.single_icon_home.view.kategori_caption
import kotlinx.android.synthetic.main.single_icon_home_small.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class AllSubkategoriFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager:SessionManager
    private lateinit var back_page: LinearLayout
    private lateinit var load_subkategori_item: RecyclerView
    private lateinit var kategoriList: RecyclerView
    private lateinit var loading_indicator: RelativeLayout
    private lateinit var nama_subkategori: TextView
    private lateinit var readID: String
    private lateinit var readNama: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_all_subkategori, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        parentFragment = activity as MainActivity
        sessionManager = parentFragment?.applicationContext?.let { SessionManager(it) }!!

        load_subkategori_item = root.findViewById(R.id.load_subkategori_item)
        nama_subkategori = root.findViewById(R.id.nama_subkategori)
        kategoriList = root.findViewById(R.id.kategoriList)
        loading_indicator = root.findViewById(R.id.loading_indicator)

        val getUID = arguments
        readID = getUID!!.getString("id", "")
        readNama = getUID.getString("nama", "")

        if(readID.isEmpty() || readID.toString().equals("")) {
            readID = "1"
            readNama = "Fire Protection System"
        }

        refreshOtherKategori()
        refreshKategori(readID, "list", load_subkategori_item, "none")

        nama_subkategori.text = readNama


        back_page = root.findViewById(R.id.backHome)
        back_page.setOnClickListener {
            parentFragment.runOnUiThread {
                val targetFragment: Fragment = HomeFragment()
                val args = Bundle()
                targetFragment.arguments = args
                parentFragment.fragments_history[0] = HomeFragment()
                parentFragment.fragments[0] = targetFragment
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(0, true)
            }
        }



        return root
    }


    open fun refreshOtherKategori(){
        var retInc : KategoriApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(KategoriApiInterface::class.java)
        val kategori_data: Call<KategoriResponseData> = retInc.getData()
        kategori_data.enqueue(object : Callback<KategoriResponseData> {
            override fun onResponse(call: Call<KategoriResponseData>, response: Response<KategoriResponseData>) {
                var dataset: KategoriResponseData? = response.body()

                if(response.code() == 200) {
                    loading_indicator.visibility = View.GONE
                    val kategoriAdapter = AllKategoriAdapter(dataset?.getResponseData())
                    var layManager = LinearLayoutManager(parentFragment)
                    layManager.orientation = LinearLayoutManager.HORIZONTAL

                    kategoriList.apply {
                        layoutManager = layManager
                        adapter = kategoriAdapter
                    }
                } else if(response.code() == 202) {
                    sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                } else {
                    //refreshKategori()
                }
            }

            override fun onFailure(call: Call<KategoriResponseData>, t: Throwable) {
                Log.e("TANAKA", "Failure : " + t.message)
            }

        })
    }





    open fun refreshKategori(target: String, type: String, loader: RecyclerView, limit: String){
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(ApiInterface::class.java)
        val kategori_data: Call<ResponseData> = retInc.getData(target, limit)
        kategori_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()

                if(response.code() == 200) {
                    loading_indicator.visibility = View.GONE
                    if(type.equals("grid")) {
                        val kategoriAdapter = KategoriListAdapter(dataset?.getResponseData())
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
        }

        fun loadSvg(url: String?, image: ImageView, context: Context) {
            GlideToVectorYou
                .init()
                .with(context)
                .setPlaceHolder(R.drawable.ic_baseline_no_photography_24, R.drawable.ic_baseline_no_photography_24)
                .load(Uri.parse(url), image)
        }
    }

    /*inner class KategoriAdapter(private val kategories: ArrayList<Kategori>?) : RecyclerView.Adapter<KategoriHolder>() {
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
                parentFragment?.runOnUiThread {
                    val targetFragment: Fragment = ListItemSubKatFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("id", it1.toString()) }
                    nameSelected?.let { it1 -> args.putString("name", it1.toString()) }
                    targetFragment.arguments = args
                    parentFragment.fragments_history.set(0, HomeFragment())
                    parentFragment.fragments.set(0, targetFragment)
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
                }
            }
            return this
        }
    }*/










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


                val targetFragment: Fragment = ListItemSubKatFragment()
                val args = Bundle()
                dataSelected?.let { it1 -> args.putString("id", it1.toString()) }
                nameSelected?.let { it1 -> args.putString("name", it1.toString()) }
                args.putString("subkategori_id", readID)
                args.putString("subkategori_nama", readNama)
                targetFragment.arguments = args
                parentFragment.fragments_history.set(0, AllSubkategoriFragment())
                parentFragment.fragments.set(0, targetFragment)
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(0, true)
            }
            return this
        }
    }

















    //All Kategori Data
    class KategoriResponseData {
        @SerializedName("token")
        @Expose
        private var response_token: String? = null

        fun getResponseToken(): String? {
            return response_token
        }

        @SerializedName("response_package")
        @Expose
        private var response_data: ArrayList<AllKategori>? = null

        fun getResponseData(): ArrayList<AllKategori>? {
            return response_data
        }
    }

    class AllKategori {
        @SerializedName("id")
        @Expose
        private var response_id: Int? = null

        @SerializedName("nama")
        @Expose
        private var response_nama: String? = null

        @SerializedName("icon")
        @Expose
        private var response_icon: String? = null

        fun getResponseNama(): String? {
            return response_nama
        }

        fun getResponseIcon(): String? {
            return response_icon
        }

        fun getResponseId(): Int? {
            return response_id
        }
    }


    interface KategoriApiInterface {
        @GET("Aset/kategori")
        fun getData(): Call<KategoriResponseData>
    }

    class AllKategoriHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val kat_cap = view.kategori_caption
        private val kat_icon = view.icon_other
        private val context = view.context

        fun bindKategori(kategori: AllKategori) {
            kat_cap.text = kategori.getResponseNama()
            kat_icon.visibility = View.GONE
            //loadSvg(kategori.getResponseIcon(),kat_icon,context)
            kat_icon.visibility = View.GONE
        }

        fun loadSvg(url: String?, image: ImageView, context: Context) {
            GlideToVectorYou
                .init()
                .with(context)
                .setPlaceHolder(R.drawable.ic_baseline_no_photography_24, R.drawable.ic_baseline_no_photography_24)
                .load(Uri.parse(url), image)
        }
    }

    inner class AllKategoriAdapter(private val all_kategories: ArrayList<AllKategori>?) : RecyclerView.Adapter<AllKategoriHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): AllKategoriHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_icon_home_small, viewGroup, false)
            //inflater.layoutParams = ViewGroup.LayoutParams((viewGroup.width * 0.7).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            inflater.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            /*val height: Int = viewGroup.getMeasuredHeight() / 3
            inflater.minimumHeight = height*/

            return AllKategoriHolder(inflater).listen{
                    pos, type, context ->
                val item = all_kategories?.get(pos)
            }

        }

        override fun getItemCount(): Int = all_kategories?.size!!

        override fun onBindViewHolder(holder: AllKategoriHolder, position: Int) {
            all_kategories?.get(position)?.let { holder.bindKategori(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: Int? = all_kategories?.get(position)?.getResponseId()
                var nameSelected: String? = all_kategories?.get(position)?.getResponseNama()
                parentFragment?.runOnUiThread {
                    loading_indicator.visibility = View.VISIBLE
                    refreshKategori(dataSelected.toString(), "list", load_subkategori_item, "none")
                    nama_subkategori.text = nameSelected
                }
            }
            return this
        }
    }

    override  fun onBackPressed() : Boolean {
        parentFragment.runOnUiThread {
            val targetFragment: Fragment = HomeFragment()
            parentFragment.fragments_history[0] = HomeFragment()
            parentFragment.fragments[0] = targetFragment
            parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
            parentFragment.mPager!!.setCurrentItem(0, true)
        }
        return true
    }
}