package com.example.mynotes.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentSignupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


class SignupFragment : Fragment(R.layout.fragment_signup) {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var signupView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signupView = view

        val loginText = binding.goToLogin
        val singupBtn = binding.singUpButton

        singupBtn.setOnClickListener {
            createAccount()
        }

        loginText.setOnClickListener {
            it.findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    private fun createAccount() {
        val mail = binding.mailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()
        val name = binding.nameInput.text.toString().trim()

        val isValidated = validateInput(mail,password,name)
        if (!isValidated) { return}

        createAccountInFirebase(mail,password,name)
    }

    private fun createAccountInFirebase(mail: String, password: String, name: String) {
        changeInProgress(true)

        // Initialize Firebase Auth
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(mail, password).addOnCompleteListener(requireActivity()) { task ->
            changeInProgress(false)
            if (task.isSuccessful) {
                // Create Account is done
                Toast.makeText(
                    signupView.context,
                    "Successfully Created Account",
                    Toast.LENGTH_SHORT,
                ).show()
                val user = auth.currentUser
                val userMap = hashMapOf("name" to name)

                if (user != null) {
                    FirebaseFirestore.getInstance().collection("notes").document(user.uid).set(userMap)
                }
                signupView.findNavController().navigate(R.id.action_signupFragment_to_homeFragment)
            } else {
                // Authentication failed
                Toast.makeText(
                    signupView.context,
                    "Authentication failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun changeInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.singUpButton.visibility = View.GONE
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.singUpButton.visibility = View.VISIBLE
        }
    }

    private fun validateInput(mail: String, password: String, name: String): Boolean {
        if (name.isEmpty()) {
            binding.nameInput.error = "Please enter name"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
            binding.mailInput.error = "Mail is invalid"
            return false
        }
        if (password.length < 6) {
            binding.passwordInput.error = "Password length is invalid"
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding =null
    }
}