package com.example.mynotes.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentEditNoteBinding
import com.example.mynotes.model.NoteF
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date


class EditNoteFragment : Fragment(R.layout.fragment_edit_note), MenuProvider {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentNote: NoteF

    private lateinit var editNoteView: View

    private val args: EditNoteFragmentArgs by navArgs()

    private val currentUser = FirebaseAuth.getInstance().currentUser

    private var db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEditNoteBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

        editNoteView = view
        currentNote = args.note!!

        db = FirebaseFirestore.getInstance()


        binding.editNoteTitle.setText(currentNote.title)
        binding.editNoteDesc.setText(currentNote.description)

        binding.editNoteFab.setOnClickListener {
            val noteTitle = binding.editNoteTitle.text.toString().trim()
            val noteDesc = binding.editNoteDesc.text.toString().trim()

            val currentTime = Date()
            val formatter = SimpleDateFormat("EEE, MMM d, ''yy ,HH:mm")
            val noteDate = formatter.format(currentTime)



            if(noteTitle.isNotEmpty()){
                val note = NoteF(noteTitle,noteDate,noteDesc)

                if (currentUser != null) {
                    val query = db.collection("notes").document(currentUser.uid).collection("my_notes").whereEqualTo("title", currentNote.title)
                    query.get()
                        .addOnSuccessListener { querySnapshot ->
                            // Proceed with deletion if a document is found
                            if (querySnapshot.isEmpty) {
                                Toast.makeText(context,"No document matching the criteria found", Toast.LENGTH_SHORT).show()
                                // Handle case where no document is found
                            } else {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentRef = documentSnapshot.reference

                                // ### Updating the current note in DB ###
                                documentRef.set(note)
                                    .addOnCompleteListener(requireActivity())  {task ->
                                        if (task.isSuccessful){
                                            // Document successfully updated
                                            Toast.makeText(context,"Note updated", Toast.LENGTH_SHORT).show()
                                            editNoteView.findNavController().popBackStack(R.id.homeFragment, false)
                                        }
                                        else {
                                            // Handle updating failure
                                            Toast.makeText(context,"Error updating the note", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context,"Error getting document", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                binding.editNoteTitle.error = "Please enter title"
            }
        }
    }

    private fun deleteNote(){
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Note")
            setMessage("Do you want to delete this note?")
            setPositiveButton("Delete"){ _,_ ->
                if (currentUser != null) {
                    val query = db.collection("notes").document(currentUser.uid).collection("my_notes").whereEqualTo("title", currentNote.title)
                    query.get()
                        .addOnSuccessListener { querySnapshot ->
                            // Proceed with deletion if a document is found
                            if (querySnapshot.isEmpty) {
                                // Handle case where no document is found
                                Toast.makeText(context,"No document matching the criteria found", Toast.LENGTH_SHORT).show()
                            } else {
                                val documentSnapshot = querySnapshot.documents[0]
                                val documentRef = documentSnapshot.reference
                                // ### Deleting the current note in DB ###
                                documentRef.delete()
                                    .addOnSuccessListener {
                                        // Document successfully deleted
                                        Toast.makeText(context,"Note Deleted", Toast.LENGTH_SHORT).show()
                                        editNoteView.findNavController().popBackStack(R.id.homeFragment, false)
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle deletion failure
                                        Toast.makeText(context,"Error deleting the note", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context,"Error getting document", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.edit_note_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.deleteMenu -> {
                deleteNote()
                true
            } else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding =null
    }
}