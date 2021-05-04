package app.pondokcoder.pln_sidak.ui.home

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_icon_home.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*
import java.io.ByteArrayOutputStream
import java.util.*


class LaporanNew : Fragment () {

    private lateinit var sessionManager:SessionManager
    private lateinit var parentFragment: MainActivity
    private val configuration : Config = Config()
    private var readID: Int ? = 0

    private var imageResult : String = ""

    private var imgCapture : ImageView ? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_form_lapor, container, false)
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity

        val getUID = arguments
        readID = getUID!!.getInt("id", 0)

        val back_page : LinearLayout = root.findViewById(R.id.backHome)

        back_page.setOnClickListener{
            parentFragment.runOnUiThread {
                val targetFragment: Fragment = HomeFragment()
                parentFragment.fragments_history[0] = HomeFragment()
                parentFragment.fragments[0] = targetFragment
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(0, true)
            }
        }


        val btnCapture : MaterialButton = root.findViewById(R.id.btn_camera)
        imgCapture = root.findViewById(R.id.capturedImage) as ImageView
        btnCapture.setOnClickListener {
            val cInt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cInt, 1)
        }

        imgCapture!!.setOnClickListener{
            val cInt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cInt, 1)
        }



        val btnSubmit : MaterialButton = root.findViewById(R.id.btn_submit)
        btnSubmit.setOnClickListener {
            val judul : TextInputEditText = root.findViewById(R.id.judul_laporan)
            val isi : TextInputEditText = root.findViewById(R.id.isi_laporan)
            val lokasi : TextInputEditText = root.findViewById(R.id.lokasi_laporan)
            if(
                !judul.text.toString().equals("") &&
                !isi.text.toString().equals("") &&
                !lokasi.text.toString().equals("")
            ) {
                kirim(readID.toString(),judul.text.toString(),isi.text.toString(),lokasi.text.toString(),imageResult)
            }
        }


        val retIn = RetroInstance.getRetrofitInstance(configuration!!.server, sessionManager.jWT).create(ApiInterface::class.java)
        val kategori_data: Call<ResponseData> = retIn.getData(readID.toString())

        kategori_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()

                if(response.code() == 200 || response.code() == 202) {
                    val caption : TextView = root.findViewById(R.id.kategori_caption)
                    caption
                        .setText(" " + dataset?.getResponseData()?.get(0)?.getResponseNama())
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })




        return root
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                val photo = data!!.extras!!["data"] as Bitmap?
                imgCapture?.setImageBitmap(photo);

                val byteArrayOutputStream = ByteArrayOutputStream()
                photo?.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                imageResult = Base64.getEncoder().encodeToString(byteArray);

            } else if (resultCode == RESULT_CANCELED) {
                //Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }










    class ResponseData {
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

        fun getResponseNama(): String? {
            return response_nama
        }

        fun getResponseId(): Int? {
            return response_id
        }
    }


    interface ApiInterface {
        @Headers(
            "Accept: application/json"
        )
        @GET("Pengaduan/kategori/detail/{id}")
        fun getData(@Path("id") id : String?): Call<ResponseData>
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
            @Field("kategori") kategori: String,
            @Field("judul") judul: String,
            @Field("isi") isi: String,
            @Field("lokasi") lokasi: String,
            @Field("gambar") foto: String
        ): Call<Hasil>
    }

    private fun kirim(kategori : String, judul: String, isi:String, lokasi : String, foto : String){

        val retPost = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(SubmitInterface::class.java)
        retPost.kirim("laporan_baru", kategori, judul, isi, lokasi, foto).enqueue(object : Callback<Hasil>{
            override fun onFailure(call: Call<Hasil>, t: Throwable) {
                parentFragment.runOnUiThread {
                    val targetFragment: Fragment = HomeFragment()
                    parentFragment.fragments_history[0] = HomeFragment()
                    parentFragment.fragments[0] = targetFragment
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(1, true)
                }
            }

            override fun onResponse(call: Call<Hasil>, response: Response<Hasil>) {
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



    class KategoriHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val kat_cap = view.kategori_caption

        fun bindKategori(kategori: Kategori) {
            kat_cap.text = kategori.getResponseNama()
        }
    }
}