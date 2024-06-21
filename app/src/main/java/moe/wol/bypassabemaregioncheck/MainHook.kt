package moe.wol.bypassabemaregioncheck

import android.os.Build
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.io.File


class MainHook: IXposedHookLoadPackage {
    private val ANDROID_VERSION_CLASSNAME_MAPPING = mapOf(
        2120000 to mapOf("Success" to "xi.a\$b", "DefaultIpCheckApi" to "Hh.f\$a", "Root" to "pu.n"),
        2120004 to mapOf("Success" to "Ai.a\$b", "DefaultIpCheckApi" to "Jh.f\$a", "Root" to "Nu.m")
    )
    private val TV_VERSION_CLASSNAME_MAPPING = mapOf(
        2110001 to mapOf("RegionCheckRepositoryImpl" to "pb.V", "IPCheckEntity" to "Ab.L0", "countryCodeEntity" to "Ab.X",
            "divisionType" to "Bb.j", "Root" to "Pc.O", "Continuation" to "D8.d"),
        2110018 to mapOf("RegionCheckRepositoryImpl" to "ec.H0", "IPCheckEntity" to "oc.T0", "countryCodeEntity" to "oc.d0",
            "divisionType" to "pc.j", "Root" to "Dd.C", "Continuation" to "o9.d")
    )

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (!lpparam?.packageName.equals("tv.abema")) return
        hook(lpparam)
    }

    private fun hook(lpparam: XC_LoadPackage.LoadPackageParam?) {
        val tv = XposedHelpers.findClassIfExists("pb.V", lpparam?.classLoader) != null
        val versionCode = this.getAppVersionCode(lpparam)
        val classname = if (tv) {
            TV_VERSION_CLASSNAME_MAPPING[versionCode]
        } else {
            ANDROID_VERSION_CLASSNAME_MAPPING[versionCode]
        }
        if (classname == null) {
            XposedBridge.log("Unsupported version!")
            return
        }
        if (tv) {
            val classLoader = lpparam?.classLoader
            val interfaceC0880d = classLoader?.loadClass(classname["Continuation"])
            XposedHelpers.findAndHookMethod(classname["RegionCheckRepositoryImpl"], lpparam?.classLoader, "c", interfaceC0880d, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val ipCheckEntity = XposedHelpers.findClass(classname["IPCheckEntity"], lpparam?.classLoader)
                    val countryCodeEntity = XposedHelpers.findClass(classname["countryCodeEntity"], lpparam?.classLoader)
                    val countryCodeJP = XposedHelpers.newInstance(countryCodeEntity, "JP")
                    val divisionType = XposedHelpers.findClass(classname["divisionType"], lpparam?.classLoader)
                    val divisionJAPAN = XposedHelpers.getStaticObjectField(divisionType, "c")
                    val ipCheckEntityResult = XposedHelpers.newInstance(ipCheckEntity, countryCodeJP, divisionJAPAN)
                    param.result = ipCheckEntityResult
                }
            })
            XposedHelpers.findAndHookMethod(classname["Root"], lpparam?.classLoader, "a", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = false
                }
            })
        } else {
            val classLoader = lpparam?.classLoader
            val obj = classLoader?.loadClass("java.lang.Object")
            XposedHelpers.findAndHookMethod(classname["DefaultIpCheckApi"], lpparam?.classLoader, "invokeSuspend", obj, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log(param.args.toString())
                    val division = XposedHelpers.findClass("tv.abema.protos.Division", lpparam?.classLoader)
                    val divisionNONE = XposedHelpers.getStaticObjectField(division, "NONE")
                    val c18457h = XposedHelpers.findClass("okio.h", lpparam?.classLoader)
                    val f85841e = XposedHelpers.getStaticObjectField(c18457h, "e")
                    val getIPCheckResponse = XposedHelpers.findClass("tv.abema.protos.GetIPCheckResponse", lpparam?.classLoader)
                    val getIPCheckResponseJP = XposedHelpers.newInstance(getIPCheckResponse, "JP", "Asia/Tokyo", "+09:00", "https://ds-linear-abematv.akamaized.net/region", divisionNONE, f85841e)
                    val success = XposedHelpers.findClass(classname["Success"], lpparam?.classLoader)
                    val successJP = XposedHelpers.newInstance(success, getIPCheckResponseJP)
                    param.args[0] = successJP
                }
            })
            XposedHelpers.findAndHookMethod(classname["Root"], lpparam?.classLoader, "a", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    param.result = false
                }
            })
        }
    }

    private fun getAppVersionCode(lpparam: XC_LoadPackage.LoadPackageParam?): Int {
        val apkPath = File(lpparam!!.appInfo.sourceDir)
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pkgParserClass =
                XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
            val packageLite =
                XposedHelpers.callStaticMethod(pkgParserClass, "parsePackageLite", apkPath, 0)
            XposedHelpers.getIntField(packageLite, "versionCode")
        } else {
            val parserCls =
                XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
            val pkg = XposedHelpers.callMethod(parserCls.newInstance(), "parsePackage", apkPath, 0)
            XposedHelpers.getIntField(pkg, "mVersionCode")
        }
        return versionCode
    }
}