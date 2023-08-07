package com.samra.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samra.artbookkotlin.databinding.RecyclerRowBinding

class ArtAdapter(val artList: ArrayList<ArtName>): RecyclerView.Adapter<ArtAdapter.ArtHolder>() {
    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        var binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent , false)
        return  ArtHolder(binding)
    }

    override fun getItemCount(): Int {
        return artList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = artList.get(position).artName
        // asagida itemView ona gore yazdiq ki her hansisa item da tiklananda ne olacaq onu versin
        holder.itemView.setOnClickListener{
            var intent = Intent(holder.itemView.context ,ArtActivity::class.java )
            intent.putExtra("info" , "old")
            intent.putExtra("id" , artList.get(position).id)
            holder.itemView.context.startActivity(intent)

        }
    }

}