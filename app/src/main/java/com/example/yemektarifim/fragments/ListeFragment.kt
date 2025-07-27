package com.example.yemektarifim.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.yemektarifim.R
import com.example.yemektarifim.databinding.FragmentListeBinding
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.yemektarifim.model.Tarif
import com.example.yemektarifim.roomdb.TarifDao
import com.example.yemektarifim.roomdb.TarifDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers


class ListeFragment : Fragment() {
    private var _binding : FragmentListeBinding? = null
    private val binding get() = _binding!!
    private val mDisposable = CompositeDisposable()
    private lateinit var tarifDao : TarifDao
    private lateinit var db : TarifDatabase




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(requireContext(), TarifDatabase::class.java , "Tarifler").build()
        tarifDao = db.tarifDao()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListeBinding.inflate(inflater , container ,  false)
        val view = binding.root
        return view

     }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        binding.floatingActionButton2.setOnClickListener{yeniEkle(it)}

        binding.tarifRecylerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun verileriAl(){
        mDisposable.add(
            tarifDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
    private fun handleResponse(tarifler : List<Tarif>){
        tarifler.forEach {
            println(it.isim)
            println(it.malzeme)
        }

    }

    fun yeniEkle(view : View){
        val action = ListeFragmentDirections.actionListeFragmentToTarifFragment(bilgi="yeni" , id=0)
        Navigation.findNavController(view).navigate(action)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}