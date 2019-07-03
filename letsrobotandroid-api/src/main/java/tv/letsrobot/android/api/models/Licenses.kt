package tv.letsrobot.android.api.models

import org.btelman.licensehelper.License
import org.btelman.licensehelper.LicenseType

/**
 * LR api licenses
 */
object Licenses {
    val licenses = arrayListOf(
            License("Android AppCompat Library V7",LicenseType.APACHE2_0),
            License("Android AppCompat Library V7",LicenseType.APACHE2_0),
            License("Kotlin Standard Library JDK 7", LicenseType.APACHE2_0),
            License("Kotlinx Coroutines Core", LicenseType.APACHE2_0),
            License("Kotlinx Coroutines Android", LicenseType.APACHE2_0),
            License("Android Lifecycle ViewModel", LicenseType.APACHE2_0),
            License("Android Lifecycle Extensions", LicenseType.APACHE2_0),
            License("Guava: Google Core Libraries For Java", LicenseType.APACHE2_0),

            License("Socket IO Client", LicenseType.MIT),
            License("github.com/felHR85/UsbSerial", LicenseType.MIT),
            License("OkHttp", LicenseType.APACHE2_0),
            License("github.com/btelman96/ffmpeg-android-java", LicenseType.GPL3),
            License("github.com/btelman96/AndroidUvcDemo", LicenseType.GPL3
                    , "https://raw.githubusercontent.com/btelman96/AndroidUvcDemo/master/LICENCE")
    )
}