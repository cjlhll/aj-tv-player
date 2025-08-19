package com.tvplayer.webdav.ui.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.Actor

/**
 * 演员列表适配器
 */
class ActorAdapter(
    private val actors: List<Actor>,
    private val onActorClick: (Actor) -> Unit
) : RecyclerView.Adapter<ActorAdapter.ActorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actor, parent, false)
        return ActorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActorViewHolder, position: Int) {
        holder.bind(actors[position])
    }

    override fun getItemCount(): Int = actors.size

    inner class ActorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivActorAvatar: ImageView = itemView.findViewById(R.id.iv_actor_avatar)
        private val tvActorName: TextView = itemView.findViewById(R.id.tv_actor_name)
        private val tvActorRole: TextView = itemView.findViewById(R.id.tv_actor_role)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onActorClick(actors[position])
                }
            }
        }

        fun bind(actor: Actor) {
            tvActorName.text = actor.name
            
            // 设置角色标签
            if (actor.isDirector) {
                tvActorRole.text = "导演"
                tvActorRole.background = itemView.context.getDrawable(R.drawable.director_badge_background)
            } else {
                tvActorRole.text = "饰"
                tvActorRole.background = itemView.context.getDrawable(R.drawable.role_badge_background)
            }

            // 加载演员头像
            if (!actor.avatarUrl.isNullOrEmpty()) {
                try {
                    com.bumptech.glide.Glide.with(itemView.context)
                        .load(actor.avatarUrl)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivActorAvatar)
                } catch (e: Exception) {
                    ivActorAvatar.setImageResource(R.drawable.ic_person_placeholder)
                }
            } else {
                ivActorAvatar.setImageResource(R.drawable.ic_person_placeholder)
            }
        }
    }
}