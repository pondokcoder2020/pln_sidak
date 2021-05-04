package app.pondokcoder.pln_sidak.ui.home

import android.content.Context
import android.net.Uri
import android.opengl.Visibility
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
import app.pondokcoder.pln_sidak.ui.login.LoginKeamananFragment
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_icon_home.view.*
import kotlinx.android.synthetic.main.single_icon_home.view.kategori_caption
import kotlinx.android.synthetic.main.single_icon_home_small.view.*
import kotlinx.android.synthetic.main.single_subkategori_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class ListItemSubKatFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View

    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity
    private lateinit var load_subkategori_item: RecyclerView
    private lateinit var kategoriList: RecyclerView
    private lateinit var data_process_indicator: TextView
    private lateinit var backHome: LinearLayout

    private lateinit var nama_subkategori: TextView
    private lateinit var no_data_indicator: RelativeLayout

    private var readID: String ? = ""
    private var readName: String ? = ""
    private var readSubkategoriID: String ? = ""
    private var readSubkategoriNama: String ? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_subkategori_item, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        parentFragment = activity as MainActivity
        sessionManager = parentFragment.applicationContext?.let { SessionManager(it) }!!
        load_subkategori_item = root.findViewById(R.id.load_subkategori_item)
        nama_subkategori = root.findViewById(R.id.nama_subkategori)
        no_data_indicator = root.findViewById(R.id.no_data_indicator)
        kategoriList = root.findViewById(R.id.other_sub)
        data_process_indicator = root.findViewById(R.id.data_process_indicator)

        val getUID = arguments
        readID = getUID!!.getString("id", "")
        readName = getUID!!.getString("name", "")
        readSubkategoriID = getUID!!.getString("subkategori_id", "")
        readSubkategoriNama = getUID!!.getString("subkategori_nama", "")

        /*if(readID.toString().isEmpty()) {
            readID = "1"
            readName = "Fire Protection System"
        }*/


        nama_subkategori.setText(readName)
        refreshKategori(readSubkategoriID.toString(),"none")
        refreshItem(readID.toString(), "", sessionManager.jWT.toString())
        backHome = root.findViewById(R.id.backHome)
        backHome.setOnClickListener {
            val targetFragment: Fragment = AllSubkategoriFragment()
            val args = Bundle()
            if(!readSubkategoriID.toString().isEmpty() && !readSubkategoriNama.toString().isEmpty()) {
                args.putString("id", readSubkategoriID.toString())
                args.putString("nama", readSubkategoriNama.toString())
                //parentFragment.fragments_history[0] = HomeFragment()

            } else {
                args.putString("id", "1")
                args.putString("nama", "Fire Protection System")
            }
            targetFragment.arguments = args
            parentFragment.fragments[0] = targetFragment
            parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
            parentFragment.mPager!!.setCurrentItem(0, true)
        }
        return root
    }








    private fun refreshItem(targetID : String, search:String, token : String){
        var retInc = RetroInstance.getRetrofitInstance(configuration.server, token).create(ApiInterface::class.java)
        val kategori_data: Call<ResponseData> = retInc.getData(targetID, search)
        kategori_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()
                parentFragment.runOnUiThread {
                    if(dataset?.getResponseData()?.size!! == 0) {
                        no_data_indicator.visibility = View.VISIBLE
                        data_process_indicator.setText("Tidak ada data")
                        load_subkategori_item.visibility = View.GONE
                    } else {
                        no_data_indicator.visibility = View.GONE
                        load_subkategori_item.visibility = View.VISIBLE
                        if (response.code() == 200) {
                            val kategoriAdapter =
                                dataset?.getResponseData()?.let { KategoriAdapter(it) }

                            load_subkategori_item?.apply {
                                layoutManager = LinearLayoutManager(parentFragment)
                                adapter = kategoriAdapter
                            }
                        } else if (response.code() == 202) {
                            sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                            refreshItem(targetID, "", dataset?.getResponseToken().toString())
                        } else {
                            Log.e("TANAKA", response.code().toString())
                        }
                    }
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
        @SerializedName("uid")
        @Expose
        private var response_uid: String? = null

        @SerializedName("kode")
        @Expose
        private var response_kode: String? = null

        @SerializedName("varian")
        @Expose
        private var response_varian: String? = null

        @SerializedName("kapasitas")
        @Expose
        private var response_kapasitas: String? = null

        @SerializedName("jumlah")
        @Expose
        private var response_jumlah: Int? = null

        @SerializedName("nama_lokasi")
        @Expose
        private var response_lokasi: String? = null

        @SerializedName("nama_perusahaan")
        @Expose
        private var response_perusahaan: String? = null

        @SerializedName("nomor")
        @Expose
        private var response_nomor: String? = null

        @SerializedName("satuan")
        @Expose
        private var response_satuan: String? = null

        @SerializedName("created_at")
        @Expose
        private var response_tanggal: String? = null

        fun getResponseUID(): String? {
            return response_uid
        }

        fun getResponseKode(): String? {
            return response_kode
        }

        fun getResponseJumlah(): Int? {
            return response_jumlah
        }

        fun getResponseVarian(): String? {
            return response_varian
        }

        fun getResponseKapasitas(): String? {
            return response_kapasitas
        }

        fun getResponseSatuan(): String? {
            return response_satuan
        }

        fun getResponseLokasi(): String? {
            return response_lokasi
        }

        fun getResponsePerusahaan(): String? {
            return response_perusahaan
        }

        fun getResponseNomor(): String? {
            return response_nomor
        }

        fun getResponseTanggal(): String? {
            return response_tanggal
        }
    }

    interface ApiInterface {
        @GET("Aset/subkategori_item/{id}/{search}")
        fun getData(@Path("id") id : String?, @Path("search") search : String?): Call<ResponseData>
    }

    class KategoriHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val kode = view.txt_kode
        private val tanggal = view.tanggal
        private val keterangan = view.txt_keterangan

        fun bindKategori(kategori: Kategori) {
            var iconStatus : String

            kode.text = kategori.getResponseKode()
            tanggal.text = kategori.getResponseTanggal()
            keterangan.text = kategori.getResponseVarian() + "\n" + kategori.getResponseKapasitas() + " " + kategori.getResponseSatuan() + "\n" + kategori.getResponseLokasi()
        }
    }

    inner class KategoriAdapter(private val kategories: List<Kategori>) : RecyclerView.Adapter<KategoriHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): KategoriHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_subkategori_item, viewGroup, false)

            return KategoriHolder(inflater).listen{
                    pos, type, context ->
                val item = kategories.get(pos)
            }

        }

        override fun getItemCount(): Int = kategories.size

        override fun onBindViewHolder(holder: KategoriHolder, position: Int) {
            holder.bindKategori(kategories[position])
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: String? = kategories.get(position).getResponseUID()

                parentFragment.runOnUiThread {
                    val targetFragment: Fragment = ItemDetailFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("uid", it1) }
                    args.putString("id", readID)
                    args.putString("name", readName)
                    targetFragment.arguments = args
                    parentFragment.fragments[0] = targetFragment
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
                }

                //event.invoke(getAdapterPosition(), getItemViewType())
            }
            return this
        }
    }















    open fun refreshKategori(target: String, limit: String){
        var retInc : ApiInterfaceSub = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(ApiInterfaceSub::class.java)
        val kategori_data: Call<ResponseDataSub> = retInc.getData(target, limit)
        kategori_data.enqueue(object : Callback<ResponseDataSub> {
            override fun onResponse(call: Call<ResponseDataSub>, response: Response<ResponseDataSub>) {
                var dataset: ResponseDataSub? = response.body()

                if(response.code() == 200) {
                    val kategoriAdapter = KategoriListAdapter(dataset?.getResponseData())
                    var layManager = LinearLayoutManager(parentFragment)
                    layManager.orientation = LinearLayoutManager.HORIZONTAL
                    kategoriList.apply {
                        layoutManager = layManager
                        adapter = kategoriAdapter
                    }
                } else if(response.code() == 202) {
                    sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                } else {
                    Toast.makeText(parentFragment, response.code().toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ResponseDataSub>, t: Throwable) {
                Log.e("TANAKA", "Failure : " + t.message)
            }

        })
    }














    class ResponseDataSub {
        @SerializedName("token")
        @Expose
        private var response_token: String? = null

        fun getResponseToken(): String? {
            return response_token
        }

        @SerializedName("response_package")
        @Expose
        private var response_data: ArrayList<KategoriSub>? = null

        fun getResponseData(): ArrayList<KategoriSub>? {
            return response_data
        }
    }

    class KategoriSub {
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


    interface ApiInterfaceSub {
        @GET("Aset/subkategori/{id}/{limit}")
        fun getData(@Path("id") id: String?, @Path("limit") limit: String?): Call<ResponseDataSub>
    }

    class KategoriHolderSub(view: View) : RecyclerView.ViewHolder(view) {
        private val kat_cap = view.kategori_caption
        private val kat_count = view.jlh_unit
        private val kat_icon = view.icon_other
        private val context = view.context

        fun bindKategori(kategori: KategoriSub) {
            kat_cap.visibility = View.GONE
            //kat_cap.text = kategori.getResponseNama()
            //kat_count.text = kategori.getCountItem().toString()
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

    inner class KategoriListAdapter(private val kategories: ArrayList<KategoriSub>?) : RecyclerView.Adapter<KategoriHolderSub>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): KategoriHolderSub {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(R.layout.single_icon_home_small, viewGroup, false)

            val height: Int = viewGroup.getMeasuredHeight() / 3
            inflater.minimumHeight = height

            return KategoriHolderSub(inflater).listen{
                    pos, type, context ->
                val item = kategories?.get(pos)
            }

        }

        override fun getItemCount(): Int = kategories?.size!!

        override fun onBindViewHolder(holder: KategoriHolderSub, position: Int) {
            kategories?.get(position)?.let { holder.bindKategori(it) }
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context : Context) -> Unit): T {
            itemView.setOnClickListener {

                var dataSelected: Int? = kategories?.get(position)?.getResponseId()
                var nameSelected: String? = kategories?.get(position)?.getResponseNama()
                parentFragment?.runOnUiThread {
                    no_data_indicator.visibility = View.VISIBLE
                    data_process_indicator.setText("Loading...")
                    refreshItem(dataSelected.toString(), "", sessionManager.jWT.toString())
                    nama_subkategori.setText(nameSelected)
                }
            }
            return this
        }
    }












    override  fun onBackPressed() : Boolean {
        val targetFragment: Fragment = AllSubkategoriFragment()
        parentFragment.fragments_history[0] = HomeFragment()
        val args = Bundle()
        args.putString("id", readSubkategoriID)
        args.putString("nama", readSubkategoriNama)
        targetFragment.arguments = args
        parentFragment.fragments[0] = targetFragment
        parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
        parentFragment.mPager!!.setCurrentItem(0, true)
        return true
    }
}