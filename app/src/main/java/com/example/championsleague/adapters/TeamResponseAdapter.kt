package com.example.championsleague.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.championsleague.models.TeamEmpty
import com.google.android.material.checkbox.MaterialCheckBox
import java.util.function.BiConsumer

class TeamResponseAdapter(val context: Context): RecyclerView.Adapter<TeamResponseAdapter.ViewHolder>() {

    private var mTeams: ArrayList<TeamEmpty> = ArrayList<TeamEmpty>()
    lateinit var mCheckboxAction: BiConsumer<TeamEmpty, Boolean>


    fun setTeams(teams: List<TeamEmpty>){
        mTeams.clear()
        mTeams.addAll(teams)
        notifyItemRangeChanged(0, mTeams.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(MaterialCheckBox(context).apply { layoutParams = parent.layoutParams })
    }

    override fun getItemCount(): Int = mTeams.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val team = mTeams.get(position)

        holder.mTeamBox.setText(team.name)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var mTeamBox: MaterialCheckBox

        init {
            mTeamBox = itemView as MaterialCheckBox
            mTeamBox.setOnCheckedChangeListener { _, isChecked ->
                mCheckboxAction.accept(mTeams.get(absoluteAdapterPosition), isChecked)
            }
        }
    }
}