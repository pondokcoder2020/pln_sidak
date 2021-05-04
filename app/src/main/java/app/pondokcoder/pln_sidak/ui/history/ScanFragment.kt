package app.pondokcoder.pln_sidak.ui.history

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.pondokcoder.pln_sidak.*
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
//import info.androidhive.barcode.BarcodeReader
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException


class ScanFragment : Fragment(), MainActivity.IOnBackPressed {
    private lateinit var root: View
    private lateinit var parentFragment: MainActivity
    private lateinit var sessionManager: SessionManager
    //private lateinit var barcodeReader: BarcodeReader
    private val configuration: Config = Config()
    private lateinit var cameraView: SurfaceView
    private lateinit var barcodeInfo: TextView
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private lateinit var  scannerLayout: LinearLayout
    private lateinit var scannerBar: View
    private lateinit var animator:ObjectAnimator
    private lateinit var vto: ViewTreeObserver




    companion object {
        var OPTION = ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_scan, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        sessionManager = activity?.applicationContext?.let { SessionManager(it) }!!
        parentFragment = activity as MainActivity
        cameraView = root.findViewById(R.id.cameraSurfaceView)
        barcodeInfo = root.findViewById(R.id.tv_scan)
        scannerLayout = root.findViewById(R.id.scannerLayout)
        scannerBar = root.findViewById(R.id.scannerBar)

        vto = scannerLayout.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scannerLayout.viewTreeObserver.removeGlobalOnLayoutListener(this)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    scannerLayout.viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    scannerLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
                animator = ObjectAnimator.ofFloat(
                    scannerBar, "translationY",
                    scannerLayout.y - 320,
                    (scannerLayout.y  - 20 +
                            scannerLayout.height)
                )
                animator.repeatMode = ValueAnimator.REVERSE
                animator.repeatCount = ValueAnimator.INFINITE
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.duration = 2000
                animator.start()
            }
        })
        /*cameraView.setOnClickListener  {
            cameraSource.stop()
            val targetFragment: Fragment = InspeksiFragment()
            val args = Bundle()
            //args.putString("barcode", "58d70f30-da91-f0f6-74a2-512427e783e2")
            args.putString("barcode", "0ce66aa0-1ee0-9d93-5f14-c487ff900ff6")
            //args.putString("barcode", "2cd0f255-a017-ff5d-2af0-3979c2280a2b")

            targetFragment.arguments = args
            parentFragment.fragments_history.set(1, ScanFragment())
            parentFragment.fragments.set(1, targetFragment)
            parentFragment.mPager!!.adapter = parentFragment.pagerAdapter
            parentFragment.mPager!!.setCurrentItem(1, true)
        }*/

        barcodeDetector = BarcodeDetector.Builder(parentFragment)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(parentFragment, barcodeDetector)
            //.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
            .setAutoFocusEnabled(true)
            .setRequestedPreviewSize(640, 480)
            .build()

        cameraView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            parentFragment,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource.start(cameraView.holder)
                    } else {
                        cameraSource.start(cameraView.holder)
                    }
                } catch (ie: IOException) {
                    Toast.makeText(parentFragment, "Error : " + ie.toString(), Toast.LENGTH_SHORT)
                        .show();
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                //
            }
        })




        barcodeDetector.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {}
            override fun receiveDetections(detections: Detections<Barcode?>) {
                val barcodeResult: SparseArray<Barcode?> = detections.detectedItems
                parentFragment.runOnUiThread {
                    if (barcodeResult.size() != 0) {

                        cameraSource.stop()
                        barcodeInfo.setText(barcodeResult.valueAt(0)?.displayValue.toString())
                        var retInc: ApiInterface = RetroInstance.getRetrofitInstance(
                            configuration.server,
                            sessionManager.jWT
                        ).create(ApiInterface::class.java)
                        val detail_data: Call<ResponseData> =
                            retInc.getData(barcodeResult.valueAt(0)?.displayValue.toString())
                        //val detail_data: Call<ResponseData> = retInc.getData("ABC123")
                        detail_data.enqueue(object : Callback<ResponseData> {
                            override fun onResponse(
                                call: Call<ResponseData>,
                                response: Response<ResponseData>
                            ) {
                                var dataset: ResponseData? = response.body()
                                if (response.code() == 200 || response.code() == 202) {
                                    if (response.code() == 202) {
                                        sessionManager.saveSPString(
                                            "__TOKEN__",
                                            dataset?.getResponseToken()
                                        )
                                    }

                                    val status: String? =
                                        dataset?.getResponseData()?.get(0)?.getResponseStatus()
                                    if (status.equals("available")) {
                                        var scanned: String? = dataset?.getResponseData()?.get(0)?.getResponseScanned()
                                        if(scanned.equals("yes")) {
                                            val builder = AlertDialog.Builder(parentFragment)
                                            builder.setMessage("Aset sudah pernah di scan bulan ini. Scan ulang?")
                                                .setCancelable(false)
                                                .setPositiveButton("Yes") { dialog, id ->
                                                    val UID: String? =
                                                        dataset?.getResponseData()?.get(0)?.getResponseId()
                                                    val targetFragment: Fragment = InspeksiFragment()
                                                    val args = Bundle()
                                                    args.putString("barcode", UID)
                                                    targetFragment.arguments = args
                                                    parentFragment.fragments_history.set(1, ScanFragment())
                                                    parentFragment.fragments.set(1, targetFragment)
                                                    parentFragment.mPager!!.adapter =
                                                        parentFragment.pagerAdapter
                                                    parentFragment.mPager!!.setCurrentItem(1, true)
                                                }
                                                .setNegativeButton("No") { dialog, id ->
                                                    // Dismiss the dialog
                                                    dialog.dismiss()
                                                }
                                            val alert = builder.create()
                                            alert.show()
                                        } else {
                                            val UID: String? =
                                                dataset?.getResponseData()?.get(0)?.getResponseId()
                                            val targetFragment: Fragment = InspeksiFragment()
                                            val args = Bundle()
                                            args.putString("barcode", UID)
                                            targetFragment.arguments = args
                                            parentFragment.fragments_history.set(1, ScanFragment())
                                            parentFragment.fragments.set(1, targetFragment)
                                            parentFragment.mPager!!.adapter =
                                                parentFragment.pagerAdapter
                                            parentFragment.mPager!!.setCurrentItem(1, true)
                                        }
                                    } else {
                                        barcodeInfo.setText("Aset tidak dikenali")
                                        if (ActivityCompat.checkSelfPermission(
                                                parentFragment,
                                                Manifest.permission.CAMERA
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            //
                                            return
                                        }
                                        cameraSource.start(cameraView.holder)
                                    }
                                } else {
                                    barcodeInfo.setText(response.code())
                                }
                            }

                            override fun onFailure(call: Call<ResponseData>, t: Throwable) {
                                Log.e("TANAKA", "Retro : " + t.message)
                                if (ActivityCompat.checkSelfPermission(
                                        parentFragment,
                                        Manifest.permission.CAMERA
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    //
                                    return
                                }
                                cameraSource.start(cameraView.holder)
                            }
                        })
                    } else {
                        barcodeInfo.setText("Scanning...")
                    }
                }
            }
        })
        return root
    }




    override  fun onBackPressed() : Boolean {
        parentFragment.runOnUiThread {
            parentFragment.mPager!!.setCurrentItem(0, true)
        }
        return true
    }







    interface ApiInterface {
        @GET("Aset/check_asset/{kode}")
        fun getData(@Path("kode") uid: String?): Call<ResponseData>
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
        @SerializedName("aset_status")
        @Expose
        private var response_status: String? = null


        @SerializedName("scanned")
        @Expose
        private var response_scanned: String? = null

        @SerializedName("uid")
        @Expose
        private var response_id: String? = null



        fun getResponseStatus(): String? {
            return response_status
        }

        fun getResponseScanned(): String? {
            return response_scanned
        }


        fun getResponseId(): String? {
            return response_id
        }
    }
}