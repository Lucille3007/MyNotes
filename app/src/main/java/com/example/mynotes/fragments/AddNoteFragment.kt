package com.example.mynotes.fragments

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
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentAddNoteBinding
import com.example.mynotes.model.NoteF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date


class AddNoteFragment : Fragment(R.layout.fragment_add_note), MenuProvider {

    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var addNoteView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAddNoteBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)

       // notesViewModel = (activity as MainActivity).noteViewModel
        addNoteView = view
    }

    private fun saveNote(view: View){

        val noteTitle = binding.addNoteTitle.text.toString().trim()
        val noteDesc = binding.addNoteDesc.text.toString().trim()

        val currentTime = Date()
        val formatter = SimpleDateFormat("EEE, MMM d, ''yy ,HH:mm")
        val noteDate = formatter.format(currentTime)

        if(noteTitle.isNotEmpty()){
            val note = NoteF(noteTitle,noteDate,noteDesc)
            saveNoteToFireBase(note)

        } else {
            binding.addNoteTitle.error = "Please enter title"
        }
    }

    private fun saveNoteToFireBase(note: NoteF){
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser!=null) {

            val documentReference =
                FirebaseFirestore.getInstance().collection("notes").document(currentUser.uid)
                    .collection("my_notes").document()

            // ### Save the note of to DB ###
            documentReference.set(note).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Note saved
                    Toast.makeText(
                        requireContext(),
                        "Note saved!",
                        Toast.LENGTH_SHORT,
                    ).show()
                    addNoteView.findNavController().popBackStack(R.id.homeFragment, false)
                } else {
                    // saving note failed
                    Toast.makeText(
                        requireContext(),
                        "Failed while adding note.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.add_note_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.saveMenu -> {
                saveNote(addNoteView)
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding =null
    }
}