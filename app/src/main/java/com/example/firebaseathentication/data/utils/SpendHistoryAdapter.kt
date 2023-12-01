package com.example.firebaseathentication.data.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

class SpendHistoryAdapter (
    val onEachItem: (Any, View) -> Unit,
    @LayoutRes val holderView: Int
) : RecyclerView.Adapter<SpendHistoryAdapter.SpendHistoryViewHolder>() {


    private var dataList : List<Any> = emptyList()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpendHistoryViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(holderView, parent, false)
        return SpendHistoryViewHolder(view, onEachItem)
    }


    override fun onBindViewHolder(
        holder: SpendHistoryViewHolder,
        position: Int
    ) {
        val item = dataList.get(position)
        if (item != null) {
            holder.bind(item)
        }
    }

    fun setData(dataList: List<Any>){
        this.dataList = dataList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class SpendHistoryViewHolder (
        itemView: View,
        val onEachItem: (Any, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView
    ) {

        fun bind(item: Any) {
            onEachItem(item, itemView)
        }

    }


}