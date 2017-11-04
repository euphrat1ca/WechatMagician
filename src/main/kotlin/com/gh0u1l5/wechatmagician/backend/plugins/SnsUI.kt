package com.gh0u1l5.wechatmagician.backend.plugins

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import com.gh0u1l5.wechatmagician.C
import com.gh0u1l5.wechatmagician.backend.WechatEvents
import com.gh0u1l5.wechatmagician.backend.WechatPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers

object SnsUI {

    private val pkg = WechatPackage
    private val events = WechatEvents

    // Hook AdFrameLayout constructors to add popup menu.
    fun setItemLongPressPopupMenu() {
        if (pkg.AdFrameLayout == null) {
            return
        }

        XposedHelpers.findAndHookConstructor(pkg.AdFrameLayout, C.Context, C.AttributeSet, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val layout = param.thisObject as FrameLayout
                var x: Float? = null
                var y: Float? = null
                (layout as View).setOnTouchListener { _, motion ->
                    x = motion.rawX; y = motion.rawY; false
                }
                layout.isLongClickable = true
                layout.setOnLongClickListener {
                    events.onAdFrameLongClick(layout, x, y)
                }
            }
        })
    }

    // Hook SnsUploadUI.onCreate to clean EditText properly before forwarding.
    fun cleanTextViewBeforeForwarding() {
        if (pkg.SnsUploadUI == null || pkg.SnsUploadUIEditTextField == "") {
            return
        }

        XposedHelpers.findAndHookMethod(pkg.SnsUploadUI, "onCreate", C.Bundle, object : XC_MethodHook() {
            @Throws(Throwable::class)
            override fun afterHookedMethod(param: MethodHookParam) {
                val intent = (param.thisObject as Activity).intent ?: return
                if (intent.getBooleanExtra("Ksnsforward", false)) {
                    val content = intent.getStringExtra("Kdescription")
                    val editText = XposedHelpers.getObjectField(
                            param.thisObject, pkg.SnsUploadUIEditTextField
                    )
                    XposedHelpers.callMethod(editText, "setText", content)
                }
            }
        })
    }
}