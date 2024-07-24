package com.example.mynotes.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mynotes.databinding.NoteLayoutBinding
import com.example.mynotes.fragments.HomeFragmentDirections
import com.example.mynotes.model.NoteF

class NoteFAdapter(private val notesList: List<NoteF>) : RecyclerView.Adapter<NoteFAdapter.NoteFViewHolder>() {

    inner class NoteFViewHolder(private val binding: NoteLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteF) {
            binding.noteTitle.text = note.title
            binding.noteDate.text = note.date
            binding.noteDesc.text = note.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteFViewHolder {
        return NoteFViewHolder(NoteLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    override fun onBindViewHolder(holder: NoteFViewHolder, position: Int) {
        val currentNote = notesList[position]

        holder.bind(currentNote)

        holder.itemView.setOnClickListener {
            val direction = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(currentNote)
            it.findNavController().navigate(direction)
        }
    }
}

