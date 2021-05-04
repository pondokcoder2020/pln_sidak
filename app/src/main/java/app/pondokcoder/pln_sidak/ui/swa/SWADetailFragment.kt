package app.pondokcoder.pln_sidak.ui.swa

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.JsonReader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.pondokcoder.pln_sidak.*
import app.pondokcoder.pln_sidak.ui.home.HomeFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.single_swa_item.view.*
import kotlinx.android.synthetic.main.single_swa_kriteria.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import retrofit2.http.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class SWADetailFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private val configuration: Config = Config()
    private lateinit var sessionManager: SessionManager
    private lateinit var parentFragment: MainActivity
    private lateinit var btn_submit_swa: MaterialButton
    private lateinit var load_kriteria: RecyclerView
    private lateinit var txt_keterangan_swa: EditText
    private lateinit var txt_detail_pekerjaan: TextView
    private lateinit var txt_tanggal_pekerjaan: TextView
    private lateinit var txt_sejak: TextInputEditText
    private lateinit var txt_sampai: TextInputEditText
    private lateinit var back_page: LinearLayout
    private lateinit var upload_kondisi: LinearLayout
    private lateinit var upload_tindakan: LinearLayout
    private lateinit var list_kondisi_bahaya: RecyclerView
    private lateinit var list_tindakan_bahaya: RecyclerView
    private lateinit var targettedList: RecyclerView
    private lateinit var readID : String
    private lateinit var filePhoto: File
    private var TARGET_DIR = ""
    private val FILE_NAME = "newImage"
    private var target_ob = "";
    val kondisi_report: MutableList<LaporanItem> = ArrayList()
    val tindakan_report: MutableList<LaporanItem> = ArrayList()
    var target_report: MutableList<LaporanItem> = ArrayList()
    var selected_kriteria: MutableList<String> = ArrayList()

    private lateinit var call_me: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_swa_detail, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity
        btn_submit_swa = root.findViewById(R.id.btn_submit_swa)
        txt_keterangan_swa = root.findViewById(R.id.txt_keterangan_swa)
        txt_detail_pekerjaan = root.findViewById(R.id.txt_detail_pekerjaan)
        txt_tanggal_pekerjaan = root.findViewById(R.id.txt_tanggal_pekerjaan)
        load_kriteria = root.findViewById(R.id.load_kriteria)
        upload_kondisi = root.findViewById(R.id.upload_kondisi)
        upload_tindakan = root.findViewById(R.id.upload_tindakan)
        list_kondisi_bahaya = root.findViewById(R.id.list_kondisi_bahaya)
        list_tindakan_bahaya = root.findViewById(R.id.list_tindakan_bahaya)
        txt_sejak = root.findViewById(R.id.txt_sejak)
        txt_sampai = root.findViewById(R.id.txt_sampai)


        upload_kondisi.setOnClickListener {
            openUploadSelector(list_kondisi_bahaya)
            targettedList = list_kondisi_bahaya
            target_ob = "kondisi"
        }

        upload_tindakan.setOnClickListener {
            openUploadSelector(list_tindakan_bahaya)
            targettedList = list_tindakan_bahaya
            target_ob = "tindakan"
        }

        val getUID = arguments
        readID = getUID!!.getString("uid", "")

        //Toast.makeText(parentFragment, "!!!", Toast.LENGTH_LONG).show()
        btn_submit_swa.setOnClickListener {
            if(!readID.equals("")) {
                //Toast.makeText(parentFragment, "???", Toast.LENGTH_LONG).show()
                var listKondisi = mutableMapOf<String, RequestBody>()
                val listFotoKondisi = ArrayList<String>()
                //val listFotoKondisi = mutableListOf<MultipartBody.Part>()

                kondisi_report.forEach { items ->
                    val kondisiItem: File = File(items.getFoto().toString())
                    val imageKondisiBody: RequestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), kondisiItem)
                    val multiKondisi: MultipartBody.Part = MultipartBody.Part.createFormData("kondisi_foto", kondisiItem.name, imageKondisiBody)
                    listKondisi.put(
                        kondisiItem.name, imageKondisiBody
                    )

                    val imageName = MultipartBody.Part.createFormData(items.getFoto().toString(),
                        kondisiItem.name,
                        imageKondisiBody)

                    listFotoKondisi.add("data:image/" + kondisiItem.extension + ";base64," + getBase64FromImage(kondisiItem))
                    //listFotoKondisi.add(imageName)
                }


                var listTindakan = mutableMapOf<String, RequestBody>()
                val listFotoTindakan = ArrayList<String>()
                //val listFotoTindakan = mutableListOf<MultipartBody.Part>()
                tindakan_report.forEach { items ->
                    val tindakanItem: File = File(items.getFoto().toString())
                    val imageTindakanBody: RequestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), tindakanItem)
                    val multiTindakan: MultipartBody.Part = MultipartBody.Part.createFormData("tindakan_foto", tindakanItem.name, imageTindakanBody)
                    listTindakan.put(
                        tindakanItem.name, imageTindakanBody
                    )

                    val imageName = MultipartBody.Part.createFormData(items.getFoto().toString(),
                        tindakanItem.name,
                        imageTindakanBody)

                    listFotoTindakan.add("data:image/" + tindakanItem.extension + ";base64," + getBase64FromImage(tindakanItem))
                    //listFotoTindakan.add(imageName)
                }

                var keterangan : String = txt_keterangan_swa.text.toString()

                kirim(readID, keterangan, selected_kriteria, listFotoTindakan, listFotoKondisi ,txt_sejak.text.toString(),txt_sampai.text.toString())
            } else {
                Toast.makeText(parentFragment, "Working Permit tidak ditemukan", Toast.LENGTH_LONG).show()
            }
        }

        back_page = root.findViewById(R.id.back_page)
        back_page.setOnClickListener {
            parentFragment.runOnUiThread {
                val targetFragment: Fragment = SWAFragment()
                val args = Bundle()
                targetFragment.arguments = args
                parentFragment.fragments_history[3] = SWAFragment()
                parentFragment.fragments[3] = targetFragment
                parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                parentFragment.mPager!!.setCurrentItem(3, true)
            }
        }

        refreshSWA(readID)
        refreshKriteria(readID, "list", load_kriteria)
        return root
    }

    fun getBase64FromImage(image: File) : String {
        var base64 = ""
        try{
            val buffer = ByteArray(image.length().toInt() + 100)
            val length : Int = FileInputStream(image).read(buffer)
            base64 = Base64.encodeToString(buffer, 0, length, Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return base64
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openUploadSelector(targetList: RecyclerView) {
        val alert: AlertDialog.Builder = AlertDialog.Builder(parentFragment)
        alert.setTitle("Pilih sumber gambar")
        alert.setPositiveButton("Kamera",
            DialogInterface.OnClickListener { dialog, whichButton ->
                val takePhotoIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                val values: ContentValues = ContentValues(1)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                val fileUri = parentFragment.contentResolver
                    .insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        values
                    )

                TARGET_DIR = fileUri.toString()
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                takePhotoIntent.addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                startActivityForResult(takePhotoIntent, 200)

            })

        /*alert.setNegativeButton("Folder",
            DialogInterface.OnClickListener { dialog, whichButton ->
                if (checkSelfPermission(
                        parentFragment,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(
                        parentFragment,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(
                        parentFragment, arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ), 1
                    )
                } else {
                    chooseImageGallery();
                }
            })*/

        alert.show()
    }

    fun isPermissionsAllowed(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                parentFragment,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED) {
            false
        } else true
    }

    fun askForPermissions(): Boolean {
        if (!isPermissionsAllowed()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    parentFragment as Activity,
                    android.Manifest.permission.CAMERA
                )) {
                showPermissionDeniedDialog()
            } else {
                ActivityCompat.requestPermissions(
                    this as Activity,
                    arrayOf(android.Manifest.permission.CAMERA),
                    200
                )
            }
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            200 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission is granted, you can perform your operation here
                } else {
                    // permission is denied, you can ask for permission again, if you want
                    //  askForPermissions()
                }
                return
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(parentFragment)
            .setTitle("Permission Denied")
            .setMessage("Permission is denied, Please allow permissions from App Settings.")
            .setPositiveButton("App Settings",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    // send to app settings if permission is denied permanently
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", parentFragment.getPackageName(), null)
                    intent.data = uri
                    startActivity(intent)
                })
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = parentFragment.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val fileMgr: File = File(directoryStorage.toString())

        Log.e("TANAKA", TARGET_DIR)
        TARGET_DIR = directoryStorage.toString()
        if(!fileMgr.exists()) {
            fileMgr.mkdir()
        }
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    fun chooseImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 200 && resultCode == Activity.RESULT_OK){
            val cursor = parentFragment.contentResolver.query(
                Uri.parse(TARGET_DIR),
                Array(1) { android.provider.MediaStore.Images.ImageColumns.DATA },
                null, null, null
            )
            if (cursor != null) {
                cursor.moveToFirst()
            }
            val photoPath = cursor!!.getString(0)
            cursor.close()
            val file = File(photoPath)
            val uri = Uri.fromFile(file)

            parentFragment.runOnUiThread {

                var newReport: LaporanItem = LaporanItem()
                newReport.setKeterangan("")
                newReport.setFoto(photoPath)
                if(target_ob.equals("kondisi")) {
                    kondisi_report.add(newReport)
                    target_report = kondisi_report
                    var lapAdapter = KondisiAdapter(target_report)
                    targettedList.apply {
                        layoutManager = LinearLayoutManager(parentFragment)
                        adapter = lapAdapter
                    }
                } else {
                    tindakan_report.add(newReport)
                    target_report = tindakan_report
                    var lapAdapter = TindakanAdapter(target_report)
                    targettedList.apply {
                        layoutManager = LinearLayoutManager(parentFragment)
                        adapter = lapAdapter
                    }
                }
            }
        }
        else {
            //test_camera.setImageURI(data?.data)
        }

    }

    fun refreshKriteria(uid: String, type: String, loader: RecyclerView) {
        var retInc : KriteriaApiInterface = RetroInstance.getRetrofitInstance(
            configuration.server,
            sessionManager.jWT
        ).create(KriteriaApiInterface::class.java)
        val detail_data: Call<KriteriaResponseData> = retInc.getData(uid)
        detail_data.enqueue(object : Callback<KriteriaResponseData> {
            override fun onResponse(
                call: Call<KriteriaResponseData>,
                response: Response<KriteriaResponseData>
            ) {
                var dataset: KriteriaResponseData? = response.body()
                if (response.code() == 200 || response.code() == 202) {
                    parentFragment.runOnUiThread {
                        if (response.code() == 202) {
                            sessionManager.saveSPString("__TOKEN__", dataset?.getResponseToken())
                        }

                        if (type.equals("grid")) {
                            val kriteriaAdapter = KriteriaAdapter(dataset?.getResponseData())
                            loader.apply {
                                layoutManager = GridLayoutManager(parentFragment, 3)
                                adapter = kriteriaAdapter
                            }
                        } else {
                            val kriteriaAdapter = KriteriaAdapter(dataset?.getResponseData())
                            loader.apply {
                                layoutManager = LinearLayoutManager(parentFragment)
                                adapter = kriteriaAdapter
                            }
                        }

                        val UID: String? = dataset?.getResponseData()?.get(0)?.getResponseUID()
                        val subkategori_id: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseUID()
                    }
                }
            }

            override fun onFailure(call: Call<KriteriaResponseData>, t: Throwable) {
                Log.e("TANAKA", t.message.toString())
            }

        })
    }



    interface KriteriaApiInterface {
        @GET("SWA/kriteria/{uid}")
        fun getData(@Path("uid") uid: String?): Call<KriteriaResponseData>
    }

    class KriteriaResponseData {
        @SerializedName("token")
        @Expose
        private var response_token: String? = null

        fun getResponseToken(): String? {
            return response_token
        }

        @SerializedName("response_package")
        @Expose
        private var response_data: ArrayList<KriteriaItem>? = null

        fun getResponseData(): ArrayList<KriteriaItem>? {
            return response_data
        }
    }

    class KriteriaItem {
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


        @SerializedName("nilai")
        @Expose
        private var response_nilai: String? = null
        fun getResponseNilai(): String? {
            return response_nilai
        }
    }


    class KriteriaHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val nama = view.keterangan_swa
        private val nilai = view.switch_swa

        fun bindKategori(kriteria: KriteriaItem) {
            var iconStatus : String

            nama.text = kriteria.getResponseNama()
            if(kriteria.getResponseNilai().equals("Y")) {
                nilai.isChecked = true
            } else {
                nilai.isChecked = false
            }
        }
    }


    inner class KriteriaAdapter(private val kriteriaItem: List<KriteriaItem>?) : RecyclerView.Adapter<KriteriaHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): KriteriaHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_swa_kriteria,
                viewGroup,
                false
            )
            return KriteriaHolder(inflater).listen{ pos, type, context ->
                val item = kriteriaItem?.get(pos)
            }

        }


        override fun getItemCount(): Int = kriteriaItem!!.size

        override fun onBindViewHolder(holder: KriteriaHolder, position: Int) {
            holder.bindKategori(kriteriaItem!!.get(position))
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {

            itemView.setOnClickListener {
                //
            }

            itemView.switch_swa.setOnCheckedChangeListener() { buttonView, isChecked ->
                val item = kriteriaItem?.get(position)
                if(isChecked) {
                    if(selected_kriteria.indexOf(item?.getResponseNama().toString()) < 0) {
                        selected_kriteria.add(item?.getResponseNama().toString())
                    }
                } else {
                    selected_kriteria.removeAt(selected_kriteria.indexOf(item?.getResponseNama().toString()))
                }
            }

            return this
        }
    }

    class LaporanItem {
        private var keterangan: String? = null
        fun getKeterangan(): String? {
            return keterangan
        }
        fun setKeterangan(target: String) {
            keterangan = target
        }

        private var foto: String? = null
        fun getFoto(): String? {
            return foto
        }
        fun setFoto(target: String) {
            foto = target
        }
    }

    class LaporanHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val foto = view.foto_laporan
        private val btnDel = view.delete_laporan

        fun bindLaporan(kriteria: LaporanItem) {
            //val myBitmap = BitmapFactory.decodeFile(photoPath)
            //test_camera.setImageBitmap(myBitmap)

            val myBitmap = BitmapFactory.decodeFile(kriteria.getFoto())
            foto.setImageBitmap(myBitmap)
        }
    }

    inner class TindakanAdapter(private val laporanItem: MutableList<LaporanItem>) : RecyclerView.Adapter<LaporanHolder>() {
        val me = this
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): LaporanHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_swa_item,
                viewGroup,
                false
            )
            return LaporanHolder(inflater).listen{ pos, type, context ->
                val item = laporanItem?.get(pos)
            }

        }


        override fun getItemCount(): Int = laporanItem!!.size

        override fun onBindViewHolder(holder: LaporanHolder, position: Int) {
            holder.bindLaporan(laporanItem!!.get(position))
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {

            itemView.setOnClickListener {
                //
            }

            itemView.delete_laporan.setOnClickListener {
                tindakan_report.removeAt(position)
                list_tindakan_bahaya.removeViewAt(position)
                me.notifyItemRemoved(position)
                me.notifyItemRangeChanged(position, tindakan_report.size)
                me.notifyDataSetChanged()
            }

            return this
        }
    }




    inner class KondisiAdapter(private val laporanItem: MutableList<LaporanItem>) : RecyclerView.Adapter<LaporanHolder>() {
        val me = this
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): LaporanHolder {
            val inflater = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.single_swa_item,
                viewGroup,
                false
            )
            return LaporanHolder(inflater).listen{ pos, type, context ->
                val item = laporanItem?.get(pos)
            }

        }


        override fun getItemCount(): Int = laporanItem!!.size

        override fun onBindViewHolder(holder: LaporanHolder, position: Int) {
            holder.bindLaporan(laporanItem!!.get(position))
        }

        fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int, context: Context) -> Unit): T {

            itemView.setOnClickListener {
                //
            }

            itemView.delete_laporan.setOnClickListener {
                kondisi_report.removeAt(position)
                list_kondisi_bahaya.removeViewAt(position)
                me.notifyItemRemoved(position)
                me.notifyItemRangeChanged(position, kondisi_report.size)
                me.notifyDataSetChanged()
            }

            return this
        }
    }














    fun refreshSWA(uid: String) {
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

                        val UID: String? = dataset?.getResponseData()?.get(0)?.getResponseUID()
                        var detail_pekerjaan: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseDetail()
                        val tanggal_mulai: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseTanggalMulai()
                        val tanggal_selesai: String? =
                            dataset?.getResponseData()?.get(0)?.getResponseTanggalSelesai()

                        txt_detail_pekerjaan.text = detail_pekerjaan.toString()
                        txt_tanggal_pekerjaan.text =
                            tanggal_mulai.toString() + " - " + tanggal_selesai.toString()
                    }
                }
            }

            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                Log.e("TANAKA", t.message.toString())
            }

        })
    }





    interface ApiInterface {
        @GET("SWA/work_detail/{uid}")
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
        private var response_data: ArrayList<SWADetail>? = null

        fun getResponseData(): ArrayList<SWADetail>? {
            return response_data
        }
    }

    class SWADetail {
        @SerializedName("uid_working_permit")
        @Expose
        private var response_uid: String? = null
        fun getResponseUID(): String? {
            return response_uid
        }


        @SerializedName("detail_pekerjaan")
        @Expose
        private var response_detail: String? = null
        fun getResponseDetail(): String? {
            return response_detail
        }


        @SerializedName("tanggal_mulai")
        @Expose
        private var response_tanggal_mulai: String? = null
        fun getResponseTanggalMulai(): String? {
            return response_tanggal_mulai
        }

        @SerializedName("tanggal_selesai")
        @Expose
        private var response_tanggal_selesai: String? = null
        fun getResponseTanggalSelesai(): String? {
            return response_tanggal_selesai
        }
    }



    @Parcelize
    data class ImageContainer(
        val gambar: String
    ) : Parcelable



    private fun kirim(uid: String, keterangan: String, kriteria: List<String>,
                      //foto_tindakan:Map<String, RequestBody>, foto_kondisi:Map<String, RequestBody>,
                      foto_tindakan: ArrayList<String>, foto_kondisi: ArrayList<String>,
                      sejak:String, sampai:String){
        //Toast.makeText(parentFragment, "Nah", Toast.LENGTH_LONG).show()
        parentFragment.runOnUiThread {
            btn_submit_swa.isEnabled = false
        }
        val retPost = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(
            SubmitInterface::class.java
        )
        retPost.kirim("simpan_swa", uid, keterangan, kriteria, foto_tindakan, foto_kondisi,
            sejak, sampai).enqueue(object :
            Callback<Hasil> {
            override fun onFailure(call: Call<Hasil>, t: Throwable) {
                Toast.makeText(parentFragment, "Error : " + t.toString(), Toast.LENGTH_LONG).show()
                Log.e("TANAKA", t.message.toString())
                parentFragment.runOnUiThread {
                    btn_submit_swa.isEnabled = true
                }
            }

            override fun onResponse(call: Call<Hasil>, response: Response<Hasil>) {
                //Toast.makeText(parentFragment, response.body()?.getResponse().toString(), Toast.LENGTH_LONG).show()
                parentFragment.runOnUiThread {
                    //Log.e("TANAKA", response.body()?.getResponse().toString())
                    if (response.body()?.getResponse().toString().equals("0")) {

                    } else {
                        val targetFragment: Fragment = SWAFragment()
                        val args = Bundle()
                        targetFragment.arguments = args
                        parentFragment.fragments_history[3] = SWAFragment()
                        parentFragment.fragments[3] = targetFragment
                        parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
                        parentFragment.mPager!!.setCurrentItem(3, true)
                    }

                    btn_submit_swa.isEnabled = true
                }
            }

        })
    }


    private fun kirim2(uid: String, keterangan: String, kriteria: List<String>,
        //foto_tindakan:Map<String, RequestBody>, foto_kondisi:Map<String, RequestBody>,
                      foto_tindakan: List<MultipartBody.Part>, foto_kondisi: List<MultipartBody.Part>,
                      sejak:String, sampai:String){
        //Toast.makeText(parentFragment, "Nah", Toast.LENGTH_LONG).show()
        parentFragment.runOnUiThread {
            btn_submit_swa.isEnabled = false
        }
        val retPost = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(
            SubmitInterface::class.java
        )

        retPost.kirim2("simpan_swa", uid, keterangan, kriteria, foto_tindakan, foto_kondisi,
            sejak, sampai).enqueue(object :
            Callback<Hasil> {
            override fun onFailure(call: Call<Hasil>, t: Throwable) {
                Toast.makeText(parentFragment, "Error : " + t.toString(), Toast.LENGTH_LONG).show()
                Log.e("TANAKA", t.message.toString())
                parentFragment.runOnUiThread {
                    btn_submit_swa.isEnabled = true
                }
            }

            override fun onResponse(call: Call<Hasil>, response: Response<Hasil>) {
                Toast.makeText(parentFragment, response.body()?.getResponse().toString(), Toast.LENGTH_LONG).show()
                parentFragment.runOnUiThread {
                    //Log.e("TANAKA", response.body()?.getResponse().toString())
                    if (response.body()?.getResponse().toString().equals("0")) {

                    } else {
                        val targetFragment: Fragment = SWAFragment()
                    }

                    btn_submit_swa.isEnabled = true
                }
            }

        })
    }


    class Hasil {



        @SerializedName("response_package")
        @Expose
        private var response: String? = null

        private var token : String? = null

        fun getResponse(): String? {
            return response
        }

        fun getToken() : String? = token
    }

    /*@Parcelize
    data class Hasil (
        var response_package : String?,

        var token: String?

    ) : Parcelable */

    interface SubmitInterface {
        @Headers("Accept: application/json")
        @POST("SWA")
        //@JvmSuppressWildcards
        @FormUrlEncoded
        fun kirim(
            @Field("request") request: String,
            @Field("uid") uid: String,
            @Field("keterangan") keterangan: String,
            @Field("kriteria") kriteria: List<String>,
            //@PartMap foto_tindakan: Map<String, RequestBody>,
            //@PartMap foto_kondisi: Map<String, RequestBody>,
            @Field("foto_tindakan[]") foto_tindakan: ArrayList<String>,
            @Field("foto_kondisi[]") foto_kondisi: ArrayList<String>,
            @Field("sejak") sejak: String,
            @Field("sampai") sampai: String
        ): Call<Hasil>


        @Headers("Accept: application/json")
        @POST("SWA")
        @Multipart
        fun kirim2(
            @Part("request") request: String,
            @Part("uid") uid: String,
            @Part("keterangan") keterangan: String,
            @Part("kriteria") kriteria: List<String>,
            //@PartMap foto_tindakan: Map<String, RequestBody>,
            //@PartMap foto_kondisi: Map<String, RequestBody>,
            @Part("foto_tindakan") foto_tindakan: List<MultipartBody.Part>,
            @Part("foto_kondisi") foto_kondisi: List<MultipartBody.Part>,
            @Part("sejak") sejak: String,
            @Part("sampai") sampai: String
        ) : Call<Hasil>

    }






    override  fun onBackPressed() : Boolean {
        var fragmentManagers: FragmentManager? = parentFragment.supportFragmentManager
        var transaction: FragmentTransaction? = fragmentManagers!!.beginTransaction()
        parentFragment.targetted = SWAFragment()
        transaction!!.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
        transaction!!.replace(R.id.nav_host_fragment, parentFragment.targetted!!)
        transaction!!.commit()
        return true
    }
}