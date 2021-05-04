package app.pondokcoder.pln_sidak.ui.history

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import app.pondokcoder.pln_sidak.ui.home.ItemDetailFragment
import app.pondokcoder.pln_sidak.ui.login.LauncherFragment
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_inspeksi_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.*

class InspeksiFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager: SessionManager
    private val configuration: Config = Config()
    private lateinit var txt_kode: TextView
    private lateinit var back_page: LinearLayout
    private lateinit var btn_submit: MaterialButton
    private lateinit var load_inspeksi_item: RecyclerView
    private lateinit var page_loader: FrameLayout
    private lateinit var inspeksiCheckItem: ArrayList<ItemInspeksi>

    private var readID: String ? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_inspeksi, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        parentFragment = activity as MainActivity
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        page_loader = root.findViewById(R.id.page_loader)
        txt_kode = root.findViewById(R.id.txt_kode)
        back_page = root.findViewById(R.id.backHome)
        back_page.setOnClickListener {
            parentFragment.runOnUiThread {
                /*val targetFragment: Fragment = ScanFragment()
                parentFragment.fragments_history[1] = ScanFragment()
                parentFragment.fragments[1] = targetFragment
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter*/
                parentFragment.mPager!!.setCurrentItem(2, true)
            }
        }
        btn_submit = root.findViewById(R.id.btn_submit)
        btn_submit.setOnClickListener {
            val gson = Gson()
            val inspeksiResult: String = gson.toJson(inspeksiCheckItem)

            readID?.let { it1 -> kirim(it1, inspeksiResult) }
        }

        load_inspeksi_item = root.findViewById(R.id.load_check)

        val getUID = arguments
        readID = getUID!!.getString("barcode", "")

        parentFragment.runOnUiThread {
            var retInc : ApiInterface = RetroInstance.getRetrofitInstance(
                configuration.server,
                sessionManager.jWT
            ).create(
                ApiInterface::class.java
            )
            val detail_data: Call<ResponseData> = retInc.getData(readID)
            detail_data.enqueue(object : Callback<ResponseData> {
                override fun onResponse(
                    call: Call<ResponseData>,
                    response: Response<ResponseData>
                ) {
                    var dataset: ResponseData? = response.body()
                    if (response.code() == 200 || response.code() == 202) {
                        if (response.code() == 202) {
                            sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                        }

                        val UID: String? = dataset?.getResponseData()?.get(0)?.getResponseId()
                        val Kode: String? = dataset?.getResponseData()?.get(0)?.getResponseKode()

                        val inspeksiAdapter =
                            dataset?.getResponseData()?.get(0)?.getResponseInspeksi()?.let {
                                ItemInspeksiAdapter(
                                    it
                                )
                            }
                        inspeksiCheckItem =
                            dataset?.getResponseData()?.get(0)?.getResponseInspeksi()!!
                        load_inspeksi_item?.apply {
                            layoutManager = LinearLayoutManager(parentFragment)
                            adapter = inspeksiAdapter
                        }

                        txt_kode.setText(Kode)

                        page_loader.visibility = View.GONE

                    } else {
                        Log.e("TANAKA", response.code().toString())
                    }
                }

                override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                    Log.e("TANAKA", "Retro : " + t.message)
                }
            })
        }
        return root
    }



    interface ApiInterface {
        @GET("Aset/detail/{uid}")
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
        private var response_data: ArrayList<Aset>? = null

        fun getResponseData(): ArrayList<Aset>? {
            return response_data
        }
    }

    class Aset {
        @SerializedName("uid")
        @Expose
        private var response_id: String? = null

        fun getResponseId(): String? {
            return response_id
        }




        @SerializedName("kode")
        @Expose
        private var response_kode: String? = null

        fun getResponseKode(): String? {
            return response_kode
        }



        @SerializedName("item_periksa")
        @Expose
        private var response_inspeksi: ArrayList<ItemInspeksi>? = null

        fun getResponseInspeksi(): ArrayList<ItemInspeksi>? {
            return response_inspeksi
        }
    }

    class ItemInspeksi {
        @SerializedName("id")
        @Expose
        private var response_id: Integer? = null

        fun getResponseUID(): Integer? {
            return response_id
        }


        @SerializedName("nama")
        @Expose
        private var response_nama: String? = null

        fun getResponseNama(): String? {
            return response_nama
        }


        @SerializedName("result")
        @Expose
        private var response_result: String? = null

        fun getResponseResult(): String? {
            return response_result
        }

        fun setResult(newValue: String) {
            response_result = newValue
        }
    }








    class ItemInspeksiHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nama = view.txt_nama
        public val checker = view.toggle

        fun bindKategori(kategori: ItemInspeksi) {
            var iconStatus : String

            nama.text = kategori.getResponseNama()
        }
    }

    inner class ItemInspeksiAdapter(private val inspeksiItem: List<ItemInspeksi>) : RecyclerView.Adapter<ItemInspeksiHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ItemInspeksiHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_inspeksi_item,
                viewGroup,
                false
            )
            return ItemInspeksiHolder(inflater).listen{ pos, type, context ->
                val item = inspeksiItem.get(pos)
            }

        }


        override fun getItemCount(): Int = inspeksiItem.size

        override fun onBindViewHolder(holder: ItemInspeksiHolder, position: Int) {
            holder.checker.setOnCheckedChangeListener { group, i ->
                var selected = this.getItemId(R.id.good)
                inspeksiCheckItem.get(position).setResult(resources.getResourceEntryName(group.checkedRadioButtonId))
                /*for(i in 0 .. inspeksiCheckItem.size - 1) {
                    Log.e("TANAKA", inspeksiCheckItem.get(i).getResponseResult().toString())
                }*/
            }

            holder.bindKategori(inspeksiItem[position])
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {

            itemView.setOnClickListener {

                var dataSelected: Integer? = inspeksiItem.get(position).getResponseUID()

                /*parentFragment.runOnUiThread {
                    val targetFragment: Fragment = ItemDetailFragment()
                    val args = Bundle()
                    dataSelected?.let { it1 -> args.putString("uid", it1) }
                    targetFragment.arguments = args
                    parentFragment.fragments[0] = targetFragment
                    parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                    parentFragment.mPager!!.setCurrentItem(0, true)
                }*/

                //event.invoke(getAdapterPosition(), getItemViewType())
            }

            return this
        }
    }





    private fun kirim(uid: String, hasil: String){
        Log.e("TANAKA", hasil)
        parentFragment.runOnUiThread {
            btn_submit.isEnabled = false
        }
        val retPost = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(
            SubmitInterface::class.java
        )
        retPost.kirim("hasil_inspeksi", uid, hasil).enqueue(object : Callback<Hasil> {
            override fun onFailure(call: Call<Hasil>, t: Throwable) {
                Toast.makeText(parentFragment, "Data gagal disimpan", Toast.LENGTH_LONG).show();
                parentFragment.runOnUiThread {
                    btn_submit.isEnabled = true
                }
            }

            override fun onResponse(call: Call<Hasil>, response: Response<Hasil>) {
                parentFragment.runOnUiThread {
                    if(response.body()?.getResponse().toString().equals("0")) {
                        Toast.makeText(parentFragment, "Data gagal disimpan", Toast.LENGTH_LONG).show();
                        btn_submit.isEnabled = true
                    } else {
                        /*val targetFragment: Fragment = ScanFragment()
                        parentFragment.fragments_history[1] = ScanFragment()
                        parentFragment.fragments[1] = targetFragment
                        parentFragment.mPager!!.adapter = parentFragment.pagerAdapter*/
                        parentFragment.mPager!!.setCurrentItem(2, true)
                    }
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
        @POST("Aset")
        @FormUrlEncoded
        fun kirim(
            @Field("request") request: String,
            @Field("uid") uid: String,
            @Field("hasil") hasil: String
        ): Call<Hasil>
    }

















    override  fun onBackPressed() : Boolean {
        val targetFragment: Fragment = ScanFragment()
        parentFragment.fragments_history[2] = ScanFragment()
        parentFragment.fragments[2] = targetFragment
        parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
        parentFragment.mPager!!.setCurrentItem(2, true)
        return true
    }
}