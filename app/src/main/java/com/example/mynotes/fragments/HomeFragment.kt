package com.example.mynotes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mynotes.R
import com.example.mynotes.adapter.NoteFAdapter
import com.example.mynotes.databinding.FragmentHomeBinding
import com.example.mynotes.model.NoteF
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class HomeFragment : Fragment(R.layout.fragment_home), MenuProvider {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeNoteView: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var notesList: ArrayList<NoteF>

    private var db = Firebase.firestore

    private var flagListIsEmpty = true
    private var memberName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeNoteView = view

        val user = FirebaseAuth.getInstance().currentUser
        recyclerView = binding.homeRecyclerView
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.setHasFixedSize(true)

        notesList = arrayListOf()

        db = FirebaseFirestore.getInstance()

        // Check if the user was logged in or not
        if (user == null) {
            homeNoteView.findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
        }
        else{
            // ### Export the name of the user from DB ###
            val nameCollection = db.collection("notes").document(user.uid)

            nameCollection.get().addOnSuccessListener{
                val name = it.data?.get("name")
                memberName = name.toString()

                val menuHost: MenuHost = requireActivity()
                menuHost.addMenuProvider(this,viewLifecycleOwner, Lifecycle.State.RESUMED)
            }

            // ### Export the notes of the user from DB ###
            db.collection("notes").document(user.uid).collection("my_notes")
                .get().addOnSuccessListener {
                    if(!it.isEmpty) {
                        updateUI(false)
                       // binding.emptyNotesImage.visibility = View.GONE
                       // recyclerView.visibility = View.VISIBLE
                        for (data in it.documents) {
                            val note = data.toObject(NoteF::class.java)
                            if (note != null) {
                                notesList.add(note)
                            }
                        }
                        val sortedNotesList = notesList.sortedByDescending { it.date }
                        recyclerView.adapter = NoteFAdapter( sortedNotesList)
                    }
                    else{
                        updateUI(true)
                        //binding.emptyNotesImage.visibility = View.VISIBLE
                       // recyclerView.visibility = View.GONE
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        homeNoteView.context,
                        it.toString(),
                        Toast.LENGTH_SHORT,
                    ).show()
                }

            binding.addNoteFab.setOnClickListener{
                it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
            }
        }
    }

    private fun updateUI(flagListIsEmpty: Boolean){
        if (flagListIsEmpty){
            binding.emptyNotesImage.visibility = View.VISIBLE
            binding.homeRecyclerView.visibility = View.GONE
        }
        else {
            binding.emptyNotesImage.visibility = View.GONE
            binding.homeRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

       menu.findItem(R.id.nameMenu).setTitle("Hello $memberName")

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.logoutMenu -> {
                FirebaseAuth.getInstance().signOut()
                homeNoteView.findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
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