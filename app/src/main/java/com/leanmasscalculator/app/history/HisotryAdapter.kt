package com.leanmasscalculator.app.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.leanmasscalculator.app.R
import com.leanmasscalculator.app.model.Calculation

/**
 * RecyclerView adapter for displaying calculation history items.
 * Converts Calculation objects into visible cards on screen.
 */
class HistoryAdapter(
    private val calculations: MutableList<Calculation>,
    private val onDeleteClick: (Calculation, Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    /**
     * ViewHolder = holds references to the views inside one list item.
     * "ViewHolder" because it HOLDS the VIEW references so we don't
     * have to call findViewById every time we scroll.
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView       = itemView.findViewById(R.id.tv_date)
        val tvLbmValue: TextView   = itemView.findViewById(R.id.tv_lbm_value)
        val tvDetails: TextView    = itemView.findViewById(R.id.tv_details)
        val tvResultLabel: TextView = itemView.findViewById(R.id.tv_result_label)
        val ivStatusIcon: ImageView = itemView.findViewById(R.id.iv_status_icon)
        val btnDelete: ImageButton  = itemView.findViewById(R.id.btn_delete)
    }

    /**
     * STEP 1: Create a ViewHolder (inflate the item layout).
     * Called only enough times to fill the screen — then views are RECYCLED.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    /**
     * STEP 2: Bind data to a ViewHolder.
     * Called when a ViewHolder needs to display a specific calculation.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val calc = calculations[position]

        // --- Fill in the data ---
        holder.tvDate.text = calc.getFormattedDate()
        holder.tvLbmValue.text = String.format("LBM: %.2f kg", calc.lbmValue)
        holder.tvDetails.text = String.format(
            "Poids: %.1f kg | Taille: %.1f cm | Sexe: %s",
            calc.weight, calc.height,
            if (calc.gender == "male") "Homme" else "Femme"
        )

        if (calc.satisfactory) {
            holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle)
            holder.tvResultLabel.text = "Satisfaisant"
            holder.tvResultLabel.setTextColor(
                holder.itemView.context.getColor(R.color.green_satisfactory)
            )
        } else {
            holder.ivStatusIcon.setImageResource(R.drawable.ic_warning)
            holder.tvResultLabel.text = "A surveiller"
            holder.tvResultLabel.setTextColor(
                holder.itemView.context.getColor(R.color.orange_warning)
            )
        }

        // --- Delete button ---
        holder.btnDelete.setOnClickListener {
            onDeleteClick(calc, holder.adapterPosition)
        }
    }

    /**
     * STEP 3: How many items total?
     */
    override fun getItemCount(): Int = calculations.size

    /**
     * Remove an item from the list and animate it away.
     */
    fun removeItem(position: Int) {
        calculations.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, calculations.size)
    }
}