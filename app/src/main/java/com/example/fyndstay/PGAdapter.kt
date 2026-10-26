package com.example.fyndstay

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PGAdapter(private val context: Context, private val pgList: List<PG>, private val listener: Listener) : RecyclerView.Adapter<PGAdapter.PGViewHolder>() {
    interface Listener {
        fun onPGClicked(pg: PG)
        fun onWishlistToggle(pg: PG, newState: Boolean, position: Int)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PGViewHolder =
        PGViewHolder(LayoutInflater.from(context).inflate(R.layout.item_pg, parent, false))

    override fun onBindViewHolder(holder: PGViewHolder, position: Int) {
        val pg = pgList[position]
        holder.name.text = pg.pgName ?: "Unnamed PG"
        holder.locality.text = pg.locality ?: "-"
        holder.rent.text = pg.rent?.let { "₹ $it/month" } ?: "Rent NA"
        holder.gender.text = pg.genderRestriction ?: "Any"
        holder.badge.visibility = if (SamplePGs.isSample(pg)) View.VISIBLE else View.GONE
        holder.wishlist.setImageResource(if (pg.inWishlist) R.drawable.ic_favorite else R.drawable.ic_not_favorite)
        val imageUrl = pg.imageUrl
        if (imageUrl.isNullOrBlank()) {
            // Friendly placeholder so listings without photos still look neat.
            val pad = (36 * context.resources.displayMetrics.density).toInt()
            holder.image.setPadding(pad, pad, pad, pad)
            holder.image.scaleType = ImageView.ScaleType.CENTER_INSIDE
            holder.image.setBackgroundColor(Color.parseColor("#EFE6E0"))
            holder.image.setColorFilter(Color.parseColor("#B79C93"))
            holder.image.setImageResource(R.drawable.ic_home)
        } else {
            holder.image.setPadding(0, 0, 0, 0)
            holder.image.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.image.setBackgroundColor(Color.WHITE)
            holder.image.clearColorFilter()
            Glide.with(context).load(imageUrl).into(holder.image)
        }
        holder.itemView.setOnClickListener { listener.onPGClicked(pg) }
        holder.wishlist.setOnClickListener {
            val next = !pg.inWishlist
            pg.inWishlist = next
            holder.wishlist.setImageResource(if (next) R.drawable.ic_favorite else R.drawable.ic_not_favorite)
            listener.onWishlistToggle(pg, next, holder.bindingAdapterPosition)
        }
    }
    override fun getItemCount() = pgList.size
    class PGViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imgPG)
        val name: TextView = itemView.findViewById(R.id.tvPGName)
        val locality: TextView = itemView.findViewById(R.id.tvLocality)
        val rent: TextView = itemView.findViewById(R.id.tvRent)
        val gender: TextView = itemView.findViewById(R.id.tvGender)
        val badge: TextView = itemView.findViewById(R.id.tvBadge)
        val wishlist: ImageButton = itemView.findViewById(R.id.btnWishlist)
    }
}
