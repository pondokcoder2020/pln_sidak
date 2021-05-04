package app.pondokcoder.pln_sidak

import com.google.gson.JsonElement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

class Utility {
    val configuration : Config = Config()

    open fun generate_kecamatan(sessionManager : SessionManager, kecamatanCall : (returnData : String?) -> Unit) {
        var retInc : KecamatanInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(KecamatanInterface::class.java)
        val response_data: Call<JsonElement> = retInc.getData()
        response_data.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                kecamatanCall(response.body().toString())
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                //Log.e("TANAKA", t.message)
            }

        })
    }


    open fun generate_kelurahan(sessionManager : SessionManager, kecamatan : String, kelurahanCall : (returnData : String?) -> Unit) {
        var retInc : KelurahanInterface = RetroInstance.getRetrofitInstance(configuration.server, sessionManager.jWT).create(KelurahanInterface::class.java)
        val response_data: Call<JsonElement> = retInc.getData(kecamatan)
        response_data.enqueue(object : Callback<JsonElement> {
            override fun onResponse(call: Call<JsonElement>, response: Response<JsonElement>) {
                kelurahanCall(response.body().toString())
            }

            override fun onFailure(call: Call<JsonElement>, t: Throwable) {
                //Log.e("TANAKA", t.message)
            }

        })
    }


    interface KecamatanInterface {
        @GET("Master/kecamatan")
        fun getData(): Call<JsonElement>
    }

    interface KelurahanInterface {
        @GET("Master/desa/kecamatan/{id}")
        fun getData(@Path("id") id : String?): Call<JsonElement>
    }

}