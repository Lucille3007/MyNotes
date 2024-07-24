package com.example.mynotes.fragments

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.mynotes.R
import com.example.mynotes.databinding.FragmentLoginBinding
import com.example.mynotes.databinding.FragmentSignupBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    private lateinit var loginView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater,container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginView = view

        val singupText = binding.goToSignup
        val loginBtn = binding.loginButton

        singupText.setOnClickListener {
            it.findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }

        loginBtn.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser(){
        val mail = binding.mailInput.text.toString().trim()
        val password = binding.passwordInput.text.toString().trim()

        val isValidated = validateInput(mail,password)
        if (!isValidated) { return}

        loginAccountInFirebase(mail,password)

    }

    private fun loginAccountInFirebase(mail: String, password: String) {
        changeInProgress(true)

        // Initialize Firebase Auth
        auth = Firebase.auth

        auth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(requireActivity()) { task ->
            changeInProgress(false)
            if (task.isSuccessful) {
                // Login Account is done
                Toast.makeText(
                    requireContext(),
                    "Successfully Login",
                    Toast.LENGTH_SHORT,
                ).show()
                loginView.findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                // Login failed
                Toast.makeText(
                    requireContext(),
                    "Login failed.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun changeInProgress(inProgress: Boolean) {
        if (inProgress) {
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.visibility = View.GONE
        }
        else {
            binding.progressBar.visibility = View.GONE
            binding.loginButton.visibility = View.VISIBLE
        }
    }

    private fun validateInput(mail: String, password: String): Boolean {

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