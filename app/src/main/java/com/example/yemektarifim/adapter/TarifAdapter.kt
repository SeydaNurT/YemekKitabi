package com.example.yemektarifim.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.yemektarifim.databinding.RecyclerRowBinding
import com.example.yemektarifim.fragments.ListeFragmentDirections
import com.example.yemektarifim.model.Tarif

class TarifAdapter(val tarifListesi : List<Tarif >) : RecyclerView.Adapter<TarifAdapter.TarifHolder>(){

    class TarifHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TarifHolder {

        val RecyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent , false)

        return TarifHolder(RecyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return tarifListesi.size
    }

    override fun onBindViewHolder(holder: TarifHolder, position: Int) {
        holder.binding.recyclerTarifText.text = tarifListesi[position].isim
        holder.itemView.setOnClickListener {
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi = "eski" , id = tarifListesi[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }
}