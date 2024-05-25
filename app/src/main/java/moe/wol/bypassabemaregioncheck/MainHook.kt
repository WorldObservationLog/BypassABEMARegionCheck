package moe.wol.bypassabemaregioncheck

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook: IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (!lpparam?.packageName.equals("tv.abema")) return
        hook(lpparam)
    }

    private fun hook(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val tv = XposedHelpers.findClassIfExists("pb.V", lpparam?.classLoader) != null
        if (tv) {
            val classLoader = lpparam?.classLoader
            val interfaceC0880d = classLoader?.loadClass("D8.d")
            XposedHelpers.findAndHookMethod("pb.V", lpparam?.classLoader, "c", interfaceC0880d, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val ipCheckEntity = XposedHelpers.findClass("Ab.L0", lpparam?.classLoader)
                    val countryCodeEntity = XposedHelpers.findClass("Ab.X", lpparam?.classLoader)
                    val countryCodeJP = XposedHelpers.newInstance(countryCodeEntity, "JP")
                    val divisionType = XposedHelpers.findClass("Bb.j", lpparam?.classLoader)
                    val divisionJAPAN = XposedHelpers.getStaticObjectField(divisionType, "c")
                    val ipCheckEntityResult = XposedHelpers.newInstance(ipCheckEntity, countryCodeJP, divisionJAPAN)
                    param.result = ipCheckEntityResult
                }
            })
            XposedHelpers.findAndHookMethod("Pc.O", lpparam?.classLoader, "a", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = false
                }
            })
        } else {
            val classLoader = lpparam?.classLoader
            val obj = classLoader?.loadClass("java.lang.Object")
            XposedHelpers.findAndHookMethod("Hh.f\$a", lpparam?.classLoader, "invokeSuspend", obj, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log(param.args.toString())
                    val division = XposedHelpers.findClass("tv.abema.protos.Division", lpparam?.classLoader)
                    val divisionNONE = XposedHelpers.getStaticObjectField(division, "NONE")
                    val c18457h = XposedHelpers.findClass("okio.h", lpparam?.classLoader)
                    val f85841e = XposedHelpers.getStaticObjectField(c18457h, "e")
                    val getIPCheckResponse = XposedHelpers.findClass("tv.abema.protos.GetIPCheckResponse", lpparam?.classLoader)
                    val getIPCheckResponseJP = XposedHelpers.newInstance(getIPCheckResponse, "JP", "Asia/Tokyo", "+09:00", "https://ds-linear-abematv.akamaized.net/region", divisionNONE, f85841e)
                    val success = XposedHelpers.findClass("xi.a\$b", lpparam?.classLoader)
                    val successJP = XposedHelpers.newInstance(success, getIPCheckResponseJP)
                    param.args[0] = successJP
                }
            })
            XposedHelpers.findAndHookMethod("pu.n", lpparam?.classLoader, "a", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = false
                }
            })
        }
    }
}