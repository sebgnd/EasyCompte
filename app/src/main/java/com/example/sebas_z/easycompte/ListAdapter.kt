package com.example.sebas_z.easycompte

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

class ListAdapter(private val dataSet : MutableList<Transfer>, val context: Context, val currency : Char, val listener: CustomClickListener) : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {

    //What is in the ViewHolder -> same in the custom view
    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val sourceView: TextView = view.findViewById(R.id.source)
        val dateView: TextView = view.findViewById(R.id.date)
        val amountView: TextView = view.findViewById(R.id.moneyAmount)
        val currencyView : TextView = view.findViewById(R.id.currency)
    }

    //Link the custom view to the viewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_list, parent, false) as View
        val mViewHolder = MyViewHolder(view)

        view.setOnClickListener {
            if (mViewHolder.adapterPosition != 0) {
                listener.onItemClick(view, mViewHolder.adapterPosition, context)
            }
        }
        return mViewHolder
    }

    //Get the number of items in the list
    override fun getItemCount(): Int {
        return dataSet.size
    }

    //Binds the data de the TextViews
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.sourceView.text = dataSet[position].source
        holder.dateView.text = dataSet[position].date.toString()
        holder.currencyView.text = currency.toString()

        holder.amountView.text = (dataSet[position].amount.toFloat() + 0.0f).toString()
        if (dataSet[position].amount.toFloat() > 0) {
            holder.amountView.setTextColor(ContextCompat.getColor(context, R.color.positiveAmount))
            holder.currencyView.setTextColor(ContextCompat.getColor(context, R.color.positiveAmount))
        } else if (dataSet[position].amount.toFloat() < 0) {
            holder.amountView.setTextColor(ContextCompat.getColor(context, R.color.negativeAmount))
            holder.currencyView.setTextColor(ContextCompat.getColor(context, R.color.negativeAmount))
        } else {
            holder.amountView.setTextColor(ContextCompat.getColor(context, R.color.zeroAmount))
            holder.currencyView.setTextColor(ContextCompat.getColor(context, R.color.zeroAmount))
        }
    }
}