package com.dk.organizeu.activity_main.fragments.auth.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dk.organizeu.R
import com.dk.organizeu.activity_admin.AdminActivity
import com.dk.organizeu.databinding.FragmentAdminLoginBinding
import com.dk.organizeu.enum_class.AdminLocalDBKey
import com.dk.organizeu.pojo.AdminPojo
import com.dk.organizeu.repository.AdminRepository
import com.dk.organizeu.utils.SharedPreferencesManager
import com.dk.organizeu.utils.UtilFunction.Companion.showToast
import com.dk.organizeu.utils.UtilFunction.Companion.unexpectedErrorMessagePrint

class AdminLoginFragment : Fragment() {

    private lateinit var viewModel: AdminLoginViewModel
    private lateinit var binding: FragmentAdminLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admin_login, container, false)
        binding = DataBindingUtil.bind(view)!!
        viewModel = ViewModelProvider(this)[AdminLoginViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.apply {

                if(SharedPreferencesManager.containsKey(requireContext(),AdminLocalDBKey.REMEMBER.displayName))
                {
                    etAdminId.setText(SharedPreferencesManager.getString(requireContext(),AdminLocalDBKey.REMEMBER.displayName))
                    cbRemember.isChecked = true
                }

                btnLoginAdmin.setOnClickListener {
                    val adminId = etAdminId.text.toString()
                    val password = etAdminPassword.text.toString()
                    adminLogin(adminId, password)
                }
            }
        }
    }

    private fun adminLogin(adminId:String,password:String){
        binding.apply {
            viewModel.apply {
                AdminRepository.isMatchAdminCrediantails(adminId,password){
                    if(it)
                    {
                        AdminRepository.getAdminPojoById(adminId){adminPojo ->
                            saveAdminLocal(adminPojo!!)
                            if(binding.cbRemember.isChecked)
                            {
                                rememberAdmin(adminId)
                            }
                            else{
                                if(SharedPreferencesManager.containsKey(requireContext(),AdminLocalDBKey.REMEMBER.displayName)){
                                    SharedPreferencesManager.removeValue(requireContext(),AdminLocalDBKey.REMEMBER.displayName)
                                }
                            }
                            gotoAdminActivity()
                        }
                    }
                    else
                    {
                        requireContext().showToast("Invalid Credentials")
                    }
                }
            }
        }
    }

    fun saveAdminLocal(adminPojo: AdminPojo)
    {
        SharedPreferencesManager.addValue(requireContext(),AdminLocalDBKey.ID.displayName,adminPojo.id)
        SharedPreferencesManager.addValue(requireContext(),AdminLocalDBKey.NAME.displayName,adminPojo.name)
    }

    fun rememberAdmin(adminId: String)
    {
        SharedPreferencesManager.addValue(requireContext(),AdminLocalDBKey.REMEMBER.displayName,adminId)
    }

    /**
     * Navigates to the AdminActivity.
     * If successful, finishes the current activity.
     */
    private fun gotoAdminActivity(){
        try {
            // Create an intent to start the AdminActivity
            Intent(requireActivity(), AdminActivity::class.java).apply {
                // Start the activity
                startActivity(this)
            }
            // Finish the current activity
            requireActivity().finish()
        } catch (e:Exception) {
            // Handle any exceptions and print error messages
            requireContext().unexpectedErrorMessagePrint(e)
        }
    }
}