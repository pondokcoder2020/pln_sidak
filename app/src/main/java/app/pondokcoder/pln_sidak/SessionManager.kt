package app.pondokcoder.pln_sidak

import android.content.Context
import android.content.SharedPreferences

class SessionManager {
    private val configuration = Config()
    private lateinit var sp: SharedPreferences
    var spEditor: SharedPreferences.Editor

    constructor(context: Context) {
        sp = context.getSharedPreferences("PLN", 0)
        spEditor = sp.edit()
    }

    fun saveSPString(keySP: String?, value: String?) {
        spEditor.putString(keySP, value)
        spEditor.commit()
    }

    fun saveSPInt(keySP: String?, value: Int) {
        spEditor.putInt(keySP, value)
        spEditor.commit()
    }

    fun saveSPBoolean(keySP: String?, value: Boolean) {
        spEditor.putBoolean(keySP, value)
        spEditor.commit()
    }

    var uID: String?
        get() = sp.getString("__UID__", "")
        set(value) {
            uID = value
        }

    var jWT: String?
        get() = sp.getString("__TOKEN__", "")
        set(value) {
            jWT = value
        }

    var nip: String?
        get() = sp.getString("__NIP__", "")
        set(value) {
            nip = value
        }

    var nama: String?
        get() = sp.getString("__NAME__", "")
        set(value) {
            nama = value
        }

    var unit: String?
        get() = sp.getString("__UNIT__", "")
        set(value) {
            unit = value
        }

    var unit_name: String?
        get() = sp.getString("__UNIT_NAME__", "")
        set(value) {
            unit_name = value
        }

    var foto: String?
        get() = sp.getString("__FOTO__", "")
        set(value) {
            foto = value
        }

    fun logout() {
        spEditor.clear().commit()
    }

}