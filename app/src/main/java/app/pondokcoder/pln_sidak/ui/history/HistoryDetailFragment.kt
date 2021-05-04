package app.pondokcoder.pln_sidak.ui.history

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import app.pondokcoder.pln_sidak.ui.home.LaporanNew
import com.google.android.material.button.MaterialButton
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

class HistoryDetailFragment:Fragment() {
    private val configuration: Config = Config()
    private lateinit var sessionManager:SessionManager
    private lateinit var parentFragment: MainActivity

    private var statusColor: ArrayList<String> = arrayListOf()
    private var stepSelect: ArrayList<Boolean> = arrayListOf()
    private var stepSelectButton: ArrayList<TextView> = arrayListOf()

    private lateinit var status_pengaduan: TextView
    private lateinit var txt_judul: TextView
    private lateinit var txt_konten: TextView
    private lateinit var kategori_caption: TextView
    private lateinit var txt_lokasi: TextView
    private lateinit var ratting_like_1: TextView
    private lateinit var ratting_like_2: TextView
    private lateinit var ratting_like_3: TextView
    private lateinit var ratting_like_4: TextView
    private lateinit var ratting_like_5: TextView
    private lateinit var capturedImage: ImageView

    private lateinit var btn_submit: MaterialButton

    private var like: Int = 0
    private var readID: String ? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history_detail, container, false)
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity

        val getUID = arguments
        readID = getUID!!.getString("uid", "")


        statusColor.add("#00DCFF")
        statusColor.add("#FF7C00")
        statusColor.add("#FFEC00")
        statusColor.add("#59D800")

        stepSelect.add(false)
        stepSelect.add(false)
        stepSelect.add(false)
        stepSelect.add(false)
        stepSelect.add(false)


        status_pengaduan = root.findViewById(R.id.status_pengaduan)
        txt_judul = root.findViewById(R.id.txt_judul)
        txt_konten = root.findViewById(R.id.txt_konten)
        txt_lokasi = root.findViewById(R.id.txt_lokasi)

        ratting_like_1 = root.findViewById(R.id.ratting_like_1)
        ratting_like_2 = root.findViewById(R.id.ratting_like_2)
        ratting_like_3 = root.findViewById(R.id.ratting_like_3)
        ratting_like_4 = root.findViewById(R.id.ratting_like_4)
        ratting_like_5 = root.findViewById(R.id.ratting_like_5)

        stepSelectButton.add(ratting_like_1)
        stepSelectButton.add(ratting_like_2)
        stepSelectButton.add(ratting_like_3)
        stepSelectButton.add(ratting_like_4)
        stepSelectButton.add(ratting_like_5)

        kategori_caption = root.findViewById(R.id.kategori_caption)

        capturedImage = root.findViewById(R.id.capturedImage)

        btn_submit = root.findViewById(R.id.btn_submit)

        ratting_like_1.setOnClickListener{
            renderRate(stepSelectButton.indexOf(ratting_like_1))
        }

        ratting_like_2.setOnClickListener{
            renderRate(stepSelectButton.indexOf(ratting_like_2))
        }

        ratting_like_3.setOnClickListener{
            renderRate(stepSelectButton.indexOf(ratting_like_3))
        }

        ratting_like_4.setOnClickListener{
            renderRate(stepSelectButton.indexOf(ratting_like_4))
        }

        ratting_like_5.setOnClickListener{
            renderRate(stepSelectButton.indexOf(ratting_like_5))
        }

        btn_submit.setOnClickListener{
            //kirim()
        }




        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(ApiInterface::class.java)
        val detail_data: Call<ResponseData> = retInc.getData(readID)
        detail_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()
                if(response.code() == 200 || response.code() == 202) {

                    if(response.code() == 202) {
                        sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                    }

                    val UID: String? = dataset?.getResponseData()?.get(0)?.getResponseId()
                    val judul: String? = dataset?.getResponseData()?.get(0)?.getResponseJudul()
                    val isi: String? = dataset?.getResponseData()?.get(0)?.getResponseJudul()
                    val kategori: String? = dataset?.getResponseData()?.get(0)?.getResponseKategori()
                    val lokasi: String? = dataset?.getResponseData()?.get(0)?.getResponseLokasi()
                    val status: String? = dataset?.getResponseData()?.get(0)?.getResponseStatus()
                    val id_status: Int? = dataset?.getResponseData()?.get(0)?.getResponseIDStatus()

                    kategori_caption.setText(kategori)
                    txt_judul.setText(judul)
                    txt_konten.setText(isi)
                    txt_lokasi.setText(lokasi)
                    status_pengaduan.setText(status)
                    status_pengaduan.setTextColor(Color.parseColor(id_status?.minus(
                        1
                    )?.let { statusColor.get(it) }))

                    Picasso.get().load(configuration.server + "app/images/" + UID + ".png").into(capturedImage);



                } else {
                    //refreshKategori()
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                //Log.e("TANAKA", t.message)
            }

        })
        return root;
    }

    private fun renderRate(setStatus: Int){
        if(stepSelect.get(setStatus) == true) {
            stepSelectButton.get(setStatus).setTextColor(Color.parseColor("#FFD100"))
            stepSelect[setStatus] = false
        } else {
            stepSelectButton.get(setStatus).setTextColor(Color.parseColor("#CCCCCC"))
            stepSelect[setStatus] = true
        }

        for(a in 0 .. setStatus) {
            stepSelectButton.get(a).setTextColor(Color.parseColor("#FFD100"))
        }

        for(a in setStatus + 1 .. 4) {
            stepSelectButton.get(a).setTextColor(Color.parseColor("#CCCCCC"))
        }

        like = setStatus + 1

    }





    interface ApiInterface {
        @GET("Pengaduan/pengaduan/detail/{uid}")
        fun getData(@Path("uid") uid : String?): Call<ResponseData>
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
        private var response_data: ArrayList<Pengaduan>? = null

        fun getResponseData(): ArrayList<Pengaduan>? {
            return response_data
        }
    }

    class Pengaduan {
        @SerializedName("uid")
        @Expose
        private var response_id: String? = null

        @SerializedName("judul")
        @Expose
        private var response_judul: String? = null

        fun getResponseJudul(): String? {
            return response_judul
        }

        @SerializedName("isi")
        @Expose
        private var response_isi: String? = null

        fun getResponseIsi(): String? {
            return response_isi
        }

        @SerializedName("lokasi")
        @Expose
        private var response_lokasi: String? = null

        fun getResponseLokasi(): String? {
            return response_lokasi
        }

        @SerializedName("id_status")
        @Expose
        private var response_id_status: Int? = null

        fun getResponseIDStatus(): Int? {
            return response_id_status
        }

        @SerializedName("status")
        @Expose
        private var response_status: String? = null

        fun getResponseStatus(): String? {
            return response_status
        }

        @SerializedName("kategori")
        @Expose
        private var response_kategori: String? = null

        fun getResponseKategori(): String? {
            return response_kategori
        }

        fun getResponseId(): String? {
            return response_id
        }
    }

















    private fun kirim(komentar : String){

        val retPost = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(LaporanNew.SubmitInterface::class.java)
        retPost.kirim("laporan_baru", komentar,"","","","").enqueue(object : Callback<LaporanNew.Hasil>{
            override fun onFailure(call: Call<LaporanNew.Hasil>, t: Throwable) {
                parentFragment.runOnUiThread {
                    val targetFragment: Fragment = HomeFragment()
                    parentFragment.fragments_history[0] = HomeFragment()
                    parentFragment.fragments[0] = targetFragment
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(1, true)
                }
            }

            override fun onResponse(call: Call<LaporanNew.Hasil>, response: Response<LaporanNew.Hasil>) {
                parentFragment.runOnUiThread {
                    val targetFragment: Fragment = HomeFragment()
                    parentFragment.fragments_history[0] = HomeFragment()
                    parentFragment.fragments[0] = targetFragment
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
                }
            }

        })
    }

    class Hasil {
        @SerializedName("response_package")
        @Expose
        private var response: String? = null

        fun getResponse(): String? {
            return response
        }
    }

    interface SubmitInterface {
        @Headers("Accept: application/json")
        @POST("Pengaduan")
        @FormUrlEncoded
        fun kirim(
            @Field("request") request: String,
            @Field("komentar") kategori: String
        ): Call<Hasil>
    }

}