package org.btelman.licensehelper

/**
 * License Types
 */
enum class LicenseType {
    APACHE2_0,
    MIT,
    BSD,
    LGPL2_1;

    fun getDefaultLink(): String {
        return when(this){
            APACHE2_0 -> "https://www.apache.org/licenses/LICENSE-2.0.txt"
            MIT -> "https://opensource.org/licenses/MIT"
            BSD -> TODO()
            LGPL2_1 -> TODO()
        }
    }
}