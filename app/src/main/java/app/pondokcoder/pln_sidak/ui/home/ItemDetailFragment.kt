package app.pondokcoder.pln_sidak.ui.home

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.history.ScanFragment
import app.pondokcoder.pln_sidak.ui.login.LauncherFragment
import com.google.android.material.button.MaterialButton
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.single_inspeksi_item.view.txt_nama
import kotlinx.android.synthetic.main.single_inspeksi_item_result.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*
import javax.xml.datatype.DatatypeConstants.MONTHS
import kotlin.collections.ArrayList

class ItemDetailFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity
    private lateinit var back_page: LinearLayout
    private var current_subkategori: Int = 0
    private var current_subkategori_name: String = ""

    private lateinit var txt_kode: TextView
    private lateinit var txt_nama_pemeriksa: TextView
    private lateinit var txt_current_tanggal: TextView
    private lateinit var tanggal_click: LinearLayout
    private lateinit var txt_varian: TextView
    private lateinit var txt_satuan: TextView
    private lateinit var txt_kapasitas: TextView
    private lateinit var load_check: RecyclerView
    private lateinit var btn_return: MaterialButton
    private lateinit var item_result_check: LinearLayout
    private lateinit var tidak_ada_data_cek: LinearLayout
    private var monthsName: ArrayList<String> = ArrayList()
    private var selectedMonth:Int = 0
    private var selectedYear:Int = 0


    private var readID: String ? = ""
    private var readName: String ? = ""
    private var readIDSubKategori: String ? = ""
    private var readNamaSubKategori: String ? = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_item_detail, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        parentFragment = activity as MainActivity
        sessionManager = parentFragment.applicationContext?.let { SessionManager(it) }!!
        val getUID = arguments
        readID = getUID!!.getString("uid", "")
        readName = getUID!!.getString("name", "")
        readIDSubKategori = getUID.getString("id", "")
        readNamaSubKategori = getUID.getString("name", "")

        txt_nama_pemeriksa = root.findViewById(R.id.txt_nama_pemeriksa)
        tanggal_click = root.findViewById(R.id.tanggal_click)
        txt_varian = root.findViewById(R.id.txt_varian)
        txt_satuan = root.findViewById(R.id.txt_satuan)
        txt_kapasitas = root.findViewById(R.id.txt_kapasitas)
        load_check = root.findViewById(R.id.load_check)
        item_result_check = root.findViewById(R.id.item_result_check)
        tidak_ada_data_cek = root.findViewById(R.id.tidak_ada_data_cek)
        txt_current_tanggal = root.findViewById(R.id.txt_current_tanggal)
        txt_kode = root.findViewById(R.id.txt_kode)
        back_page = root.findViewById(R.id.backHome)
        back_page.setOnClickListener {
            val targetFragment: Fragment

            val args = Bundle()
            if(readIDSubKategori.toString().isEmpty()) {
                args.putString("id", "1")
                args.putString("name", "Fire Protection System")
            } else {
                args.putString("id", readIDSubKategori.toString())
                args.putString("name", readNamaSubKategori.toString())
            }

            if(readIDSubKategori.toString().isEmpty()) {
                targetFragment = HomeFragment()
            } else {
                targetFragment = AllSubkategoriFragment()
            }
            targetFragment.arguments = args
            //parentFragment.fragments_history[0] = HomeFragment()
            parentFragment.fragments[0] = targetFragment
            parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
            parentFragment.mPager!!.setCurrentItem(0, true)
        }

        btn_return = root.findViewById(R.id.btn_return)
        btn_return.setOnClickListener {
            parentFragment.runOnUiThread {
                val targetFragment: Fragment = ListItemSubKatFragment()
                val args = Bundle()
                args.putString("id", readIDSubKategori)
                args.putString("name", readNamaSubKategori)
                targetFragment.arguments = args
                parentFragment.fragments_history[0] = ScanFragment()
                parentFragment.fragments[0] = targetFragment
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(0, true)
            }
        }

        monthsName.add("Januari")
        monthsName.add("Februari")
        monthsName.add("Maret")
        monthsName.add("April")
        monthsName.add("Mei")
        monthsName.add("Juni")
        monthsName.add("Juli")
        monthsName.add("Agustus")
        monthsName.add("September")
        monthsName.add("Oktober")
        monthsName.add("November")
        monthsName.add("Desember")


        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        txt_current_tanggal.text = monthsName.get(month) + " " + year.toString()
        selectedMonth = month + 1
        selectedYear = year

        txt_current_tanggal.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener(OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                txt_current_tanggal.text = monthsName.get(monthOfYear - 1) + " " + year.toString()
                selectedMonth = monthOfYear
                selectedYear = year
                //Log.e("TANAKA", selectedMonth.toString() + " - " + selectedYear.toString())
                refreshInspeksi(readID!!, selectedMonth.toString(), selectedYear.toString())
            })
            parentFragment.fragmentManager?.let { it1 -> pd.show(it1, "MonthYearPickerDialog") }
        }

        tanggal_click.setOnClickListener {
            val pd = MonthYearPickerDialog()
            pd.setListener(OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                txt_current_tanggal.text = monthsName.get(monthOfYear - 1) + " " + year.toString()
                selectedMonth = monthOfYear
                selectedYear = year
                //Log.e("TANAKA", selectedMonth.toString() + " - " + selectedYear.toString())
                refreshInspeksi(readID!!, selectedMonth.toString(), selectedYear.toString())
            })
            parentFragment.fragmentManager?.let { it1 -> pd.show(it1, "MonthYearPickerDialog") }
        }





        refreshInspeksi(readID!!, selectedMonth.toString(), selectedYear.toString())

        return root
    }


    fun refreshInspeksi (uid: String, month: String, year: String) {
        //Log.e("TANAKA", uid + " - " + selectedMonth.toString() + " - " + selectedYear.toString())
        var retInc : ApiInterface = RetroInstance.getRetrofitInstance(
            configuration.server,
            sessionManager.jWT
        ).create(ApiInterface::class.java)
        val detail_data: Call<ResponseData> = retInc.getData(uid, month, year)
        detail_data.enqueue(object : Callback<ResponseData> {
            override fun onResponse(call: Call<ResponseData>, response: Response<ResponseData>) {
                var dataset: ResponseData? = response.body()
                if (response.code() == 200 || response.code() == 202) {
                    parentFragment.runOnUiThread {
                        if (response.code() == 202) {
                            sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                        }

                        val UID: String? = dataset?.getResponseData()?.get(0)?.getResponseUID()
                        val subkategori_id: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseSubkategori()
                        current_subkategori = subkategori_id?.let { Integer.valueOf(it) }!!

                        val subkategori_name: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseSubkategoriName()
                        current_subkategori_name = subkategori_name.toString()

                        val nama_pemeriksa: String? = dataset?.getResponseData()?.get(0)?.getResponseInspector()
                        txt_nama_pemeriksa.text = nama_pemeriksa

                        val kode: String? = dataset?.getResponseData()?.get(0)?.getResponseKode()
                        txt_kode.text = kode

                        val varian: String? = dataset?.getResponseData()?.get(0)?.getResponseVarian()
                        txt_varian.text = varian

                        val kapasitas: String? = dataset?.getResponseData()?.get(0)?.getResponseKapasitas()
                        txt_kapasitas.text = kapasitas

                        val satuan: String? = dataset?.getResponseData()?.get(0)?.getResponseSatuan()
                        txt_satuan.text = satuan

                        if (dataset?.getResponseData()?.get(0)?.getResponseInspeksi()?.size!! > 0) {
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
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                //Log.e("TANAKA", t.message)
            }

        })
    }


    class MonthYearPickerDialog : DialogFragment() {
        private var listener: OnDateSetListener? = null
        fun setListener(listener: OnDateSetListener?) {
            this.listener = listener
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder: AlertDialog.Builder = activity?.let { AlertDialog.Builder(it) }!!
            // Get the layout inflater
            val inflater: LayoutInflater = requireActivity().layoutInflater
            val cal = Calendar.getInstance()
            val dialog: View = inflater!!.inflate(R.layout.date_picker_layout, null)
            val monthPicker: NumberPicker =
                dialog.findViewById<View>(R.id.picker_month) as NumberPicker
            val yearPicker: NumberPicker =
                dialog.findViewById<View>(R.id.picker_year) as NumberPicker
            monthPicker.setMinValue(1)
            monthPicker.setMaxValue(12)
            monthPicker.setValue(cal[Calendar.MONTH])
            val year = cal[Calendar.YEAR]
            yearPicker.setMinValue(year)
            yearPicker.setMaxValue(MAX_YEAR)
            yearPicker.setValue(year)
            builder.setView(dialog) // Add action buttons
                .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        listener!!.onDateSet(null, yearPicker.getValue(), monthPicker.getValue(), 0)
                    }
                })
                .setNegativeButton(R.string.cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, id: Int) {
                        this@MonthYearPickerDialog.getDialog()?.cancel()
                    }
                })
            return builder.create()
        }

        companion object {
            private const val MAX_YEAR = 2099
        }
    }

    interface ApiInterface {
        @GET("Aset/detail_result/{uid}/{month}/{year}")
        fun getData(@Path("uid") uid: String?, @Path("month") month: String?, @Path("year") year: String?): Call<ResponseData>
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


        @SerializedName("kode")
        @Expose
        private var response_kode: String? = null
        fun getResponseKode(): String? {
            return response_kode
        }


        @SerializedName("inspector")
        @Expose
        private var response_inspector: String? = null
        fun getResponseInspector(): String? {
            return response_inspector
        }



        @SerializedName("id_subkategori")
        @Expose
        private var response_subkategori: String? = null
        fun getResponseSubkategori(): String? {
            return response_subkategori
        }


        @SerializedName("subkategori_name")
        @Expose
        private var response_subkategori_name: String? = null
        fun getResponseSubkategoriName(): String? {
            return response_subkategori_name
        }

        @SerializedName("varian")
        @Expose
        private var response_varian: String? = null
        fun getResponseVarian(): String? {
            return response_varian
        }

        @SerializedName("kapasitas")
        @Expose
        private var response_kapasitas: String? = null
        fun getResponseKapasitas(): String? {
            return response_kapasitas
        }

        @SerializedName("satuan")
        @Expose
        private var response_satuan: String? = null
        fun getResponseSatuan(): String? {
            return response_satuan
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
        public val checker = view.txt_result

        fun bindKategori(kategori: ItemInspeksi) {
            var iconStatus : String

            nama.text = kategori.getResponseNama()
            checker.text = kategori.getResponseResult()
        }
    }

    inner class ItemInspeksiAdapter(private val inspeksiItem: List<ItemInspeksi>) : RecyclerView.Adapter<ItemInspeksiHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ItemInspeksiHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_inspeksi_item_result,
                viewGroup,
                false
            )
            return ItemInspeksiHolder(inflater).listen{ pos, type, context ->
                val item = inspeksiItem.get(pos)
            }

        }


        override fun getItemCount(): Int = inspeksiItem.size

        override fun onBindViewHolder(holder: ItemInspeksiHolder, position: Int) {
            holder.bindKategori(inspeksiItem[position])
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {

            itemView.setOnClickListener {
                //
            }

            return this
        }
    }









    override  fun onBackPressed() : Boolean {
        val targetFragment: Fragment = ListItemSubKatFragment()
        val args = Bundle()
        args.putString("id", readID)
        args.putString("name", readName)
        targetFragment.arguments = args
        parentFragment.fragments[0] = targetFragment
        parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
        parentFragment.mPager!!.setCurrentItem(0, true)
        return true
    }
}