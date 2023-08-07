package com.samra.artbookkotlin

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.samra.artbookkotlin.databinding.ActivityArtBinding
import com.samra.artbookkotlin.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var artList:ArrayList<ArtName>
    private  lateinit var artAdapter: ArtAdapter
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        artList = ArrayList<ArtName>()

        artAdapter = ArtAdapter(artList)
        binding.recyclerVIew.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerVIew.adapter = artAdapter


        //databasadan verini cekme
        try{
            Log.d("Check", "NOt error")

            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE , null)
            Log.d("Check", "NOt error2")

                var cursor = database.rawQuery("SELECT * FROM arts" , null)
            Log.d("Check", "NOt error3")

            var artNameIx = cursor.getColumnIndex("artname")
            var idIx = cursor.getColumnIndex("id")
            Log.d("Check", "Database")

            while(cursor.moveToNext()){
                val name = cursor.getString(artNameIx)
                val id = cursor.getInt(idIx)
                var art = ArtName(name , id)
                Log.d("Check", name+id)

                artList.add(art)
            }

            artAdapter.notifyDataSetChanged()
            // birinci oncreate bos olur sornadan doldururuq deye notify edirik
            cursor.close()
        }catch (e:Exception){
            Log.d("Check", "error")

            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflate
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu , menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.add_art_item) {
            var intent = Intent(this@MainActivity, ArtActivity::class.java)
            intent.putExtra("info" , "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


}