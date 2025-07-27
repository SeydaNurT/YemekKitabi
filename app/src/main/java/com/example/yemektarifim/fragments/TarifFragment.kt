package com.example.yemektarifim.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.example.yemektarifim.R
import com.example.yemektarifim.databinding.FragmentTarifBinding
import com.example.yemektarifim.model.Tarif
import com.example.yemektarifim.roomdb.TarifDao
import com.example.yemektarifim.roomdb.TarifDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream


class TarifFragment : Fragment() {
    private var _binding : FragmentTarifBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    //izin istemek için
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    //galeriye gitmek için
    private var secilenGorsel : Uri? = null
    //uri yer belirten , kaynagın yerini belirtir
    private var secilenBitmap : Bitmap? = null
    //bitmap uri'yi gorsele cevirir
    private val mDisposable = CompositeDisposable()
    //istek yapıldıgında hafızada birikmemesi icin hafızadan temizler

    private lateinit var tarifDao : TarifDao
    private lateinit var db : TarifDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerLauncher()

        db = Room.databaseBuilder(requireContext(), TarifDatabase::class.java , "Tarifler").build()
        tarifDao = db.tarifDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTarifBinding.inflate(inflater , container ,  false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view : View , savedInstanceState : Bundle?) {
        super.onViewCreated(view , savedInstanceState)
        binding.kaydetButton.setOnClickListener{kaydet()}
        binding.silButton.setOnClickListener{sil()}
        binding.imageView.setOnClickListener{gorselSec(it)}

        arguments?.let{
            val bilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if(bilgi== "yeni"){
                //yeni tarif eklenecek
                binding.silButton.isEnabled = false
                binding.kaydetButton.isEnabled = true
                binding.isimText.setText("")
                binding.tarifText.setText("")
            }
            else{
                //eski tarif gosterilecek
                binding.silButton.isEnabled = true
                binding.kaydetButton.isEnabled = false

                val id = TarifFragmentArgs.fromBundle(it).id

                mDisposable.add(
                    tarifDao.findById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleResponse)
                )


            }
        }

    }
    private fun handleResponse(tarif : Tarif){
        binding.isimText.setText(tarif.isim)
        binding.tarifText.setText(tarif.malzeme)
        val bitmap = BitmapFactory.decodeByteArray(tarif.gorsel , 0 , tarif.gorsel.size)
        binding.imageView.setImageBitmap(bitmap)

    }

    fun kaydet(){
        val isim = binding.isimText.text.toString()
        val malzeme = binding.tarifText.toString()

        if(secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!! , 300)
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG , 50 , outputStream)
            val byteDizisi = outputStream.toByteArray()

            val tarif = Tarif(isim,malzeme,byteDizisi)

            //tarifDao.insert(tarif)
            //threading hatası verir
            //Rxjava

            mDisposable.add(
            tarifDao.insert(tarif)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponseForInsert)
            )
        }
    }

    private  fun handleResponseForInsert(){
        //onceki fragmenta don
        val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
        Navigation.findNavController(requireView()).navigate(action)

    }

    fun sil(){

    }
    fun gorselSec(view: View){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                /* Compat = compatable : bu izin daha once alındı mı uyumlu mu
                         onceki versiyonlarlarla uyumlu mu
                         extarnalstorage api19 ve oncesinde sorulmasına gerek yoktu
                         izin istemeden galeriye giderdi
                         granted = izin verildi
                         izin veilmediyse !=
                         */

                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.READ_MEDIA_IMAGES)){

                    //true ise snackbar gostermek kullanıcıdan neden izin istedigimizi soylemek

                    Snackbar.make(view , "Galeriye ulaşıp görsel seçmemiz lazım!" ,
                        Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver" ,
                        View.OnClickListener {
                            //izin isteyecegiz
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    ).show()
                    /*
                      - izin istedikten sonra kullanıcı izin vermediyse bilincli ya da
                    bilincsiz tekrar izin isteme kararını android veriyor
                      - rational = kullanıcıya neden istedigimizi soylemem gerekiyor mu diye
                    android kontrol ediyor ve karar veriyor

                    */
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    //izin iste

                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
                //izin verilmiş galeriye gidilsin
            }



        }else{
            if(ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                /* Compat = compatable : bu izin daha once alındı mı uyumlu mu
                         onceki versiyonlarlarla uyumlu mu
                         extarnalstorage api19 ve oncesinde sorulmasına gerek yoktu
                         izin istemeden galeriye giderdi
                         granted = izin verildi
                         izin veilmediyse !=
                         */

                if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)){

                    //true ise snackbar gostermek kullanıcıdan neden izin istedigimizi soylemek

                    Snackbar.make(view , "Galeriye ulaşıp görsel seçmemiz lazım!" ,
                        Snackbar.LENGTH_INDEFINITE).setAction(
                        "İzin ver" ,
                        View.OnClickListener {
                            //izin isteyecegiz
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    ).show()
                    /*
                      - izin istedikten sonra kullanıcı izin vermediyse bilincli ya da
                    bilincsiz tekrar izin isteme kararını android veriyor
                      - rational = kullanıcıya neden istedigimizi soylemem gerekiyor mu diye
                    android kontrol ediyor ve karar veriyor

                    */
                }else{
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //izin iste

                }

            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
                //izin verilmiş galeriye gidilsin
            }

        }






    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){

                    secilenGorsel = intentFromResult.data

                    try{
                        if(Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver , secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }else{

                            secilenBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver , secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }

                    }catch(e : Exception){
                        println(e.localizedMessage)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                //izin verildi galeriye gidilebilir
                val intentToGallery = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //izin verilmedi
                Toast.makeText(context , "İzin verilmedi" , Toast.LENGTH_LONG).show()
            }
        }

    }


    private fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap ,
                                   maxBoyut : Int) : Bitmap {

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        var bitmapOrani : Double = width.toDouble() / height.toDouble()

        if(bitmapOrani > 1 ){
            //gorsel yatay
            width= maxBoyut
            val scaledHeight = width / bitmapOrani
            height = scaledHeight.toInt()
        }else{
            //gorsel dikey
            height = maxBoyut
            val scaledWidth = height * bitmapOrani
            width = scaledWidth.toInt()

        }


        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap , width ,height, true)
    }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding = null

        mDisposable.clear()
    }


}