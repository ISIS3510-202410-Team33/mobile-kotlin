package com.example.ventura.ui.activity

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.ventura.R
import com.example.ventura.utils.FeatureCrashHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CampusImagesActivity: AppCompatActivity() {

    private val featureCrashHandler = FeatureCrashHandler("campus_images")
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var mainText: TextView
    private var internetJob: Job? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        try
        {
            // Activity UI set
            super.onCreate(savedInstanceState)
            setContentView(R.layout.campus_images)

            // Find view elements and initialize them
            imagesRecyclerView = findViewById(R.id.imagesRecyclerView)
            backButton = findViewById(R.id.imageView8)
            mainText = findViewById(R.id.main_text)


            // Setting action listeners for the buttons
            backButton.setOnClickListener{
                finish()
            }

            // Links for the Campus images used in this activity
            val imageUrls = arrayOf(
                "https://uniandes.edu.co/sites/default/files/campus_ng.jpg",
                "https://cloudfront-us-east-1.images.arcpublishing.com/semana/PXIOHD56UFFY5JRQEAM3HQRNMI.jpg",
                "https://uniandes.edu.co/sites/default/files/u5940/centro_deportivo.jpg",
                "https://cienciassociales.uniandes.edu.co/lenguas-cultura/wp-content/uploads/sites/19/elementor/thumbs/nota-andes-top2020-q380cn1mg7hofcukyj6s1s5a6bzu1epgi1l37ior60.jpg",
                "https://static1.educaedu-colombia.com/adjuntos/12/00/38/universidad-de-los-andes---pregrado-003891_large.jpg",
                "https://uniandes.edu.co/sites/default/files/voces-academia-sociedad-n.jpg",
                "https://centrodeljapon.uniandes.edu.co/sites/default/files/Centro_japon/edificio-cj/Edificio1.jpg",
                "https://arqdis.b-cdn.net/wp-content/uploads/2021/02/biblioteca-galeria-11.jpg",
                "https://serenadelmar.com.co/wp-content/uploads/2019/08/PRENSA02.jpg",
                "https://uniandes.edu.co/sites/default/files/campus-centro-deportivo-7.jpg"
            )

            // Initialization of the layout-manager and the adapter
            val layoutManager = LinearLayoutManager(this)
            imagesRecyclerView.layoutManager = layoutManager
            val adapter = ImageAdapter(imageUrls)
            imagesRecyclerView.adapter = adapter

            internetJob = CoroutineScope(Dispatchers.IO).launch {
                while (isActive) {
                    val isConnected = isInternetConnected()
                    updateMainText(isConnected)
                    delay(1000) // Verificar la conexión cada segundo
                }
            }


        }
        catch (e: Exception)
        {
            featureCrashHandler.logCrash("display", e)
        }


    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun updateMainText(isConnected: Boolean) {
        runOnUiThread {
            if (isConnected) {
                mainText.text = "Get to know the campus!"
            } else {
                mainText.text =
                    "No internet connection detected"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        internetJob?.cancel()
    }
}

class ImageAdapter(private val imageUrls: Array<String>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false) as ImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(imageUrls[position])
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Mantener en caché hasta que se cierre la app
            .placeholder(R.drawable.loading_placeholder) // Placeholder image while loading
            .error(R.drawable.no_net) // Error image if loading fails
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }
}