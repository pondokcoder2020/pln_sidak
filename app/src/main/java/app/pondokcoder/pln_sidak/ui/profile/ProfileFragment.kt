package app.pondokcoder.pln_sidak.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.home.AllSubkategoriFragment
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import app.pondokcoder.pln_sidak.ui.login.LauncherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.single_sertifikasi.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class ProfileFragment : Fragment(), MainActivity.IOnBackPressed{
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity

    private lateinit var txt_jabatan: TextView
    private lateinit var txt_nama: TextView
    private lateinit var txt_nip: TextView
    private lateinit var txt_email: TextView
    private lateinit var txt_contact: TextView
    private lateinit var txt_unit: TextView
    private lateinit var profile_image: ImageView
    private lateinit var sertifikasi: RecyclerView
    private lateinit var back_page: LinearLayout

    private lateinit var call_me: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_profile, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity

        txt_jabatan = root.findViewById(R.id.txt_jabatan)
        txt_nama = root.findViewById(R.id.txt_nama)
        txt_nip = root.findViewById(R.id.txt_nip)
        txt_contact = root.findViewById(R.id.txt_contact)
        txt_email = root.findViewById(R.id.txt_email)
        profile_image = root.findViewById(R.id.profile_image)
        txt_unit = root.findViewById(R.id.txt_unit)
        sertifikasi = root.findViewById(R.id.sertifikasi)
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

        txt_unit.setText(sessionManager.unit_name)

        refreshProfile(sessionManager.uID.toString())
        val btn_logout : MaterialButton = root.findViewById(R.id.btn_logout)
        btn_logout.setOnClickListener{
            sessionManager.logout()
            parentFragment.runOnUiThread {
                val navView: BottomNavigationView = parentFragment.findViewById(R.id.nav_view)
                navView.visibility = View.INVISIBLE
                parentFragment.mPager?.visibility = View.INVISIBLE
            }

            val ft = fragmentManager?.beginTransaction()
            ft?.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            val goToHome: Fragment = LauncherFragment()
            ft?.replace(R.id.nav_host_fragment, goToHome, "Fragment")
            ft?.commit()
        }
        return root
    }

    fun refreshProfile(uid: String) {
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(
            configuration.server,
            sessionManager.jWT
        ).create(ApiInterface::class.java)
        val detail_data: Call<ResponseData> = retInc.getData(uid)
        detail_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()
                if (response.code() == 200 || response.code() == 202) {
                    parentFragment.runOnUiThread {
                        if (response.code() == 202) {
                            sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                        }
                        val nama_pegawai: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseNama()
                        txt_nama.text = nama_pegawai

                        val nama_jabatan: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseNamaJabatan()
                        txt_jabatan.text = nama_jabatan

                        val nip: String? = dataset?.getResponseData()?.get(0)?.getResponseNIP()
                        txt_nip.text = nip

                        val email: String? = dataset?.getResponseData()?.get(0)?.getResponseEmail()
                        txt_email.text = email

                        val kontak: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseKontak()
                        txt_contact.text = kontak

                        Picasso.get().load(configuration.foto_server + "pegawai/" + dataset?.getResponseData()?.get(0)?.getResponseFoto()).transform(CircleTransform())
                            .into(profile_image, object: com.squareup.picasso.Callback{
                                override fun onSuccess() {
                                    // Image is loaded successfully...
                                }
                                override fun onError(e: java.lang.Exception?) {
                                    Picasso.get().load(configuration.foto_server + "/impostor.png").transform(CircleTransform())
                                        .into(profile_image)
                                }
                            })

                        if(dataset!!.getResponseData()!!.get(0)!!.getResponseSertifikat()!!.size!! > 0) {
                            val sertifikasiAdapter =
                                dataset?.getResponseData()?.get(0)?.getResponseSertifikat()?.let {
                                    SertifikasiAdapter(it)
                                }

                            sertifikasi?.apply {
                                layoutManager = LinearLayoutManager(parentFragment)
                                adapter = sertifikasiAdapter
                            }
                            Log.e("TANAKA", "Load Data")
                        } else {
                            Log.e("TANAKA", "No Data")
                        }

                        call_me = root.findViewById(R.id.call_me)

                        call_me.setOnClickListener {
                            if (!kontak.equals("") || !kontak!!.isEmpty()) {
                                val intent =
                                    Intent(Intent.ACTION_CALL, Uri.parse("tel:" + kontak))
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    parentFragment,
                                    "Nomor kontak belum di set oleh admin",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }

                        /*if (dataset?.getResponseData()?.get(0)?.getResponseInspeksi()?.size!! > 0) {
                            item_result_check.visibility = View.VISIBLE
                            tidak_ada_data_cek.visibility = View.GONE
                            val inspeksiAdapter =
                                dataset?.getResponseData()?.get(0)?.getResponseInspeksi()?.let {
                                    ItemInspeksiAdapter(
                                        it
                                    )
                                }
                            load_check?.apply {
                                layoutManager = LinearLayoutManager(parentFragment)
                                adapter = inspeksiAdapter
                            }
                        } else {
                            item_result_check.visibility = View.GONE
                            tidak_ada_data_cek.visibility = View.VISIBLE
                        }*/
                    }
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                //Log.e("TANAKA", t.message)
            }

        })
    }

    interface ApiInterface {
        @GET("Pegawai/detail/{uid}")
        fun getData(@Path("uid") uid: String?): Call<ResponseData>
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
        private var response_data: ArrayList<ItemDetail>? = null

        fun getResponseData(): ArrayList<ItemDetail>? {
            return response_data
        }
    }

    class ItemDetail {
        @SerializedName("uid")
        @Expose
        private var response_uid: String? = null
        fun getResponseUID(): String? {
            return response_uid
        }

        @SerializedName("nama")
        @Expose
        private var response_nama: String? = null
        fun getResponseNama(): String? {
            return response_nama
        }

        @SerializedName("foto")
        @Expose
        private var response_foto: String? = null
        fun getResponseFoto(): String? {
            return response_foto
        }


        @SerializedName("nama_jabatan")
        @Expose
        private var response_nama_jabatan: String? = null
        fun getResponseNamaJabatan(): String? {
            return response_nama_jabatan
        }

        @SerializedName("nip")
        @Expose
        private var response_nip: String? = null
        fun getResponseNIP(): String? {
            return response_nip
        }

        @SerializedName("email")
        @Expose
        private var response_email: String? = null
        fun getResponseEmail(): String? {
            return response_email
        }

        @SerializedName("kontak")
        @Expose
        private var response_kontak: String? = null
        fun getResponseKontak(): String? {
            return response_kontak
        }

        /*@SerializedName("item_periksa")
        @Expose
        private var response_inspeksi: ArrayList<ItemInspeksi>? = null

        fun getResponseInspeksi(): ArrayList<ItemInspeksi>? {
            return response_inspeksi
        }*/

        @SerializedName("sertifikat")
        @Expose
        private var response_sertifikasi: ArrayList<Sertifikasi>? = null

        fun getResponseSertifikat(): ArrayList<Sertifikasi>? {
            return response_sertifikasi
        }
    }


    class Sertifikasi {
        @SerializedName("uid")
        @Expose
        private var response_id: String? = null

        fun getResponseUID(): String? {
            return response_id
        }


        @SerializedName("nama")
        @Expose
        private var response_nama: String? = null

        fun getResponseNama(): String? {
            return response_nama
        }


        @SerializedName("nomor")
        @Expose
        private var response_nomor: String? = null

        fun getResponseNomor(): String? {
            return response_nomor
        }

        @SerializedName("tanggal_terbit")
        @Expose
        private var response_tanggal_terbit: String? = null

        fun getResponseTerbit(): String? {
            return response_tanggal_terbit
        }

        @SerializedName("penerbit")
        @Expose
        private var response_penerbit: String? = null

        fun getResponsePenerbit(): String? {
            return response_penerbit
        }

        @SerializedName("expired")
        @Expose
        private var response_exp: String? = null

        fun getResponseExp(): String? {
            return response_exp
        }
    }






    class SertifikasiHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nama = view.nama_sertifikasi
        private val nomor_sertifikasi = view.txt_nomor
        private val tanggal_terbit = view.txt_tanggal_terbit
        private val penerbit = view.txt_penerbit
        private val tanggal_exp = view.txt_tanggal_exp

        fun bindKategori(sertifikasi: Sertifikasi) {
            nama.text = sertifikasi.getResponseNama()
            nomor_sertifikasi.text = sertifikasi.getResponseNomor()
            tanggal_terbit.text = sertifikasi.getResponseTerbit()
            penerbit.text = sertifikasi.getResponsePenerbit()
            tanggal_exp.text = sertifikasi.getResponseExp()
        }
    }

    inner class SertifikasiAdapter(private val sertifikasiItem: List<Sertifikasi>) : RecyclerView.Adapter<SertifikasiHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): SertifikasiHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_sertifikasi,
                viewGroup,
                false
            )
            return SertifikasiHolder(inflater).listen{ pos, type, context ->
                val item = sertifikasiItem.get(pos)
            }

        }


        override fun getItemCount(): Int = sertifikasiItem.size

        override fun onBindViewHolder(holder: SertifikasiHolder, position: Int) {
            holder.bindKategori(sertifikasiItem[position])
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {
            itemView.setOnClickListener {
                //
            }
            return this
        }
    }

    override  fun onBackPressed() : Boolean {
        var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
        var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
        parentFragment.targetted = LauncherFragment()
        transaction!!.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
        transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
        transaction!!.commit()
        return true
    }
}