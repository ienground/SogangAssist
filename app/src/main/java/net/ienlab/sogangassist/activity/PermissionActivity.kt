package net.ienlab.sogangassist.activity

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.databinding.ActivityPermissionBinding

class PermissionActivity : AppCompatActivity() {

    lateinit var binding: ActivityPermissionBinding

    lateinit var typefaceBold: Typeface
    lateinit var typefaceRegular: Typeface
    lateinit var nm: NotificationManager

    val REQUEST_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permission)
        binding.activity = this

        typefaceBold = ResourcesCompat.getFont(this, R.font.pretendard_black) ?: Typeface.DEFAULT
        typefaceRegular = ResourcesCompat.getFont(this, R.font.pretendard_regular) ?: Typeface.DEFAULT
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        binding.title.typeface = typefaceBold
        binding.infoMessage.typeface = typefaceRegular
        binding.tvLocationTitle.typeface = typefaceBold
        binding.tvLocationSummary.typeface = typefaceRegular
        binding.tvDndTitle.typeface = typefaceBold
        binding.tvDndSummary.typeface = typefaceRegular
        binding.btnConfirm.typeface = typefaceRegular

        binding.btnConfirm.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
                    Toast.makeText(this, getString(R.string.background_location_message), Toast.LENGTH_LONG).show()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
                Toast.makeText(this, getString(R.string.background_location_message), Toast.LENGTH_LONG).show()
            } else {
                setResult(RESULT_OK)
                finish()
            }

            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) !nm.isNotificationPolicyAccessGranted else false) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_PERMISSION -> {
                var isGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        isGranted = false
                        break
                    }
                }

                if (!isGranted) {
                    Toast.makeText(this, getString(R.string.permission_allow_msg), Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(TAG, "isGranted")
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
                    Toast.makeText(this, getString(R.string.background_location_message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}