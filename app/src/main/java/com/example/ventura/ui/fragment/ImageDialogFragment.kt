package com.example.ventura.ui.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.ventura.R

class ImageDialogFragment : DialogFragment() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_dialog, container, false)

        // Establecer el fondo transparente
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Hacer el fragmento cancelable al tocar fuera de la imagen
        dialog?.setCanceledOnTouchOutside(true)

        // Configurar el Listener de toques para cerrar el fragmento
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                dismiss()
                true
            } else {
                false
            }
        }

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val noConnectionTextView = view.findViewById<TextView>(R.id.noConnectionTextView)

        // Verificar si la imagen está en la caché de Glide
        val imageUrl = "https://uniandes.edu.co/sites/default/files/campus_ng.jpg"
        Glide.with(requireContext())
            .load(imageUrl)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)) // Cache la imagen para usos futuros
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable?> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Si falla la carga, mostrar el texto de conexión
                    noConnectionTextView.visibility = View.VISIBLE
                    noConnectionTextView.setBackgroundColor(Color.WHITE)
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable?>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Si la imagen se carga correctamente, mostrarla y ocultar el texto de conexión
                    noConnectionTextView.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    return false
                }
            })
            .into(imageView)

        return view
    }
}
