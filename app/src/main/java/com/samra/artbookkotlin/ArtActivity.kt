package com.samra.artbookkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.samra.artbookkotlin.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var database: SQLiteDatabase
    var selectedBitmap: Bitmap? = null

    // result laucnherlerin initializationi ni on create da yazmaq lazimdir , sonra harda istesek istifade ede bilerik , bunlar bize gelen resultlari saxlayiri , meselen sekil ya icaze verildimi verilmedimi bu tipde seyleri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        registerLauncher()
        var intent = intent
        val info = intent.getStringExtra("info")
        if (info != null) {
            if (info.equals("new")) {
                binding.artNameText.setText("")
                binding.yearText.setText("")
                binding.artistNameText.setText("")
                binding.button.visibility = View.VISIBLE
                binding.imageView.setImageResource(R.drawable.selectimage)
            } else {
                binding.button.visibility = View.INVISIBLE
                val selectedId = intent.getIntExtra("id", 1)
                var cursor = database.rawQuery(
                    "SELECT * FROM arts WHERE id = ?",
                    arrayOf(selectedId.toString())
                )
                var artNameIx = cursor.getColumnIndex("artname")
                var artistsNameIx = cursor.getColumnIndex("artistname")
                var yearIx = cursor.getColumnIndex("year")
                var imageIx = cursor.getColumnIndex("image")

                while (cursor.moveToNext()) {
                    binding.artNameText.setText(cursor.getString(artNameIx))
                    binding.yearText.setText(cursor.getString(yearIx))
                    binding.artistNameText.setText(cursor.getString(artistsNameIx))
                    var byteArray = cursor.getBlob(imageIx)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    binding.imageView.setImageBitmap(bitmap)
                }

                cursor.close()

            }
        }

    }

    fun saveButtonClicked(view: View) {
        var artName = binding.artNameText.text.toString()
        var artistName = binding.artistNameText.text.toString()
        var year = binding.yearText.text.toString()

        if (selectedBitmap != null) {
            var smallerImage = makeSmallerBitmap(selectedBitmap!!, 30)
            //databasaya sekil kayd elemek olmur png falan formatda , ona gore olari bytelara sevirmeliyik
            var outputStream = ByteArrayOutputStream()
            smallerImage.compress(Bitmap.CompressFormat.PNG, 60, outputStream)
            var byteArray = outputStream.toByteArray()

            try {
                // var database = openOrCreateDatabase("Arts" , MODE_PRIVATE , null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY ,artname VARCHAR , artistname VARCHAR , year VARCHAR , image BLOB ) ")
                // deyerlerimiz degiskenlerdedi deye statment funksiyasinda istfade edirik
                var sqlString =
                    "INSERT INTO arts(artname , artistname , year , image) VALUES(?,?,?,?)"
                var statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var intent = Intent(this@ArtActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //  bu o demekdir ki menden evvelde ne ki aciq activity var bagla olari
            startActivity(intent)
        }
    }

    fun selectImage(view: View) {
        // Compat uygunluq demekdir , hemise kohne versiyalarla uygulugu yoxlamaliyiq , meselen api 19 dan asagi olanlara ancaq manifeste elave etmek kifayetdir , icazeye ehtiyac yoxdur ,ona gore de compatdan istfade edirik
        // Manifeste hansilari ancaq elave elemek kifayet edir? Protection level: normal olari , dangerous olanlari her iki terefden yoxlama lazimdir

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //rationale , izine denay edende altda bir bar acilir ki icaze ver, men bu isi gormek isteyirem ,bu mentiqe resional deyilir
                /*  true: Kullanıcı daha önce bu izni reddettiğinde veya izin reddedildiğinde ve "Artık gösterme" seçeneğini işaretlediğinde, yani iznin açıklamasının gösterilmesi gerektiğini belirtir.
                  false: Kullanıcı daha önce izni reddetmediyse veya "Artık gösterme" seçeneği işaretlenmediyse, yani iznin açıklamasının gösterilmesine gerek yoktur. asagiya aiddir */
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )
                ) {
                    // rational
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("GIVE PERMISSION", View.OnClickListener {
                            // request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // intent i  ancaq her hansisa bir activity e getmek ucun istifade etmirik
                // hemcinin mediadan nese goturmek ( pick ) , bateraya baxmaq falan bele seyler ucun de istfade edirik
                var intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    // rational
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE)
                        .setAction("GIVE PERMISSION", View.OnClickListener {
                            // request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                var intentToGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var height = image.height
        var width = image.width

        var bitmapRatio: Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1) {
            //landscape
            width = maximumSize
            var scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = maximumSize
            var scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun registerLauncher() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    var intentFromResult = result.data
                    if (intentFromResult != null) {
                        val imageData = intentFromResult.data
                        // binding.imageView.setImageResource(imageData)
                        if (imageData != null) {
                            try {
                                if (Build.VERSION.SDK_INT >= 28) {
                                    val source = ImageDecoder.createSource(
                                        this@ArtActivity.contentResolver,
                                        imageData
                                    )
                                    selectedBitmap = ImageDecoder.decodeBitmap(source)
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                } else {
                                    selectedBitmap = MediaStore.Images.Media.getBitmap(
                                        contentResolver,
                                        imageData
                                    )
                                    binding.imageView.setImageBitmap(selectedBitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    // permission granted
                    val intentToGallery =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    // permission denied
                    Toast.makeText(this@ArtActivity, "Permission is needed!", Toast.LENGTH_LONG)
                }
            }

    }
}