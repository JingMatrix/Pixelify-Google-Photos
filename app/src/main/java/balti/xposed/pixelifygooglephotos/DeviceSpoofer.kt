package balti.xposed.pixelifygooglephotos

import android.util.Log
import balti.xposed.pixelifygooglephotos.Constants.PACKAGE_NAME_GOOGLE_PHOTOS
import balti.xposed.pixelifygooglephotos.Constants.PREF_DEVICE_TO_SPOOF
import balti.xposed.pixelifygooglephotos.Constants.PREF_STRICTLY_CHECK_GOOGLE_PHOTOS
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Codenames of pixels:
 * https://oneandroid.net/all-google-pixel-codenames-from-sailfish-to-redfin/
 */
class DeviceSpoofer: IXposedHookLoadPackage {

    /**
     * Simple message to log messages in xposed log as well as android log.
     */
    private fun log(message: String){
        XposedBridge.log("PixelifyGooglePhotos: $message")
        Log.d("PixelifyGooglePhotos", message)
    }

    /**
     * To read preference of user.
     */
    private val pref by lazy {
        XSharedPreferences("balti.xposed.pixelifygooglephotos", Constants.SHARED_PREF_FILE_NAME)
    }

    /**
     * This is the final device to spoof.
     * By default use Pixel 5.
     */
    private val finalDeviceToSpoof by lazy {
        val deviceShortHand = pref.getString(PREF_DEVICE_TO_SPOOF, "p5")
        log("Device spoof: $deviceShortHand")
        getDeviceProps(deviceShortHand)
    }

    /**
     * Inspired by:
     * https://github.com/itsuki-t/FakeDeviceData/blob/master/src/jp/rmitkt/xposed/fakedevicedata/FakeDeviceData.java
     */
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {

        /**
         * If user selects to never use this on any other app other than Google photos,
         * then check package name and return if necessary.
         */
        if (pref.getBoolean(PREF_STRICTLY_CHECK_GOOGLE_PHOTOS, false) &&
            lpparam?.packageName != PACKAGE_NAME_GOOGLE_PHOTOS) return

        log("Loaded DeviceSpoofer for ${lpparam?.packageName}")
        log("Device spoof: $finalDeviceToSpoof")

        finalDeviceToSpoof?.run {

            val classLoader = lpparam?.classLoader ?: return
            val classBuild = XposedHelpers.findClass("android.os.Build", classLoader)
            keys.forEach {
                XposedHelpers.setStaticObjectField(classBuild, it, this[it])
            }

        }

    }

}